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

	private HashMap<ItemStack, Double> DamageModifiers = new HashMap<ItemStack, Double>();

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
	public void oreRegistered(String Name, ItemStack item)
	{
		if ( !(Name.startsWith( "berry" ) || Name.startsWith( "nugget" )) )
			return;

		// addNugget( "Cobble", 18 ); // ?
		considerItem( Name, item, "MeatRaw", 32 );
		considerItem( Name, item, "MeatCooked", 32 );
		considerItem( Name, item, "Meat", 32 );
		considerItem( Name, item, "Chicken", 32 );
		considerItem( Name, item, "Beef", 32 );
		considerItem( Name, item, "Sheep", 32 );
		considerItem( Name, item, "Fish", 32 );

		// real world...
		considerItem( Name, item, "Lithium", 6.941 );
		considerItem( Name, item, "Beryllium", 9.0122 );
		considerItem( Name, item, "Boron", 10.811 );
		considerItem( Name, item, "Carbon", 12.0107 );
		considerItem( Name, item, "Coal", 12.0107 );
		considerItem( Name, item, "Charcoal", 12.0107 );
		considerItem( Name, item, "Sodium", 22.9897 );
		considerItem( Name, item, "Magnesium", 24.305 );
		considerItem( Name, item, "Aluminum", 26.9815 );
		considerItem( Name, item, "Silicon", 28.0855 );
		considerItem( Name, item, "Phosphorus", 30.9738 );
		considerItem( Name, item, "Sulfur", 32.065 );
		considerItem( Name, item, "Potassium", 39.0983 );
		considerItem( Name, item, "Calcium", 40.078 );
		considerItem( Name, item, "Scandium", 44.9559 );
		considerItem( Name, item, "Titanium", 47.867 );
		considerItem( Name, item, "Vanadium", 50.9415 );
		considerItem( Name, item, "Manganese", 54.938 );
		considerItem( Name, item, "Iron", 55.845 );
		considerItem( Name, item, "Nickel", 58.6934 );
		considerItem( Name, item, "Cobalt", 58.9332 );
		considerItem( Name, item, "Copper", 63.546 );
		considerItem( Name, item, "Zinc", 65.39 );
		considerItem( Name, item, "Gallium", 69.723 );
		considerItem( Name, item, "Germanium", 72.64 );
		considerItem( Name, item, "Bromine", 79.904 );
		considerItem( Name, item, "Krypton", 83.8 );
		considerItem( Name, item, "Rubidium", 85.4678 );
		considerItem( Name, item, "Strontium", 87.62 );
		considerItem( Name, item, "Yttrium", 88.9059 );
		considerItem( Name, item, "Zirconiumm", 91.224 );
		considerItem( Name, item, "Niobiumm", 92.9064 );
		considerItem( Name, item, "Technetium", 98 );
		considerItem( Name, item, "Ruthenium", 101.07 );
		considerItem( Name, item, "Rhodium", 102.9055 );
		considerItem( Name, item, "Palladium", 106.42 );
		considerItem( Name, item, "Silver", 107.8682 );
		considerItem( Name, item, "Cadmium", 112.411 );
		considerItem( Name, item, "Indium", 114.818 );
		considerItem( Name, item, "Tin", 118.71 );
		considerItem( Name, item, "Antimony", 121.76 );
		considerItem( Name, item, "Iodine", 126.9045 );
		considerItem( Name, item, "Tellurium", 127.6 );
		considerItem( Name, item, "Xenon", 131.293 );
		considerItem( Name, item, "Cesium", 132.9055 );
		considerItem( Name, item, "Barium", 137.327 );
		considerItem( Name, item, "Lanthanum", 138.9055 );
		considerItem( Name, item, "Cerium", 140.116 );
		considerItem( Name, item, "Tantalum", 180.9479 );
		considerItem( Name, item, "Tungsten", 183.84 );
		considerItem( Name, item, "Osmium", 190.23 );
		considerItem( Name, item, "Iridium", 192.217 );
		considerItem( Name, item, "Platinum", 195.078 );
		considerItem( Name, item, "Lead", 207.2 );
		considerItem( Name, item, "Bismuth", 208.9804 );
		considerItem( Name, item, "Uranium", 238.0289 );
		considerItem( Name, item, "Plutonium", 244 );

		// TE stuff...
		considerItem( Name, item, "Invar", (58.6934 + 55.845 + 55.845) / 3.0 );
		considerItem( Name, item, "Electrum", (107.8682 + 196.96655) / 2.0 );
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
