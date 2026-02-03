package slimeknights.mantle.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;

/**
 * Packet interface to add common methods for registration
 * In NeoForge 1.21+, this extends CustomPacketPayload for the new payload
 * system
 */
public interface ISimplePacket extends CustomPacketPayload {
  /**
   * Encodes a packet for the buffer
   * 
   * @param buf Buffer instance
   */
  void encode(FriendlyByteBuf buf);

  /**
   * Handles receiving the packet
   * For NeoForge 1.21, this becomes part of the handler logic
   * 
   * @param context Packet handling context (supplier for legacy compatibility)
   */
  void handle(java.util.function.Supplier<ISimplePacket.IPayloadContext> context);

  /**
   * Get the payload type for this packet
   * Subclasses should override to return their specific type
   */
  @Override
  default Type<?> type() {
    // This will be overridden by each packet implementation
    throw new UnsupportedOperationException("Packet type must be defined by subclass");
  }

  /**
   * Context interface for packet handling - minimal wrapper for compatibility
   * Provides access to the player and thread-safe execution context
   */
  interface IPayloadContext {
    /**
     * Gets the player associated with this packet (sender for server, local for
     * client)
     * 
     * @return the player, or null if not available
     */
    @Nullable
    Player getSender();

    /**
     * Enqueues work to run on the main game thread
     * 
     * @param runnable work to execute
     */
    void enqueueWork(Runnable runnable);
  }
}
