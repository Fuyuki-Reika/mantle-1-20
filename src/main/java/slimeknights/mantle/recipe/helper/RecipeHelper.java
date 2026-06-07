package slimeknights.mantle.recipe.helper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import slimeknights.mantle.recipe.IMultiRecipe;

import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Helpers used in creation of recipes
 */
@SuppressWarnings({ "WeakerAccess", "unused" })
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RecipeHelper {
  // TODO 1.21.1: RecipeManager.byType is now private, must use reflection
  private static final Method BY_TYPE_METHOD;

  static {
    try {
      BY_TYPE_METHOD = RecipeManager.class.getDeclaredMethod("byType", RecipeType.class);
      BY_TYPE_METHOD.setAccessible(true);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException("Failed to find RecipeManager.byType method", e);
    }
  }

  /* Recipe manager utils */

  /**
   * Gets a recipe of a specific class type by name from the manager
   * 
   * @param manager Recipe manager
   * @param name    Recipe name
   * @param clazz   Output class
   * @param <C>     Return type
   * @return Optional of the recipe, or empty if the recipe is missing
   */
  public static <C extends Recipe<?>> Optional<C> getRecipe(RecipeManager manager, ResourceLocation name,
      Class<C> clazz) {
    return manager.byKey(name).filter(clazz::isInstance).map(clazz::cast);
  }

  /**
   * Gets a list of all recipes from the manager, safely casting to the specified
   * type. Multi Recipes are kept as a single recipe instance
   * 
   * @param manager Recipe manager
   * @param type    Recipe type
   * @param clazz   Preferred recipe class type
   * @param <I>     Recipe input type
   * @param <T>     Recipe class
   * @param <C>     Return type
   * @return List of recipes from the manager
   */
  // TODO 1.21.1: RecipeType<T> changed to RecipeType<?> - type parameter removed
  // TODO 1.21.1: byType now returns Map<ResourceLocation, RecipeHolder<T>> - need
  // to extract value()
  // TODO 1.21.1: Container changed to RecipeInput - Recipe<I extends RecipeInput>
  // instead of Recipe<C extends Container>
  // TODO 1.21.1: RecipeManager.byType is now private - using reflection
  @SuppressWarnings("unchecked")
  public static <I extends net.minecraft.world.item.crafting.RecipeInput, T extends Recipe<I>, C extends T> List<C> getRecipes(
      RecipeManager manager, RecipeType<?> type, Class<C> clazz) {
    try {
      Object recipeMap = BY_TYPE_METHOD.invoke(manager, (RecipeType<T>) type);
      if (recipeMap instanceof java.util.Map<?, ?> map) {
        return map.values().stream()
            .map(holder -> ((net.minecraft.world.item.crafting.RecipeHolder<?>) holder).value())
            .filter(clazz::isInstance)
            .map(clazz::cast)
            .collect(Collectors.toList());
      }
    } catch (Exception e) {
      throw new RuntimeException("Failed to access recipes for type " + type, e);
    }
    return List.of();
  }

  /**
   * Gets a list of recipes for display in a UI list, such as UI buttons. Will be
   * sorted to keep the order the same on both sides, and filtered based on the
   * given predicate and class
   * 
   * @param manager Recipe manager
   * @param type    Recipe type
   * @param clazz   Preferred recipe class type
   * @param filter  Filter for which recipes to add to the list
   * @param <I>     Recipe input type
   * @param <T>     Recipe class
   * @param <C>     Return type
   * @return Recipe list
   */
  // TODO 1.21.1: RecipeType<T> changed to RecipeType<?> - type parameter removed
  // TODO 1.21.1: Recipe.getId() removed - sorting by ID no longer possible,
  // removed sorting
  // TODO 1.21.1: byType now returns Map<ResourceLocation, RecipeHolder<T>> - need
  // to extract value()
  // TODO 1.21.1: Container changed to RecipeInput - Recipe<I extends RecipeInput>
  // instead of Recipe<C extends Container>
  // TODO 1.21.1: RecipeManager.byType is now private - using reflection
  @SuppressWarnings("unchecked")
  public static <I extends net.minecraft.world.item.crafting.RecipeInput, T extends Recipe<I>, C extends T> List<C> getUIRecipes(
      RecipeManager manager, RecipeType<?> type, Class<C> clazz, Predicate<? super C> filter) {
    try {
      Object recipeMap = BY_TYPE_METHOD.invoke(manager, (RecipeType<T>) type);
      if (recipeMap instanceof java.util.Map<?, ?> map) {
        return map.values().stream()
            .map(holder -> ((net.minecraft.world.item.crafting.RecipeHolder<?>) holder).value())
            .filter(clazz::isInstance)
            .map(clazz::cast)
            .filter(filter)
            .collect(Collectors.toList());
      }
    } catch (Exception e) {
      throw new RuntimeException("Failed to access recipes for type " + type, e);
    }
    return List.of();
  }

  /**
   * Gets a list of all recipes from the manager, expanding multi recipes.
   * Intended for use in recipe display such as JEI
   * 
   * @param <C>     Return type
   * @param access  Registry access instance
   * @param recipes Stream of recipes
   * @param clazz   Preferred recipe class type
   * @return List of flattened recipes from the manager
   */
  // TODO 1.21.1: Recipe.getId() removed - sorting by ID removed, kept
  // multi-recipe priority
  public static <C> List<C> getJEIRecipes(RegistryAccess access, Stream<? extends Recipe<?>> recipes, Class<C> clazz) {
    return recipes
        .sorted((r1, r2) -> {
          // if one is multi, and the other not, the multi recipe is larger
          boolean m1 = r1 instanceof IMultiRecipe<?>;
          boolean m2 = r2 instanceof IMultiRecipe<?>;
          if (m1 && !m2)
            return 1;
          if (!m1 && m2)
            return -1;
          // TODO 1.21.1: Recipe.getId() removed - can no longer sort by ID
          return 0;
        })
        .flatMap((recipe) -> {
          // if its a multi recipe, extract child recipes and stream those
          if (recipe instanceof IMultiRecipe<?>) {
            return ((IMultiRecipe<?>) recipe).getRecipes(access).stream();
          }
          return Stream.of(recipe);
        })
        .filter(clazz::isInstance)
        .map(clazz::cast)
        .collect(Collectors.toList());
  }

  /**
   * Gets a list of all recipes from the manager, expanding multi recipes.
   * Intended for use in recipe display such as JEI
   * 
   * @param <C>     Return type
   * @param access  Registry access instance
   * @param manager Recipe manager
   * @param type    Recipe type
   * @param clazz   Preferred recipe class type
   * @return List of flattened recipes from the manager
   */
  // TODO 1.21.1: RecipeType<T> changed to RecipeType<?> - type parameter removed
  // TODO 1.21.1: byType now returns Map<ResourceLocation, RecipeHolder<T>> - need
  // to extract value()
  // TODO 1.21.1: Container changed to RecipeInput - Recipe<I extends RecipeInput>
  // instead of Recipe<C extends Container>
  // TODO 1.21.1: RecipeManager.byType is now private - using reflection
  @SuppressWarnings("unchecked")
  public static <I extends net.minecraft.world.item.crafting.RecipeInput, T extends Recipe<I>, C> List<C> getJEIRecipes(
      RegistryAccess access, RecipeManager manager, RecipeType<?> type, Class<C> clazz) {
    try {
      Object recipeMap = BY_TYPE_METHOD.invoke(manager, (RecipeType<T>) type);
      if (recipeMap instanceof java.util.Map<?, ?> map) {
        Stream<? extends Recipe<?>> recipes = map.values().stream()
            .map(holder -> ((net.minecraft.world.item.crafting.RecipeHolder<?>) holder).value());
        return getJEIRecipes(access, recipes, clazz);
      }
    } catch (Exception e) {
      throw new RuntimeException("Failed to access recipes for type " + type, e);
    }
    return List.of();
  }
}
