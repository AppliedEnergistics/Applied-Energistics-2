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

		public OreReference oreValue = null;

	};

	HashMap<ItemRef, OreResult> refrences = new HashMap();

	public OreReference isOre(ItemStack ItemStack)
	{
		ItemRef ir = new ItemRef( ItemStack );
		OreResult or = refrences.get( ir );

		if ( or == null )
		{
			or = new OreResult();
			refrences.put( ir, or );

			OreReference ref = new OreReference();
			Collection<Integer> ores = ref.getOres();
			Collection<ItemStack> set = ref.getEquivilients();

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
				or.oreValue = ref;
		}

		return or.oreValue;
	}

	public boolean sameOre(AEItemStack aeItemStack, IAEItemStack is)
	{
		OreReference a = aeItemStack.def.isOre;
		OreReference b = ((AEItemStack) aeItemStack).def.isOre;

		if ( a == b )
			return true;

		if ( a == null || b == null )
			return false;

		Collection<Integer> bOres = b.getOres();
		for (Integer ore : a.getOres())
		{
			if ( bOres.contains( ore ) )
				return true;
		}

		return false;
	}

	public boolean sameOre(OreReference a, OreReference b)
	{
		if ( a == null || b == null )
			return false;

		if ( a == b )
			return true;

		Collection<Integer> bOres = b.getOres();
		for (Integer ore : a.getOres())
		{
			if ( bOres.contains( ore ) )
				return true;
		}

		return false;
	}

	public boolean sameOre(AEItemStack aeItemStack, ItemStack o)
	{
		OreReference a = aeItemStack.def.isOre;
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