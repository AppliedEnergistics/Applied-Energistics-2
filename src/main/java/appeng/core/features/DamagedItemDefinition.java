package appeng.core.features;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import appeng.api.util.AEItemDefinition;

public class DamagedItemDefinition implements AEItemDefinition
{

	final IStackSrc src;

	public DamagedItemDefinition(IStackSrc is) {
		src = is;
	}

	@Override
	public Block block()
	{
		return null;
	}

	@Override
	public Item item()
	{
		return src.getItem();
	}

	@Override
	public Class<? extends TileEntity> entity()
	{
		return null;
	}

	@Override
	public ItemStack stack(int stackSize)
	{
		return src.stack( stackSize );
	}

	@Override
	public boolean sameAsStack(ItemStack comparableItem)
	{
		if ( comparableItem == null )
			return false;

		return comparableItem.getItem() == src.getItem() && comparableItem.getItemDamage() == src.getDamage();
	}

	@Override
	public boolean sameAsBlock(IBlockAccess world, int x, int y, int z)
	{
		return false;
	}

}
