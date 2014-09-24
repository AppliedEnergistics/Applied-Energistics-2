package appeng.core.features;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemStackSrc implements IStackSrc
{

	public final Item item;
	public final Block block;
	public final int damage;

	public ItemStackSrc(Item i, int dmg) {
		block = null;
		item = i;
		damage = dmg;
	}

	public ItemStackSrc(Block b, int dmg) {
		item = null;
		block = b;
		damage = dmg;
	}

	@Override
	public ItemStack stack(int i)
	{
		if ( block != null )
			return new ItemStack( block, i, damage );

		if ( item != null )
			return new ItemStack( item, i, damage );

		return null;
	}

	@Override
	public Item getItem()
	{
		return item;
	}

	@Override
	public int getDamage()
	{
		return damage;
	}
}
