package slimeknights.mantle.recipe.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * Ingredient for a non-NBT sensitive item from another mod, should never be
 * used outside datagen
 */
public class ItemNameIngredient implements ICustomIngredient {
  /** MapCodec for serialization */
  public static final MapCodec<ItemNameIngredient> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
      ResourceLocation.CODEC.listOf().fieldOf("names").forGetter(i -> i.names))
      .apply(instance, ItemNameIngredient::new));

  private final List<ResourceLocation> names;

  protected ItemNameIngredient(List<ResourceLocation> names) {
    this.names = names;
  }

  /** Creates a new ingredient from a list of names */
  public static ItemNameIngredient from(List<ResourceLocation> names) {
    return new ItemNameIngredient(names);
  }

  /** Creates a new ingredient from a list of names */
  public static ItemNameIngredient from(ResourceLocation... names) {
    return from(Arrays.asList(names));
  }

  @Override
  public boolean test(@Nullable ItemStack stack) {
    if (stack == null || stack.isEmpty()) {
      return false;
    }
    ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
    return names.contains(itemId);
  }

  @Override
  public Stream<ItemStack> getItems() {
    return names.stream()
        .map(BuiltInRegistries.ITEM::get)
        .filter(item -> item != null)
        .map(ItemStack::new);
  }

  /** Creates a JSON object for a name */
  private static JsonObject forName(ResourceLocation name) {
    JsonObject json = new JsonObject();
    json.addProperty("item", name.toString());
    return json;
  }
  public JsonElement toJson() {
    if (names.size() == 1) {
      return forName(names.get(0));
    }
    JsonArray array = new JsonArray();
    for (ResourceLocation name : names) {
      array.add(forName(name));
    }
    return array;
  }

  @Override
  public boolean isSimple() {
    return false;
  }

  @Override
  public IngredientType<?> getType() {
    // This is a datagen-only helper, so we return a placeholder type
    // In practice, this serializes as vanilla ingredient JSON
    throw new UnsupportedOperationException("ItemNameIngredient is for datagen only");
  }
}
