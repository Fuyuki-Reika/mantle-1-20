package slimeknights.mantle.block.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.minecraft.world.level.material.MapColor;
import slimeknights.mantle.registration.deferred.FluidDeferredRegister;

import java.util.ArrayList;
import java.util.function.Function;
import java.util.function.Supplier;

/** Liquid block setting the entity on fire */
public class MobEffectLiquidBlock extends LiquidBlock {
  /** Reference to the fluid for accessing fluid type */
  private final BaseFlowingFluid fluid;
  private final Supplier<MobEffectInstance> effect;

  public MobEffectLiquidBlock(Supplier<? extends BaseFlowingFluid> supplier, Properties properties,
      Supplier<MobEffectInstance> effect) {
    super(supplier.get(), properties);
    this.fluid = supplier.get();
    this.effect = effect;
  }

  @Override
  public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
    if (entity.getFluidTypeHeight(fluid.getFluidType()) > 0 && entity instanceof LivingEntity living) {
      MobEffectInstance sourceEffect = this.effect.get();
      // Create a new instance - use the basic constructor
      MobEffectInstance uncurableEffect = new MobEffectInstance(
          sourceEffect.getEffect(),
          sourceEffect.getDuration(),
          sourceEffect.getAmplifier(),
          sourceEffect.isAmbient(),
          sourceEffect.isVisible(),
          sourceEffect.showIcon());
      living.addEffect(uncurableEffect);
    }
  }

  /** Creates a new block supplier */
  public static Function<Supplier<? extends BaseFlowingFluid>, LiquidBlock> createEffect(MapColor color, int lightLevel,
      Supplier<MobEffectInstance> effect) {
    return fluid -> new MobEffectLiquidBlock(fluid, FluidDeferredRegister.createProperties(color, lightLevel), effect);
  }
}
