package appeng.core.features.registries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import appeng.api.features.IGrinderEntry;
import appeng.api.features.IGrinderRegistry;
import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.core.features.registries.entries.AppEngGrinderRecipe;
import appeng.recipes.ores.IOreListener;
import appeng.recipes.ores.OreDictionaryHandler;
import appeng.util.Platform;

public class GrinderRecipeManager implements IGrinderRegistry, IOreListener
{

	public List<IGrinderEntry> RecipeList;

	private ItemStack copy(ItemStack is)
	{
		if ( is != null )
			return is.copy();
		return null;
	}

	public GrinderRecipeManager() {
		RecipeList = new ArrayList();

		addOre( "Coal", new ItemStack( Items.coal ) );
		addOre( "Charcoal", new ItemStack( Items.coal, 1, 1 ) );

		addOre( "NetherQuartz", new ItemStack( Blocks.quartz_ore ) );
		addIngot( "NetherQuartz", new ItemStack( Items.quartz ) );

		addOre( "Gold", new ItemStack( Blocks.gold_ore ) );
		addIngot( "Gold", new ItemStack( Items.gold_ingot ) );

		addOre( "Iron", new ItemStack( Blocks.iron_ore ) );
		addIngot( "Iron", new ItemStack( Items.iron_ingot ) );

		addOre( "Obsidian", new ItemStack( Blocks.obsidian ) );
		addIngot( "Ender", new ItemStack( Items.ender_pearl ) );

		addIngot( "Wheat", new ItemStack( Items.wheat ) );

		OreDictionaryHandler.instance.observe( this );
	}

	@Override
	public List<IGrinderEntry> getRecipes()
	{
		log( "API - getRecipes" );
		return RecipeList;
	}

	private void injectRecipe(AppEngGrinderRecipe appEngGrinderRecipe)
	{
		for (IGrinderEntry gr : RecipeList)
			if ( Platform.isSameItemPrecise( gr.getInput(), appEngGrinderRecipe.getInput() ) )
				return;

		RecipeList.add( appEngGrinderRecipe );
	}

	@Override
	public void addRecipe(ItemStack in, ItemStack out, int cost)
	{
		if ( in == null || out == null )
		{
			log( "Invalid Grinder Recipe Specified." );
			return;
		}

		log( "Allow Grinding of " + Platform.getItemDisplayName( in ) + " to " + Platform.getItemDisplayName( out ) + " for " + cost );
		injectRecipe( new AppEngGrinderRecipe( copy( in ), copy( out ), cost ) );
	}

	@Override
	public void addRecipe(ItemStack in, ItemStack out, ItemStack optional, float chance, int cost)
	{
		if ( in == null || (optional == null && out == null) )
		{
			log( "Invalid Grinder Recipe Specified." );
			return;
		}

		log( "Allow Grinding of " + Platform.getItemDisplayName( in ) + " to " + Platform.getItemDisplayName( out ) + " with optional "
				+ Platform.getItemDisplayName( optional ) + " for " + cost );
		injectRecipe( new AppEngGrinderRecipe( copy( in ), copy( out ), copy( optional ), chance, cost ) );
	}

	@Override
	public IGrinderEntry getRecipeForInput(ItemStack input)
	{
		log( "Looking up recipe for " + Platform.getItemDisplayName( input ) );
		if ( input != null )
		{
			for (IGrinderEntry r : RecipeList)
			{
				if ( Platform.isSameItem( input, r.getInput() ) )
				{
					log( "Recipe for " + input.getUnlocalizedName() + " found " + Platform.getItemDisplayName( r.getOutput() ) );
					return r;
				}
			}

			log( "Count not find recipe for " + Platform.getItemDisplayName( input ) );
		}

		return null;
	}

	public void log(String o)
	{
		AELog.grinder( o );
	}

	private int getDustToOreRatio(String name)
	{
		if ( name.equals( "Obsidian" ) )
			return 1;
		if ( name.equals( "Charcoal" ) )
			return 1;
		if ( name.equals( "Coal" ) )
			return 1;
		return 2;
	}

	public Map<ItemStack, String> Ores = new HashMap<ItemStack, String>();
	public Map<ItemStack, String> Ingots = new HashMap<ItemStack, String>();
	public Map<String, ItemStack> Dusts = new HashMap<String, ItemStack>();

	private void addOre(String name, ItemStack item)
	{
		if ( item == null )
			return;
		log( "Adding Ore - " + name + " : " + Platform.getItemDisplayName( item ) );

		Ores.put( item, name );

		if ( Dusts.containsKey( name ) )
		{
			ItemStack is = Dusts.get( name ).copy();
			int ratio = getDustToOreRatio( name );
			if ( ratio > 1 )
			{
				ItemStack extra = is.copy();
				extra.stackSize = ratio - 1;
				addRecipe( item, is, extra, (float) (AEConfig.instance.oreDoublePercentage / 100.0), 8 );
			}
			else
				addRecipe( item, is, 8 );
		}
	}

	private void addIngot(String name, ItemStack item)
	{
		if ( item == null )
			return;
		log( "Adding Ingot - " + name + " : " + Platform.getItemDisplayName( item ) );

		Ingots.put( item, name );

		if ( Dusts.containsKey( name ) )
		{
			addRecipe( item, Dusts.get( name ), 4 );
		}
	}

	private void addDust(String name, ItemStack item)
	{
		if ( item == null )
			return;
		if ( Dusts.containsKey( name ) )
		{
			log( "Rejecting Dust - " + name + " : " + Platform.getItemDisplayName( item ) );
			return;
		}

		log( "Adding Dust - " + name + " : " + Platform.getItemDisplayName( item ) );

		Dusts.put( name, item );

		for (Entry<ItemStack, String> d : Ores.entrySet())
			if ( name.equals( d.getValue() ) )
			{
				ItemStack is = item.copy();
				is.stackSize = 1;
				int ratio = getDustToOreRatio( name );
				if ( ratio > 1 )
				{
					ItemStack extra = is.copy();
					extra.stackSize = ratio - 1;
					addRecipe( d.getKey(), is, extra, (float) (AEConfig.instance.oreDoublePercentage / 100.0), 8 );
				}
				else
					addRecipe( d.getKey(), is, 8 );
			}

		for (Entry<ItemStack, String> d : Ingots.entrySet())
			if ( name.equals( d.getValue() ) )
				addRecipe( d.getKey(), item, 4 );
	}

	@Override
	public void oreRegistered(String Name, ItemStack item)
	{
		if ( Name.startsWith( "ore" ) || Name.startsWith( "crystal" ) || Name.startsWith( "ingot" ) || Name.startsWith( "dust" ) )
		{
			for (String ore : AEConfig.instance.grinderOres)
			{
				if ( Name.equals( "ore" + ore ) )
				{
					addOre( ore, item );
				}
				else if ( Name.equals( "crystal" + ore ) )
				{
					addIngot( ore, item );
				}
				else if ( Name.equals( "ingot" + ore ) )
				{
					addIngot( ore, item );
				}
				else if ( Name.equals( "dust" + ore ) )
				{
					addDust( ore, item );
				}
			}
		}
	}
}
