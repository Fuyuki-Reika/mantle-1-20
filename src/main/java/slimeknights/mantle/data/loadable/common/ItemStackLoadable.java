package slimeknights.mantle.data.loadable.common;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.netty.handler.codec.EncoderException;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import slimeknights.mantle.data.loadable.ErrorFactory;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.field.LoadableField;
import slimeknights.mantle.data.loadable.primitive.IntLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.util.typed.TypedMap;

import javax.annotation.Nullable;
import java.util.function.BiFunction;
import java.util.function.Function;

/** Loadable for an item stack */
@SuppressWarnings("unused") // API
public class ItemStackLoadable {
  private ItemStackLoadable() {
  }

  /* reused lambdas */
  /** Getter for an item from a stack */
  private static final Function<ItemStack, Item> ITEM_GETTER = ItemStack::getItem;
  /** Maps an item stack that may be empty to a strictly not empty one */
  private static final BiFunction<ItemStack, ErrorFactory, ItemStack> NOT_EMPTY = (stack, error) -> {
    if (stack.isEmpty()) {
      throw error.create("ItemStack cannot be empty");
    }
    return stack;
  };
  /**
   * Gets the CustomData tag from a stack, or null if absent/empty.
   * Replaces the removed ItemStack.getTag().
   */
  @Nullable
  private static CompoundTag getCustomDataTag(ItemStack stack) {
    CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
    return data.isEmpty() ? null : data.copyTag();
  }

  /* fields */
  /** Field for an optional item */
  private static final LoadableField<Item, ItemStack> ITEM = Loadables.ITEM.defaultField("item", Items.AIR, false,
      ITEM_GETTER);
  /** Field for item stack count that allows empty */
  private static final LoadableField<Integer, ItemStack> COUNT = IntLoadable.FROM_ZERO.defaultField("count", 1, true,
      ItemStack::getCount);
  /**
   * NBT field using CustomData component — reads/writes "nbt" key as a CompoundTag.
   * On serialize: extracts CustomData component as CompoundTag.
   * On deserialize: applied via CustomData.of(tag) into DataComponents.CUSTOM_DATA.
   */
  private static final LoadableField<CompoundTag, ItemStack> NBT =
      NBTLoadable.ALLOW_STRING.nullableField("nbt", ItemStackLoadable::getCustomDataTag);

  /* Optional */
  /** Single item which may be empty with a count of 1 */
  public static final Loadable<ItemStack> OPTIONAL_ITEM = Loadables.ITEM.flatXmap(item -> makeStack(item, 1),
      ITEM_GETTER);
  /** Loadable for a stack that may be empty with variable count */
  public static final RecordLoadable<ItemStack> OPTIONAL_STACK = RecordLoadable
      .create(ITEM, COUNT, ItemStackLoadable::makeStack)
      .compact(OPTIONAL_ITEM, stack -> stack.getCount() == 1);
  /** Loadable for a stack that may be empty with CustomData (NBT equivalent) and a count of 1 */
  public static final RecordLoadable<ItemStack> OPTIONAL_ITEM_NBT = NBTStack.FIXED_COUNT;
  /** Loadable for a stack that may be empty with variable count and CustomData */
  public static final RecordLoadable<ItemStack> OPTIONAL_STACK_NBT = NBTStack.READ_COUNT;

  /* Required */
  /** Single item which may not be empty with a count of 1 */
  public static final Loadable<ItemStack> REQUIRED_ITEM = notEmpty(OPTIONAL_ITEM);
  /** Loadable for a stack that may not be empty with variable count */
  public static final RecordLoadable<ItemStack> REQUIRED_STACK = notEmpty(OPTIONAL_STACK);
  /** Loadable for a stack that may not be empty with CustomData (NBT equivalent) and a count of 1 */
  public static final RecordLoadable<ItemStack> REQUIRED_ITEM_NBT = notEmpty(OPTIONAL_ITEM_NBT);
  /** Loadable for a stack that may not be empty with variable count and CustomData */
  public static final RecordLoadable<ItemStack> REQUIRED_STACK_NBT = notEmpty(OPTIONAL_STACK_NBT);

