package slimeknights.mantle.plugin.jei;

import com.google.common.collect.Streams;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.client.SafeClientAccess;
import slimeknights.mantle.recipe.crafting.ShapedRetexturedRecipe;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * JEI crafting extension to properly show, animate, and focus
 * {@link ShapedRetexturedRecipe} instances.
 * Stateless — all recipe data is extracted from the RecipeHolder in
 * setRecipe().
 */
public class RetexturableRecipeExtension implements ICraftingCategoryExtension<ShapedRetexturedRecipe> {

  /** Checks if two ingredients match based on their display items */
  private static boolean ingredientsMatch(Ingredient left, Ingredient right) {
    ItemStack[] leftStacks = left.getItems();
    ItemStack[] rightStacks = right.getItems();
    if (leftStacks.length != rightStacks.length) {
      return false;
    }
    for (int i = 0; i < leftStacks.length; i++) {
      if (!ItemStack.isSameItemSameComponents(leftStacks[i], rightStacks[i])) {
        return false;
      }
    }
    return true;
  }

  @Override
  public java.util.Optional<ResourceLocation> getRegistryName(
      net.minecraft.world.item.crafting.RecipeHolder<ShapedRetexturedRecipe> holder) {
    return java.util.Optional.of(holder.id());
  }

  @Override
  public int getWidth(net.minecraft.world.item.crafting.RecipeHolder<ShapedRetexturedRecipe> holder) {
    return holder.value().getWidth();
  }

  @Override
  public int getHeight(net.minecraft.world.item.crafting.RecipeHolder<ShapedRetexturedRecipe> holder) {
    return holder.value().getHeight();
  }

  @Override
  public void setRecipe(net.minecraft.world.item.crafting.RecipeHolder<ShapedRetexturedRecipe> holder,
      IRecipeLayoutBuilder builder, ICraftingGridHelper craftingGridHelper, IFocusGroup focuses) {
    ShapedRetexturedRecipe recipe = holder.value();
    RegistryAccess access = Objects.requireNonNull(SafeClientAccess.getRegistryAccess());

    // build display outputs from texture ingredient
    Ingredient texture = recipe.getTexture();
    List<ItemStack> displayOutputs = Arrays.stream(texture.getItems())
        .map(stack -> recipe.getResultItem(stack.getItem(), access))
        .toList();
    if (displayOutputs.isEmpty()) {
      displayOutputs = List.of(recipe.getResultItem(access));
    }

    // find texture slots
    List<Ingredient> inputs = recipe.getIngredients();
    int[] textureSlots = IntStream.range(0, inputs.size())
        .filter(i -> ingredientsMatch(texture, inputs.get(i)))
        .toArray();

    // add invisible output for recipe lookup
    builder.addInvisibleIngredients(RecipeIngredientRole.OUTPUT)
        .addItemStack(recipe.getResultItem(access));

    int width = recipe.getWidth();
    int height = recipe.getHeight();
    List<List<ItemStack>> inputStacks = inputs.stream()
        .map(ingredient -> List.of(ingredient.getItems())).toList();
    List<IRecipeSlotBuilder> inputSlots = craftingGridHelper.createAndSetInputs(builder, VanillaTypes.ITEM_STACK,
        inputStacks, width, height);
    IRecipeSlotBuilder outputSlot = craftingGridHelper.createAndSetOutputs(builder, displayOutputs);
    if (inputSlots.size() == 9) {
      builder.createFocusLink(Streams
          .concat(Stream.of(outputSlot),
              Arrays.stream(textureSlots)
                  .mapToObj(i -> inputSlots.get(MantleJEIConstants.getCraftingIndex(i, width, height))))
          .toArray(IRecipeSlotBuilder[]::new));
    } else {
      Mantle.logger.error("Failed to create focus link for {} as the layout {} is not 3x3",
          holder.id(), builder.getClass().getName());
    }
  }
}
