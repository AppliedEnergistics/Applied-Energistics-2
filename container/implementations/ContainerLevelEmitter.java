package appeng.container.implementations;

import net.minecraft.entity.player.InventoryPlayer;
import appeng.container.AEBaseContainer;
import appeng.parts.automation.PartLevelEmitter;

public class ContainerLevelEmitter extends AEBaseContainer
{

	PartLevelEmitter myte;

	public ContainerLevelEmitter(InventoryPlayer ip, PartLevelEmitter te) {
		super( ip, te.getHost().getTile() );
		myte = te;

		// addSlotToContainer( new SlotRestrictedInput(
		// PlaceableItemType.WIRELESS_TERMINAL, te, 0, 71, 14 ) );
		// addSlotToContainer( new SlotOutput( te, 1, 71, 14,
		// PlaceableItemType.WIRELESS_TERMINAL.icon ) );

		bindPlayerInventory( ip, 0, 199 - /* height of playerinventory */82 );
	}
}
