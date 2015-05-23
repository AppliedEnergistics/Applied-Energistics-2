package appengee3compat.core;

import appeng.api.exceptions.MissingIngredientError;
import appeng.api.exceptions.RegistrationError;
import appeng.api.recipes.IIngredient;
import appeng.recipes.game.ShapedRecipe;
import appeng.recipes.game.ShapelessRecipe;
import com.pahimar.ee3.api.exchange.RecipeRegistryProxy;
import com.pahimar.ee3.exchange.OreStack;
import com.pahimar.ee3.exchange.WrappedStack;
import com.pahimar.ee3.util.ItemHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EE3RecipeHelper {
    public static void initRecipes() {

        for (Object recipeObject : CraftingManager.getInstance().getRecipeList()) {
            if (recipeObject instanceof ShapelessRecipe || recipeObject instanceof ShapedRecipe) {
                IRecipe recipe = (IRecipe) recipeObject;
                ItemStack recipeOutput = recipe.getRecipeOutput();

                if (recipeOutput != null) {
                    List<WrappedStack> recipeInputs = getRecipeInputs(recipe);

                    if (!recipeInputs.isEmpty()) {
                        RecipeRegistryProxy.addRecipe(recipeOutput, recipeInputs);
                    }
                }
            }
        }
    }

    public static List<WrappedStack> getRecipeInputs(IRecipe recipe) {
        ArrayList<WrappedStack> recipeInputs = new ArrayList<WrappedStack>();


        if (recipe instanceof ShapedRecipe) {
            ShapedRecipe shapedOreRecipe = (ShapedRecipe) recipe;
            for (int i = 0; i < shapedOreRecipe.getInput().length; i++) {
                if (shapedOreRecipe.getInput()[i] instanceof IIngredient) {
                    IIngredient ing = (IIngredient) shapedOreRecipe.getInput()[i];

                    try {
                        ItemStack[] is = ing.getItemStackSet();
                        if (is.length == 17) {
                            recipeInputs.add(WrappedStack.wrap(is[16]));
                        } else {
                            recipeInputs.add(WrappedStack.wrap(is[0]));
                        }
                    }
                    catch (RegistrationError ignored) { }
                    catch (MissingIngredientError ignored) { }
                }
            }

        } else if (recipe instanceof ShapelessRecipe) {
            ShapelessRecipe shapelessOreRecipe = (ShapelessRecipe) recipe;

            for (int i = 0; i < shapelessOreRecipe.getInput().size(); i++) {
                if (shapelessOreRecipe.getInput().get(i) instanceof IIngredient) {
                    IIngredient ing = (IIngredient) shapelessOreRecipe.getInput().get(i);

                    try {
                        ItemStack[] is = ing.getItemStackSet();
                        if (is.length == 17) {
                            recipeInputs.add(WrappedStack.wrap(is[16]));
                        } else {
                            recipeInputs.add(WrappedStack.wrap(is[0]));
                        }
                    }
                    catch (RegistrationError ignored) { }
                    catch (MissingIngredientError ignored) { }
                }
            }
        }

        return collateInputStacks(recipeInputs);
    }

    public static List<WrappedStack> collateInputStacks(List<?> uncollatedStacks) {
        List<WrappedStack> collatedStacks = new ArrayList<WrappedStack>();

        WrappedStack stack;
        boolean found;

        for (Object object : uncollatedStacks) {
            found = false;

            if (WrappedStack.canBeWrapped(object)) {
                stack = WrappedStack.wrap(object);

                if (collatedStacks.isEmpty()) {
                    collatedStacks.add(stack);
                } else {

                    for (WrappedStack collatedStack : collatedStacks) {
                        if (collatedStack.getWrappedObject() != null) {
                            if (stack.getWrappedObject() instanceof ItemStack && collatedStack.getWrappedObject() instanceof ItemStack) {
                                if (ItemHelper.equals((ItemStack) stack.getWrappedObject(), (ItemStack) collatedStack.getWrappedObject())) {
                                    collatedStack.setStackSize(collatedStack.getStackSize() + stack.getStackSize());
                                    found = true;
                                }
                            } else if (stack.getWrappedObject() instanceof OreStack && collatedStack.getWrappedObject() instanceof OreStack) {
                                if (OreStack.compareOreNames((OreStack) stack.getWrappedObject(),
                                        (OreStack) collatedStack.getWrappedObject())) {
                                    collatedStack.setStackSize(collatedStack.getStackSize() + stack.getStackSize());
                                    found = true;
                                }
                            }
                        }
                    }

                    if (!found) {
                        collatedStacks.add(stack);
                    }
                }
            }
        }
        Collections.sort(collatedStacks);
        return collatedStacks;
    }
}
