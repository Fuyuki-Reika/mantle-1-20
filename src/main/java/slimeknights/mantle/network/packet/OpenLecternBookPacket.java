package slimeknights.mantle.network.packet;

import lombok.AllArgsConstructor;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import slimeknights.mantle.item.ILecternBookItem;

/**
 * Packet to open a book on a lectern
 */
@AllArgsConstructor
public class OpenLecternBookPacket implements IThreadsafePacket {
  private final BlockPos pos;
  private final ItemStack book;

  public OpenLecternBookPacket(FriendlyByteBuf buffer) {
    this.pos = buffer.readBlockPos();
    // TODO 1.21.1: buffer.readItem() removed, using ItemStack.STREAM_CODEC with
    // RegistryFriendlyByteBuf cast
    this.book = ItemStack.STREAM_CODEC.decode((RegistryFriendlyByteBuf) buffer);
  }

  @Override
  public void encode(FriendlyByteBuf buffer) {
    buffer.writeBlockPos(pos);
    // TODO 1.21.1: buffer.writeItem() removed, using ItemStack.STREAM_CODEC with
    // RegistryFriendlyByteBuf cast
    ItemStack.STREAM_CODEC.encode((RegistryFriendlyByteBuf) buffer, book);
  }

  @Override
  public void handleThreadsafe(ISimplePacket.IPayloadContext context) {
    if (book.getItem() instanceof ILecternBookItem) {
      ((ILecternBookItem) book.getItem()).openLecternScreenClient(pos, book);
    }
  }
}
