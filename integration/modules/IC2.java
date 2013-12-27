package appeng.integration.modules;

import ic2.api.recipe.RecipeInputItemStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import appeng.api.AEApi;
import appeng.integration.IIntegrationModule;

public class IC2 implements IIntegrationModule
{

	public static IC2 instance;

	@Override
	public void Init()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void PostInit()
	{
		// certus quartz
		maceratorRecipe( AEApi.instance().materials().materialCertusQuartzCrystal.stack( 1 ),
				AEApi.instance().materials().materialCertusQuartzDust.stack( 1 ) );

		maceratorRecipe( AEApi.instance().materials().materialCertusQuartzCrystalCharged.stack( 1 ),
				AEApi.instance().materials().materialCertusQuartzDust.stack( 1 ) );

		// fluix
		maceratorRecipe( AEApi.instance().materials().materialFluixCrystal.stack( 1 ),
				AEApi.instance().materials().materialFluixDust.stack( 1 ) );

		// nether quartz
		maceratorRecipe( new ItemStack( Item.netherQuartz ), AEApi.instance().materials().materialNetherQuartzDust.stack( 1 ) );
	}

	private void maceratorRecipe(ItemStack in, ItemStack out)
	{
		ic2.api.recipe.Recipes.macerator.addRecipe( new RecipeInputItemStack( in, in.stackSize ), null, out );
	}

}
