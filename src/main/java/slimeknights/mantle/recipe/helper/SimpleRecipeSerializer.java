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

  // TODO 1.21.1: fromNetwork with new signature - no ID available, throws
  // exception
  public T fromNetwork(RegistryFriendlyByteBuf pBuffer) {
    // Cannot construct without ID, throw exception
    throw new UnsupportedOperationException("SimpleRecipeSerializer.fromNetwork() requires ResourceLocation id");
  }

  // TODO 1.21.1: Old fromNetwork signature kept for internal use
  public T fromNetwork(ResourceLocation id, FriendlyByteBuf pBuffer) {
    return constructor.apply(id);
  }

  // TODO 1.21.1: toNetwork no longer part of RecipeSerializer interface in 1.21.1
  public void toNetwork(RegistryFriendlyByteBuf pBuffer, T pRecipe) {
  }

  // TODO 1.21.1: streamCodec() required by RecipeSerializer interface
  @Override
  public StreamCodec<RegistryFriendlyByteBuf, T> streamCodec() {
    return StreamCodec.of(this::toNetwork, this::fromNetwork);
  }

  // TODO 1.21.1: codec() required by RecipeSerializer interface
  @Override
  public MapCodec<T> codec() {
    throw new UnsupportedOperationException(
        "SimpleRecipeSerializer uses custom deserialization via fromJson(ResourceLocation, JsonObject), codec() not supported");
  }
}
