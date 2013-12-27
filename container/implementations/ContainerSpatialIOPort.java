package appeng.container.implementations;

import net.minecraft.entity.player.InventoryPlayer;
import appeng.container.AEBaseContainer;
import appeng.container.slot.SlotOutput;
import appeng.container.slot.SlotRestrictedInput;
import appeng.container.slot.SlotRestrictedInput.PlaceableItemType;
import appeng.tile.spatial.TileSpatialIOPort;

public class ContainerSpatialIOPort extends AEBaseContainer
{

	TileSpatialIOPort myte;

	public ContainerSpatialIOPort(InventoryPlayer ip, TileSpatialIOPort te) {
		super( ip, te );
		myte = te;

		addSlotToContainer( new SlotRestrictedInput( PlaceableItemType.SPATIAL_STORAGE_CELLS, te, 0, 71, 14 ) );
		addSlotToContainer( new SlotOutput( te, 1, 71, 14, PlaceableItemType.SPATIAL_STORAGE_CELLS.icon ) );

		bindPlayerInventory( ip, 0, 199 - /* height of playerinventory */82 );
	}

}
