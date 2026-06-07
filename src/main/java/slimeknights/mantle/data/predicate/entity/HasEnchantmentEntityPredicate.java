package slimeknights.mantle.data.predicate.entity;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.record.RecordLoadable;

// TODO 1.21.1: Loadables.ENCHANTMENT was commented out because BuiltInRegistries.ENCHANTMENT removed
// This predicate cannot be loaded until enchantment registry access is restored
/**
 * Predicate that checks if the given entity has the given enchantment on any of
 * their equipment
 */
public record HasEnchantmentEntityPredicate(Enchantment enchantment) implements LivingEntityPredicate {
  // public static final RecordLoadable<HasEnchantmentEntityPredicate> LOADER =
  // RecordLoadable.create(Loadables.ENCHANTMENT.requiredField("enchantment",
  // HasEnchantmentEntityPredicate::enchantment),
  // HasEnchantmentEntityPredicate::new);

  @Override
  public boolean matches(LivingEntity entity) {
    // TODO 1.21.1: BuiltInRegistries.ENCHANTMENT removed - registry moved to
    // datapack
    // return
    // EnchantmentHelper.getEnchantmentLevel(BuiltInRegistries.ENCHANTMENT.wrapAsHolder(enchantment),
    // entity) > 0;
    throw new UnsupportedOperationException(
        "HasEnchantmentEntityPredicate temporarily disabled - ENCHANTMENT registry moved to datapack");
  }

  @Override
  public RecordLoadable<HasEnchantmentEntityPredicate> getLoader() {
    // return LOADER;
    throw new UnsupportedOperationException(
        "HasEnchantmentEntityPredicate.LOADER temporarily disabled - enchantment registry unavailable");
  }
}
