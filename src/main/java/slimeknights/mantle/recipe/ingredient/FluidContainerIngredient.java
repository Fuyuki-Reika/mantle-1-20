package slimeknights.mantle.recipe.ingredient;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.MapCodec;
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
  public static final Serializer SERIALIZER = new Serializer();

  /** MapCodec for serialization */
  public static final MapCodec<FluidContainerIngredient> CODEC = RecordCodecBuilder.mapCodec(instance ->
      instance.group(
          FluidIngredient.LOADABLE.codec().fieldOf("fluid").forGetter(i -> i.fluidIngredient),
          Ingredient.CODEC.optionalFieldOf("display").forGetter(i -> Optional.ofNullable(i.display))
      ).apply(instance, (fluid, display) -> new FluidContainerIngredient(fluid, display.orElse(null)))
  );

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
        ItemStack copy = ItemHandlerHelper.copyStackWithSize(stack, 1);
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
        displayStacks = display.getItems().toArray(ItemStack[]::new);
      }
    }
    return Stream.of(displayStacks);
  }

  @Override
  public Ingredient toVanilla() {
    return display != null ? display : Ingredient.EMPTY;
  }

  @Override
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
      json.add("display", display.toJson());
    }
    return json;
  }

  @SuppressWarnings("deprecation")
  @Override
  public net.neoforged.neoforge.common.crafting.IIngredientSerializer<? extends Ingredient> getSerializer() {
    return SERIALIZER;
  }

  /** Serializer logic */
  private static class Serializer implements net.neoforged.neoforge.common.crafting.IIngredientSerializer<FluidContainerIngredient> {
    @Override
    public FluidContainerIngredient parse(JsonObject json) {
      FluidIngredient fluidIngredient;
      // if we have fluid and its not a primitive, then its nested
      if (json.has("fluid") && !json.get("fluid").isJsonPrimitive()) {
        fluidIngredient = FluidIngredient.LOADABLE.getIfPresent(json, "fluid");
      } else {
        fluidIngredient = FluidIngredient.LOADABLE.convert(json, "fluid");
      }
      Ingredient displayIngredient = null;
      if (json.has("display")) {
        displayIngredient = Ingredient.fromJson(JsonHelper.getElement(json, "display"));
      }
      return new FluidContainerIngredient(fluidIngredient, displayIngredient);
    }

    @Override
    public FluidContainerIngredient parse(FriendlyByteBuf buffer) {
      FluidIngredient fluidIngredient = FluidIngredient.LOADABLE.decode(buffer);
      Ingredient displayIngredient = null;
      if (buffer.readBoolean()) {
        displayIngredient = Ingredient.fromNetwork(buffer);
      }
      return new FluidContainerIngredient(fluidIngredient, displayIngredient);
    }

    @Override
    public void write(FriendlyByteBuf buffer, FluidContainerIngredient ingredient) {
      FluidIngredient.LOADABLE.encode(buffer, ingredient.fluidIngredient);
      if (ingredient.display != null) {
        buffer.writeBoolean(true);
        ingredient.display.toNetwork(buffer);
      } else {
        buffer.writeBoolean(false);
      }
    }
  }
}
