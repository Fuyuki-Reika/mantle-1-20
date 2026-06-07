package slimeknights.mantle.recipe.helper;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;

/**
 * @deprecated FinishedRecipe no longer exists in NeoForge 1.21.1.
 *             Instead, use RecipeOutput.accept(ResourceLocation id, Recipe<?>
 *             recipe, AdvancementHolder advancement, ICondition... conditions)
 *             directly with your Recipe object.
 */
@Deprecated(forRemoval = true, since = "NeoForge 21.1")
public record SimpleFinishedRecipe(ResourceLocation getId, RecipeSerializer<?> getType) {
  /**
   * @deprecated This class is deprecated and should not be used. Migrate to
   *             RecipeOutput.accept() pattern.
   */
  // TODO 1.21.1: Canonical constructor parameter names must match record
  // component names
  @Deprecated(forRemoval = true)
  public SimpleFinishedRecipe(ResourceLocation getId, RecipeSerializer<?> getType) {
    this.getId = getId;
    this.getType = getType;
    throw new UnsupportedOperationException(
        "SimpleFinishedRecipe is deprecated. Use RecipeOutput.accept(id, recipe, advancement, conditions) instead.");
  }
}
