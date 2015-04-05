package appeng.api.features;


import java.util.Collection;
import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;


/**
 * Builder for an inscriber recipe
 *
 * @author thatsIch
 * @version rv2
 * @since rv2
 */
public interface IInscriberRecipeBuilder
{
	/**
	 * Creates an inscriber recipe with inputs.
	 * Needs to be invoked.
	 *
	 * @param inputs new inputs for the recipe
	 *
	 * @return currently used builder
	 */
	@Nonnull
	IInscriberRecipeBuilder withInputs( @Nonnull Collection<ItemStack> inputs );

	/**
	 * Creates an inscriber recipe with output.
	 * Needs to be invoked.
	 *
	 * @param output new output for the recipe
	 *
	 * @return currently used builder
	 */
	@Nonnull
	IInscriberRecipeBuilder withOutput( @Nonnull ItemStack output );

	/**
	 * Creates an inscriber recipe with top.
	 * Either this or bot needs to be invoked.
	 *
	 * @param topOptional new top for the recipe
	 *
	 * @return currently used builder
	 */
	@Nonnull
	IInscriberRecipeBuilder withTopOptional( @Nonnull ItemStack topOptional );

	/**
	 * Creates an inscriber recipe with bot.
	 * Either this or top needs to be invoked.
	 *
	 * @param bottomOptional new bot for the recipe
	 *
	 * @return currently used builder
	 */
	@Nonnull
	IInscriberRecipeBuilder withBottomOptional( @Nonnull ItemStack bottomOptional );

	/**
	 * Creates an inscriber recipe with type.
	 * Needs to be invoked.
	 *
	 * @param type new type for the recipe
	 *
	 * @return currently used builder
	 */
	@Nonnull
	IInscriberRecipeBuilder withProcessType( @Nonnull InscriberProcessType type );

	/**
	 * Finalizes the process of making the recipe.
	 * Needs to be invoked to fetch inscriber recipe.
	 *
	 * @return legal inscriber recipe
	 *
	 * @throws IllegalStateException when input is not defined
	 * @throws IllegalStateException when input has no size
	 * @throws IllegalStateException when output is not defined
	 * @throws IllegalStateException when both optionals are not defined
	 * @throws IllegalStateException when process type is not defined
	 */
	@Nonnull
	IInscriberRecipe build();
}
