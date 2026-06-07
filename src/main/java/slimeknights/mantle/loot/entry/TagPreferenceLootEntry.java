package slimeknights.mantle.loot.entry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import slimeknights.mantle.loot.MantleLoot;
import slimeknights.mantle.recipe.helper.TagPreference;
import slimeknights.mantle.util.JsonHelper;

import java.util.List;
import java.util.function.Consumer;

/**
 * Loot entry that returns the preferred item from a tag. See
 * {@link TagPreference}
 */
public class TagPreferenceLootEntry extends LootPoolSingletonContainer {
  private final TagKey<Item> tag;

  protected TagPreferenceLootEntry(TagKey<Item> tag, int weight, int quality, List<LootItemCondition> conditions,
      List<LootItemFunction> functions) {
    super(weight, quality, conditions, functions);
    this.tag = tag;
  }

  /** Codec for serialization/deserialization */
  // TODO 1.21.1: CODEC disabled - parent class fields (weight, quality,
  // conditions, functions) no longer accessible
  // Loot entry serialization may have changed API pattern - needs
  // reimplementation
  public static final MapCodec<TagPreferenceLootEntry> CODEC = MapCodec.unit(() -> {
    throw new UnsupportedOperationException(
        "TagPreferenceLootEntry CODEC temporarily disabled - needs NeoForge 1.21.1 loot API migration");
  });
  /*
   * public static final MapCodec<TagPreferenceLootEntry>CODEC =
   * RecordCodecBuilder.mapCodec(instance -> instance.group(
   * TagKey.codec(Registries.ITEM).fieldOf("tag").forGetter(e -> e.tag),
   * Codec.INT.fieldOf("weight").forGetter(e -> e.weight),
   * Codec.INT.fieldOf("quality").forGetter(e -> e.quality),
   * LootItemCondition.DIRECT_CODEC.listOf().optionalFieldOf("conditions",
   * List.of()).forGetter(e -> e.conditions),
   * LootItemFunction.DIRECT_CODEC.listOf().optionalFieldOf("functions",
   * List.of()).forGetter(e -> e.functions)
   * ).apply(instance, TagPreferenceLootEntry::new));
   */

  @Override
  public LootPoolEntryType getType() {
    return MantleLoot.TAG_PREFERENCE;
  }

  @Override
  protected void createItemStack(Consumer<ItemStack> consumer, LootContext context) {
    TagPreference.getPreference(tag).ifPresent(item -> consumer.accept(new ItemStack(item)));
  }

  /** Creates a new builder */
  @SuppressWarnings("unused") // API
  public static Builder<?> tagPreference(TagKey<Item> tag) {
    return simpleBuilder((weight, quality, conditions, functions) -> new TagPreferenceLootEntry(tag, weight, quality,
        conditions, functions));
  }
}
