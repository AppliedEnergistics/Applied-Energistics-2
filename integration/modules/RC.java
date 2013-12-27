package appeng.integration.modules;

import mods.railcraft.api.crafting.IRockCrusherRecipe;
import mods.railcraft.api.crafting.RailcraftCraftingManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import appeng.api.AEApi;
import appeng.integration.IIntegrationModule;

public class RC implements IIntegrationModule
{

	public static RC instance;

	public void rockCrusher(ItemStack input, ItemStack output)
	{
		IRockCrusherRecipe re = RailcraftCraftingManager.rockCrusher.createNewRecipe( input, true, true );
		re.addOutput( output, 1.0f );
	}

	public void rockCrusher(ItemStack input, ItemStack output, ItemStack secondary, float chance)
	{
		IRockCrusherRecipe re = RailcraftCraftingManager.rockCrusher.createNewRecipe( input, true, true );
		re.addOutput( output, 1.0f );
		if ( secondary != null ) re.addOutput( secondary, chance );
	}

	@Override
	public void Init()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void PostInit()
	{
		// certus quartz
		rockCrusher( AEApi.instance().materials().materialCertusQuartzCrystal.stack( 1 ),
				AEApi.instance().materials().materialCertusQuartzDust.stack( 1 ) );

		rockCrusher( AEApi.instance().materials().materialCertusQuartzCrystalCharged.stack( 1 ),
				AEApi.instance().materials().materialCertusQuartzDust.stack( 1 ) );

		// fluix
		rockCrusher( AEApi.instance().materials().materialFluixCrystal.stack( 1 ), AEApi.instance().materials().materialFluixDust.stack( 1 ) );

		// nether quartz
		rockCrusher( new ItemStack( Item.netherQuartz ), AEApi.instance().materials().materialNetherQuartzDust.stack( 1 ) );
	}
}
