package slimeknights.mantle.recipe.data;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.neoforge.common.conditions.ICondition;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds a recipe consumer wrapper, which adds conditions to recipes
 */
@SuppressWarnings("unused") // API
public class ConsumerWrapperBuilder {
  private final List<ICondition> conditions = new ArrayList<>();

  private ConsumerWrapperBuilder() {
  }

  /**
   * Creates a wrapper builder with no conditions
   * 
   * @return Default builder
   */
  public static ConsumerWrapperBuilder wrap() {
    return new ConsumerWrapperBuilder();
  }

  /**
   * Adds a conditional to the consumer
   * 
   * @param condition Condition to add
   * @return Builder instance
   */
  @CanIgnoreReturnValue
  public ConsumerWrapperBuilder addCondition(ICondition condition) {
    conditions.add(condition);
    return this;
  }

  /**
   * Builds the recipe output for the wrapper builder
   * 
   * @param output Base recipe output
   * @return Built wrapper output
   */
  public RecipeOutput build(RecipeOutput output) {
    if (conditions.isEmpty()) {
      return output;
    }
    return new WrappedRecipeOutput(output, conditions);
  }

  private static class WrappedRecipeOutput implements RecipeOutput {
    private final RecipeOutput delegate;
    private final ICondition[] conditions;

    private WrappedRecipeOutput(RecipeOutput delegate, List<ICondition> conditions) {
      this.delegate = delegate;
      this.conditions = conditions.toArray(new ICondition[0]);
    }

    @Override
    public void accept(ResourceLocation id, Recipe<?> recipe, @Nullable AdvancementHolder advancement,
        ICondition... additionalConditions) {
      // Merge conditions from builder with additional conditions from caller
      ICondition[] allConditions = additionalConditions.length == 0
          ? this.conditions
          : ArrayUtils.addAll(this.conditions, additionalConditions);
      delegate.accept(id, recipe, advancement, allConditions);
    }

    @Override
    public Advancement.Builder advancement() {
      return delegate.advancement();
    }
  }
}
