package slimeknights.mantle.recipe.condition;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.storage.loot.LootContext;
import net.neoforged.neoforge.common.conditions.ICondition;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.util.JsonHelper;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Function;

/** Common logic for {@link TagEmptyCondition} and {@link TagFilledCondition} */
@RequiredArgsConstructor
public abstract class TagCondition<T> implements ICondition {
  @Getter
  protected final TagKey<T> tag;
  @Nullable
  private Optional<Registry<T>> registry;

  /** Gets the registry */
  @Nullable
  protected Registry<T> registry(LootContext context) {
    // registry is not going to disappear within the lifetime of this object
    if (registry == null) {
      registry = context.getLevel().registryAccess().registry(tag.registry());
      if (registry.isEmpty()) {
        Mantle.logger.error("Failed to find registry for tag " + tag + " in " + getClass().getSimpleName()
            + ", this indicates a broken resource or datapack.");
      }
    }
    return registry.orElse(null);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "(\"" + tag + "\")";
  }

  /** Serializer logic for tag keys */
  public record Serializer<C extends TagCondition<?>>(ResourceLocation getID, Function<TagKey<?>, C> constructor) {
    // TODO 1.21.1: write() is not an override, removed @Override annotation
    public void write(JsonObject json, C value) {
      TagKey<?> tag = value.getTag();
      // save some space in JSON by not setting registry if item (most common)
      if (!Registries.ITEM.equals(tag.registry())) {
        json.addProperty("registry", tag.registry().location().toString());
      }
      json.addProperty("tag", tag.location().toString());
    }

    // TODO 1.21.1: read() is not an override, removed @Override annotation
    public C read(JsonObject json) {
      return constructor.apply(TagKey.create(
          // default to item registry if registry is unset
          ResourceKey.createRegistryKey(JsonHelper.getResourceLocation(json, "registry", Registries.ITEM.location())),
          JsonHelper.getResourceLocation(json, "tag")));
    }

    // TODO 1.21.1: serialize() is not an override, removed @Override annotation
    public void serialize(JsonObject json, C value, JsonSerializationContext context) {
      write(json, value);
    }

    // TODO 1.21.1: deserialize() is not an override, removed @Override annotation
    public C deserialize(JsonObject json, JsonDeserializationContext context) {
      return read(json);
    }

    /** Creates a MapCodec for 1.21.1 ICondition dispatch */
    public MapCodec<C> codec() {
      return RecordCodecBuilder.mapCodec(instance -> instance.group(
          Codec.STRING.optionalFieldOf("registry", Registries.ITEM.location().toString())
              .forGetter(c -> c.getTag().registry().location().toString()),
          Codec.STRING.fieldOf("tag").forGetter(c -> c.getTag().location().toString()))
          .apply(instance, (registry, tag) -> {
            // TODO 1.21.1: ResourceLocation constructor changed, now uses parse() for
            // string input
            // Need raw type cast to work around wildcard type inference
            ResourceKey<?> regKey = ResourceKey.createRegistryKey(ResourceLocation.parse(registry));
            @SuppressWarnings("unchecked")
            TagKey<?> tagKey = TagKey.create((ResourceKey) regKey, ResourceLocation.parse(tag));
            return constructor.apply(tagKey);
          }));
    }
  }
}
