package slimeknights.mantle.recipe.helper;

import com.google.gson.JsonObject;
import com.mojang.serialization.MapCodec;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.data.loadable.field.ContextKey;
import slimeknights.mantle.data.loadable.field.LoadableField;
import slimeknights.mantle.data.loadable.primitive.StringLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.util.typed.TypedMapBuilder;

import javax.annotation.Nullable;
import java.util.function.Supplier;

/**
 * Recipe serializer instance using loadables. Use {@link ContextKey#ID} to get
 * the recipe ID.
 * 
 * @param <T> Recipe type
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class LoadableRecipeSerializer<T extends Recipe<?>> implements LoggingRecipeSerializer<T> {
  /**
   * Context key to use if you want the recipe serializer passed into your recipe
   */
  public static final ContextKey<RecipeSerializer<?>> SERIALIZER = new ContextKey<>("serializer");
  /**
   * Context key to use if you want a type aware serializer in the recipe,
   * requires {@link #of(RecordLoadable, Supplier)} for your serializer.
   */
  public static final ContextKey<TypeAwareRecipeSerializer<?>> TYPED_SERIALIZER = new ContextKey<>("typed_serializer");
  /**
   * Context key to use if you want the recipe type passed into your recipe,
   * requires {@link #of(RecordLoadable, Supplier)} for your serializer.
   */
  public static final ContextKey<RecipeType<?>> TYPE = new ContextKey<>("type");
  /** Field for a group key in a recipe (common requirement) */
  public static final LoadableField<String, Recipe<?>> RECIPE_GROUP = StringLoadable.DEFAULT.defaultField("group", "",
      Recipe::getGroup);

  protected final RecordLoadable<T> loadable;

  /** Creates a standard serializer from a loadable */
  public static <T extends Recipe<?>> RecipeSerializer<T> of(RecordLoadable<T> loadable) {
    return new LoadableRecipeSerializer<>(loadable);
  }

  /** Creates a type aware serializer from a loadable */
  public static <T extends R, R extends Recipe<?>> TypeAwareRecipeSerializer<T> of(RecordLoadable<T> loadable,
      Supplier<? extends RecipeType<R>> type) {
    return new TypeAware<>(loadable, type);
  }

  /** Creates a serializer that is deprecated, logging a warning when used */
  public static <T extends Recipe<?>> RecipeSerializer<T> deprecated(RecordLoadable<T> loadable, String replacement) {
    return new Deprecated<>(loadable, replacement);
  }

  /** Builds a context for the given ID */
  protected TypedMapBuilder buildContext(ResourceLocation id) {
    return TypedMapBuilder.builder().put(ContextKey.ID, id).put(ContextKey.DEBUG, "Recipe " + id).put(SERIALIZER, this);
  }

  // TODO 1.21.1: fromJson(ResourceLocation, JsonObject) no longer in
  // RecipeSerializer, uses codec() now
  public T fromJson(ResourceLocation id, JsonObject json) {
    return loadable.deserialize(json, buildContext(id).build());
  }

  // TODO 1.21.1: fromNetworkSafe signature changed - removed ResourceLocation id
  // parameter
  @Override
  public T fromNetworkSafe(RegistryFriendlyByteBuf buffer) {
    // Extract ID from buffer context if needed, for now use placeholder
    return loadable.decode(buffer, buildContext(ResourceLocation.withDefaultNamespace("unknown")).build());
  }

  // TODO 1.21.1: fromNetwork with old signature kept for internal use, no longer
  // overrides parent
  // Old signature calls loadable directly since fromNetworkSafe signature changed
  @Nullable
  public T fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
    try {
      return loadable.decode(buffer, buildContext(id).build());
    } catch (RuntimeException e) {
      Mantle.logger.error("{}: Error reading recipe {} from packet using loadable {}", this.getClass().getSimpleName(),
          id, loadable, e);
      throw e;
    }
  }

  // TODO 1.21.1: toNetworkSafe signature changed to use RegistryFriendlyByteBuf
  @Override
  public void toNetworkSafe(RegistryFriendlyByteBuf buffer, T recipe) {
    loadable.encode(buffer, recipe);
  }

  // TODO 1.21.1: streamCodec() is now required by RecipeSerializer
  @Override
  public StreamCodec<RegistryFriendlyByteBuf, T> streamCodec() {
    return StreamCodec.of(this::toNetworkSafe, this::fromNetworkSafe);
  }

  // TODO 1.21.1: codec() is now required by RecipeSerializer
  // MapCodec cannot be easily created from Loadable without additional
  // infrastructure
  // For now, delegate to fromJson which takes ResourceLocation id
  @Override
  public MapCodec<T> codec() {
    throw new UnsupportedOperationException(
        "LoadableRecipeSerializer uses custom deserialization via fromJson(ResourceLocation, JsonObject), codec() not supported");
  }

  public static class TypeAware<T extends Recipe<?>> extends LoadableRecipeSerializer<T>
      implements TypeAwareRecipeSerializer<T> {
    private final Supplier<? extends RecipeType<?>> type;

    protected TypeAware(RecordLoadable<T> loadable, Supplier<? extends RecipeType<?>> type) {
      super(loadable);
      this.type = type;
    }

    @Override
    protected TypedMapBuilder buildContext(ResourceLocation id) {
      return super.buildContext(id).put(TYPE, getType()).put(TYPED_SERIALIZER, this);
    }

    @Override
    public RecipeType<?> getType() {
      return type.get();
    }

    // TODO 1.21.1: fromNetwork with old signature - using loadable directly since
    // fromNetworkSafe changed
    @Nullable
    @Override
    public T fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
      try {
        return loadable.decode(buffer, buildContext(id).build());
      } catch (RuntimeException e) {
        Mantle.logger.error("{}: Error reading recipe {} of type {} from packet using loadable {}",
            this.getClass().getSimpleName(), id, getType(), loadable, e);
        throw e;
      }
    }
  }

  /** Helper class that logs a warning on recipe parse about planned removal */
  private static class Deprecated<T extends Recipe<?>> extends LoadableRecipeSerializer<T> {
    private final String replacement;

    protected Deprecated(RecordLoadable<T> loadable, String replacement) {
      super(loadable);
      this.replacement = replacement;
    }

    // TODO 1.21.1: Recipe.getId() removed, using id parameter instead
    @Override
    public T fromJson(ResourceLocation id, JsonObject json) {
      T recipe = super.fromJson(id, json);
      Mantle.logger.warn("Using deprecated recipe serializer {} for recipe {}, {}",
          BuiltInRegistries.RECIPE_SERIALIZER.getKey(this), id, replacement);
      return recipe;
    }
  }
}
