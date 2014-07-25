package appeng.integration.modules;

import mekanism.api.RecipeHelper;
import net.minecraft.item.ItemStack;
import appeng.integration.BaseModule;
import appeng.integration.abstraction.IMekanism;

public class Mekanism extends BaseModule implements IMekanism
{

	public static Mekanism instance;

	@Override
	public void Init() throws Throwable
	{
		TestClass( mekanism.api.energy.IStrictEnergyAcceptor.class );
	}

	@Override
	public void PostInit() throws Throwable
	{

	}

	@Override
	public void addCrusherRecipe(ItemStack in, ItemStack out)
	{
		RecipeHelper.addCrusherRecipe( in, out );
	}

	@Override
	public void addEnrichmentChamberRecipe(ItemStack in, ItemStack out)
	{
		RecipeHelper.addEnrichmentChamberRecipe( in, out );
	}

}
