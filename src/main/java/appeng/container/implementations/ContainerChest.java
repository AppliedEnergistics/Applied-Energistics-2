package appeng.container.implementations;

import net.minecraft.entity.player.InventoryPlayer;
import appeng.container.AEBaseContainer;
import appeng.container.slot.SlotRestrictedInput;
import appeng.tile.storage.TileChest;

public class ContainerChest extends AEBaseContainer
{

	TileChest chest;

	public ContainerChest(InventoryPlayer ip, TileChest chest) {
		super( ip, chest, null );
		this.chest = chest;

		addSlotToContainer( new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.STORAGE_CELLS, this.chest, 1, 80, 37, invPlayer ) );

		bindPlayerInventory( ip, 0, 166 - /* height of player inventory */82 );
	}

}
