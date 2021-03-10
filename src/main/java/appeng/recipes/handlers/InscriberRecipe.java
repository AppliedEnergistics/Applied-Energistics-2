package appeng.recipes.handlers;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import appeng.api.features.InscriberProcessType;
import appeng.core.AppEng;

public class InscriberRecipe implements IRecipe<IInventory> {

    public static final ResourceLocation TYPE_ID = AppEng.makeId("inscriber");

    public static final IRecipeType<InscriberRecipe> TYPE = IRecipeType.register(TYPE_ID.toString());

    private final ResourceLocation id;
    private final String group;

    private final Ingredient middleInput;
    private final Ingredient topOptional;
    private final Ingredient bottomOptional;
    private final ItemStack output;
    private final InscriberProcessType processType;

    public InscriberRecipe(ResourceLocation id, String group, Ingredient middleInput, ItemStack output,
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
    public boolean matches(IInventory inv, World worldIn) {
        return false;
    }

    @Override
    public ItemStack assemble(IInventory inv) {
        return this.output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem() {
        return output;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return InscriberRecipeSerializer.INSTANCE;
    }

    @Override
    public IRecipeType<?> getType() {
        return TYPE;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> nonnulllist = NonNullList.create();
        nonnulllist.add(this.topOptional);
        nonnulllist.add(this.middleInput);
        nonnulllist.add(this.bottomOptional);
        return nonnulllist;
    }

    public Ingredient getMiddleInput() {
        return middleInput;
    }

    public ItemStack getOutput() {
        return output;
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
}
