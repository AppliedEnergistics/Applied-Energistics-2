package appeng.container.implementations;

import appeng.container.slot.SlotInaccessible;
import net.minecraft.entity.player.InventoryPlayer;
import appeng.container.AEBaseContainer;
import appeng.container.slot.SlotOutput;
import appeng.container.slot.SlotRestrictedInput;
import appeng.tile.grindstone.TileGrinder;

public class ContainerGrinder extends AEBaseContainer
{

	TileGrinder grinder;

	public ContainerGrinder(InventoryPlayer ip, TileGrinder grinder) {
		super( ip, grinder, null );
		this.grinder = grinder;

		addSlotToContainer( new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.ORE, grinder, 0, 12, 17, invPlayer ) );
		addSlotToContainer( new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.ORE, grinder, 1, 12 + 18, 17, invPlayer ) );
		addSlotToContainer( new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.ORE, grinder, 2, 12 + 36, 17, invPlayer ) );

		addSlotToContainer( new SlotInaccessible( grinder, 6, 80, 40 ) );

		addSlotToContainer( new SlotOutput( grinder, 3, 112, 63, 2 * 16 + 15 ) );
		addSlotToContainer( new SlotOutput( grinder, 4, 112 + 18, 63, 2 * 16 + 15 ) );
		addSlotToContainer( new SlotOutput( grinder, 5, 112 + 36, 63, 2 * 16 + 15 ) );

		bindPlayerInventory( ip, 0, 176 - /* height of player inventory */82 );
	}

}
