package slimeknights.mantle.data.predicate.entity;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.record.RecordLoadable;

/**
 * Predicate that checks if the given entity has the given enchantment on any of
 * their equipment. Uses ResourceKey since enchantments are now a datapack registry.
 */
public record HasEnchantmentEntityPredicate(ResourceKey<Enchantment> enchantmentKey) implements LivingEntityPredicate {
  public static final RecordLoadable<HasEnchantmentEntityPredicate> LOADER = RecordLoadable.create(
      Loadables.RESOURCE_LOCATION.xmap(
          (loc, e) -> ResourceKey.create(Registries.ENCHANTMENT, loc),
          (key, e) -> key.location()
      ).requiredField("enchantment", HasEnchantmentEntityPredicate::enchantmentKey),
      HasEnchantmentEntityPredicate::new);

  @Override
  public boolean matches(LivingEntity entity) {
    // Resolve the enchantment holder from the level's registry
    return entity.level().registryAccess()
        .registry(Registries.ENCHANTMENT)
        .flatMap(registry -> registry.getHolder(enchantmentKey))
        .map(holder -> EnchantmentHelper.getEnchantmentLevel(holder, entity) > 0)
        .orElse(false);
  }

  @Override
  public RecordLoadable<? extends LivingEntityPredicate> getLoader() {
    return LOADER;
  }
}
