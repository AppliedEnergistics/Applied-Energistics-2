
package appeng.core.features.registries.entries;


import java.util.Collection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;

import appeng.api.features.InscriberProcessType;


/**
 * inscribe recipes do not use up the provided optional upon craft
 *
 * @author thatsIch
 * @version rv2
 * @since rv2
 */
public class InscriberInscribeRecipe extends InscriberRecipe
{
	public InscriberInscribeRecipe( @Nonnull Collection<ItemStack> inputs, @Nonnull ItemStack output, @Nullable ItemStack top, @Nullable ItemStack bot )
	{
		super( inputs, output, top, bot, InscriberProcessType.Inscribe );
	}
}
