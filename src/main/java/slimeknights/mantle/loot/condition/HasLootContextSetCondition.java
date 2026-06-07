package slimeknights.mantle.loot.condition;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import slimeknights.mantle.loot.MantleLoot;
import slimeknights.mantle.util.JsonHelper;

import java.util.Objects;

/**
 * Loot condition that only runs if all required values in the given loot
 * context set are present. Good heuristic for using that set.
 */
public record HasLootContextSetCondition(LootContextParamSet set) implements LootItemCondition {
  /** Creates a new builder instance */
  public static Builder builder(LootContextParamSet set) {
    return new Builder(set);
  }

  @Override
  public LootItemConditionType getType() {
    return MantleLoot.HAS_CONTEXT_SET;
  }

  @Override
  public boolean test(LootContext context) {
    for (LootContextParam<?> param : set.getRequired()) {
      if (!context.hasParam(param)) {
        return false;
      }
    }
    return true;
  }

  /** Builder logic for this condition */
  public record Builder(LootContextParamSet set) implements LootItemCondition.Builder {
    @Override
    public LootItemCondition build() {
      return new HasLootContextSetCondition(set);
    }
  }

  /** Serializer logic */
  public static class Serializer {
    public void serialize(JsonObject json, HasLootContextSetCondition value, JsonSerializationContext context) {
      // TODO 1.21.1: LootContextParamSets.getKey() removed - JSON serialization needs
      // API migration
      throw new UnsupportedOperationException(
          "HasLootContextSetCondition JSON serialization disabled - needs NeoForge 1.21.1 loot API migration");
      /*
       * json.addProperty("set",
       * Objects.requireNonNull(LootContextParamSets.getKey(value.set),
       * "Unregistered loot LootContextParamSets").toString());
       */
    }

    public HasLootContextSetCondition deserialize(JsonObject json, JsonDeserializationContext context) {
      // TODO 1.21.1: LootContextParamSets.get() removed - JSON deserialization needs
      // API migration
      throw new UnsupportedOperationException(
          "HasLootContextSetCondition JSON deserialization disabled - needs NeoForge 1.21.1 loot API migration");
      /*
       * ResourceLocation key = JsonHelper.getResourceLocation(json, "set");
       * LootContextParamSet set = LootContextParamSets.get(key);
       * if (set == null) {
       * throw new JsonSyntaxException("Unknown LootContextParamSet " + key);
       * }
       * return new HasLootContextSetCondition(set);
       */
    }

    public MapCodec<HasLootContextSetCondition> codec() {
      // TODO 1.21.1: LootContextParamSets API completely removed - entire codec needs
      // reimplementation
      // Returning null to compile, but codec registration will fail at runtime
      throw new UnsupportedOperationException(
          "HasLootContextSetCondition codec disabled - LootContextParamSets API removed in NeoForge 1.21.1");
      /*
       * return Codec.STRING.xmap(
       * id -> {
       * // ResourceLocation(String) constructor removed - use parse()
       * throw new
       * UnsupportedOperationException("HasLootContextSetCondition codec disabled - LootContextParamSets.get() removed"
       * );
       * // LootContextParamSet set =
       * LootContextParamSets.get(ResourceLocation.parse(id));
       * },
       * set -> {
       * // LootContextParamSets.getKey() removed
       * throw new
       * UnsupportedOperationException("HasLootContextSetCondition codec disabled - LootContextParamSets.getKey() removed"
       * );
       * // Objects.requireNonNull(LootContextParamSets.getKey(set),
       * "Unregistered loot LootContextParamSets").toString()
       * }
       * ).fieldOf("set").xmap(HasLootContextSetCondition::new,
       * HasLootContextSetCondition::set);
       */
    }
  }
}
