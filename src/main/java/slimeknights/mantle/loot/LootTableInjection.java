package slimeknights.mantle.loot;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.primitive.StringLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Record holding a list of entries to inject into the given loot table
 */
public record LootTableInjection(ResourceLocation name, List<LootPoolInjection> pools) {
  // TODO 1.21.1: Disabled due to LootPoolInjection.LOADABLE being unavailable
  // public static final RecordLoadable<LootTableInjection> LOADABLE =
  // RecordLoadable.create(
  // Loadables.RESOURCE_LOCATION.requiredField("name", LootTableInjection::name),
  // LootPoolInjection.LOADABLE.list(1).requiredField("pools",
  // LootTableInjection::pools),
  // LootTableInjection::new);

  /**
   * Record holding a list of entries to inject into the given pool
   */
  public record LootPoolInjection(String name, LootPoolEntryContainer[] entries) {
    // TODO 1.21.1: Loadables.LOOT_ENTRY removed, pool.entries field made private -
    // needs loot system API migration
    // public static final RecordLoadable<LootPoolInjection> LOADABLE =
    // RecordLoadable.create(
    // StringLoadable.DEFAULT.requiredField("name", LootPoolInjection::name),
    // Loadables.LOOT_ENTRY.list(1).requiredField("entries", pool ->
    // List.of(pool.entries)),
    // LootPoolInjection::new);

    public LootPoolInjection(String name, List<LootPoolEntryContainer> entries) {
      this(name, entries.toArray(new LootPoolEntryContainer[0]));
    }

    /** Injects this into the given loot pool */
    // TODO 1.21.1: LootPool.entries field made private - need to find new API or
    // use AccessTransformer
    public void inject(LootTable table) {
      throw new UnsupportedOperationException(
          "Loot injection temporarily disabled - needs NeoForge 1.21.1 loot API migration");
      // LootPool pool = table.getPool(name);
      // //noinspection ConstantConditions method is annotated wrongly
      // if (pool != null) {
      // int oldLength = pool.entries.length;
      // pool.entries = Arrays.copyOf(pool.entries, oldLength + entries.length);
      // System.arraycopy(entries, 0, pool.entries, oldLength, entries.length);
      // } else {
      // Mantle.logger.warn("Failed to inject loot into {} pool {}",
      // table.getLootTableId(), name);
      // }
    }
  }

  /** Builder instance for a loot table injection */
  public static class Builder {
    private final Map<String, List<LootPoolEntryContainer>> pools = new LinkedHashMap<>();

    /** Inserts the given entries into the pool */
    @CanIgnoreReturnValue
    public Builder addToPool(String name, LootPoolEntryContainer... entries) {
      Collections.addAll(pools.computeIfAbsent(name, n -> new ArrayList<>()), entries);
      return this;
    }

    /** Inserts the given entries into the pool */
    @CanIgnoreReturnValue
    public Builder addToPool(LootPoolInjection injection) {
      return addToPool(injection.name, injection.entries);
    }

    /** Builds the list of injections */
    public LootTableInjection build(ResourceLocation name) {
      return new LootTableInjection(name, pools.entrySet().stream()
          .map(entry -> new LootPoolInjection(entry.getKey(), List.copyOf(entry.getValue()))).toList());
    }
  }
}
