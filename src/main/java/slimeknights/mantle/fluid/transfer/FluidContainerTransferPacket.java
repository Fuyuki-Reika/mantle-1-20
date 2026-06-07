package slimeknights.mantle.fluid.transfer;

import lombok.RequiredArgsConstructor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import slimeknights.mantle.network.packet.IThreadsafePacket;
import slimeknights.mantle.network.packet.ISimplePacket;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** Packet to sync fluid container transfer */
@RequiredArgsConstructor
public class FluidContainerTransferPacket implements IThreadsafePacket {
  private final Set<Item> items;

  public FluidContainerTransferPacket(FriendlyByteBuf buffer) {
    // TODO 1.21.1: readRegistryIdUnsafe() and writeRegistryIdUnsafe() removed
    // Network serialization needs StreamCodec migration
    // Temporarily creating empty set
    // int size = buffer.readVarInt();
    // List<Item> builder = new ArrayList<>(size);
    // for (int i = 0; i < size; i++) {
    // builder.add(buffer.readRegistryIdUnsafe(BuiltInRegistries.ITEM));
    // }
    // this.items = Set.copyOf(builder);
    this.items = Set.of();
  }

  @Override
  public void encode(FriendlyByteBuf buffer) {
    // TODO 1.21.1: readRegistryIdUnsafe() and writeRegistryIdUnsafe() removed
    // Network serialization needs StreamCodec migration
    // Temporarily writing nothing
    // buffer.writeVarInt(items.size());
    // for (Item item : items) {
    // buffer.writeRegistryIdUnsafe(BuiltInRegistries.ITEM, item);
    // }
  }

  @Override
  public void handleThreadsafe(IPayloadContext context) {
    FluidContainerTransferManager.INSTANCE.setContainerItems(items);
  }
}
