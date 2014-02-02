package appeng.container.implementations;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.implementations.guiobjects.IPortableCell;
import appeng.api.storage.IStorageMonitorable;
import appeng.util.Platform;

public class ContainerMEPortableCell extends ContainerMEMonitorable
{

	double powerMultiplier = 0.5;
	IPortableCell civ;

	public ContainerMEPortableCell(InventoryPlayer ip, IPortableCell montiorable) {
		super( ip, (IStorageMonitorable) montiorable, false );
		lockPlayerInventorySlot( ip.currentItem );
		civ = montiorable;
		bindPlayerInventory( ip, 0, 0 );
	}

	int ticks = 0;

	@Override
	public void detectAndSendChanges()
	{
		ItemStack currentItem = getPlayerInv().getCurrentItem();

		if ( civ != null )
		{
			if ( currentItem != civ.getItemStack() )
			{
				if ( currentItem != null )
				{
					if ( Platform.isSameItem( civ.getItemStack(), currentItem ) )
						getPlayerInv().setInventorySlotContents( getPlayerInv().currentItem, civ.getItemStack() );
					else
						getPlayerInv().player.closeScreen();
				}
				else
					getPlayerInv().player.closeScreen();
			}
		}
		else
			getPlayerInv().player.closeScreen();

		// drain 1 ae t
		ticks++;
		if ( ticks > 10 )
		{
			civ.extractAEPower( powerMultiplier * (double) ticks, Actionable.MODULATE, PowerMultiplier.CONFIG );
			ticks = 0;
		}
		super.detectAndSendChanges();
	}
}
