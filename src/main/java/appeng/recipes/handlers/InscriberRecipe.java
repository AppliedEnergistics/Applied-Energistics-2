package appeng.recipes.handlers;

import appeng.api.features.InscriberProcessType;
import com.google.common.collect.ImmutableList;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import java.util.List;

public class InscriberRecipe implements IRecipe<IInventory> {

    public static IRecipeType<InscriberRecipe> TYPE;

    private final ResourceLocation id;
    private final String group;

    private List<Ingredient> inputs;
    private ItemStack output;
    private ItemStack topOptional;
    private ItemStack bottomOptional;
    private InscriberProcessType type;

    public InscriberRecipe(ResourceLocation id, String group, List<Ingredient> inputs, ItemStack output, ItemStack topOptional, ItemStack bottomOptional, InscriberProcessType type) {
        this.id = id;
        this.group = group;
        this.inputs = inputs;
        this.output = output;
        this.topOptional = topOptional;
        this.bottomOptional = bottomOptional;
        this.type = type;
    }

    @Override
    public boolean matches(IInventory inv, World worldIn) {
        return false;
    }

    @Override
    public ItemStack getCraftingResult(IInventory inv) {
        return null;
    }

    @Override
    public boolean canFit(int width, int height) {
        return false;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return null;
    }

    @Override
    public ResourceLocation getId() {
        return null;
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return null;
    }

    @Override
    public IRecipeType<?> getType() {
        return TYPE;
    }

    public List<Ingredient> getInputs() {
        return inputs;
    }

    public ItemStack getOutput() {
        return output;
    }

    public ItemStack getTopOptional() {
        return topOptional;
    }

    public ItemStack getBottomOptional() {
        return bottomOptional;
    }

}
