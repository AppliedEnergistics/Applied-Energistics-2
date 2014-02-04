package appeng.container.implementations;

import net.minecraft.entity.player.InventoryPlayer;
import appeng.container.AEBaseContainer;
import appeng.container.slot.SlotRestrictedInput;
import appeng.container.slot.SlotRestrictedInput.PlaceableItemType;
import appeng.tile.networking.TileWireless;

public class ContainerWireless extends AEBaseContainer
{

	TileWireless myte;

	public ContainerWireless(InventoryPlayer ip, TileWireless te) {
		super( ip, te, null );
		myte = te;

		addSlotToContainer( new SlotRestrictedInput( PlaceableItemType.RANGE_BOOSTER, myte, 0, 80, 37 ) );

		bindPlayerInventory( ip, 0, 166 - /* height of playerinventory */82 );
	}

}
