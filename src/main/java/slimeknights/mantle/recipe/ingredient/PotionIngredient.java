package slimeknights.mantle.recipe.ingredient;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.crafting.IngredientType;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/** Simple ingredient checking for an item with a specific potion */
public class PotionIngredient extends ItemIngredient {
  /** MapCodec for serialization */
  public static final MapCodec<PotionIngredient> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
      BuiltInRegistries.ITEM.byNameCodec().listOf().fieldOf("items").forGetter((PotionIngredient i) -> i.items),
      TagKey.hashedCodec(Registries.ITEM).optionalFieldOf("tag")
          .forGetter((PotionIngredient i) -> java.util.Optional.ofNullable(i.tag)),
      BuiltInRegistries.POTION.holderByNameCodec().optionalFieldOf("potion")
          .forGetter((PotionIngredient i) -> java.util.Optional.ofNullable(i.potion)))
      .apply(instance, (items, tag, potion) -> new PotionIngredient(items, tag.orElse(null), potion.orElse(null))));

  @Nullable
  private final Holder<Potion> potion;

  protected PotionIngredient(List<Item> items, @Nullable TagKey<Item> itemTag, @Nullable Holder<Potion> potion) {
    super(items, itemTag);
    this.potion = potion;
  }

  /** Creates a potion ingredient matching a list of items */
  public static PotionIngredient of(@Nullable Holder<Potion> potion, List<ItemLike> items) {
    return new PotionIngredient(toItem(items), null, potion);
  }

  /** Creates a potion ingredient matching a list of items */
  public static PotionIngredient of(@Nullable Holder<Potion> potion, ItemLike... items) {
    return of(potion, Arrays.asList(items));
  }

  /** Creates a potion ingredient matching a tag */
  public static PotionIngredient of(@Nullable Holder<Potion> potion, TagKey<Item> tag) {
    return new PotionIngredient(List.of(), tag, potion);
  }

  @Override
  public boolean test(@Nullable ItemStack stack) {
    // stack must match, any item must match, and potion must match (or potion is
    // null for wildcard)
    if (stack == null || !super.test(stack)) {
      return false;
    }
    if (potion == null) {
      return true; // null potion means match any potion
    }
    return stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).potion()
        .orElse(null) == potion;
  }

  @Override
  public Stream<ItemStack> getItems() {
    // Get items from parent (items list + tag items) and add potion to each
    return super.getItems().map(stack -> {
      ItemStack potionStack = stack.copy();
      if (potion != null) {
        potionStack.set(DataComponents.POTION_CONTENTS, new PotionContents(potion));
      }
      return potionStack;
    });
  }

  @Override
  public boolean isSimple() {
    return false;
  }

  @Override
  public IngredientType<?> getType() {
    return slimeknights.mantle.recipe.MantleRecipes.POTION_INGREDIENT.get();
  }
}
