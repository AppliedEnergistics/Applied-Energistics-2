package appeng.recipes.game;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import appeng.api.AEApi;
import appeng.api.util.AEItemDefinition;
import appeng.items.parts.ItemFacade;

public class FacadeRecipe implements IRecipe
{

	private AEItemDefinition anchor = AEApi.instance().parts().partCableAnchor;
	private ItemFacade facade = (ItemFacade) AEApi.instance().items().itemFacade.item();

	private ItemStack getOutput(InventoryCrafting inv, boolean createFacade)
	{
		if ( inv.getStackInSlot( 0 ) == null && inv.getStackInSlot( 2 ) == null && inv.getStackInSlot( 6 ) == null && inv.getStackInSlot( 8 ) == null )
		{
			if ( anchor.sameAs( inv.getStackInSlot( 1 ) ) && anchor.sameAs( inv.getStackInSlot( 3 ) ) && anchor.sameAs( inv.getStackInSlot( 5 ) )
					&& anchor.sameAs( inv.getStackInSlot( 7 ) ) )
			{
				ItemStack facadeis = facade.createFacadeForItem( inv.getStackInSlot( 1 ), !createFacade );
				if ( facadeis != null )
					facadeis.stackSize = 4;
				return facadeis;
			}
		}
		return null;
	}

	@Override
	public boolean matches(InventoryCrafting inv, World w)
	{
		return getOutput( inv, false ) != null;
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting inv)
	{
		return getOutput( inv, true );
	}

	@Override
	public int getRecipeSize()
	{
		return 9;
	}

	@Override
	public ItemStack getRecipeOutput() // no default output..
	{
		return null;
	}

}