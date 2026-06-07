package slimeknights.mantle.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Sign block entity to make it easier for signs to be registered, as the
 * vanilla block entity has a closed set of blocks
 */
public class MantleSignBlockEntity extends SignBlockEntity {
  /** Sign blocks to use for the block entity valid blocks */
  private static final List<Supplier<? extends Block>> SIGN_BLOCKS = new ArrayList<>();
  /**
   * The registered BlockEntityType for MantleSignBlockEntity — set during
   * registration
   */
  @Nullable
  private static BlockEntityType<MantleSignBlockEntity> REGISTERED_TYPE = null;

  public MantleSignBlockEntity(BlockPos pos, BlockState state) {
    super(pos, state);
  }

  @Override
  public BlockEntityType<?> getType() {
    return REGISTERED_TYPE;
  }

  /** Called during block entity registration to store the registered type */
  public static void setRegisteredType(BlockEntityType<MantleSignBlockEntity> type) {
    REGISTERED_TYPE = type;
  }

  /**
   * Registers a sign block to be injected into the tile entity, should be called
   * before common setup
   * 
   * @param sign Sign block supplier
   */
  public static void registerSignBlock(Supplier<? extends Block> sign) {
    synchronized (SIGN_BLOCKS) {
      SIGN_BLOCKS.add(sign);
    }
  }

  /** Builds the list of sign blocks for TE registration */
  public static Set<Block> buildSignBlocks() {
    return SIGN_BLOCKS.stream().map(Supplier::get).collect(Collectors.toSet());
  }
}
