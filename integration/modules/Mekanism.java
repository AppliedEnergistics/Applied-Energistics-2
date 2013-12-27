package appeng.integration.modules;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import appeng.api.AEApi;
import appeng.integration.IIntegrationModule;

public class Mekanism implements IIntegrationModule
{

	public static Mekanism instance;

	@Override
	public void Init()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void PostInit()
	{
		// certus quartz
		crusher( AEApi.instance().materials().materialCertusQuartzCrystal.stack( 1 ),
				AEApi.instance().materials().materialCertusQuartzDust.stack( 1 ) );

		crusher( AEApi.instance().materials().materialCertusQuartzCrystalCharged.stack( 1 ),
				AEApi.instance().materials().materialCertusQuartzDust.stack( 1 ) );

		// fluix
		crusher( AEApi.instance().materials().materialFluixCrystal.stack( 1 ), AEApi.instance().materials().materialFluixDust.stack( 1 ) );

		// nether quartz
		crusher( new ItemStack( Item.netherQuartz ), AEApi.instance().materials().materialNetherQuartzDust.stack( 1 ) );
	}

	private void crusher(ItemStack input, ItemStack output)
	{
		mekanism.api.RecipeHelper.addCrusherRecipe( input, output );
	}
}
