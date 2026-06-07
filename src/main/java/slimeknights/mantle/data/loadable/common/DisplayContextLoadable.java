package slimeknights.mantle.data.loadable.common;

import com.google.gson.JsonSyntaxException;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.data.loadable.mapping.EnumMapLoadable;
import slimeknights.mantle.data.loadable.primitive.ResourceLocationLoadable;
import slimeknights.mantle.util.typed.TypedMap;

import java.util.Map;

/**
 * Special loadable for display contexts due to the Forge weirdness in
 * {@link ItemDisplayContext}
 */
public enum DisplayContextLoadable implements ResourceLocationLoadable<ItemDisplayContext> {
  INSTANCE;

  @Override
  public ItemDisplayContext fromKey(ResourceLocation name, String key, TypedMap context) {
    // TODO 1.21.1: NeoForgeRegistries.DISPLAY_CONTEXTS removed - ItemDisplayContext
    // likely now vanilla enum
    // Temporary: try to match by name string
    for (ItemDisplayContext ctx : ItemDisplayContext.values()) {
      if (ctx.getSerializedName().equals(name.toString()) || ctx.getSerializedName().equals(name.getPath())) {
        return ctx;
      }
    }
    throw new JsonSyntaxException(
        "Unable to parse " + key + " as no ItemDisplayContext matches name " + name);
  }

  @Override
  public ResourceLocation getKey(ItemDisplayContext object) {
    // TODO 1.21.1: NeoForgeRegistries.DISPLAY_CONTEXTS removed - using serialized
    // name
    return ResourceLocation.withDefaultNamespace(object.getSerializedName());
  }

  @Override
  public ItemDisplayContext decode(FriendlyByteBuf buffer, TypedMap context) {
    // TODO 1.21.1: NeoForgeRegistries.DISPLAY_CONTEXTS removed - reading by ordinal
    int ordinal = buffer.readVarInt();
    ItemDisplayContext[] values = ItemDisplayContext.values();
    if (ordinal >= 0 && ordinal < values.length) {
      return values[ordinal];
    }
    throw new RuntimeException("Invalid ItemDisplayContext ordinal: " + ordinal);
  }

  @Override
  public void encode(FriendlyByteBuf buffer, ItemDisplayContext value) {
    // TODO 1.21.1: NeoForgeRegistries.DISPLAY_CONTEXTS removed - writing by ordinal
    buffer.writeVarInt(value.ordinal());
  }

  @Override
  public <V> Loadable<Map<ItemDisplayContext, V>> mapWithValues(Loadable<V> valueLoadable, int minSize) {
    return new EnumMapLoadable<>(ItemDisplayContext.class, this, valueLoadable, minSize);
  }
}
