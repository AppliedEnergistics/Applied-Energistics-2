package appeng.container.implementations;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.implementations.guiobjects.IPortableCell;
import appeng.util.Platform;

public class ContainerMEPortableCell extends ContainerMEMonitorable
{

	double powerMultiplier = 0.5;
	final IPortableCell civ;

	public ContainerMEPortableCell(InventoryPlayer ip, IPortableCell monitorable) {
		super( ip, monitorable, false );
		lockPlayerInventorySlot( ip.currentItem );
		civ = monitorable;
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
						isContainerValid = false;
				}
				else
					isContainerValid = false;
			}
		}
		else
			isContainerValid = false;

		// drain 1 ae t
		ticks++;
		if ( ticks > 10 )
		{
			civ.extractAEPower( powerMultiplier * ticks, Actionable.MODULATE, PowerMultiplier.CONFIG );
			ticks = 0;
		}
		super.detectAndSendChanges();
	}
}
