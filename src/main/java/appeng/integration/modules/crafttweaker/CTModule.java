
package appeng.integration.modules.crafttweaker;


import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.IAction;
import crafttweaker.api.item.IItemStack;

import appeng.integration.abstraction.ICraftTweaker;


public class CTModule implements ICraftTweaker
{
	static List<IAction> ADDITIONS = new ArrayList<>();
	static List<IAction> REMOVALS = new ArrayList<>();

	@Override
	public void preInit()
	{
		CraftTweakerAPI.registerClass( GrinderRecipes.class );
		CraftTweakerAPI.registerClass( InscriberRecipes.class );
	}

	@Override
	public void postInit()
	{
		ADDITIONS.forEach( CraftTweakerAPI::apply );
		REMOVALS.forEach( CraftTweakerAPI::apply );
	}

	public static ItemStack toStack( IItemStack iStack )
	{
		if( iStack == null )
		{
			return ItemStack.EMPTY;
		}
		else
		{
			return (ItemStack) iStack.getInternal();
		}
	}
}
