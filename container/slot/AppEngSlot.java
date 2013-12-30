package appeng.container.slot;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class AppEngSlot extends Slot
{

	public enum hasCalculatedValidness
	{
		NotAvailable, Valid, Invalid
	};

	public boolean isPlayerSide = false;

	public Slot setPlayerSide()
	{
		isPlayerSide = true;
		return this;
	}

	public int icon = -1;
	public hasCalculatedValidness isValid;
	public int defX, defY;

	public String getTooltip()
	{
		return null;
	}

	@Override
	public void onSlotChanged()
	{
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
		if ( isDisplay )
		{
			isDisplay = false;
			return getDisplayStack();
		}
		return super.getStack();
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
		return icon;
	}

	public boolean isPlayerSide()
	{
		return isPlayerSide;
	}

}
