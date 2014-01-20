package appeng.container.implementations;

import net.minecraft.entity.player.InventoryPlayer;
import appeng.container.AEBaseContainer;
import appeng.container.slot.SlotRestrictedInput;
import appeng.container.slot.SlotRestrictedInput.PlaceableItemType;
import appeng.tile.qnb.TileQuantumBridge;

public class ContainerQNB extends AEBaseContainer
{

	TileQuantumBridge myte;

	public ContainerQNB(InventoryPlayer ip, TileQuantumBridge te) {
		super( ip, te, null );
		myte = te;

		addSlotToContainer( (new SlotRestrictedInput( PlaceableItemType.QE_SINGULARTIY, te, 0, 80, 37 )).setStackLimit( 1 ) );

		bindPlayerInventory( ip, 0, 166 - /* height of playerinventory */82 );
	}

}
