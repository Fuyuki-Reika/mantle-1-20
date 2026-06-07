package slimeknights.mantle.recipe.helper;

import com.google.gson.JsonObject;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.function.Function;

/**
 * Simple implementation of a recipe serializer with no properties other than
 * recipe ID.
 */
public record SimpleRecipeSerializer<T extends Recipe<?>>(Function<ResourceLocation, T> constructor)
    implements RecipeSerializer<T> {
  // TODO 1.21.1: fromJson(ResourceLocation, JsonObject) no longer in
  // RecipeSerializer interface
  public T fromJson(ResourceLocation id, JsonObject pSerializedRecipe) {
    return constructor.apply(id);
  }

  // TODO 1.21.1: fromNetwork with new signature - no ID available
  public T fromNetwork(RegistryFriendlyByteBuf pBuffer) {
    // Zero-data recipe: construct with placeholder ID (actual ID comes from
    // RecipeHolder)
    return constructor.apply(ResourceLocation.withDefaultNamespace("simple_recipe"));
  }

  // TODO 1.21.1: Old fromNetwork signature kept for internal use
  public T fromNetwork(ResourceLocation id, FriendlyByteBuf pBuffer) {
    return constructor.apply(id);
  }

  // TODO 1.21.1: toNetwork no longer part of RecipeSerializer interface in 1.21.1
  public void toNetwork(RegistryFriendlyByteBuf pBuffer, T pRecipe) {
  }

  // TODO 1.21.1: streamCodec() required by RecipeSerializer interface
  // Zero-data recipe: encode/decode nothing; ID comes from RecipeHolder wrapper
  @Override
  public StreamCodec<RegistryFriendlyByteBuf, T> streamCodec() {
    return StreamCodec.of(
        (buf, recipe) -> {
        }, // no data to write
        buf -> constructor.apply(ResourceLocation.withDefaultNamespace("simple_recipe")));
  }

  // TODO 1.21.1: codec() required by RecipeSerializer interface
  // Zero-data recipe: no fields needed; ID comes from RecipeHolder wrapper
  @Override
  public MapCodec<T> codec() {
    return MapCodec.unit(() -> constructor.apply(ResourceLocation.withDefaultNamespace("simple_recipe")));
  }
}
