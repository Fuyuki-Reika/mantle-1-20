package slimeknights.mantle.recipe.crafting;

import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.Level;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.recipe.MantleRecipes;
import slimeknights.mantle.util.JsonHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@SuppressWarnings("WeakerAccess")
public class ShapedFallbackRecipe extends ShapedRecipe {

  /** Recipes to skip if they match */
  private final List<ResourceLocation> alternatives;
  private List<CraftingRecipe> alternativeCache;

  /**
   * Main constructor, creates a recipe from all parameters
   * 
   * @param id           Recipe ID
   * @param group        Recipe group
   * @param width        Recipe width
   * @param height       Recipe height
   * @param ingredients  Recipe input ingredients
   * @param output       Recipe output
   * @param alternatives List of recipe names to fail this match if they match
   */
  /** Creates a recipe from codec fields (pattern-based) */
  public ShapedFallbackRecipe(String group, CraftingBookCategory category, ShapedRecipePattern pattern,
      ItemStack output, List<ResourceLocation> alternatives) {
    super(group, category, pattern, output);
    this.alternatives = alternatives;
  }

  // TODO 1.21.1: ShapedRecipe constructor now uses ShapedRecipePattern instead of
  // individual parameters
  public ShapedFallbackRecipe(ResourceLocation id, String group, CraftingBookCategory category, int width, int height,
      NonNullList<Ingredient> ingredients, ItemStack output, List<ResourceLocation> alternatives) {
    super(group, category, new ShapedRecipePattern(width, height, ingredients, java.util.Optional.empty()), output);
    this.alternatives = alternatives;
  }

  /**
   * Creates a recipe using a shaped recipe as a base
   * 
   * @param base         Shaped recipe to copy data from
   * @param alternatives List of recipe names to fail this match if they match
   */
  // TODO 1.21.1: ShapedRecipe API changed - removed getId(), result field,
  // showNotification(), getWidth(), getHeight()
  // ShapedRecipe now exposes pattern() method to access ShapedRecipePattern
  public ShapedFallbackRecipe(ShapedRecipe base, List<ResourceLocation> alternatives) {
    super(base.getGroup(), base.category(), base.pattern,
        base.getResultItem(net.minecraft.core.HolderLookup.Provider.create(
            java.util.stream.Stream.<net.minecraft.core.HolderLookup.RegistryLookup<?>>of())));
    this.alternatives = alternatives;
  }

  /**
   * Returns an empty HolderLookup.Provider for use when a full registries context
   * is unavailable
   */
  static net.minecraft.core.HolderLookup.Provider emptyProvider() {
    return net.minecraft.core.HolderLookup.Provider.create(
        java.util.stream.Stream.<net.minecraft.core.HolderLookup.RegistryLookup<?>>of());
  }

  // TODO 1.21.1: matches() signature changed from CraftingContainer to
  // CraftingInput
  @Override
  public boolean matches(CraftingInput inv, Level world) {
    // if this recipe does not match, fail it
    if (!super.matches(inv, world)) {
      return false;
    }

    // fetch all alternatives, fail if any match
    // cache to save effort down the line
    if (alternativeCache == null) {
      RecipeManager manager = world.getRecipeManager();
      // TODO 1.21.1: byKey() now returns RecipeHolder, need to extract value()
      alternativeCache = alternatives.stream()
          .map(manager::byKey)
          .filter(Optional::isPresent)
          .map(Optional::get)
          .map(RecipeHolder::value)
          .filter(recipe -> {
            // only allow exact shaped or shapeless match, prevent infinite recursion due to
            // complex recipes
            Class<?> clazz = recipe.getClass();
            return clazz == ShapedRecipe.class || clazz == ShapelessRecipe.class;
          })
          .map(recipe -> (CraftingRecipe) recipe).collect(Collectors.toList());
    }
    // fail if any alterntaive matches
    return this.alternativeCache.stream().noneMatch(recipe -> recipe.matches(inv, world));
  }

