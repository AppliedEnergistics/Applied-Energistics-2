package appeng.container.implementations;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ICrafting;
import appeng.container.AEBaseContainer;
import appeng.container.slot.SlotOutput;
import appeng.container.slot.SlotRestrictedInput;
import appeng.container.slot.SlotRestrictedInput.PlaceableItemType;
import appeng.tile.misc.TileInscriber;
import appeng.util.Platform;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ContainerInscriber extends AEBaseContainer
{

	TileInscriber myte;

	public int maxProessingTime = -1;
	public int processingTime = -1;

	public ContainerInscriber(InventoryPlayer ip, TileInscriber te) {
		super( ip, te, null );
		myte = te;

		addSlotToContainer( new SlotRestrictedInput( PlaceableItemType.INSCRIBER_PLATE, myte, 0, 45, 16 ) );
		addSlotToContainer( new SlotRestrictedInput( PlaceableItemType.INSCRIBER_INPUT, myte, 2, 63, 39 ) );
		addSlotToContainer( new SlotRestrictedInput( PlaceableItemType.INSCRIBER_PLATE, myte, 1, 45, 62 ) );

		addSlotToContainer( new SlotOutput( myte, 3, 113, 40, -1 ) );

		bindPlayerInventory( ip, 0, 176 - /* height of playerinventory */82 );
	}

	@Override
	public void detectAndSendChanges()
	{
		super.detectAndSendChanges();

		if ( Platform.isServer() )
		{
			int localMax = this.maxProessingTime;
			int localTime = this.processingTime;

			for (int i = 0; i < this.crafters.size(); ++i)
			{
				ICrafting icrafting = (ICrafting) this.crafters.get( i );

				if ( this.maxProessingTime != localMax )
				{
					icrafting.sendProgressBarUpdate( this, 0, localMax );
				}

				if ( this.processingTime != localTime )
				{
					icrafting.sendProgressBarUpdate( this, 1, localTime );
				}
			}

			this.maxProessingTime = localMax;
			this.processingTime = localTime;
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void updateProgressBar(int idx, int value)
	{
		if ( idx == 0 )
			this.maxProessingTime = value;

		if ( idx == 1 )
			this.processingTime = value;
	}
}
