package slimeknights.mantle.network.packet;

import java.util.function.Supplier;

/**
 * Packet instance that automatically wraps the logic in
 * {@link ISimplePacket.IPayloadContext#enqueueWork(Runnable)} for thread safety
 */
public interface IThreadsafePacket extends ISimplePacket {
  @Override
  default void handle(Supplier<ISimplePacket.IPayloadContext> supplier) {
    ISimplePacket.IPayloadContext context = supplier.get();
    context.enqueueWork(() -> handleThreadsafe(context));
  }

  /**
   * Handles receiving the packet on the correct thread
   * 
   * @param context Payload context
   */
  void handleThreadsafe(ISimplePacket.IPayloadContext context);
}
