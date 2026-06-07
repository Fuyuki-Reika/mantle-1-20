package slimeknights.mantle.recipe;

import net.minecraft.core.RegistryAccess;
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
  // TODO 1.21.1: getResultItem signature likely changed - removed @Override
  // annotation
  @Deprecated
  default ItemStack getResultItem(RegistryAccess access) {
    return ItemStack.EMPTY;
  }

  /** @deprecated Item stack output not supported */
  // TODO 1.21.1: assemble signature likely changed - removed @Override annotation
  @Deprecated
  default ItemStack assemble(C inv, RegistryAccess access) {
    return ItemStack.EMPTY;
  }
}
