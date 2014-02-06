package appeng.util;

import java.util.Comparator;

import appeng.api.config.SortDir;
import appeng.api.storage.data.IAEItemStack;

public class ItemSorters
{

	public static SortDir Direction = SortDir.ASCENDING;

	public static int compareInt(int a, int b)
	{
		if ( a == b )
			return 0;
		if ( a < b )
			return -1;
		return 1;
	}

	public static int compareLong(long a, long b)
	{
		if ( a == b )
			return 0;
		if ( a < b )
			return -1;
		return 1;
	}

	public static Comparator<IAEItemStack> ConfigBased_SortByName = new Comparator<IAEItemStack>() {

		@Override
		public int compare(IAEItemStack o1, IAEItemStack o2)
		{
			if ( Direction == SortDir.ASCENDING )
				return Platform.getItemDisplayName( o1 ).compareToIgnoreCase( Platform.getItemDisplayName( o2 ) );
			return Platform.getItemDisplayName( o2 ).compareToIgnoreCase( Platform.getItemDisplayName( o1 ) );
		}
	};

	public static Comparator<IAEItemStack> ConfigBased_SortByID = new Comparator<IAEItemStack>() {

		@Override
		public int compare(IAEItemStack o1, IAEItemStack o2)
		{
			if ( Direction == SortDir.ASCENDING )
			{
				int comp = compareInt( o1.getItem().itemID, o2.getItem().itemID );
				if ( comp == 0 )
					comp = compareInt( o1.getItemDamage(), o2.getItemDamage() );
				return comp;
			}

			int comp = -compareInt( o1.getItem().itemID, o2.getItem().itemID );
			if ( comp == 0 )
				comp = -compareInt( o1.getItemDamage(), o2.getItemDamage() );
			return comp;
		}
	};

	public static Comparator<IAEItemStack> ConfigBased_SortByMod = new Comparator<IAEItemStack>() {

		@Override
		public int compare(IAEItemStack o1, IAEItemStack o2)
		{
			if ( Direction == SortDir.ASCENDING )
				return Platform.getMod( o1.getItemStack() ).compareTo( Platform.getMod( o2.getItemStack() ) );
			return Platform.getMod( o2.getItemStack() ).compareTo( Platform.getMod( o1.getItemStack() ) );
		}
	};

	public static Comparator<IAEItemStack> ConfigBased_SortBySize = new Comparator<IAEItemStack>() {

		@Override
		public int compare(IAEItemStack o1, IAEItemStack o2)
		{
			if ( Direction == SortDir.ASCENDING )
				return compareLong( o2.getStackSize(), o1.getStackSize() );
			return compareLong( o1.getStackSize(), o2.getStackSize() );
		}
	};

}
