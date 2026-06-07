package slimeknights.mantle.data;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntries;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.neoforged.neoforge.common.loot.LootModifierManager;
import slimeknights.mantle.data.JsonCodec.GsonCodec;

/**
 * This class contains codecs for various vanilla things that we need to use in
 * codecs. Typically the reason is forge pre-emptively moved a thing to codecs
 * before vanilla did.
 */
public class MantleCodecs {
  /** Codec for loot pool entries */
  public static final Codec<LootPoolEntryContainer> LOOT_ENTRY = LootPoolEntries.CODEC;
  /** Codec for loot item functions as an array */
  public static final Codec<LootItemFunction[]> LOOT_FUNCTIONS =
      LootItemFunctions.ROOT_CODEC.listOf()
          .xmap(l -> l.toArray(LootItemFunction[]::new), java.util.Arrays::asList);
  /** Codec for ingredients, handling NeoForge ingredient types */
  public static final Codec<Ingredient> INGREDIENT = new JsonCodec<>() {
    @Override
    public Ingredient deserialize(JsonElement element, DynamicOps<?> ops) {
      // TODO 1.21.1: Ingredient.fromJson removed - use CODEC instead
      return Ingredient.CODEC.parse(JsonOps.INSTANCE, element).getOrThrow();
    }

    @Override
    public JsonElement serialize(Ingredient ingredient, DynamicOps<?> ops) {
      // TODO 1.21.1: toJson() removed - use CODEC.encodeStart()
      return Ingredient.CODEC.encodeStart(JsonOps.INSTANCE, ingredient).getOrThrow();
    }

    @Override
    public String toString() {
      return "Ingredient";
    }
  };
}
