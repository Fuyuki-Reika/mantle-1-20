import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.advancements.AdvancementHolder;

public class TestRecipeOutput {
    public void test(RecipeOutput output) {
        // Test to see what accept methods are available
        output.accept(null, null, null);
    }
}