  /* Helpers */

  /** Makes an item stack from the given parameters */
  private static ItemStack makeStack(Item item, int count) {
    if (item == Items.AIR || count == 0) {
      return ItemStack.EMPTY;
    }
    return new ItemStack(item, count);
  }

  /**
   * Makes an item stack from item, count and optional NBT tag.
   * The tag is stored as CustomData component (replaces removed ItemStack.setTag).
   */
  private static ItemStack makeStack(Item item, int count, @Nullable CompoundTag nbt) {
    if (item == Items.AIR || count == 0) {
      return ItemStack.EMPTY;
    }
    ItemStack stack = new ItemStack(item, count);
    if (nbt != null && !nbt.isEmpty()) {
      stack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
    }
    return stack;
  }

  /** Creates a non-empty variant of the loadable */
  public static Loadable<ItemStack> notEmpty(Loadable<ItemStack> loadable) {
    return loadable.validate(NOT_EMPTY);
  }

  /** Creates a non-empty variant of the loadable */
  public static RecordLoadable<ItemStack> notEmpty(RecordLoadable<ItemStack> loadable) {
    return loadable.validate(NOT_EMPTY);
  }

  /**
   * Loadable for an item stack with CustomData (replacing the old NBT share-tag system).
   * Reads/writes "nbt" as a JSON object or SNBT string. On the network, serializes via
   * buffer.readNbt() / buffer.writeNbt(), matching legacy behaviour.
   */
  private enum NBTStack implements RecordLoadable<ItemStack> {
    /** Reads count from JSON */
    READ_COUNT,
    /** Count is always 1 */
    FIXED_COUNT;

    /* General JSON */

    @Override
    public ItemStack deserialize(JsonObject json, TypedMap context) {
      int count = 1;
      if (this == READ_COUNT) {
        count = COUNT.get(json, context);
      }
      return makeStack(ITEM.get(json, context), count, NBT.get(json, context));
    }

    @Override
    public void serialize(ItemStack stack, JsonObject json) {
      ITEM.serialize(stack, json);
      if (this == READ_COUNT) {
        COUNT.serialize(stack, json);
      }
      NBT.serialize(stack, json);
    }

    /* Compact JSON — falls back to primitive (item-only) when no NBT and count == 1 */

    @Override
    public ItemStack convert(JsonElement element, String key, TypedMap context) {
      if (element.isJsonPrimitive()) {
        return OPTIONAL_ITEM.convert(element, key, context);
      }
      return RecordLoadable.super.convert(element, key, context);
    }

    @Override
    public JsonElement serialize(ItemStack stack) {
      if ((this == FIXED_COUNT || stack.getCount() == 1) && getCustomDataTag(stack) == null) {
        return OPTIONAL_ITEM.serialize(stack);
      }
      return RecordLoadable.super.serialize(stack);
    }

    /* Buffer */

    @Override
    public ItemStack decode(FriendlyByteBuf buffer, TypedMap context) {
      Item item = ITEM.decode(buffer, context);
      int count = 1;
      if (this == READ_COUNT) {
        count = COUNT.decode(buffer, context);
      }
      // Read NBT as CompoundTag (was share tag in legacy code)
      CompoundTag nbt = buffer.readNbt();
      return makeStack(item, count, nbt);
    }

    @Override
    public void encode(FriendlyByteBuf buffer, ItemStack stack) throws EncoderException {
      ITEM.encode(buffer, stack);
      if (this == READ_COUNT) {
        COUNT.encode(buffer, stack);
      }
      // Write CustomData as CompoundTag on the buffer
      buffer.writeNbt(getCustomDataTag(stack));
    }
  }
}
