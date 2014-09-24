package appeng.container.implementations;

import appeng.container.slot.SlotInaccessible;
import net.minecraft.entity.player.InventoryPlayer;
import appeng.container.AEBaseContainer;
import appeng.container.slot.SlotOutput;
import appeng.container.slot.SlotRestrictedInput;
import appeng.tile.grindstone.TileGrinder;

public class ContainerGrinder extends AEBaseContainer
{

	TileGrinder myte;

	public ContainerGrinder(InventoryPlayer ip, TileGrinder te) {
		super( ip, te, null );
		myte = te;

		addSlotToContainer( new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.ORE, te, 0, 12, 17, invPlayer ) );
		addSlotToContainer( new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.ORE, te, 1, 12 + 18, 17, invPlayer ) );
		addSlotToContainer( new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.ORE, te, 2, 12 + 36, 17, invPlayer ) );

		addSlotToContainer( new SlotInaccessible( te, 6, 80, 40 ) );

		addSlotToContainer( new SlotOutput( te, 3, 112, 63, 2 * 16 + 15 ) );
		addSlotToContainer( new SlotOutput( te, 4, 112 + 18, 63, 2 * 16 + 15 ) );
		addSlotToContainer( new SlotOutput( te, 5, 112 + 36, 63, 2 * 16 + 15 ) );

		bindPlayerInventory( ip, 0, 176 - /* height of playerinventory */82 );
	}

}
