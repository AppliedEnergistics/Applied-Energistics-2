package appeng.container.implementations;

import net.minecraft.entity.player.InventoryPlayer;
import appeng.container.AEBaseContainer;
import appeng.container.slot.SlotRestrictedInput;
import appeng.tile.qnb.TileQuantumBridge;

public class ContainerQNB extends AEBaseContainer
{

	final TileQuantumBridge quantumBridge;

	public ContainerQNB(InventoryPlayer ip, TileQuantumBridge quantumBridge) {
		super( ip, quantumBridge, null );
		this.quantumBridge = quantumBridge;

		addSlotToContainer( (new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.QE_SINGULARITY, quantumBridge, 0, 80, 37, invPlayer )).setStackLimit( 1 ) );

		bindPlayerInventory( ip, 0, 166 - /* height of player inventory */82 );
	}

}
