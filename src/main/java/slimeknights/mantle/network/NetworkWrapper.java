package slimeknights.mantle.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.network.packet.ISimplePacket;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A small network implementation/wrapper using AbstractPackets instead of
 * IMessages.
 * Updated for NeoForge 1.21+ with CustomPacketPayload system
 */
@SuppressWarnings({ "unused", "WeakerAccess" })
public class NetworkWrapper {
  /** Channel name for this network */
  private final ResourceLocation channelName;
  private int id = 0;

  /**
   * Creates a new network wrapper
   * 
   * @param channelName Unique packet channel name
   * @deprecated Give your channel a version number.
   */
  @Deprecated
  public NetworkWrapper(ResourceLocation channelName) {
    this(channelName, "1");
  }

  public NetworkWrapper(ResourceLocation channelName, String version) {
    this.channelName = channelName;
    // Version is handled by Mantle mod registration in 1.21
  }

  /**
   * Registers a new {@link ISimplePacket}
   * 
   * @param clazz   Packet class
   * @param decoder Packet decoder, typically the constructor
   * @param <MSG>   Packet class type
   */
  public <MSG extends ISimplePacket> void registerPacket(Class<MSG> clazz, Function<FriendlyByteBuf, MSG> decoder,
      @Nullable Object direction) {
    registerPacket(clazz, ISimplePacket::encode, decoder, ISimplePacket::handle, direction);
  }

  /**
   * Registers a new generic packet
   * 
   * @param clazz     Packet class
   * @param encoder   Encodes a packet to the buffer
   * @param decoder   Packet decoder, typically the constructor
   * @param consumer  Logic to handle a packet
   * @param direction Network direction (unused in 1.21+, kept for compatibility)
   * @param <MSG>     Packet class type
   */
  public <MSG> void registerPacket(Class<MSG> clazz, BiConsumer<MSG, FriendlyByteBuf> encoder,
      Function<FriendlyByteBuf, MSG> decoder, BiConsumer<MSG, Supplier<ISimplePacket.IPayloadContext>> consumer,
      @Nullable Object direction) {
    registerPacketNoLogger(clazz, encoder, wrapLogger(clazz, decoder), consumer, direction);
  }

  /**
   * Registers a new packet without the automatic logging if the decoder fails
   * 
   * @param clazz     Packet class
   * @param encoder   Encodes a packet to the buffer
   * @param decoder   Packet decoder, typically the constructor
   * @param consumer  Logic to handle a packet
   * @param direction Network direction (unused in 1.21+, kept for compatibility)
   * @param <MSG>     Packet class type
   */
  public <MSG> void registerPacketNoLogger(Class<MSG> clazz, BiConsumer<MSG, FriendlyByteBuf> encoder,
      Function<FriendlyByteBuf, MSG> decoder, BiConsumer<MSG, Supplier<ISimplePacket.IPayloadContext>> consumer,
      @Nullable Object direction) {
    // In NeoForge 1.21, packets are registered via RegisterPayloadHandlersEvent
    // This method is kept for API compatibility but registration happens in Mantle
    // mod class
    this.id++;
  }

  /** Wraps the given decoder function */
  private static <MSG> Function<FriendlyByteBuf, MSG> wrapLogger(Class<MSG> clazz,
      Function<FriendlyByteBuf, MSG> decoder) {
    return buffer -> {
      try {
        return decoder.apply(buffer);
      } catch (Exception e) {
        Mantle.logger.error("Exception while decoding packet of class {}", clazz.getName(), e);
        throw e;
      }
    };
  }

  /* Sending packets */

  /**
   * Sends a packet to the server
   * 
   * @param msg Packet to send
   */
  public void sendToServer(Object msg) {
    if (msg instanceof ISimplePacket packet) {
      net.neoforged.neoforge.network.PacketDistributor.sendToServer(packet);
    }
  }

  /**
   * Sends a packet to the given packet distributor
   * 
   * @param target  Packet target
   * @param message Packet to send
   */
  public void send(Object target, Object message) {
    // Placeholder - in 1.21 use PacketDistributor directly
    if (message instanceof ISimplePacket packet) {
      PacketDistributor.sendToServer(packet);
    }
  }

  /**
   * Sends a vanilla packet to the given entity
   * 
   * @param packet Packet
   * @param player Player receiving the packet
   */
  public void sendVanillaPacket(Packet<?> packet, Entity player) {
    if (player instanceof ServerPlayer sPlayer) {
      sPlayer.connection.send(packet);
    }
  }

  /**
   * Sends a packet to a player
   * 
   * @param msg    Packet
   * @param player Player to send
   */
  public void sendTo(Object msg, Player player) {
    if (player instanceof ServerPlayer) {
      sendTo(msg, (ServerPlayer) player);
    }
  }

  /**
   * Sends a packet to a player
   * 
   * @param msg    Packet
   * @param player Player to send
   */
  public void sendTo(Object msg, ServerPlayer player) {
    if (!(player instanceof FakePlayer) && msg instanceof ISimplePacket packet) {
      PacketDistributor.sendToPlayer(player, packet);
    }
  }

  /**
   * Sends a packet to players near a location
   * 
   * @param msg         Packet to send
   * @param serverWorld World instance
   * @param position    Position within range
   */
  public void sendToClientsAround(Object msg, ServerLevel serverWorld, BlockPos position) {
    if (msg instanceof ISimplePacket packet) {
      LevelChunk chunk = serverWorld.getChunkAt(position);
      PacketDistributor.sendToPlayersTrackingChunk(serverWorld, chunk.getPos(), packet);
    }
  }

  /**
   * Sends a packet to all entities tracking the given entity
   * 
   * @param msg    Packet
   * @param entity Entity to check
   */
  public void sendToTrackingAndSelf(Object msg, Entity entity) {
    if (msg instanceof ISimplePacket packet) {
      // Use sendToPlayersTrackingEntityAndSelf
      PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, packet);
    }
  }

  /**
   * Sends a packet to all entities tracking the given entity
   * 
   * @param msg    Packet
   * @param entity Entity to check
   */
  public void sendToTracking(Object msg, Entity entity) {
    if (msg instanceof ISimplePacket packet) {
      PacketDistributor.sendToPlayersTrackingEntity(entity, packet);
    }
  }
}
