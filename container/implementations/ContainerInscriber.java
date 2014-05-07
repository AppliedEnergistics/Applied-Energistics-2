package appeng.container.implementations;

import net.minecraft.entity.player.InventoryPlayer;
import appeng.container.AEBaseContainer;
import appeng.container.guisync.GuiSync;
import appeng.container.slot.SlotOutput;
import appeng.container.slot.SlotRestrictedInput;
import appeng.container.slot.SlotRestrictedInput.PlaceableItemType;
import appeng.tile.misc.TileInscriber;
import appeng.util.Platform;

public class ContainerInscriber extends AEBaseContainer
{

	TileInscriber myte;

	@GuiSync(0)
	public int maxProessingTime = -1;

	@GuiSync(1)
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
			this.maxProessingTime = myte.maxProessingTime;
			this.processingTime = myte.processingTime;
		}
	}
}
