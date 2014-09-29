package appeng.container.implementations;

import net.minecraft.entity.player.InventoryPlayer;
import appeng.container.AEBaseContainer;
import appeng.container.guisync.GuiSync;
import appeng.container.slot.SlotRestrictedInput;
import appeng.core.AEConfig;
import appeng.tile.networking.TileWireless;

public class ContainerWireless extends AEBaseContainer
{

	TileWireless wirelessTerminal;

	@GuiSync(1)
	public long range = 0;

	@GuiSync(2)
	public long drain = 0;

	SlotRestrictedInput boosterSlot;

	public ContainerWireless(InventoryPlayer ip, TileWireless te) {
		super( ip, te, null );
		wirelessTerminal = te;

		addSlotToContainer( boosterSlot = new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.RANGE_BOOSTER, wirelessTerminal, 0, 80, 47, invPlayer ) );

		bindPlayerInventory( ip, 0, 166 - /* height of player inventory */82 );
	}

	@Override
	public void detectAndSendChanges()
	{
		int boosters = boosterSlot.getStack() == null ? 0 : boosterSlot.getStack().stackSize;

		range = (long) (10 * AEConfig.instance.wireless_getMaxRange( boosters ));
		drain = (long) (100 * AEConfig.instance.wireless_getPowerDrain( boosters ));

		super.detectAndSendChanges();
	}

}
