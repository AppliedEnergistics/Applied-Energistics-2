package appeng.items.tools.quartz;

import java.util.EnumSet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.implementations.guiobjects.IGuiItem;
import appeng.api.implementations.guiobjects.IGuiItemObject;
import appeng.core.features.AEFeature;
import appeng.core.sync.GuiBridge;
import appeng.items.AEBaseItem;
import appeng.items.contents.QuartzKnifeObj;
import appeng.util.Platform;

public class ToolQuartzCuttingKnife extends AEBaseItem implements IGuiItem
{

	final AEFeature type;

	public ToolQuartzCuttingKnife(AEFeature Type) {
		super( ToolQuartzCuttingKnife.class, Type.name() );
		setFeature( EnumSet.of( type = Type, AEFeature.QuartzKnife ) );
		setMaxDamage( 50 );
		setMaxStackSize( 1 );
	}

	@Override
	public boolean getIsRepairable(ItemStack a, ItemStack b)
	{
		return Platform.canRepair( type, a, b );
	}

	@Override
	public boolean isRepairable()
	{
		return false;
	}

	@Override
	public boolean doesContainerItemLeaveCraftingGrid(ItemStack par1ItemStack)
	{
		return false;
	}

	@Override
	public boolean hasContainerItem(ItemStack stack)
	{
		return true;
	}

	@Override
	public ItemStack onItemRightClick(ItemStack it, World w, EntityPlayer p)
	{
		if ( Platform.isServer() )
			Platform.openGUI( p, null, ForgeDirection.UNKNOWN, GuiBridge.GUI_QUARTZ_KNIFE );
		p.swingItem();
		return it;
	}

	@Override
	public boolean onItemUse(ItemStack is, EntityPlayer p, World w, int x, int y, int z, int s, float hitX, float hitY, float hitZ)
	{
		if ( Platform.isServer() )
			Platform.openGUI( p, null, ForgeDirection.UNKNOWN, GuiBridge.GUI_QUARTZ_KNIFE );
		return true;
	}

	@Override
	public ItemStack getContainerItem(ItemStack itemStack)
	{
		itemStack.setItemDamage( itemStack.getItemDamage() + 1 );
		return itemStack;
	}

	@Override
	public IGuiItemObject getGuiObject(ItemStack is, World world, int x, int y, int z)
	{
		return new QuartzKnifeObj( is );
	}

}
