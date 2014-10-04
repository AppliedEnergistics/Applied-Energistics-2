package appeng.integration.modules;

import mods.railcraft.api.crafting.IRockCrusherRecipe;
import mods.railcraft.api.crafting.RailcraftCraftingManager;
import net.minecraft.item.ItemStack;
import appeng.integration.BaseModule;
import appeng.integration.abstraction.IRC;

public class RC extends BaseModule implements IRC
{

	public static RC instance;

	@Override
	public void rockCrusher(ItemStack input, ItemStack output)
	{
		IRockCrusherRecipe re = RailcraftCraftingManager.rockCrusher.createNewRecipe( input, true, true );
		re.addOutput( output, 1.0f );
	}

	public RC() {
		TestClass( RailcraftCraftingManager.class );
		TestClass( IRockCrusherRecipe.class );
	}

	@Override
	public void Init()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void PostInit()
	{

	}
}
