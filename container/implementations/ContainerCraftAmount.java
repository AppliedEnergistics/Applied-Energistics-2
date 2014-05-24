package appeng.container.implementations;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import appeng.api.config.SecurityPermissions;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.data.IAEItemStack;
import appeng.container.AEBaseContainer;
import appeng.container.slot.SlotInaccessable;
import appeng.tile.inventory.AppEngInternalInventory;

public class ContainerCraftAmount extends AEBaseContainer
{

	ITerminalHost priHost;
	IAEItemStack stack;

	public Slot craftingItem;

	public ContainerCraftAmount(InventoryPlayer ip, ITerminalHost te) {
		super( ip, te );
		priHost = te;

		craftingItem = new SlotInaccessable( new AppEngInternalInventory( null, 1 ), 0, 34, 53 );
		addSlotToContainer( craftingItem );
	}

	@Override
	public void detectAndSendChanges()
	{
		super.detectAndSendChanges();
		verifyPermissions( SecurityPermissions.CRAFT, false );
	}

}
