package appeng.core.features;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import appeng.api.util.AEColor;
import appeng.api.util.AEColoredItemDefinition;

public class ColoredItemDefinition implements AEColoredItemDefinition
{

	ItemStack colors[] = new ItemStack[17];

	@Override
	public Item item(AEColor color)
	{
		ItemStack is = colors[color.ordinal()];

		if ( is == null )
			return null;

		return is.getItem();
	}

	@Override
	public ItemStack stack(AEColor color, int stackSize)
	{
		ItemStack is = colors[color.ordinal()];

		if ( is == null )
			return null;

		return new ItemStack( is.getItem(), stackSize, is.getItemDamage() );
	}

	@Override
	public boolean sameAs(AEColor color, ItemStack comparableItem)
	{
		ItemStack is = colors[color.ordinal()];

		if ( comparableItem == null )
			return false;

		return comparableItem.getItem() == is.getItem() && comparableItem.getItemDamage() == is.getItemDamage();
	}

	public void add(AEColor v, ItemStack is)
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

}
