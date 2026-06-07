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
  // TODO 1.21.1: StatePropertiesPredicate now wrapped in Optional -
  // Builder.build() returns Optional
  private final Optional<StatePropertiesPredicate> properties;

  public BlockTagLootCondition(TagKey<Block> tag) {
    // TODO 1.21.1: StatePropertiesPredicate.ANY removed - use
    // Builder.properties().build() for empty predicate
    this(tag, StatePropertiesPredicate.Builder.properties().build());
  }

  public BlockTagLootCondition(TagKey<Block> tag, StatePropertiesPredicate.Builder builder) {
    this(tag, builder.build());
  }

  @Override
  public boolean test(LootContext context) {
    BlockState state = context.getParamOrNull(LootContextParams.BLOCK_STATE);
    // TODO 1.21.1: StatePropertiesPredicate now wrapped in Optional - unwrap with
    // map()
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

  // TODO 1.21.1: Made SerializerImpl public to allow external codec() access from
  // MantleLoot
  public static class SerializerImpl {
    // TODO 1.21.1: JSON serialization methods temporarily disabled -
    // serializeToJson() and fromJson() removed
    // Modern loot system uses codec() instead of JSON serialization
    public void serialize(JsonObject json, BlockTagLootCondition loot, JsonSerializationContext context) {
      throw new UnsupportedOperationException(
          "BlockTagLootCondition JSON serialization disabled - use codec() instead");
      /*
       * json.addProperty("tag", loot.tag.location().toString());
       * // TODO 1.21.1: properties now Optional - use isPresent() instead of ANY
       * comparison
       * if (loot.properties.isPresent()) {
       * json.add("properties", loot.properties.get().serializeToJson());
       * }
       */
    }

    public BlockTagLootCondition deserialize(JsonObject json, JsonDeserializationContext context) {
      throw new UnsupportedOperationException(
          "BlockTagLootCondition JSON deserialization disabled - use codec() instead");
      /*
       * TagKey<Block> tag = TagKey.create(Registries.BLOCK,
       * JsonHelper.getResourceLocation(json, "tag"));
       * // TODO 1.21.1: properties now Optional - use Optional.empty() instead of ANY
       * Optional<StatePropertiesPredicate> predicate = Optional.empty();
       * if (json.has("properties")) {
       * predicate =
       * Optional.of(StatePropertiesPredicate.fromJson(json.get("properties")));
       * }
       * return new BlockTagLootCondition(tag, predicate);
       */
    }

    public MapCodec<BlockTagLootCondition> codec() {
      return RecordCodecBuilder.mapCodec(instance -> instance.group(
          TagKey.codec(Registries.BLOCK).fieldOf("tag").forGetter(c -> c.tag),
          // TODO 1.21.1: CODEC returns Optional, use optionalFieldOf with
          // Optional.empty()
          StatePropertiesPredicate.CODEC.optionalFieldOf("properties").forGetter(c -> c.properties))
          .apply(instance, BlockTagLootCondition::new));
    }
  }
}
