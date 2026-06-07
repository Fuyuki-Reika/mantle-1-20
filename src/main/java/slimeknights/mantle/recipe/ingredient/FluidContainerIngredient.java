package slimeknights.mantle.recipe.ingredient;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.registration.object.FluidObject;
import slimeknights.mantle.util.JsonHelper;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.stream.Stream;

/** Ingredient that matches a container of fluid */
@SuppressWarnings("unused") // API
public class FluidContainerIngredient implements ICustomIngredient {
  public static final ResourceLocation ID = Mantle.getResource("fluid_container");

  // TODO 1.21.1: Loadable doesn't have codec() method - create custom MapCodec
  // using Loadable's serialize/convert
  private static final MapCodec<FluidIngredient> FLUID_INGREDIENT_CODEC = new MapCodec<FluidIngredient>() {
    @Override
    public <T> Stream<T> keys(DynamicOps<T> ops) {
      return Stream.of(ops.createString("fluid"));
    }

    @Override
    public <T> DataResult<FluidIngredient> decode(DynamicOps<T> ops, MapLike<T> input) {
      return DataResult.success(input.get("fluid"))
          .flatMap(fluidValue -> {
            try {
              JsonElement element = ops.convertTo(com.mojang.serialization.JsonOps.INSTANCE, fluidValue);
              return DataResult.success(FluidIngredient.LOADABLE.convert(element, "fluid"));
            } catch (Exception e) {
              return DataResult.error(() -> "Failed to parse FluidIngredient: " + e.getMessage());
            }
          });
    }

    @Override
    public <T> RecordBuilder<T> encode(FluidIngredient input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
      JsonElement json = FluidIngredient.LOADABLE.serialize(input);
      T encoded = com.mojang.serialization.JsonOps.INSTANCE.convertTo(ops, json);
      return prefix.add("fluid", encoded);
    }
  };

  /** MapCodec for serialization */
  public static final MapCodec<FluidContainerIngredient> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
      FLUID_INGREDIENT_CODEC.forGetter(i -> i.fluidIngredient),
      Ingredient.CODEC.optionalFieldOf("display").forGetter(i -> Optional.ofNullable(i.display)))
      .apply(instance, (fluid, display) -> new FluidContainerIngredient(fluid, display.orElse(null))));

  /** Ingredient to use for matching */
  private final FluidIngredient fluidIngredient;
  /** Internal ingredient to display the ingredient recipe viewers */
  @Nullable
  private final Ingredient display;
  private ItemStack[] displayStacks;

  protected FluidContainerIngredient(FluidIngredient fluidIngredient, @Nullable Ingredient display) {
    this.fluidIngredient = fluidIngredient;
    this.display = display;
  }

  /** Creates an instance from a fluid ingredient with a display container */
  public static FluidContainerIngredient fromIngredient(FluidIngredient ingredient, Ingredient display) {
    return new FluidContainerIngredient(ingredient, display);
  }

  /**
   * Creates an instance from a fluid ingredient with no display, not recommended
   */
  public static FluidContainerIngredient fromIngredient(FluidIngredient ingredient) {
    return new FluidContainerIngredient(ingredient, null);
  }

  /** Creates an instance from a fluid ingredient with a display container */
  public static FluidContainerIngredient fromFluid(FluidObject<?> fluid) {
    return fromIngredient(fluid.ingredient(FluidType.BUCKET_VOLUME), Ingredient.of(fluid));
  }

  @Override
  public boolean test(@Nullable ItemStack stack) {
    // first, must have a fluid capability
    if (stack == null || stack.isEmpty()) {
      return false;
    }

    var cap = stack.getCapability(Capabilities.FluidHandler.ITEM);
    if (cap == null) {
      return false;
    }

    // second, must contain enough fluid
    if (cap.getTanks() == 1) {
      FluidStack contained = cap.getFluidInTank(0);
      if (!contained.isEmpty() && fluidIngredient.getAmount(contained.getFluid()) == contained.getAmount()
          && fluidIngredient.test(contained.getFluid())) {
        // so far so good, from this point on we are forced to make copies as we need to
        // try draining, so copy and fetch the copy's cap
        // TODO 1.21.1: ItemHandlerHelper.copyStackWithSize() removed, use
        // copyWithCount()
        ItemStack copy = stack.copyWithCount(1);
        var copyCap = copy.getCapability(Capabilities.FluidHandler.ITEM);
        if (copyCap == null) {
          return false;
        }

        // alright, we know it has the fluid, the question is just whether draining the
        // fluid will give us the desired result
        Fluid fluid = copyCap.getFluidInTank(0).getFluid();
        int amount = fluidIngredient.getAmount(fluid);
        FluidStack drained = copyCap.drain(amount, FluidAction.EXECUTE);
        // we need an exact match, and we need the resulting container item to be the
        // same as the item stack's container item
        return drained.getFluid() == fluid && drained.getAmount() == amount
            && ItemStack.matches(stack.getCraftingRemainingItem(), copyCap.getContainer());
      }
    }
    return false;
  }

  @Override
  public boolean isSimple() {
    return false;
  }

  @Override
  public Stream<ItemStack> getItems() {
    if (displayStacks == null) {
      // no container? unfortunately hard to display this recipe so show nothing
      if (display == null) {
        displayStacks = new ItemStack[0];
      } else {
        // TODO 1.21.1: display.getItems() returns ItemStack[] directly, no need for
        // toArray()
        displayStacks = display.getItems();
      }
    }
    return Stream.of(displayStacks);
  }

  // TODO 1.21.1: toVanilla() signature likely changed - removed @Override
  // annotation
  public Ingredient toVanilla() {
    return display != null ? display : Ingredient.EMPTY;
  }

  // TODO 1.21.1: toJson() signature likely changed - removed @Override annotation
  public JsonElement toJson() {
    JsonElement element = fluidIngredient.serialize();
    JsonObject json;
    if (element.isJsonObject()) {
      json = element.getAsJsonObject();
    } else {
      json = new JsonObject();
      json.add("fluid", element);
    }
    json.addProperty("type", ID.toString());
    if (display != null) {
      // TODO 1.21.1: Ingredient.toJson() removed - use Ingredient.CODEC_NONEMPTY to
      // serialize
      json.add("display",
          Ingredient.CODEC_NONEMPTY.encodeStart(com.mojang.serialization.JsonOps.INSTANCE, display).getOrThrow());
    }
    return json;
  }

  @Override
  public IngredientType<?> getType() {
    return slimeknights.mantle.recipe.MantleRecipes.FLUID_CONTAINER_INGREDIENT.get();
  }
}
