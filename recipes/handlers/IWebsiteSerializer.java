package appeng.recipes.handlers;

import net.minecraft.item.ItemStack;
import appeng.api.exceptions.MissingIngredientError;
import appeng.api.exceptions.RegistrationError;
import appeng.recipes.RecipeHandler;

public interface IWebsiteSerializer
{

	String getPattern(RecipeHandler han);

	boolean canCraft(ItemStack output) throws RegistrationError, MissingIngredientError;

}
