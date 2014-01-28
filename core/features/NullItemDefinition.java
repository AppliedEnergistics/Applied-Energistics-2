package appeng.core.features;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import appeng.api.util.AEItemDefinition;

public class NullItemDefinition implements AEItemDefinition
{

	@Override
	public Block block()
	{
		return null;
	}

	@Override
	public Item item()
	{
		return null;
	}

	@Override
	public Class<? extends TileEntity> entity()
	{
		return null;
	}

	@Override
	public ItemStack stack(int stackSize)
	{
		return null;
	}

	@Override
	public boolean sameAs(ItemStack comparableItem)
	{
		return false;
	}

}
