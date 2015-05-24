package appengee3compat.core;

import appeng.api.AEApi;
import appeng.api.definitions.IParts;
import appeng.api.exceptions.MissingIngredientError;
import appeng.api.exceptions.RegistrationError;
import appeng.api.recipes.IIngredient;
import appeng.api.util.AEColor;
import appeng.recipes.game.ShapedRecipe;
import appeng.recipes.game.ShapelessRecipe;
import appeng.util.Platform;
import com.pahimar.ee3.api.exchange.RecipeRegistryProxy;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EE3RecipeHelper {
    public static void initRecipes() {

        for (Object recipeObject : CraftingManager.getInstance().getRecipeList()) {
            if (recipeObject instanceof ShapelessRecipe || recipeObject instanceof ShapedRecipe) {
                IRecipe recipe = (IRecipe) recipeObject;
                ItemStack recipeOutput = recipe.getRecipeOutput();

                if (recipeOutput != null) {
                    List<ItemStack> recipeInputs = getRecipeInputs(recipe);

                    //-Dlog4j.configurationFile=log4j2.xml
                    //AELog.info(">>> " + recipeOutput.getDisplayName() + " >>> " + recipeInputs.toString());

                    if (!recipeInputs.isEmpty()) {
                        RecipeRegistryProxy.addRecipe(recipeOutput, recipeInputs);
                    }
                }
            }
        }
    }

    public static List<ItemStack> getRecipeInputs(IRecipe recipe) {
        ArrayList<ItemStack> recipeInputs = new ArrayList<ItemStack>();


        if (recipe instanceof ShapedRecipe) {
            ShapedRecipe shapedOreRecipe = (ShapedRecipe) recipe;
            for (int i = 0; i < shapedOreRecipe.getInput().length; i++) {
                if (shapedOreRecipe.getInput()[i] instanceof IIngredient) {
                    IIngredient ing = (IIngredient) shapedOreRecipe.getInput()[i];

                    try {
                        ItemStack[] is = ing.getItemStackSet().clone();
                        Object preferred = Platform.findPreferred(is);
                        ItemStack itemStack;
                        if (preferred instanceof ItemStack) {
                            itemStack = (ItemStack)preferred;
                        } else {
                            itemStack = is[0];
                        }

                        if (itemStack.stackSize == 0)
                            itemStack.stackSize = 1;

                        recipeInputs.add(itemStack);
                    }

                    catch (RegistrationError ignored) { ignored.printStackTrace(); }
                    catch (MissingIngredientError ignored) { ignored.printStackTrace(); }
                }
            }

        } else if (recipe instanceof ShapelessRecipe) {
            ShapelessRecipe shapelessOreRecipe = (ShapelessRecipe) recipe;

            for (int i = 0; i < shapelessOreRecipe.getInput().size(); i++) {
                if (shapelessOreRecipe.getInput().get(i) instanceof IIngredient) {
                    IIngredient ing = (IIngredient) shapelessOreRecipe.getInput().get(i);

                    try {
                        ItemStack[] is = ing.getItemStackSet().clone();
                        Object preferred = Platform.findPreferred(is);
                        ItemStack itemStack;
                        if (preferred instanceof ItemStack) {
                            itemStack = (ItemStack)preferred;
                        } else {
                            itemStack = is[0];
                        }

                        if (itemStack.stackSize == 0)
                            itemStack.stackSize = 1;

                        recipeInputs.add(itemStack);
                    }

                    catch (RegistrationError ignored) { ignored.printStackTrace(); }
                    catch (MissingIngredientError ignored) { ignored.printStackTrace(); }
                }
            }
        }

        boolean clear = false;
        for (ItemStack is : recipeInputs) {
            if (is.isItemEqual(new ItemStack(Items.water_bucket, 1))) {
                clear = true;
            }
        }

        if (clear)
            recipeInputs.clear();

        return recipeInputs;
    }
}
