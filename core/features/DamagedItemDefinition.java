package appeng.core.features;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import appeng.api.util.AEItemDefinition;

public class DamagedItemDefinition implements AEItemDefinition
{

	final Item baseItem;
	final int damage;

	public DamagedItemDefinition(ItemStack is) {
		if ( is == null )
		{
			baseItem = null;
			damage = -1;
		}
		else
		{
			baseItem = is.getItem();
			damage = is.getItemDamage();
		}
	}

	@Override
	public Block block()
	{
		return null;
	}

	@Override
	public Item item()
	{
		return baseItem;
	}

	@Override
	public Class<? extends TileEntity> entity()
	{
		return null;
	}

	@Override
	public ItemStack stack(int stackSize)
	{
		if ( baseItem == null )
			return null;

		return new ItemStack( baseItem, stackSize, damage );
	}

	@Override
	public boolean sameAs(ItemStack comparableItem)
	{
		if ( comparableItem == null )
			return false;

		return comparableItem.getItem() == baseItem && comparableItem.getItemDamage() == damage;
	}

}
