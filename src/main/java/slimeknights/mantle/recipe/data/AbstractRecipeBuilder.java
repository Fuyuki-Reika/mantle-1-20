package slimeknights.mantle.recipe.data;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@SuppressWarnings({ "WeakerAccess", "unused" })
public abstract class AbstractRecipeBuilder<T extends AbstractRecipeBuilder<T>> {
  /** Advancement builder for this class */
  protected final Advancement.Builder advancementBuilder = Advancement.Builder.advancement();
  /** Group for this recipe */
  @Nonnull
  protected String group = "";
  // TODO 1.21.1: Track if criteria were added since Builder.getCriteria() is not
  // accessible
  private boolean hasCriteria = false;

  /**
   * Adds a criteria to the recipe
   * 
   * @param name     Criteria name
   * @param criteria Criteria instance
   * @return Builder
   */
  @SuppressWarnings("unchecked")
  // TODO 1.21.1: Changed parameter type from CriterionTriggerInstance to
  // Criterion<?> for API compatibility
  public T unlockedBy(String name, Criterion<?> criteria) {
    this.advancementBuilder.addCriterion(name, criteria);
    this.hasCriteria = true;
    return (T) this;
  }

  /**
   * Sets the group for this recipe
   * 
   * @param group Recipe group
   * @return Builder
   */
  @SuppressWarnings("unchecked")
  public T group(String group) {
    this.group = group;
    return (T) this;
  }

  /**
   * Sets the group for this recipe
   * 
   * @param group Recipe resource location group
   * @return Builder
   */
  public T group(ResourceLocation group) {
    // if minecraft, no namepsace. Groups are technically not namespaced so this is
    // for consistency with vanilla
    if ("minecraft".equals(group.getNamespace())) {
      return group(group.getPath());
    }
    return group(group.toString());
  }

  /**
   * Builds the recipe with a default recipe ID, typically based on the output
   * 
   * @param output Recipe output consumer
   */
  public abstract void save(RecipeOutput output);

  /**
   * Builds the recipe
   * 
   * @param output Recipe output consumer
   * @param id     Recipe ID
   */
  public abstract void save(RecipeOutput output, ResourceLocation id);

  /**
   * Base logic for advancement building
   * 
   * @param id     Recipe ID
   * @param folder Group folder for saving recipes
   * @return AdvancementHolder
   */
  private AdvancementHolder buildAdvancementInternal(ResourceLocation id, String folder) {
    // TODO 1.21.1: ResourceLocation constructor changed, now uses
    // fromNamespaceAndPath for (namespace, path) constructor
    ResourceLocation advancementId = ResourceLocation.fromNamespaceAndPath(id.getNamespace(),
        "recipes/" + folder + "/" + id.getPath());
    // TODO 1.21.1: ResourceLocation constructor changed, now uses parse() for
    // string input
    // TODO 1.21.1: Builder.criteria is now private, using empty
    // AdvancementRequirements for now
    // TODO 1.21.1: parent() is deprecated, need alternative API
    this.advancementBuilder
        .parent(ResourceLocation.parse("recipes/root"))
        .rewards(AdvancementRewards.Builder.recipe(id));
    // TODO 1.21.1: RecipeUnlockedTrigger.unlocked() now returns Criterion<?>
    // directly
    this.advancementBuilder.addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id));
    // TODO 1.21.1: build() now returns AdvancementHolder directly
    return this.advancementBuilder.build(advancementId);
  }

  /**
   * Builds and validates the advancement, intended to be called in
   * {@link #save(RecipeOutput, ResourceLocation)}
   * 
   * @param id     Recipe ID
   * @param folder Group folder for saving recipes. Vanilla typically uses item
   *               groups, but for mods might as well base on the recipe
   * @return AdvancementHolder
   */
  @Nullable
  protected AdvancementHolder buildAdvancement(ResourceLocation id, String folder) {
    // TODO 1.21.1: getCriteria() not accessible, using hasCriteria flag
    if (!this.hasCriteria) {
      throw new IllegalStateException("No way of obtaining recipe " + id);
    }
    return buildAdvancementInternal(id, folder);
  }

  /**
   * Builds an optional advancement, intended to be called in
   * {@link #save(RecipeOutput, ResourceLocation)}
   * 
   * @param id     Recipe ID
   * @param folder Group folder for saving recipes. Vanilla typically uses item
   *               groups, but for mods might as well base on the recipe
   * @return AdvancementHolder, or null if the advancement was not defined
   */
  @SuppressWarnings("SameParameterValue") // API
  @Nullable
  protected AdvancementHolder buildOptionalAdvancement(ResourceLocation id, String folder) {
    // TODO 1.21.1: getCriteria() not accessible, using hasCriteria flag
    if (!this.hasCriteria) {
      return null;
    }
    return buildAdvancementInternal(id, folder);
  }
}
