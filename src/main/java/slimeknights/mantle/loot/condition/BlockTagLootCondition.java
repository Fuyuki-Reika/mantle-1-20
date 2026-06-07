package slimeknights.mantle.loot.condition;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.RequiredArgsConstructor;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import slimeknights.mantle.loot.MantleLoot;
import slimeknights.mantle.util.JsonHelper;

import java.util.Optional;
import java.util.Set;

/**
 * Variant of
 * {@link net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition}
 * that allows using a tag for block type instead of a block
 */
@RequiredArgsConstructor
public class BlockTagLootCondition implements LootItemCondition {
  public static final SerializerImpl SERIALIZER = new SerializerImpl();

  private final TagKey<Block> tag;
  private final Optional<StatePropertiesPredicate> properties;

  public BlockTagLootCondition(TagKey<Block> tag) {
    this(tag, StatePropertiesPredicate.Builder.properties().build());
  }

  public BlockTagLootCondition(TagKey<Block> tag, StatePropertiesPredicate.Builder builder) {
    this(tag, builder.build());
  }

  @Override
  public boolean test(LootContext context) {
    BlockState state = context.getParamOrNull(LootContextParams.BLOCK_STATE);
    return state != null && state.is(tag) && this.properties.map(p -> p.matches(state)).orElse(true);
  }

  @Override
  public Set<LootContextParam<?>> getReferencedContextParams() {
    return Set.of(LootContextParams.BLOCK_STATE);
  }

  @Override
  public LootItemConditionType getType() {
    return MantleLoot.BLOCK_TAG_CONDITION;
  }

  // SerializerImpl is public to allow external codec() access from MantleLoot
  public static class SerializerImpl {
    // JSON serialize/deserialize are not used — modern loot system uses codec()
    // exclusively
    public void serialize(JsonObject json, BlockTagLootCondition loot, JsonSerializationContext context) {
      throw new UnsupportedOperationException(
          "BlockTagLootCondition JSON serialization disabled - use codec() instead");
    }

    public BlockTagLootCondition deserialize(JsonObject json, JsonDeserializationContext context) {
      throw new UnsupportedOperationException(
          "BlockTagLootCondition JSON deserialization disabled - use codec() instead");
    }

    public MapCodec<BlockTagLootCondition> codec() {
      return RecordCodecBuilder.mapCodec(instance -> instance.group(
          TagKey.codec(Registries.BLOCK).fieldOf("tag").forGetter(c -> c.tag),
          StatePropertiesPredicate.CODEC.optionalFieldOf("properties").forGetter(c -> c.properties))
          .apply(instance, BlockTagLootCondition::new));
    }
  }
}
