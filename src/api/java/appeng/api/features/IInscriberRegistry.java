
package appeng.api.features;


import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

import com.google.common.base.Optional;

import net.minecraft.item.ItemStack;


/**
 * Lets you manipulate Inscriber Recipes, by adding or editing existing ones.
 *
 * @author thatsIch
 * @version rv3
 * @since rv2
 */
public interface IInscriberRegistry
{
	/**
	 * An immutable copy of currently registered recipes.
	 *
	 * Use the provided methods to actually modify the inscriber recipes.
	 *
	 * @see IInscriberRegistry#addRecipe(IInscriberRecipe)
	 * @see IInscriberRegistry#removeRecipe(IInscriberRecipe)
	 *
	 * @return currentlyRegisteredRecipes
	 */
	@Nonnull
	Collection<IInscriberRecipe> getRecipes();

	/**
	 * Optional items which are used in the top or bottom slot.
	 *
	 * @return set of all optional items
	 */
	@Nonnull
	Set<ItemStack> getOptionals();

	/**
	 * Get all registered items which are valid inputs.
	 *
	 * @return set of all input items
	 */
	@Nonnull
	Set<ItemStack> getInputs();

	/**
	 * Extensible way to create an inscriber recipe.
	 *
	 * @return builder for inscriber recipes
	 */
	@Nonnull
	IInscriberRecipeBuilder builder();

	/**
	 * add a new recipe the easy way, duplicates will not be added.
	 * Added recipes will be automatically added to the optionals and inputs.
	 *
	 * @param recipe new recipe
	 *
	 * @throws IllegalArgumentException if null is added
	 */
	void addRecipe( IInscriberRecipe recipe );

	/**
	 * Removes all equal recipes from the registry.
	 *
	 * @param toBeRemovedRecipe to be removed recipe, can be null, makes just no sense.
	 */
	void removeRecipe( IInscriberRecipe toBeRemovedRecipe );

}
