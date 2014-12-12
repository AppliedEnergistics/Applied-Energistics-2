package appeng.api.implementations.guiobjects;

import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/**
 * Implemented on Item objects, to return objects used to manage, and interact
 * with the contents.
 */
public interface IGuiItem
{

	IGuiItemObject getGuiObject(ItemStack is, World world, int x, int y, int z);

}
