package appeng.client.me;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import appeng.container.slot.AppEngSlot;
import appeng.items.misc.ItemEncodedPattern;
import appeng.util.Platform;

public class SlotDisconnected extends AppEngSlot
{

	public final ClientDCInternalInv mySlot;

	public SlotDisconnected(ClientDCInternalInv me, int which, int x, int y) {
		super( me.inv, which, x, y );
		mySlot = me;
	}

	@Override
	public ItemStack getDisplayStack()
	{
		if ( Platform.isClient() )// && (which == PlaceableItemType.ENCODED_PATTERN) )
		{
			ItemStack is = super.getStack();
			if ( is != null && is.getItem() instanceof ItemEncodedPattern )
			{
				ItemEncodedPattern iep = (ItemEncodedPattern) is.getItem();
				ItemStack out = iep.getOutput( is );
				if ( out != null )
					return out;
			}
		}
		return super.getStack();
	}

	@Override
	public boolean canTakeStack(EntityPlayer par1EntityPlayer)
	{
		return false;
	}

	@Override
	public ItemStack decrStackSize(int par1)
	{
		return null;
	}

	@Override
	public void putStack(ItemStack par1ItemStack)
	{

	}

	@Override
	public boolean getHasStack()
	{
		return getStack() != null;
	}

	@Override
	public boolean isItemValid(ItemStack par1ItemStack)
	{
		return false;
	}

	@Override
	public int getSlotStackLimit()
	{
		return 0;
	}

	@Override
	public boolean isSlotInInventory(IInventory par1iInventory, int par2)
	{
		return false;
	}

	@Override
	public void onPickupFromSlot(EntityPlayer par1EntityPlayer, ItemStack par2ItemStack)
	{
	}

}
