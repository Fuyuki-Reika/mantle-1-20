package slimeknights.mantle.loot.function;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.block.entity.IRetexturedBlockEntity;
import slimeknights.mantle.loot.MantleLoot;
import slimeknights.mantle.util.RetexturedHelper;

import java.util.List;
import java.util.Set;

/**
 * Applies the data for a retextured block to the dropped item. No configuration
 * needed.
 */
@SuppressWarnings("WeakerAccess")
public class RetexturedLootFunction extends LootItemConditionalFunction {
  // TODO 1.21.1: LootItemConditionalFunction.CONDITIONAL_CODEC removed - loot
  // function API changed
  // May need to implement Serializer instead of using CODEC pattern
  public static final MapCodec<RetexturedLootFunction> CODEC = MapCodec.unit(() -> {
    throw new UnsupportedOperationException(
        "RetexturedLootFunction CODEC temporarily disabled - needs NeoForge 1.21.1 loot API migration");
  });
  /*
   * public static final MapCodec<RetexturedLootFunction> CODEC =
   * LootItemConditionalFunction.CONDITIONAL_CODEC
   * .xmap(RetexturedLootFunction::new, function -> function.predicates);
   */

  /**
   * Creates a new instance from the given conditions
   * 
   * @param conditions Conditions list
   */
  public RetexturedLootFunction(List<LootItemCondition> conditions) {
    super(conditions);
  }

  /** Creates a new instance with no conditions */
  public RetexturedLootFunction() {
    super(List.of());
  }

  @Override
  public Set<LootContextParam<?>> getReferencedContextParams() {
    return Set.of(LootContextParams.BLOCK_ENTITY);
  }

  @Override
  protected ItemStack run(ItemStack stack, LootContext context) {
    BlockEntity te = context.getParamOrNull(LootContextParams.BLOCK_ENTITY);
    if (te instanceof IRetexturedBlockEntity retextured) {
      RetexturedHelper.setTexture(stack, retextured.getTextureName());
    } else {
      String name = te == null ? "null" : te.getClass().getName();
      Mantle.logger.warn("Found wrong tile entity for loot function, expected IRetexturedTileEntity, found {}", name);
    }
    return stack;
  }

  @Override
  public LootItemFunctionType getType() {
    return MantleLoot.RETEXTURED_FUNCTION;
  }
}
