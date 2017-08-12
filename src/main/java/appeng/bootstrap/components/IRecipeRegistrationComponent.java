
package appeng.bootstrap.components;


import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.registries.IForgeRegistry;

import appeng.bootstrap.IBootstrapComponent;


@FunctionalInterface
public interface IRecipeRegistrationComponent extends IBootstrapComponent
{
	void recipeRegistration( Side side, IForgeRegistry<IRecipe> recipeRegistry );
}
