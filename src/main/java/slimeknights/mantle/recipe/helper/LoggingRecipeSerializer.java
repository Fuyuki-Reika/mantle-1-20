package slimeknights.mantle.recipe.helper;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import slimeknights.mantle.Mantle;

import javax.annotation.Nullable;

/**
 * Recipe serializer that logs network exceptions before throwing them as
 * otherwise the exceptions may be invisible
 * 
 * @param <T> Recipe class
 */
public interface LoggingRecipeSerializer<T extends Recipe<?>> extends RecipeSerializer<T> {
  /**
   * Read the recipe from the packet
   * 
   * @param buffer Buffer instance
   * @return Parsed recipe
   * @throws RuntimeException If any errors happen, the exception will be logged
   *                          automatically
   */
  // TODO 1.21.1: fromNetwork signature changed from (ResourceLocation,
  // FriendlyByteBuf) to (RegistryFriendlyByteBuf)
  @Nullable
  T fromNetworkSafe(RegistryFriendlyByteBuf buffer);

  /**
   * Write the method to the buffer
   * 
   * @param buffer Buffer instance
   * @param recipe Recipe instance
   * @throws RuntimeException If any errors happen, the exception will be logged
   *                          automatically
   */
  // TODO 1.21.1: toNetwork signature changed to use RegistryFriendlyByteBuf
  void toNetworkSafe(RegistryFriendlyByteBuf buffer, T recipe);

  @Nullable
  // TODO 1.21.1: Removed @Override - checking if method signature matches
  // interface
  default T fromNetwork(RegistryFriendlyByteBuf buffer) {
    try {
      return fromNetworkSafe(buffer);
    } catch (RuntimeException e) {
      Mantle.logger.error("{}: Error reading recipe from packet", this.getClass().getSimpleName(), e);
      throw e;
    }
  }

  // TODO 1.21.1: Removed @Override - checking if method signature matches
  // interface
  default void toNetwork(RegistryFriendlyByteBuf buffer, T recipe) {
    try {
      toNetworkSafe(buffer, recipe);
    } catch (RuntimeException e) {
      // TODO 1.21.1: Recipe.getId() removed, logging class and type only
      Mantle.logger.error("{}: Error writing recipe of class {} and type {} to packet", this.getClass().getSimpleName(),
          recipe.getClass().getSimpleName(), recipe.getType(), e);
      throw e;
    }
  }
}
