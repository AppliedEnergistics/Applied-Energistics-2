package appeng.core.features.registries;

import java.util.HashMap;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import appeng.api.features.IMatterCannonAmmoRegistry;
import appeng.recipes.ores.IOreListener;
import appeng.recipes.ores.OreDictionaryHandler;
import appeng.util.Platform;

public class MatterCannonAmmoRegistry implements IOreListener, IMatterCannonAmmoRegistry
{

	private final HashMap<ItemStack, Double> DamageModifiers = new HashMap<ItemStack, Double>();

	@Override
	public void registerAmmo(ItemStack ammo, double weight)
	{
		DamageModifiers.put( ammo, weight );
	}

	private void considerItem(String ore, ItemStack item, String Name, double weight)
	{
		if ( ore.equals( "berry" + Name ) || ore.equals( "nugget" + Name ) )
		{
			registerAmmo( item, weight );
		}
	}

	@Override
	public void oreRegistered(String name, ItemStack item)
	{
		if ( !(name.startsWith( "berry" ) || name.startsWith( "nugget" )) )
			return;

		// addNugget( "Cobble", 18 ); // ?
		considerItem( name, item, "MeatRaw", 32 );
		considerItem( name, item, "MeatCooked", 32 );
		considerItem( name, item, "Meat", 32 );
		considerItem( name, item, "Chicken", 32 );
		considerItem( name, item, "Beef", 32 );
		considerItem( name, item, "Sheep", 32 );
		considerItem( name, item, "Fish", 32 );

		// real world...
		considerItem( name, item, "Lithium", 6.941 );
		considerItem( name, item, "Beryllium", 9.0122 );
		considerItem( name, item, "Boron", 10.811 );
		considerItem( name, item, "Carbon", 12.0107 );
		considerItem( name, item, "Coal", 12.0107 );
		considerItem( name, item, "Charcoal", 12.0107 );
		considerItem( name, item, "Sodium", 22.9897 );
		considerItem( name, item, "Magnesium", 24.305 );
		considerItem( name, item, "Aluminum", 26.9815 );
		considerItem( name, item, "Silicon", 28.0855 );
		considerItem( name, item, "Phosphorus", 30.9738 );
		considerItem( name, item, "Sulfur", 32.065 );
		considerItem( name, item, "Potassium", 39.0983 );
		considerItem( name, item, "Calcium", 40.078 );
		considerItem( name, item, "Scandium", 44.9559 );
		considerItem( name, item, "Titanium", 47.867 );
		considerItem( name, item, "Vanadium", 50.9415 );
		considerItem( name, item, "Manganese", 54.938 );
		considerItem( name, item, "Iron", 55.845 );
		considerItem( name, item, "Nickel", 58.6934 );
		considerItem( name, item, "Cobalt", 58.9332 );
		considerItem( name, item, "Copper", 63.546 );
		considerItem( name, item, "Zinc", 65.39 );
		considerItem( name, item, "Gallium", 69.723 );
		considerItem( name, item, "Germanium", 72.64 );
		considerItem( name, item, "Bromine", 79.904 );
		considerItem( name, item, "Krypton", 83.8 );
		considerItem( name, item, "Rubidium", 85.4678 );
		considerItem( name, item, "Strontium", 87.62 );
		considerItem( name, item, "Yttrium", 88.9059 );
		considerItem( name, item, "Zirconiumm", 91.224 );
		considerItem( name, item, "Niobiumm", 92.9064 );
		considerItem( name, item, "Technetium", 98 );
		considerItem( name, item, "Ruthenium", 101.07 );
		considerItem( name, item, "Rhodium", 102.9055 );
		considerItem( name, item, "Palladium", 106.42 );
		considerItem( name, item, "Silver", 107.8682 );
		considerItem( name, item, "Cadmium", 112.411 );
		considerItem( name, item, "Indium", 114.818 );
		considerItem( name, item, "Tin", 118.71 );
		considerItem( name, item, "Antimony", 121.76 );
		considerItem( name, item, "Iodine", 126.9045 );
		considerItem( name, item, "Tellurium", 127.6 );
		considerItem( name, item, "Xenon", 131.293 );
		considerItem( name, item, "Cesium", 132.9055 );
		considerItem( name, item, "Barium", 137.327 );
		considerItem( name, item, "Lanthanum", 138.9055 );
		considerItem( name, item, "Cerium", 140.116 );
		considerItem( name, item, "Tantalum", 180.9479 );
		considerItem( name, item, "Tungsten", 183.84 );
		considerItem( name, item, "Osmium", 190.23 );
		considerItem( name, item, "Iridium", 192.217 );
		considerItem( name, item, "Platinum", 195.078 );
		considerItem( name, item, "Lead", 207.2 );
		considerItem( name, item, "Bismuth", 208.9804 );
		considerItem( name, item, "Uranium", 238.0289 );
		considerItem( name, item, "Plutonium", 244 );

		// TE stuff...
		considerItem( name, item, "Invar", (58.6934 + 55.845 + 55.845) / 3.0 );
		considerItem( name, item, "Electrum", (107.8682 + 196.96655) / 2.0 );
	}

	public MatterCannonAmmoRegistry() {
		OreDictionaryHandler.instance.observe( this );
		registerAmmo( new ItemStack( Items.gold_nugget ), 196.96655 );
	}

	@Override
	public float getPenetration(ItemStack is)
	{
		for (ItemStack o : DamageModifiers.keySet())
		{
			if ( Platform.isSameItem( o, is ) )
				return DamageModifiers.get( o ).floatValue();
		}
		return 0;
	}

}
