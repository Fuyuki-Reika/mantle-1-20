package slimeknights.mantle.data.gson;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import net.minecraft.util.GsonHelper;
import net.neoforged.neoforge.common.crafting.CraftingHelper;
import net.neoforged.neoforge.common.conditions.ICondition;

import java.lang.reflect.Type;

/** Serializer for a forge condition. */
public class ConditionSerializer implements JsonDeserializer<ICondition>, JsonSerializer<ICondition> {
  public static final ConditionSerializer INSTANCE = new ConditionSerializer();

  private ConditionSerializer() {
  }

  @Override
  public ICondition deserialize(JsonElement json, Type type, JsonDeserializationContext context)
      throws JsonParseException {
    // TODO 1.21.1: CraftingHelper.getCondition removed
    throw new UnsupportedOperationException(
        "ConditionSerializer.deserialize temporarily disabled - CraftingHelper.getCondition removed");
    // return CraftingHelper.getCondition(GsonHelper.convertToJsonObject(json,
    // "condition"));
  }

  @Override
  public JsonElement serialize(ICondition condition, Type type, JsonSerializationContext context) {
    // TODO 1.21.1: CraftingHelper.serialize removed
    throw new UnsupportedOperationException(
        "ConditionSerializer.serialize temporarily disabled - CraftingHelper.serialize removed");
    // return CraftingHelper.serialize(condition);
  }
}
