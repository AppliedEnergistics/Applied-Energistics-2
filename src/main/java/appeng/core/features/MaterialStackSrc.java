package appeng.core.features;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import appeng.items.materials.MaterialType;

public class MaterialStackSrc implements IStackSrc
{

	final MaterialType src;

	public MaterialStackSrc(MaterialType src) {
		this.src = src;
		if ( src == null )
			throw new RuntimeException( "Invalid Item Stack" );
	}

	@Override
	public ItemStack stack(int stackSize)
	{
		return src.stack( stackSize );
	}

	@Override
	public Item getItem()
	{
		return src.itemInstance;
	}

	@Override
	public int getDamage()
	{
		return src.damageValue;
	}

}
