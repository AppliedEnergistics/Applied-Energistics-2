package appeng.container.implementations;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.world.World;
import appeng.api.config.SecurityPermissions;
import appeng.api.networking.IGrid;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.PlayerSource;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.data.IAEItemStack;
import appeng.container.AEBaseContainer;
import appeng.container.slot.SlotInaccessable;
import appeng.crafting.ICraftingHost;
import appeng.tile.inventory.AppEngInternalInventory;

public class ContainerCraftAmount extends AEBaseContainer implements ICraftingHost
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

	@Override
	public IGrid getGrid()
	{
		IActionHost h = ((IActionHost) this.getTarget());
		return h.getActionableNode().getGrid();
	}

	public World getWorld()
	{
		return getPlayerInv().player.worldObj;
	}

	@Override
	public BaseActionSource getActionSrc()
	{
		return new PlayerSource( getPlayerInv().player, (IActionHost) getTarget() );
	}

}
