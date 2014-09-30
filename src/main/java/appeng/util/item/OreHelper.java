package appeng.util.item;

import java.util.Collection;
import java.util.HashMap;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import appeng.api.storage.data.IAEItemStack;

public class OreHelper
{

	public static final OreHelper instance = new OreHelper();

	static class ItemRef
	{

		ItemRef(ItemStack stack)
		{
			ref = stack.getItem();

			if ( stack.getItem().isDamageable() )
				damage = 0; // IGNORED
			else
				damage = stack.getItemDamage(); // might be important...

			hash = ref.hashCode() ^ damage;
		}

		final Item ref;
		final int damage;
		final int hash;

		@Override
		public boolean equals(Object obj)
		{
			if ( obj == null )
				return false;
			if ( getClass() != obj.getClass() )
				return false;
			ItemRef other = (ItemRef) obj;
			return damage == other.damage && ref == other.ref;
		}

		@Override
		public int hashCode()
		{
			return hash;
		}

	}

	static class OreResult
	{

		public OreReference oreValue = null;

	}

	final HashMap<ItemRef, OreResult> references = new HashMap<ItemRef, OreResult>();

	public OreReference isOre(ItemStack ItemStack)
	{
		ItemRef ir = new ItemRef( ItemStack );
		OreResult or = references.get( ir );

		if ( or == null )
		{
			or = new OreResult();
			references.put( ir, or );

			OreReference ref = new OreReference();
			Collection<Integer> ores = ref.getOres();
			Collection<ItemStack> set = ref.getEquivalents();

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
		OreReference b = aeItemStack.def.isOre;

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

		for (ItemStack oreItem : a.getEquivalents())
		{
			if ( OreDictionary.itemMatches( oreItem, o, false ) )
				return true;
		}

		return false;
	}
}