package appeng.core.features;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public interface IStackSrc
{

	ItemStack stack(int i);

	Item getItem();

	int getDamage();

}
