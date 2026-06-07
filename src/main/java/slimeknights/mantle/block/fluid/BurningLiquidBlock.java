package slimeknights.mantle.block.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.minecraft.world.level.material.MapColor;
import slimeknights.mantle.registration.deferred.FluidDeferredRegister;

import java.util.function.Function;
import java.util.function.Supplier;

/** Liquid block setting the entity on fire */
public class BurningLiquidBlock extends LiquidBlock {
  /** Reference to the fluid for accessing fluid type */
  private final BaseFlowingFluid fluid;
  /** Burn time in seconds. Lava uses 15 */
  private final int burnTime;
  /** Damage from being in the fluid, lava uses 4 */
  private final float damage;

  public BurningLiquidBlock(Supplier<? extends BaseFlowingFluid> supplier, Properties properties, int burnTime,
      float damage) {
    super(supplier.get(), properties);
    this.fluid = supplier.get();
    this.burnTime = burnTime;
    this.damage = damage;
  }

  @SuppressWarnings("deprecation") // useless annotation on block methods
  @Override
  public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
    if (!entity.fireImmune() && entity.getFluidTypeHeight(fluid.getFluidType()) > 0) {
      entity.setRemainingFireTicks(burnTime * 20); // Convert seconds to ticks
      if (entity.hurt(entity.damageSources().lava(), damage)) {
        entity.playSound(SoundEvents.GENERIC_BURN, 0.4F, 2.0F + level.random.nextFloat() * 0.4F);
      }
    }
  }

  /** Creates a new block supplier */
  public static Function<Supplier<? extends BaseFlowingFluid>, LiquidBlock> createBurning(MapColor color,
      int lightLevel, int burnTime, float damage) {
    return fluid -> new BurningLiquidBlock(fluid, FluidDeferredRegister.createProperties(color, lightLevel), burnTime,
        damage);
  }
}
