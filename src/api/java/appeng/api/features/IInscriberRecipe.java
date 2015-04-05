package appeng.api.features;


import java.util.List;
import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;

import com.google.common.base.Optional;


/**
 * Registration Records for {@link IInscriberRegistry}
 *
 * You have to pay attention though, that recipes are not mirrored,
 * where the top and bottom slots are switching places.
 *
 * This is applied on runtime.
 *
 * @author thatsIch
 * @version rv2
 * @since rv2
 */
public interface IInscriberRecipe
{
	/**
	 * the current inputs
	 *
	 * @return inputs the inscriber will accept
	 */
	@Nonnull
	List<ItemStack> getInputs();

	/**
	 * gets the current output
	 *
	 * @return output that the recipe will produce
	 */
	@Nonnull
	ItemStack getOutput();

	/**
	 * gets the top optional
	 *
	 * @return item which is used top
	 */
	@Nonnull
	Optional<ItemStack> getTopOptional();

	/**
	 * gets the bottom optional
	 *
	 * @return item which is used bottom
	 */
	@Nonnull
	Optional<ItemStack> getBottomOptional();

	/**
	 * type of inscriber process
	 *
	 * @return type of process the inscriber is doing
	 */
	@Nonnull
	InscriberProcessType getProcessType();
}
