package appeng.container.implementations;

import net.minecraft.entity.player.InventoryPlayer;
import appeng.container.AEBaseContainer;
import appeng.container.slot.SlotRestrictedInput;
import appeng.tile.qnb.TileQuantumBridge;

public class ContainerQNB extends AEBaseContainer
{

	TileQuantumBridge myte;

	public ContainerQNB(InventoryPlayer ip, TileQuantumBridge te) {
		super( ip, te, null );
		myte = te;

		addSlotToContainer( (new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.QE_SINGULARITY, te, 0, 80, 37, invPlayer )).setStackLimit( 1 ) );

		bindPlayerInventory( ip, 0, 166 - /* height of player inventory */82 );
	}

}
