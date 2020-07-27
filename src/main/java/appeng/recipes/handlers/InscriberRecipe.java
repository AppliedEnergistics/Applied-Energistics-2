package appeng.recipes.handlers;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.*;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

import appeng.api.features.InscriberProcessType;

public class InscriberRecipe implements Recipe<Inventory> {

    public static RecipeType<InscriberRecipe> TYPE;

    private final Identifier id;
    private final String group;

    private final Ingredient middleInput;
    private final Ingredient topOptional;
    private final Ingredient bottomOptional;
    private final ItemStack output;
    private final InscriberProcessType processType;

    public InscriberRecipe(Identifier id, String group, Ingredient middleInput, ItemStack output,
            Ingredient topOptional, Ingredient bottomOptional, InscriberProcessType processType) {
        this.id = id;
        this.group = group;
        this.middleInput = middleInput;
        this.output = output;
        this.topOptional = topOptional;
        this.bottomOptional = bottomOptional;
        this.processType = processType;
    }

    @Override
    public boolean matches(Inventory inv, World worldIn) {
        return false;
    }

    @Override
    public ItemStack craft(Inventory inv) {
        return this.output.copy();
    }

    @Override
    public boolean fits(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getOutput() {
        return output;
    }

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return InscriberRecipeSerializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return TYPE;
    }

    @Override
    public DefaultedList<Ingredient> getPreviewInputs() {
        DefaultedList<Ingredient> nonnulllist = DefaultedList.of();
        nonnulllist.add(this.topOptional);
        nonnulllist.add(this.middleInput);
        nonnulllist.add(this.bottomOptional);
        return nonnulllist;
    }

    public Ingredient getMiddleInput() {
        return middleInput;
    }

    public Ingredient getTopOptional() {
        return topOptional;
    }

    public Ingredient getBottomOptional() {
        return bottomOptional;
    }

    public InscriberProcessType getProcessType() {
        return processType;
    }

    @Override
    public String getGroup() {
        return group;
    }

    @Override
    public boolean isIgnoredInRecipeBook() {
        return true;
    }

}
