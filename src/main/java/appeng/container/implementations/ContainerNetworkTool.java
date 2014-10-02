package appeng.container.implementations;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import appeng.api.implementations.guiobjects.INetworkTool;
import appeng.container.AEBaseContainer;
import appeng.container.guisync.GuiSync;
import appeng.container.slot.SlotRestrictedInput;
import appeng.util.Platform;

public class ContainerNetworkTool extends AEBaseContainer
{

	final INetworkTool toolInv;

	@GuiSync(1)
	public boolean facadeMode;

	public ContainerNetworkTool(InventoryPlayer ip, INetworkTool te) {
		super( ip, null, null );
		toolInv = te;

		lockPlayerInventorySlot( ip.currentItem );

		for (int y = 0; y < 3; y++)
			for (int x = 0; x < 3; x++)
				addSlotToContainer( (new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.UPGRADES, te, y * 3 + x, 80 - 18 + x * 18, 37 - 18 + y * 18, invPlayer )) );

		bindPlayerInventory( ip, 0, 166 - /* height of player inventory */82 );
	}

	public void toggleFacadeMode()
	{
		NBTTagCompound data = Platform.openNbtData( toolInv.getItemStack() );
		data.setBoolean( "hideFacades", !data.getBoolean( "hideFacades" ) );
		this.detectAndSendChanges();
	}

	@Override
	public void detectAndSendChanges()
	{
		ItemStack currentItem = getPlayerInv().getCurrentItem();

		if ( currentItem != toolInv.getItemStack() )
		{
			if ( currentItem != null )
			{
				if ( Platform.isSameItem( toolInv.getItemStack(), currentItem ) )
				{
					getPlayerInv().setInventorySlotContents( getPlayerInv().currentItem, toolInv.getItemStack() );
				}
				else
					isContainerValid = false;
			}
			else
				isContainerValid = false;
		}

		if ( isContainerValid )
		{
			NBTTagCompound data = Platform.openNbtData( currentItem );
			facadeMode = data.getBoolean( "hideFacades" );
		}

		super.detectAndSendChanges();
	}
}
