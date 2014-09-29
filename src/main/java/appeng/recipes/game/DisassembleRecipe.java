package appeng.recipes.game;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import appeng.api.AEApi;
import appeng.api.definitions.Blocks;
import appeng.api.definitions.Items;
import appeng.api.definitions.Materials;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;

public class DisassembleRecipe implements IRecipe
{

	private final Materials mats = AEApi.instance().materials();
	private final Items items = AEApi.instance().items();
	private final Blocks blocks = AEApi.instance().blocks();

	private ItemStack getOutput(InventoryCrafting inv, boolean createFacade)
	{
		ItemStack hasCell = null;

		for (int x = 0; x < inv.getSizeInventory(); x++)
		{
			ItemStack is = inv.getStackInSlot( x );
			if ( is != null )
			{
				if ( hasCell != null )
					return null;

				if ( items.itemCell1k.sameAsStack( is ) )
					hasCell = mats.materialCell1kPart.stack( 1 );

				if ( items.itemCell4k.sameAsStack( is ) )
					hasCell = mats.materialCell4kPart.stack( 1 );

				if ( items.itemCell16k.sameAsStack( is ) )
					hasCell = mats.materialCell16kPart.stack( 1 );

				if ( items.itemCell64k.sameAsStack( is ) )
					hasCell = mats.materialCell64kPart.stack( 1 );

				// make sure the storage cell is empty...
				if ( hasCell != null )
				{
					IMEInventory<IAEItemStack> cellInv = AEApi.instance().registries().cell().getCellInventory( is, null, StorageChannel.ITEMS );
					if ( cellInv != null )
					{
						IItemList<IAEItemStack> list = cellInv.getAvailableItems( StorageChannel.ITEMS.createList() );
						if ( !list.isEmpty() )
							return null;
					}
				}

				if ( items.itemEncodedPattern.sameAsStack( is ) )
					hasCell = mats.materialBlankPattern.stack( 1 );

				if ( blocks.blockCraftingStorage1k.sameAsStack( is ) )
					hasCell = mats.materialCell1kPart.stack( 1 );

				if ( blocks.blockCraftingStorage4k.sameAsStack( is ) )
					hasCell = mats.materialCell4kPart.stack( 1 );

				if ( blocks.blockCraftingStorage16k.sameAsStack( is ) )
					hasCell = mats.materialCell16kPart.stack( 1 );

				if ( blocks.blockCraftingStorage64k.sameAsStack( is ) )
					hasCell = mats.materialCell64kPart.stack( 1 );

				if ( hasCell == null )
					return null;
			}
		}

		return hasCell;
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
		return 1;
	}

	@Override
	public ItemStack getRecipeOutput() // no default output..
	{
		return null;
	}

}