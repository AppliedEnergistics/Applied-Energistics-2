package appeng.container.implementations;

import net.minecraft.entity.player.InventoryPlayer;
import appeng.container.AEBaseContainer;
import appeng.container.slot.SlotFake;
import appeng.container.slot.SlotNormal;
import appeng.container.slot.SlotRestrictedInput;
import appeng.container.slot.SlotRestrictedInput.PlaceableItemType;
import appeng.tile.misc.TileInterface;

public class ContainerInterface extends AEBaseContainer
{

	TileInterface myte;

	public ContainerInterface(InventoryPlayer ip, TileInterface te) {
		super( ip, te );
		myte = te;

		for (int x = 0; x < 8; x++)
			addSlotToContainer( new SlotFake( myte.getConfig(), x, 8 + 18 * x, 28 + 7 ) );

		for (int x = 0; x < 8; x++)
			addSlotToContainer( new SlotNormal( myte, x, 26 + 18 * x, 46 + 7 ) );

		for (int x = 0; x < 9; x++)
			addSlotToContainer( new SlotRestrictedInput( PlaceableItemType.ENCODED_PATTERN, myte.getPatterns(), x, 8 + 18 * x, 90 + 7 ) );

		bindPlayerInventory( ip, 0, 211 - /* height of playerinventory */82 );
	}

}
