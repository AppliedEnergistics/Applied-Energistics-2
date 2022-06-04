package appeng.recipes.transform;

import java.util.List;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import appeng.core.AppEng;

public final class TransformRecipe implements Recipe<Container> {
    public static final ResourceLocation TYPE_ID = AppEng.makeId("transform");
    public static RecipeType<TransformRecipe> TYPE;

    private final ResourceLocation id;
    public final List<Ingredient> ingredients;
    public final Item output;
    public final int count;

    public TransformRecipe(ResourceLocation id, List<Ingredient> ingredients, Item output, int count) {
        this.id = id;
        this.ingredients = ingredients;
        this.output = output;
        this.count = count;
    }

    @Override
    public boolean matches(Container container, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(Container container) {
        return null;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Override
    public ItemStack getResultItem() {
        return new ItemStack(output, count);
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return TransformRecipeSerializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return TYPE;
    }

    public ResourceLocation id() {
        return id;
    }

    public Item output() {
        return output;
    }

    public int count() {
        return count;
    }
}
