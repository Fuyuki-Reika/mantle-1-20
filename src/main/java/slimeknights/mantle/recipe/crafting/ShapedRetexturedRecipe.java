package slimeknights.mantle.recipe.crafting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import lombok.Getter;
import net.minecraft.core.NonNullList;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.recipe.MantleRecipes;
import slimeknights.mantle.recipe.helper.LoggingRecipeSerializer;
import slimeknights.mantle.util.JsonHelper;
import slimeknights.mantle.util.RetexturedHelper;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * Recipe which sets the texture for a
 * {@link slimeknights.mantle.block.RetexturedBlock} based on an ingredient
 * input.
 */
@SuppressWarnings("WeakerAccess")
public class ShapedRetexturedRecipe extends ShapedRecipe {
  /** Ingredient used to determine the texture on the output */
  @Getter
  private final Ingredient texture;
  private final boolean matchAll;

  /** Creates a new recipe using the passed parameters */
  protected ShapedRetexturedRecipe(String group, CraftingBookCategory category, int width,
      int height, NonNullList<Ingredient> ingredients, ItemStack result, boolean showNotification, Ingredient texture,
      boolean matchAll) {
    super(group, category, new ShapedRecipePattern(width, height, ingredients, java.util.Optional.empty()), result,
        showNotification);
    this.texture = texture;
    this.matchAll = matchAll;
  }

  /**
   * Creates a new recipe using the passed parameters (with ResourceLocation id
   * for legacy compat)
   */
  protected ShapedRetexturedRecipe(ResourceLocation id, String group, CraftingBookCategory category, int width,
      int height, NonNullList<Ingredient> ingredients, ItemStack result, boolean showNotification, Ingredient texture,
      boolean matchAll) {
    super(group, category, new ShapedRecipePattern(width, height, ingredients, java.util.Optional.empty()), result,
        showNotification);
    this.texture = texture;
    this.matchAll = matchAll;
  }

  /**
   * Creates a new recipe using an existing shaped recipe
   * 
   * @param orig     Shaped recipe to copy
   * @param texture  Ingredient to use for the texture
   * @param matchAll If true, all inputs must match for the recipe to match
   */
  protected ShapedRetexturedRecipe(ShapedRecipe orig, Ingredient texture, boolean matchAll) {
    this(Mantle.getResource("shaped_retextured"), orig.getGroup(), orig.category(), orig.getWidth(), orig.getHeight(),
        orig.getIngredients(), orig.getResultItem(null), orig.showNotification(), texture, matchAll);
  }

  /**
   * Gets the output using the given texture
   * 
   * @param texture Texture to use
   * @return Output with texture. Will be blank if the input is not a block
   */
  public ItemStack getResultItem(Item texture, HolderLookup.Provider registries) {
    return RetexturedHelper.setTexture(getResultItem(registries).copy(), Block.byItem(texture));
  }

  @Override
  public ItemStack assemble(CraftingInput craftMatrix, HolderLookup.Provider registries) {
    ItemStack result = super.assemble(craftMatrix, registries);
    Block currentTexture = null;
    for (int i = 0; i < craftMatrix.size(); i++) {
      ItemStack stack = craftMatrix.getItem(i);
      if (!stack.isEmpty() && texture.test(stack)) {
        // fetch texture from the block if it has one
        Block block = RetexturedHelper.getTexture(stack);
        // assuming it does not, use the block itself as the texture (provided it is not
        // the result that is)
        if (block == Blocks.AIR && stack.getItem() != result.getItem()) {
          block = Block.byItem(stack.getItem());
        }
        // if no texture, skip
        if (block == Blocks.AIR) {
          continue;
        }

        // if we have not found a texture yet, store the found block
        if (currentTexture == null) {
          currentTexture = block;
          // match all means we must check the rest. If not match all, we can be done
          if (!matchAll) {
            break;
          }

          // if we found a texture before, must match or we do no texture
        } else if (currentTexture != block) {
          currentTexture = null;
          break;
        }
      }
    }

    // set the texture if found. No texture will use the fallback
    if (currentTexture != null) {
      return RetexturedHelper.setTexture(result, currentTexture);
    }
    return result;
  }

  @Override
  public RecipeSerializer<?> getSerializer() {
    return MantleRecipes.CRAFTING_SHAPED_RETEXTURED.get();
  }

  public static class Serializer implements LoggingRecipeSerializer<ShapedRetexturedRecipe> {
    @Override
    public com.mojang.serialization.MapCodec<ShapedRetexturedRecipe> codec() {
      return com.mojang.serialization.codecs.RecordCodecBuilder.mapCodec(inst -> inst.group(
          com.mojang.serialization.Codec.STRING.optionalFieldOf("group", "").forGetter(ShapedRecipe::getGroup),
          net.minecraft.world.item.crafting.CraftingBookCategory.CODEC.fieldOf("category")
              .forGetter(ShapedRecipe::category),
          net.minecraft.world.item.crafting.ShapedRecipePattern.MAP_CODEC.forGetter(r -> r.pattern),
          net.minecraft.world.item.ItemStack.STRICT_CODEC.fieldOf("result")
              .forGetter(r -> r.getResultItem(ShapedFallbackRecipe.emptyProvider())),
          com.mojang.serialization.Codec.BOOL.optionalFieldOf("show_notification", true)
              .forGetter(ShapedRecipe::showNotification),
          Ingredient.CODEC_NONEMPTY.fieldOf("texture").forGetter(ShapedRetexturedRecipe::getTexture),
          com.mojang.serialization.Codec.BOOL.optionalFieldOf("match_all", false)
              .forGetter(r -> r.matchAll))
          .apply(inst,
              (group, category, pattern, result, showNotification, texture, matchAll) -> new ShapedRetexturedRecipe(
                  group, category, pattern.width(), pattern.height(),
                  pattern.ingredients(), result, showNotification, texture, matchAll)));
    }

    @Override
    public net.minecraft.network.codec.StreamCodec<RegistryFriendlyByteBuf, ShapedRetexturedRecipe> streamCodec() {
      return net.minecraft.network.codec.StreamCodec.of(this::toNetworkSafe, this::fromNetworkSafe);
    }

    @Override
    public ShapedRetexturedRecipe fromNetworkSafe(RegistryFriendlyByteBuf buffer) {
      ShapedRecipe recipe = RecipeSerializer.SHAPED_RECIPE.streamCodec().decode(buffer);
      Ingredient texture = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
      boolean matchAll = buffer.readBoolean();
      return new ShapedRetexturedRecipe(recipe, texture, matchAll);
    }

    @Override
    public void toNetworkSafe(RegistryFriendlyByteBuf buffer, ShapedRetexturedRecipe recipe) {
      RecipeSerializer.SHAPED_RECIPE.streamCodec().encode(buffer, recipe);
      Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.texture);
      buffer.writeBoolean(recipe.matchAll);
    }
  }
}
