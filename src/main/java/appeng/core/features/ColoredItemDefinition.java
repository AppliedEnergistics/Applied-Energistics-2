package appeng.core.features;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import appeng.api.util.AEColor;
import appeng.api.util.AEColoredItemDefinition;

public class ColoredItemDefinition implements AEColoredItemDefinition
{

	final ItemStackSrc[] colors = new ItemStackSrc[17];

	@Override
	public Item item(AEColor color)
	{
		ItemStackSrc is = colors[color.ordinal()];

		if ( is == null )
			return null;

		return is.item;
	}

	@Override
	public ItemStack stack(AEColor color, int stackSize)
	{
		ItemStackSrc is = colors[color.ordinal()];

		if ( is == null )
			return null;

		return is.stack( stackSize );
	}

	@Override
	public boolean sameAs(AEColor color, ItemStack comparableItem)
	{
		ItemStackSrc is = colors[color.ordinal()];

		if ( comparableItem == null || is == null )
			return false;

		return comparableItem.getItem() == is.item && comparableItem.getItemDamage() == is.damage;
	}

	public void add(AEColor v, ItemStackSrc is)
	{
		colors[v.ordinal()] = is;
	}

	@Override
	public Block block(AEColor color)
	{
		return null;
	}

	@Override
	public Class<? extends TileEntity> entity(AEColor color)
	{
		return null;
	}

	@Override
	public ItemStack[] allStacks(int stackSize)
	{
		ItemStack is[] = new ItemStack[colors.length];
		for (int x = 0; x < is.length; x++)
			is[x] = colors[x].stack( 1 );
		return is;
	}

}
