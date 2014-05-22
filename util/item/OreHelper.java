package appeng.util.item;

import java.util.Collection;
import java.util.HashMap;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import appeng.api.storage.data.IAEItemStack;

public class OreHelper
{

	public static OreHelper instance = new OreHelper();

	class ItemRef
	{

		ItemRef(ItemStack stack) {
			ref = stack.getItem();

			if ( stack.getItem().isDamageable() )
				damage = 0; // IGNORED
			else
				damage = stack.getItemDamage(); // might be important...

			hash = ref.hashCode() ^ damage;
		}

		Item ref;
		int damage;
		int hash;

		@Override
		public boolean equals(Object o)
		{
			ItemRef obj = (ItemRef) o;
			return damage == obj.damage && ref == obj.ref;
		}

		@Override
		public int hashCode()
		{
			return hash;
		}

	};

	class OreResult
	{

		public OreRefrence oreValue = null;

	};

	HashMap<ItemRef, OreResult> refrences = new HashMap();

	public OreRefrence isOre(ItemStack ItemStack)
	{
		ItemRef ir = new ItemRef( ItemStack );
		OreResult or = refrences.get( ir );

		if ( or == null )
		{
			or = new OreResult();
			refrences.put( ir, or );

			OreRefrence ref = new OreRefrence();
			Collection<Integer> ores = ref.getOres();
			Collection<ItemStack> set = ref.getEquivilients();
			Collection<IAEItemStack> aeset = ref.getAEEquivilients();

			for (String ore : OreDictionary.getOreNames())
			{
				boolean add = false;

				for (ItemStack oreItem : OreDictionary.getOres( ore ))
				{
					if ( OreDictionary.itemMatches( oreItem, ItemStack, false ) )
					{
						add = true;
						break;
					}
				}

				if ( add )
				{
					for (ItemStack oreItem : OreDictionary.getOres( ore ))
						set.add( oreItem.copy() );

					ores.add( OreDictionary.getOreID( ore ) );
				}
			}

			if ( !set.isEmpty() )
			{
				or.oreValue = ref;

				// SUMMON AE STACKS!
				for (ItemStack is : set)
					aeset.add( AEItemStack.create( is ) );
			}
		}

		return or.oreValue;
	}

	public boolean sameOre(AEItemStack aeItemStack, IAEItemStack is)
	{
		OreRefrence a = aeItemStack.def.isOre;
		OreRefrence b = ((AEItemStack) aeItemStack).def.isOre;

		if ( a == b )
			return true;

		if ( a == null || b == null )
			return false;

		Collection<Integer> bOres = b.getOres();
		for (Integer ore : a.ores)
		{
			if ( bOres.contains( ore ) )
				return true;
		}

		return false;
	}

	public boolean sameOre(OreRefrence a, OreRefrence b)
	{
		if ( a == b )
			return true;

		if ( a == null || b == null )
			return false;

		Collection<Integer> bOres = b.getOres();
		for (Integer ore : a.ores)
		{
			if ( bOres.contains( ore ) )
				return true;
		}

		return false;
	}

	public boolean sameOre(AEItemStack aeItemStack, ItemStack o)
	{
		OreRefrence a = aeItemStack.def.isOre;
		if ( a == null )
			return false;

		for (ItemStack oreItem : a.getEquivilients())
		{
			if ( OreDictionary.itemMatches( oreItem, o, false ) )
				return true;
		}

		return false;
	}
}