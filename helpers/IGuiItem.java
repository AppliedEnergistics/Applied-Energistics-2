package appeng.helpers;

import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public interface IGuiItem
{

	Object getGuiObject(ItemStack is, World world, int x, int y, int z);

}
