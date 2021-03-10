package appeng.recipes.handlers;

import java.util.List;

import com.google.common.collect.ImmutableList;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import appeng.core.AppEng;

public class GrinderRecipe implements IRecipe<IInventory> {

    public static final ResourceLocation TYPE_ID = AppEng.makeId("grinder");

    public static final IRecipeType<GrinderRecipe> TYPE = IRecipeType.register(TYPE_ID.toString());

    private final ResourceLocation id;
    private final String group;
    private final Ingredient ingredient;
    private final int ingredientCount;
    private final ItemStack result;
    private final List<GrinderOptionalResult> optionalResults;
    private final int turns;

    public GrinderRecipe(ResourceLocation id, String group, Ingredient ingredient, int ingredientCount,
            ItemStack result, int turns, List<GrinderOptionalResult> optionalResults) {
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
    public boolean matches(IInventory inv, World worldIn) {
        return this.ingredient.test(inv.getItem(0));
    }

    @Override
    public ItemStack assemble(IInventory inv) {
        // FIXME: What about secondary output
        return this.result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem() {
        return result;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return GrinderRecipeSerializer.INSTANCE;
    }

    @Override
    public IRecipeType<?> getType() {
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
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> nonnulllist = NonNullList.create();
        nonnulllist.add(this.ingredient);
        return nonnulllist;
    }

    public List<GrinderOptionalResult> getOptionalResults() {
        return optionalResults;
    }

}
