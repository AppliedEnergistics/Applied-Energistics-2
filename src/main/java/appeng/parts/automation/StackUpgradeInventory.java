package appeng.parts.automation;


import appeng.api.config.Upgrades;
import appeng.tile.inventory.IAEAppEngInventory;
import appeng.util.Platform;
import net.minecraft.item.ItemStack;


public class StackUpgradeInventory extends UpgradeInventory
{
	private final ItemStack stack;

	public StackUpgradeInventory( final ItemStack stack, final IAEAppEngInventory inventory, final int s )
	{
		super( inventory, s );
		this.stack = stack;
	}

	@Override
	public int getMaxInstalled( final Upgrades upgrades )
	{
		int max = 0;

		for( final ItemStack is : upgrades.getSupported().keySet() )
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
