package appeng.integration.modules.crafttweaker;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.item.IIngredient;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.managers.IRecipeManager;
import com.blamejared.crafttweaker.impl.actions.recipes.ActionAddRecipe;
import com.blamejared.crafttweaker.impl.item.MCWeightedItemStack;

import org.openzen.zencode.java.ZenCodeType;

import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;

import appeng.api.features.InscriberProcessType;
import appeng.recipes.handlers.GrinderOptionalResult;
import appeng.recipes.handlers.GrinderRecipe;

@ZenRegister
@ZenCodeType.Name("mods.appliedenergistics2.Grinder")
public class GrinderRecipeManager implements IRecipeManager {

    @ZenCodeType.Method
    public void addRecipe(String name, IItemStack output, IItemStack ingredient, int turns,
            MCWeightedItemStack... optionalOutputs) {
        name = fixRecipeName(name);
        GrinderRecipe recipe = makeRecipe(name, output, ingredient, ingredient.getAmount(), turns, optionalOutputs);
        CraftTweakerAPI.apply(new ActionAddRecipe(this, recipe, InscriberProcessType.INSCRIBE.name()));
    }

    @ZenCodeType.Method
    public void addRecipe(String name, IItemStack output, IIngredient ingredient, int ingredientCount, int turns,
            MCWeightedItemStack... optionalOutputs) {
        name = fixRecipeName(name);
        GrinderRecipe recipe = makeRecipe(name, output, ingredient, ingredientCount, turns, optionalOutputs);
        CraftTweakerAPI.apply(new ActionAddRecipe(this, recipe, InscriberProcessType.INSCRIBE.name()));
    }

    private GrinderRecipe makeRecipe(String name, IItemStack output, IIngredient ingredient, int ingredientCount,
            int turns, MCWeightedItemStack... optionalOutputs) {
        ResourceLocation resourceLocation = new ResourceLocation("crafttweaker", name);
        List<GrinderOptionalResult> optionalResults = Arrays.stream(optionalOutputs)
                .map(mcWeightedItemStack -> new GrinderOptionalResult((float) mcWeightedItemStack.getWeight(),
                        mcWeightedItemStack.getItemStack().getInternal()))
                .collect(
                        Collectors.toList());
        return new GrinderRecipe(resourceLocation, "", ingredient.asVanillaIngredient(),
                ingredientCount,
                output.getInternal(), turns, optionalResults);
    }

    @Override
    public IRecipeType<GrinderRecipe> getRecipeType() {
        return GrinderRecipe.TYPE;
    }
}
