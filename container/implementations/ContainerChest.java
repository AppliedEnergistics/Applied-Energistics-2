package appeng.container.implementations;

import net.minecraft.entity.player.InventoryPlayer;
import appeng.container.AEBaseContainer;
import appeng.container.slot.SlotRestrictedInput;
import appeng.container.slot.SlotRestrictedInput.PlaceableItemType;
import appeng.tile.storage.TileChest;

public class ContainerChest extends AEBaseContainer
{

	TileChest myte;

	public ContainerChest(InventoryPlayer ip, TileChest te) {
		super( ip, te, null );
		myte = te;

		addSlotToContainer( new SlotRestrictedInput( PlaceableItemType.STORAGE_CELLS, myte, 1, 80, 37, invPlayer ) );

		bindPlayerInventory( ip, 0, 166 - /* height of playerinventory */82 );
	}

}
