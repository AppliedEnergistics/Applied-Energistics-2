package uristqwerty.CraftGuide;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemCraftGuide extends Item
{
	public ItemCraftGuide(String iconName)
	{
		setUnlocalizedName("craftguide_item");

		setTextureName(iconName);
		setCreativeTab(CreativeTabs.tabMisc);

		registerItemName();
	}

	private  void registerItemName()
	{
		try
		{
			Class registry = Class.forName("cpw.mods.fml.common.registry.GameRegistry");
			Method registerItem = registry.getMethod("registerItem", Item.class, String.class, String.class);
			registerItem.invoke(null, this, "craftguide_item", "craftguide");
		}
		catch(ClassNotFoundException e){}
		catch(SecurityException e){}
		catch(NoSuchMethodException e){}
		catch(IllegalArgumentException e){}
		catch(IllegalAccessException e){}
		catch(InvocationTargetException e){}
	}

	@Override
    public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer player)
    {
		CraftGuide.side.openGUI(player);
        return itemstack;
    }

	@Override
	public int getColorFromItemStack(ItemStack par1ItemStack, int par2)
	{
		return 0x9999ff;
	}
}
