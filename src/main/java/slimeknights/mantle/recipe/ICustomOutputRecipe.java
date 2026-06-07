package slimeknights.mantle.recipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

/**
 * Recipe that has an output other than an {@link ItemStack}
 * 
 * @param <C> Inventory type
 */
// TODO 1.21.1: Container changed to RecipeInput - ICommonRecipe<C extends
// RecipeInput>
public interface ICustomOutputRecipe<C extends RecipeInput> extends ICommonRecipe<C> {
  /** @deprecated Item stack output not supported */
  @Deprecated
  @Override
  default ItemStack getResultItem(HolderLookup.Provider registries) {
    return ItemStack.EMPTY;
  }

  /** @deprecated Item stack output not supported */
  @Deprecated
  @Override
  default ItemStack assemble(C inv, HolderLookup.Provider registries) {
    return ItemStack.EMPTY;
  }
}
