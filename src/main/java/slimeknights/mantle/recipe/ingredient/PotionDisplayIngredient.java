package slimeknights.mantle.recipe.ingredient;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.crafting.IngredientType;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.recipe.helper.LoadableIngredientSerializer;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

/** Ingredient that shows all potion variants on the displayed item list */
public class PotionDisplayIngredient extends ItemIngredient {
  /** Ingredient serializer instance */
  public static final LoadableIngredientSerializer<PotionDisplayIngredient> SERIALIZER = new LoadableIngredientSerializer<>(RecordLoadable.create(ItemsField.INSTANCE, TAG_FIELD, PotionDisplayIngredient::new));

  /** MapCodec for network serialization */
  public static final MapCodec<PotionDisplayIngredient> CODEC = RecordCodecBuilder.mapCodec(instance ->
      instance.group(
          BuiltInRegistries.ITEM.byNameCodec().listOf().fieldOf("items").forGetter(i -> i.items),
          TagKey.hashedCodec(Registries.ITEM).optionalFieldOf("tag").forGetter(i -> java.util.Optional.ofNullable(i.tag))
      ).apply(instance, (items, tag) -> new PotionDisplayIngredient(items, tag.orElse(null)))
  );

  /** StreamCodec for network sync */
  public static final StreamCodec<RegistryFriendlyByteBuf, PotionDisplayIngredient> STREAM_CODEC =
      StreamCodec.composite(
          ByteBufCodecs.registry(Registries.ITEM).apply(ByteBufCodecs.list()),
          i -> i.items,
          ByteBufCodecs.optional(ByteBufCodecs.registry(Registries.ITEM).apply(ByteBufCodecs.tagKey(Registries.ITEM))),
          i -> java.util.Optional.ofNullable(i.tag),
          PotionDisplayIngredient::new
      );

  /** last return of {@link Ingredient#getItems()} */
  private ItemStack[] lastParentStacks = null;
  /** cache for {@link #getItems()} */
  private ItemStack[] displayStacks = null;

  protected PotionDisplayIngredient(List<Item> items, @Nullable TagKey<Item> tag) {
    super(items, tag);
  }

  /** Creates a ingredient matching a list of items */
  public static PotionDisplayIngredient of(List<ItemLike> items) {
    return new PotionDisplayIngredient(toItem(items), null);
  }

  /** Creates a ingredient matching a list of items */
  public static PotionDisplayIngredient of(ItemLike... items) {
    return of(List.of(items));
  }

  /** Creates a ingredient matching a tag */
  public static PotionDisplayIngredient of(TagKey<Item> tag) {
    return new PotionDisplayIngredient(List.of(), tag);
  }

  @Override
  public boolean isSimple() {
    return true;
  }

  @Override
  public java.util.stream.Stream<ItemStack> getItems() {
    // if empty, means we want wildcard, show all potions on the stack
    ItemStack[] parentStacks = super.getItems().map(ItemStack::copy).toArray(ItemStack[]::new);
    if (lastParentStacks != parentStacks) {
      lastParentStacks = parentStacks;
      displayStacks = BuiltInRegistries.POTION.stream()
          .filter(pot -> pot != Potions.EMPTY)
          .flatMap(pot -> Arrays.stream(parentStacks).map(item -> {
            var copy = item.copy();
            copy.set(net.minecraft.world.item.alchemy.PotionContents.DEFAULT, new net.minecraft.world.item.alchemy.PotionContents(pot));
            return copy;
          }))
          .toArray(ItemStack[]::new);
    }
    return Arrays.stream(displayStacks);
  }

  @Override
  public LoadableIngredientSerializer<? extends Ingredient> getSerializer() {
    return SERIALIZER;
  }

  @Override
  public com.google.gson.JsonElement toJson() {
    return SERIALIZER.serialize(this);
  }
}
