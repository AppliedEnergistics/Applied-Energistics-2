package appeng.hooks;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public interface IBlockTool
{

	boolean onItemUse(ItemStack dispensedItem, EntityPlayer player, World w, int x, int y, int z, int ordinal, float hitx, float hity, float hitz);

}
