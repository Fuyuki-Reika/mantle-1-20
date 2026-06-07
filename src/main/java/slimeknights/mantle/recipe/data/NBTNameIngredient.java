package slimeknights.mantle.recipe.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Ingredient for a NBT sensitive item from another mod, should never be used
 * outside datagen
 */
public class NBTNameIngredient implements ICustomIngredient {
  /** MapCodec for serialization */
  public static final MapCodec<NBTNameIngredient> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
      ResourceLocation.CODEC.fieldOf("name").forGetter(i -> i.name),
      CompoundTag.CODEC.optionalFieldOf("nbt").forGetter(i -> java.util.Optional.ofNullable(i.nbt)))
      .apply(instance, (name, nbt) -> new NBTNameIngredient(name, nbt.orElse(null))));

  private final ResourceLocation name;
  @Nullable
  private final CompoundTag nbt;

  protected NBTNameIngredient(ResourceLocation name, @Nullable CompoundTag nbt) {
    this.name = name;
    this.nbt = nbt;
  }

  /**
   * Creates an ingredient for the given name and NBT
   * 
   * @param name Item name
   * @param nbt  NBT
   * @return Ingredient
   */
  public static NBTNameIngredient from(ResourceLocation name, CompoundTag nbt) {
    return new NBTNameIngredient(name, nbt);
  }

  /**
   * Creates an ingredient for an item that must have no NBT
   * 
   * @param name Item name
   * @return Ingredient
   */
  public static NBTNameIngredient from(ResourceLocation name) {
    return new NBTNameIngredient(name, null);
  }

  @Override
  public boolean test(@Nullable ItemStack stack) {
    if (stack == null || stack.isEmpty()) {
      return false;
    }
    ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
    if (!name.equals(itemId)) {
      return false;
    }
    if (nbt == null) {
      net.minecraft.world.item.component.CustomData customData = stack.getOrDefault(
          net.minecraft.core.component.DataComponents.CUSTOM_DATA,
          net.minecraft.world.item.component.CustomData.EMPTY);
      return customData.isEmpty();
    }
    // Otherwise, NBT must match exactly
    net.minecraft.world.item.component.CustomData customData = stack.getOrDefault(
        net.minecraft.core.component.DataComponents.CUSTOM_DATA,
        net.minecraft.world.item.component.CustomData.EMPTY);
    return !customData.isEmpty() && customData.copyTag().equals(nbt);
  }

  @Override
  public Stream<ItemStack> getItems() {
    ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.get(name));
    if (nbt != null) {
      stack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
          net.minecraft.world.item.component.CustomData.of(nbt.copy()));
    }
    return Stream.of(stack);
  }

  @Override
  public boolean isSimple() {
    return false;
  }
  public JsonElement toJson() {
    JsonObject json = new JsonObject();
    json.addProperty("item", name.toString());
    if (nbt != null) {
      json.addProperty("nbt", nbt.toString());
    }
    return json;
  }

  @Override
  public IngredientType<?> getType() {
    // This is a datagen-only helper
    throw new UnsupportedOperationException("NBTNameIngredient is for datagen only");
  }
}
