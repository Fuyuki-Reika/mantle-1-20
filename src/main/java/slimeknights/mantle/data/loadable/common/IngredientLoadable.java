package slimeknights.mantle.data.loadable.common;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.crafting.Ingredient;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.util.typed.TypedMap;

/** Loadable for ingredients, handling Forge ingredients */
public enum IngredientLoadable implements Loadable<Ingredient> {
  ALLOW_EMPTY,
  DISALLOW_EMPTY;

  @Override
  public Ingredient convert(JsonElement element, String key, TypedMap context) {
    // TODO 1.21.1: Ingredient.fromJson removed - use CODEC instead
    return Ingredient.CODEC.parse(JsonOps.INSTANCE, element).getOrThrow();
  }

  @Override
  public JsonElement serialize(Ingredient object) {
    if (object.isEmpty() && this == DISALLOW_EMPTY) {
      throw new IllegalArgumentException("Ingredient cannot be empty");
    }
    // TODO 1.21.1: toJson() removed - use CODEC.encodeStart()
    return Ingredient.CODEC.encodeStart(JsonOps.INSTANCE, object).getOrThrow();
  }

  @Override
  public Ingredient decode(FriendlyByteBuf buffer, TypedMap context) {
    // TODO 1.21.1: fromNetwork removed - use STREAM_CODEC with
    // RegistryFriendlyByteBuf
    return Ingredient.CONTENTS_STREAM_CODEC.decode((RegistryFriendlyByteBuf) buffer);
  }

  @Override
  public void encode(FriendlyByteBuf buffer, Ingredient object) {
    // TODO 1.21.1: toNetwork removed - use STREAM_CODEC with
    // RegistryFriendlyByteBuf
    Ingredient.CONTENTS_STREAM_CODEC.encode((RegistryFriendlyByteBuf) buffer, object);
  }
}
