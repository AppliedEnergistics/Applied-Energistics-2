package appeng.integration.modules;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import appeng.api.AEApi;
import appeng.integration.IIntegrationModule;

public class TE implements IIntegrationModule
{

	public static TE instance;

	@Override
	public void Init()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void PostInit()
	{
		// certus quartz
		pulverizer( AEApi.instance().materials().materialCertusQuartzCrystal.stack( 1 ), AEApi.instance().materials().materialCertusQuartzDust.stack( 1 ) );

		pulverizer( AEApi.instance().materials().materialCertusQuartzCrystalCharged.stack( 1 ), AEApi.instance().materials().materialCertusQuartzDust.stack( 1 ) );

		// fluix
		pulverizer( AEApi.instance().materials().materialFluixCrystal.stack( 1 ), AEApi.instance().materials().materialFluixDust.stack( 1 ) );

		// nether quartz
		pulverizer( new ItemStack( Item.netherQuartz ), AEApi.instance().materials().materialNetherQuartzDust.stack( 1 ) );
	}

	private void pulverizer(ItemStack in, ItemStack out)
	{
		thermalexpansion.api.crafting.CraftingManagers.pulverizerManager.addRecipe( 320, in, out );
	}

	public void addItemsToPipe(TileEntity ad, ItemStack itemstack, ForgeDirection dir)
	{
		// TODO Auto-generated method stub

	}

	public boolean canAddItemsToPipe(TileEntity ad, ItemStack itemstack, ForgeDirection dir)
	{
		// TODO Auto-generated method stub
		return false;
	}

}