  @Override
  public RecipeSerializer<?> getSerializer() {
    return MantleRecipes.CRAFTING_SHAPED_FALLBACK.get();
  }

  public static class Serializer implements net.minecraft.world.item.crafting.RecipeSerializer<ShapedFallbackRecipe> {
    private static final ShapedRecipe.Serializer BASE = new ShapedRecipe.Serializer();

    @Override
    public com.mojang.serialization.MapCodec<ShapedFallbackRecipe> codec() {
      return com.mojang.serialization.codecs.RecordCodecBuilder.mapCodec(inst -> inst.group(
          com.mojang.serialization.Codec.STRING.optionalFieldOf("group", "").forGetter(ShapedRecipe::getGroup),
          net.minecraft.world.item.crafting.CraftingBookCategory.CODEC.fieldOf("category")
              .forGetter(ShapedRecipe::category),
          net.minecraft.world.item.crafting.ShapedRecipePattern.MAP_CODEC.forGetter(r -> r.pattern),
          net.minecraft.world.item.ItemStack.STRICT_CODEC.fieldOf("result")
              .forGetter(r -> r.getResultItem(emptyProvider())),
          com.mojang.serialization.Codec.BOOL.optionalFieldOf("show_notification", true)
              .forGetter(ShapedRecipe::showNotification),
          ResourceLocation.CODEC.listOf().fieldOf("alternatives").forGetter(r -> r.alternatives)
      ).apply(inst, (group, category, pattern, result, showNotification, alternatives) ->
          new ShapedFallbackRecipe(group, category, pattern, result, alternatives)));
    }

    @Override
    public net.minecraft.network.codec.StreamCodec<RegistryFriendlyByteBuf, ShapedFallbackRecipe> streamCodec() {
      return net.minecraft.network.codec.StreamCodec.of(
          (buf, recipe) -> {
            BASE.streamCodec().encode(buf, recipe);
            buf.writeVarInt(recipe.alternatives.size());
            for (ResourceLocation alt : recipe.alternatives) buf.writeResourceLocation(alt);
          },
          buf -> {
            ShapedRecipe base = BASE.streamCodec().decode(buf);
            int size = buf.readVarInt();
            List<ResourceLocation> alts = new ArrayList<>(size);
            for (int i = 0; i < size; i++) alts.add(buf.readResourceLocation());
            return new ShapedFallbackRecipe(base, List.copyOf(alts));
          });
    }
    public ShapedFallbackRecipe fromJsonFallback(ResourceLocation id, JsonObject json) {
      ShapedRecipe base = BASE.codec().codec().parse(com.mojang.serialization.JsonOps.INSTANCE, json).getOrThrow();
      List<ResourceLocation> alternatives = JsonHelper.parseList(json, "alternatives", Loadables.RESOURCE_LOCATION);
      return new ShapedFallbackRecipe(base, alternatives);
    }

    public ShapedFallbackRecipe fromNetworkFallback(ResourceLocation id, RegistryFriendlyByteBuf buffer) {
      ShapedRecipe base = BASE.streamCodec().decode(buffer);
      int size = buffer.readVarInt();
      List<ResourceLocation> builder = new ArrayList<>(size);
      for (int i = 0; i < size; i++) {
        builder.add(buffer.readResourceLocation());
      }
      return new ShapedFallbackRecipe(base, List.copyOf(builder));
    }

    public void toNetworkFallback(RegistryFriendlyByteBuf buffer, ShapedRecipe recipe) {
      // write base recipe
      BASE.streamCodec().encode(buffer, recipe);
      // write extra data
      assert recipe instanceof ShapedFallbackRecipe;
      List<ResourceLocation> alternatives = ((ShapedFallbackRecipe) recipe).alternatives;
      buffer.writeVarInt(alternatives.size());
      for (ResourceLocation alternative : alternatives) {
        buffer.writeResourceLocation(alternative);
      }
    }
  }
}
