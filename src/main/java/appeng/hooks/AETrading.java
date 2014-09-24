package appeng.hooks;

import java.util.Random;

import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import appeng.api.AEApi;
import cpw.mods.fml.common.registry.VillagerRegistry.IVillageTradeHandler;

public class AETrading implements IVillageTradeHandler
{

	private void addToList(MerchantRecipeList l, ItemStack a, ItemStack b)
	{
		if ( a.stackSize < 1 )
			a.stackSize = 1;
		if ( b.stackSize < 1 )
			b.stackSize = 1;

		if ( a.stackSize > a.getMaxStackSize() )
			a.stackSize = a.getMaxStackSize();
		if ( b.stackSize > b.getMaxStackSize() )
			b.stackSize = b.getMaxStackSize();

		l.add( new MerchantRecipe( a, b ) );
	}

	private void addTrade(MerchantRecipeList list, ItemStack a, ItemStack b, Random rand, int conversion_Variance)
	{
		// Sell
		ItemStack From = a.copy();
		ItemStack To = b.copy();

		From.stackSize = 1 + (Math.abs( rand.nextInt() ) % (1 + conversion_Variance));
		To.stackSize = 1;

		addToList( list, From, To );
	}

	private void addMerchant(MerchantRecipeList list, ItemStack item, int emera, Random rand, int greed)
	{
		if ( item == null )
			return;
		
		// Sell
		ItemStack From = item.copy();
		ItemStack To = new ItemStack( Items.emerald );

		int multiplier = (Math.abs( rand.nextInt() ) % 6);
		emera += (Math.abs( rand.nextInt() ) % greed) - multiplier;
		int mood = rand.nextInt() % 2;

		From.stackSize = multiplier + mood;
		To.stackSize = multiplier * emera - mood;

		if ( To.stackSize < 0 )
		{
			From.stackSize -= To.stackSize;
			To.stackSize -= To.stackSize;
		}

		addToList( list, From, To );

		// Buy
		ItemStack reverseTo = From.copy();
		ItemStack reverseFrom = To.copy();

		reverseFrom.stackSize = (int) (reverseFrom.stackSize * (rand.nextFloat() * 3.0f + 1.0f));
		reverseTo.stackSize = reverseTo.stackSize;

		addToList( list, reverseFrom, reverseTo );
	}

	@Override
	public void manipulateTradesForVillager(EntityVillager villager, MerchantRecipeList recipeList, Random random)
	{
		addMerchant( recipeList, AEApi.instance().materials().materialSilicon.stack( 1 ), 1, random, 2 );
		addMerchant( recipeList, AEApi.instance().materials().materialCertusQuartzCrystal.stack( 1 ), 2, random, 4 );
		addMerchant( recipeList, AEApi.instance().materials().materialCertusQuartzDust.stack( 1 ), 1, random, 3 );

		addTrade( recipeList, AEApi.instance().materials().materialCertusQuartzDust.stack( 1 ),
				AEApi.instance().materials().materialCertusQuartzCrystal.stack( 1 ), random, 2 );
	}

}
