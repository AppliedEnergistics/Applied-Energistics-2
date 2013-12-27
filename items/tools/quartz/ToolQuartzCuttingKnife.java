package appeng.items.tools.quartz;

import java.util.EnumSet;

import net.minecraft.item.ItemStack;
import appeng.core.features.AEFeature;
import appeng.items.AEBaseItem;

public class ToolQuartzCuttingKnife extends AEBaseItem
{

	public ToolQuartzCuttingKnife(AEFeature type) {
		super( ToolQuartzCuttingKnife.class, type.name() );
		setfeature( EnumSet.of( type, AEFeature.QuartzKnife ) );
		setMaxDamage( 50 );
		setMaxStackSize( 1 );
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
	public boolean hasContainerItem()
	{
		return true;
	}

	@Override
	public ItemStack getContainerItemStack(ItemStack itemStack)
	{
		itemStack.setItemDamage( itemStack.getItemDamage() + 1 );
		return itemStack;
	}

}
