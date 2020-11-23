package appeng.integration.modules.crafttweaker;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.item.IIngredient;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.managers.IRecipeManager;
import com.blamejared.crafttweaker.impl.actions.recipes.ActionAddRecipe;

import org.openzen.zencode.java.ZenCodeType;

import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;

import appeng.api.features.InscriberProcessType;
import appeng.recipes.handlers.InscriberRecipe;

@ZenRegister
@ZenCodeType.Name("mods.appliedenergistics2.Inscriber")
public class InscriberRecipeManager implements IRecipeManager {

    @ZenCodeType.Method
    public void addInscribeRecipe(String name, IItemStack output, IIngredient middleInput,
            @ZenCodeType.Optional IIngredient[] otherInputs) {
        name = fixRecipeName(name);
        InscriberRecipe recipe = makeRecipe(name, output, middleInput, otherInputs, InscriberProcessType.INSCRIBE);
        CraftTweakerAPI.apply(new ActionAddRecipe(this, recipe, InscriberProcessType.INSCRIBE.name()));
    }

    @ZenCodeType.Method
    public void addPressRecipe(String name, IItemStack output, IIngredient middleInput,
            @ZenCodeType.Optional IIngredient[] otherInputs) {
        name = fixRecipeName(name);
        InscriberRecipe recipe = makeRecipe(name, output, middleInput, otherInputs, InscriberProcessType.PRESS);
        CraftTweakerAPI.apply(new ActionAddRecipe(this, recipe, InscriberProcessType.PRESS.name()));
    }

    private InscriberRecipe makeRecipe(String name, IItemStack output, IIngredient middleInput,
            IIngredient[] otherInputs, InscriberProcessType type) {
        ResourceLocation resourceLocation = new ResourceLocation("crafttweaker", name);

        Ingredient middleIngredient = middleInput.asVanillaIngredient();
        Ingredient topIngredient = Ingredient.EMPTY;
        Ingredient bottomIngredient = Ingredient.EMPTY;
        if (otherInputs != null && otherInputs.length >= 1) {
            topIngredient = otherInputs[0].asVanillaIngredient();
            if (otherInputs.length == 2) {
                bottomIngredient = otherInputs[1].asVanillaIngredient();
            }
        }
        return new InscriberRecipe(resourceLocation, "", middleIngredient,
                output.getInternal(), topIngredient, bottomIngredient, type);
    }

    @Override
    public IRecipeType<InscriberRecipe> getRecipeType() {
        return InscriberRecipe.TYPE;
    }
}
