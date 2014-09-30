package appeng.container.slot;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import appeng.container.AEBaseContainer;
import appeng.tile.inventory.AppEngInternalInventory;

public class AppEngSlot extends Slot
{

	public enum hasCalculatedValidness
	{
		NotAvailable, Valid, Invalid
	}

	public boolean isDraggable = true;
	public boolean isPlayerSide = false;
	public AEBaseContainer myContainer = null;

	public Slot setNotDraggable()
	{
		isDraggable = false;
		return this;
	}

	public Slot setPlayerSide()
	{
		isPlayerSide = true;
		return this;
	}

	public int IIcon = -1;
	public hasCalculatedValidness isValid;
	public final int defX;
	public final int defY;

	@Override
	public boolean func_111238_b()
	{
		return isEnabled();
	}

	public boolean isEnabled()
	{
		return true;
	}

	public String getTooltip()
	{
		return null;
	}

	@Override
	public void onSlotChanged()
	{
		if ( inventory instanceof AppEngInternalInventory )
			((AppEngInternalInventory) inventory).markDirty( getSlotIndex() );
		else
			super.onSlotChanged();

		isValid = hasCalculatedValidness.NotAvailable;
	}

	public AppEngSlot(IInventory inv, int idx, int x, int y) {
		super( inv, idx, x, y );
		defX = x;
		defY = y;
		isValid = hasCalculatedValidness.NotAvailable;
	}

	public boolean isDisplay = false;

	@Override
	public ItemStack getStack()
	{
		if ( !isEnabled() )
			return null;

		if ( inventory.getSizeInventory() <= getSlotIndex() )
			return null;

		if ( isDisplay )
		{
			isDisplay = false;
			return getDisplayStack();
		}
		return super.getStack();
	}

	@Override
	public void putStack(ItemStack par1ItemStack)
	{
		if ( isEnabled() )
		{
			super.putStack( par1ItemStack );

			if ( myContainer != null )
				myContainer.onSlotChange( this );
		}
	}

	public void clearStack()
	{
		super.putStack( null );
	}

	@Override
	public boolean canTakeStack(EntityPlayer par1EntityPlayer)
	{
		if ( isEnabled() )
			return super.canTakeStack( par1EntityPlayer );
		return false;
	}

	@Override
	public boolean isItemValid(ItemStack par1ItemStack)
	{
		if ( isEnabled() )
			return super.isItemValid( par1ItemStack );
		return false;
	}

	public ItemStack getDisplayStack()
	{
		return super.getStack();
	}

	public float getOpacityOfIcon()
	{
		return 0.4f;
	}

	public boolean renderIconWithItem()
	{
		return false;
	}

	public int getIcon()
	{
		return IIcon;
	}

	public boolean isPlayerSide()
	{
		return isPlayerSide;
	}

	public boolean shouldDisplay()
	{
		return isEnabled();
	}

}
