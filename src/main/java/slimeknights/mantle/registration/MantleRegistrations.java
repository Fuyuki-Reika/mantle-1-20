package slimeknights.mantle.registration;

import net.minecraft.world.level.block.entity.BlockEntityType;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.block.entity.MantleHangingSignBlockEntity;
import slimeknights.mantle.block.entity.MantleSignBlockEntity;

import static slimeknights.mantle.registration.RegistrationHelper.injected;

/**
 * Various objects registered under Mantle
 * 
 * @deprecated ObjectHolder removed in NeoForge 1.21.1
 */
@Deprecated(forRemoval = true)
public class MantleRegistrations {
  private MantleRegistrations() {
  }

  /** @deprecated ObjectHolder removed in NeoForge 1.21.1 */
  @Deprecated(forRemoval = true)
  public static final BlockEntityType<MantleSignBlockEntity> SIGN = injected();

  /** @deprecated ObjectHolder removed in NeoForge 1.21.1 */
  @Deprecated(forRemoval = true)
  public static final BlockEntityType<MantleHangingSignBlockEntity> HANGING_SIGN = injected();
}
