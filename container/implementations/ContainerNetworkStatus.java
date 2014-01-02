package appeng.container.implementations;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.tileentity.TileEntity;
import appeng.container.AEBaseContainer;

public class ContainerNetworkStatus extends AEBaseContainer
{

	TileEntity myte;

	public ContainerNetworkStatus(InventoryPlayer ip, TileEntity te) {
		super( ip, te, null );
		myte = te;

		// addSlotToContainer( new SlotRestrictedInput(
		// PlaceableItemType.WIRELESS_TERMINAL, te, 0, 71, 14 ) );
		// addSlotToContainer( new SlotOutput( te, 1, 71, 14,
		// PlaceableItemType.WIRELESS_TERMINAL.icon ) );

		bindPlayerInventory( ip, 0, 199 - /* height of playerinventory */82 );
	}

}
