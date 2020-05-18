
package appeng.bootstrap.components;


import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.registries.IForgeRegistry;

import appeng.bootstrap.IBootstrapComponent;


@FunctionalInterface
public interface IRecipeRegistrationComponent extends IBootstrapComponent
{
	void recipeRegistration( Dist dist, IForgeRegistry<IRecipe> recipeRegistry );
}
