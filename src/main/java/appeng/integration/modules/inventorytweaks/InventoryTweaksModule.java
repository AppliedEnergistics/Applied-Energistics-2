
package appeng.integration.modules.inventorytweaks;


import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Loader;

import invtweaks.api.InvTweaksAPI;

import appeng.integration.abstraction.IInvTweaks;


public class InventoryTweaksModule implements IInvTweaks
{
	InvTweaksAPI api = null;

	public InventoryTweaksModule()
	{
		try
		{
			this.api = (InvTweaksAPI) Class.forName( "invtweaks.forge.InvTweaksMod", true, Loader.instance().getModClassLoader() )
					.getField( "instance" )
					.get( null );
		}
		catch( Exception ex )
		{
		}
	}

	@Override
	public boolean isEnabled()
	{
		return this.api != null;
	}

	@Override
	public int compareItems( ItemStack i, ItemStack j )
	{
		return this.api.compareItems( i, j );
	}
}
