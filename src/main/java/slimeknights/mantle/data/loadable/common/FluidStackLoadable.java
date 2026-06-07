package slimeknights.mantle.data.loadable.common;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import slimeknights.mantle.data.loadable.ErrorFactory;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.field.LoadableField;
import slimeknights.mantle.data.loadable.primitive.IntLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;

import javax.annotation.Nullable;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/** Loadable for a fluid stack */
@SuppressWarnings("unused") // API
public class FluidStackLoadable {
  private FluidStackLoadable() {
  }

  /* reused lambdas */
  /** Getter for a fluid from a stack */
  private static final Function<FluidStack, Fluid> FLUID_GETTER = FluidStack::getFluid;
  /**
   * Checks if a stack can be serialized to a primitive form (fluid-only, no components).
   * Replaces the removed hasTag() check.
   */
  private static final Predicate<FluidStack> COMPACT_NBT = stack -> stack.getComponentsPatch().isEmpty();
  /** Maps a fluid stack that may be empty to a strictly not empty one */
  private static final BiFunction<FluidStack, ErrorFactory, FluidStack> NOT_EMPTY = (stack, error) -> {
    if (stack.isEmpty()) {
      throw error.create("FluidStack cannot be empty");
    }
    return stack;
  };

  /**
   * Gets the CustomData CompoundTag from a FluidStack, or null if absent/empty.
   * Replaces the removed FluidStack.getTag().
   */
  @Nullable
  private static CompoundTag getFluidCustomDataTag(FluidStack stack) {
    CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
    return data.isEmpty() ? null : data.copyTag();
  }

  /* fields */
  /** Field for an optional fluid */
  private static final LoadableField<Fluid, FluidStack> FLUID = Loadables.FLUID.defaultField("fluid", Fluids.EMPTY,
      false, FLUID_GETTER);
  /** Field for fluid stack amount */
  private static final LoadableField<Integer, FluidStack> AMOUNT = IntLoadable.FROM_ZERO.requiredField("amount",
      FluidStack::getAmount);
  /**
   * NBT field stored as CustomData component.
   * Reads/writes "nbt" JSON key as CompoundTag; applied to DataComponents.CUSTOM_DATA.
   */
  private static final LoadableField<CompoundTag, FluidStack> NBT =
      NBTLoadable.ALLOW_STRING.nullableField("nbt", FluidStackLoadable::getFluidCustomDataTag);

  /* Optional */
  /** Single item which may be empty with an amount of 1000 */
  public static final Loadable<FluidStack> OPTIONAL_BUCKET = fixedSize(FluidType.BUCKET_VOLUME);
  /** Loadable for a stack that may be empty with variable count */
  public static final RecordLoadable<FluidStack> OPTIONAL_STACK = RecordLoadable.create(FLUID, AMOUNT,
      FluidStackLoadable::makeStack);
  /** Loadable for a stack that may be empty with CustomData and an amount of 1000 */
  public static final RecordLoadable<FluidStack> OPTIONAL_BUCKET_NBT = fixedSizeNBT(FluidType.BUCKET_VOLUME);
  /** Loadable for a stack that may be empty with variable count and CustomData */
  public static final RecordLoadable<FluidStack> OPTIONAL_STACK_NBT =
      RecordLoadable.create(FLUID, AMOUNT, NBT, FluidStackLoadable::makeStack);

  /* Required */
  /** Single item which may not be empty with an amount of 1000 */
  public static final Loadable<FluidStack> REQUIRED_BUCKET = notEmpty(OPTIONAL_BUCKET);
  /** Loadable for a stack that may not be empty with variable count */
  public static final RecordLoadable<FluidStack> REQUIRED_STACK = notEmpty(OPTIONAL_STACK);
  /** Loadable for a stack that may not be empty with CustomData and an amount of 1000 */
  public static final RecordLoadable<FluidStack> REQUIRED_BUCKET_NBT = notEmpty(OPTIONAL_BUCKET_NBT);
  /** Loadable for a stack that may not be empty with variable count and CustomData */
  public static final RecordLoadable<FluidStack> REQUIRED_STACK_NBT = notEmpty(OPTIONAL_STACK_NBT);

  /* Helpers */

  /** Makes a fluid stack from fluid and amount */
  private static FluidStack makeStack(Fluid fluid, int amount) {
    if (fluid == Fluids.EMPTY || amount <= 0) {
      return FluidStack.EMPTY;
    }
    return new FluidStack(fluid, amount);
  }

  /**
   * Makes a fluid stack from fluid, amount, and optional CustomData tag.
   * The tag is stored as DataComponents.CUSTOM_DATA (replaces removed FluidStack(Fluid, int, CompoundTag)).
   */
  private static FluidStack makeStack(Fluid fluid, int amount, @Nullable CompoundTag nbt) {
    if (fluid == Fluids.EMPTY || amount <= 0) {
      return FluidStack.EMPTY;
    }
    FluidStack stack = new FluidStack(fluid, amount);
    if (nbt != null && !nbt.isEmpty()) {
      stack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
    }
    return stack;
  }

  /** Creates a loadable for a fluid stack with a fixed amount and no components */
  public static Loadable<FluidStack> fixedSize(int amount) {
    if (amount <= 0) {
      throw new IllegalArgumentException("Count must be positive, received " + amount);
    }
    return Loadables.FLUID.flatXmap(fluid -> makeStack(fluid, amount), FLUID_GETTER);
  }

  /** Creates a loadable for a fluid stack with a fixed amount and CustomData component support */
  public static RecordLoadable<FluidStack> fixedSizeNBT(int amount) {
    if (amount <= 0) {
      throw new IllegalArgumentException("Amount must be positive, received " + amount);
    }
    return RecordLoadable.create(FLUID, NBT, (fluid, tag) -> makeStack(fluid, amount, tag))
        .compact(fixedSize(amount), COMPACT_NBT);
  }

  /** Creates a non-empty variant of the loadable */
  public static Loadable<FluidStack> notEmpty(Loadable<FluidStack> loadable) {
    return loadable.validate(NOT_EMPTY);
  }

  /** Creates a non-empty variant of the loadable */
  public static RecordLoadable<FluidStack> notEmpty(RecordLoadable<FluidStack> loadable) {
    return loadable.validate(NOT_EMPTY);
  }
}
