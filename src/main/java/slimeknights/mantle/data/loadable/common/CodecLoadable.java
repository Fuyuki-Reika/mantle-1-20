package slimeknights.mantle.data.loadable.common;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import slimeknights.mantle.data.loadable.ErrorFactory;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.util.typed.TypedMap;

/**
 * Implementation of a loadable using a codec. Note this will be inefficient
 * when reading from and writing to the network
 */
public record CodecLoadable<T>(DynamicOps<Tag> ops, Codec<T> codec) implements Loadable<T> {
  public CodecLoadable(Codec<T> codec) {
    this(NbtOps.INSTANCE, codec);
  }

  @Override
  public T convert(JsonElement element, String key, TypedMap context) {
    // TODO 1.21.1: DataResult.getOrThrow() no longer takes arguments
    return codec.parse(JsonOps.INSTANCE, element).getOrThrow();
  }

  @Override
  public JsonElement serialize(T object) {
    // TODO 1.21.1: DataResult.getOrThrow() no longer takes arguments
    return codec.encodeStart(JsonOps.INSTANCE, object).getOrThrow();
  }

  @Override
  public T decode(FriendlyByteBuf buffer, TypedMap context) {
    CompoundTag wrapper = buffer.readNbt();
    if (wrapper == null) {
      throw new RuntimeException("Expected NBT tag but got null in CodecLoadable.decode");
    }
    Tag encoded = wrapper.contains("__data") ? wrapper.get("__data") : wrapper;
    return codec.parse(ops, encoded).getOrThrow();
  }

  @Override
  public void encode(FriendlyByteBuf buffer, T object) {
    Tag encoded = codec.encodeStart(ops, object).getOrThrow();
    CompoundTag wrapper;
    if (encoded instanceof CompoundTag ct) {
      wrapper = ct;
    } else {
      wrapper = new CompoundTag();
      wrapper.put("__data", encoded);
    }
    buffer.writeNbt(wrapper);
  }
}
