package appeng.parts.automation;


import net.minecraft.item.ItemStack;

import appeng.api.config.Upgrades;
import appeng.tile.inventory.IAEAppEngInventory;
import appeng.util.Platform;


public class StackUpgradeInventory extends UpgradeInventory
{
	private final ItemStack stack;

	public StackUpgradeInventory( ItemStack stack, IAEAppEngInventory inventory, int s )
	{
		super( inventory, s );
		this.stack = stack;
	}

	public int getMaxInstalled( Upgrades upgrades )
	{
		int max = 0;

		for( ItemStack is : upgrades.getSupported().keySet() )
		{
			if( Platform.isSameItem( this.stack, is ) )
			{
				max = upgrades.getSupported().get( is );
				break;
			}
		}

		return max;
	}
}
