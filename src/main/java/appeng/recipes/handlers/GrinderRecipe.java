package appeng.recipes.handlers;

import java.util.List;

import com.google.common.collect.ImmutableList;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

public class GrinderRecipe implements Recipe<Inventory> {

    public static RecipeType<GrinderRecipe> TYPE;

    private final Identifier id;
    private final String group;
    private final Ingredient ingredient;
    private final int ingredientCount;
    private final ItemStack result;
    private final List<GrinderOptionalResult> optionalResults;
    private final int turns;

    public GrinderRecipe(Identifier id, String group, Ingredient ingredient, int ingredientCount, ItemStack result,
            int turns, List<GrinderOptionalResult> optionalResults) {
        this.id = id;
        this.group = group;
        this.ingredient = ingredient;
        this.ingredientCount = ingredientCount;
        this.result = result;
        this.turns = turns;
        this.optionalResults = ImmutableList.copyOf(optionalResults);
    }

    @Override
    public String getGroup() {
        return group;
    }

    @Override
    public boolean matches(Inventory inv, World worldIn) {
        return this.ingredient.test(inv.getStack(0));
    }

    @Override
    public ItemStack craft(Inventory inv) {
        // FIXME: What about secondary output
        return this.result.copy();
    }

    @Override
    public boolean fits(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getOutput() {
        return result;
    }

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return GrinderRecipeSerializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return TYPE;
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    public int getTurns() {
        return turns;
    }

    public int getIngredientCount() {
        return ingredientCount;
    }

    @Override
    public DefaultedList<Ingredient> getPreviewInputs() {
        DefaultedList<Ingredient> nonnulllist = DefaultedList.of();
        nonnulllist.add(this.ingredient);
        return nonnulllist;
    }

    public List<GrinderOptionalResult> getOptionalResults() {
        return optionalResults;
    }

    @Override
    public boolean isIgnoredInRecipeBook() {
        return true;
    }

}
