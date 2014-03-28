package appeng.client.me;

import net.minecraft.item.ItemStack;
import appeng.api.storage.data.IAEItemStack;

public class InternalSlotME
{

	private final ItemRepo repo;

	public int offset;
	public int xPos;
	public int yPos;

	public InternalSlotME(ItemRepo def, int offset, int displayX, int displayY) {
		this.repo = def;
		this.offset = offset;
		this.xPos = displayX;
		this.yPos = displayY;
	}

	public ItemStack getStack()
	{
		return repo.getItem( offset );
	}

	public IAEItemStack getAEStack()
	{
		return repo.getRefrenceItem( offset );
	}

	public boolean hasPower()
	{
		return repo.hasPower();
	}
}
