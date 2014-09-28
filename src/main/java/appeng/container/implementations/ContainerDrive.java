package appeng.container.implementations;

import net.minecraft.entity.player.InventoryPlayer;
import appeng.container.AEBaseContainer;
import appeng.container.slot.SlotRestrictedInput;
import appeng.tile.storage.TileDrive;

public class ContainerDrive extends AEBaseContainer
{

	TileDrive drive;

	public ContainerDrive(InventoryPlayer ip, TileDrive drive) {
		super( ip, drive, null );
		this.drive = drive;

		for (int y = 0; y < 5; y++)
			for (int x = 0; x < 2; x++)
			{
				addSlotToContainer( new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.STORAGE_CELLS, drive, x + y * 2, 71 + x * 18, 14 + y * 18, invPlayer ) );
			}

		bindPlayerInventory( ip, 0, 199 - /* height of player inventory */82 );
	}

}
