package slimeknights.mantle.recipe.helper;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.crafting.Ingredient;
import slimeknights.mantle.data.loadable.record.RecordLoadable;

import java.util.Objects;

/**
 * @deprecated This class is obsolete in NeoForge 21.1+. The
 *             IIngredientSerializer system has been
 *             replaced by IngredientType with MapCodec. Instead of using
 *             LoadableIngredientSerializer:
 * 
 *             <pre>
 * // Old pattern (removed):
 * public static final LoadableIngredientSerializer&lt;MyIngredient&gt; SERIALIZER = 
 *     new LoadableIngredientSerializer&lt;&gt;(RecordLoadable.create(...));
 * 
 * // New pattern (NeoForge 21.1+):
 * public static final MapCodec&lt;MyIngredient&gt; CODEC = RecordCodecBuilder.mapCodec(instance -&gt; ...);
 * // Then register: DeferredRegister.create(NeoForgeRegistries.Keys.INGREDIENT_TYPES, MOD_ID)
 * //     .register("my_ingredient", () -&gt; new IngredientType&lt;&gt;(CODEC));
 *             </pre>
 * 
 *             This class is kept only for compilation compatibility during
 *             migration.
 */
@Deprecated(forRemoval = true, since = "NeoForge 21.1")
public record LoadableIngredientSerializer<T extends Ingredient>(RecordLoadable<T> loadable) {

  /**
   * @deprecated No longer supported. Use MapCodec with IngredientType instead.
   */
  @Deprecated(forRemoval = true)
  public T parse(FriendlyByteBuf buffer) {
    throw new UnsupportedOperationException(
        "LoadableIngredientSerializer is obsolete. Use MapCodec with IngredientType instead.");
  }

  /**
   * @deprecated No longer supported. Use MapCodec with IngredientType instead.
   */
  @Deprecated(forRemoval = true)
  public T parse(JsonObject json) {
    throw new UnsupportedOperationException(
        "LoadableIngredientSerializer is obsolete. Use MapCodec with IngredientType instead.");
  }

  /**
   * @deprecated No longer supported. Use MapCodec with IngredientType instead.
   */
  @Deprecated(forRemoval = true)
  public void write(FriendlyByteBuf buffer, T ingredient) {
    throw new UnsupportedOperationException(
        "LoadableIngredientSerializer is obsolete. Use MapCodec with IngredientType instead.");
  }

  /**
   * Serializes the ingredient to JSON.
   * 
   * @deprecated No longer supported. Use MapCodec with IngredientType instead.
   */
  @Deprecated(forRemoval = true)
  public JsonObject serialize(T ingredient) {
    throw new UnsupportedOperationException(
        "LoadableIngredientSerializer is obsolete. Use MapCodec with IngredientType instead.");
  }
}
