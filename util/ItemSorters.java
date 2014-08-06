package appeng.util;

import java.util.Comparator;

import appeng.api.config.SortDir;
import appeng.api.storage.data.IAEItemStack;
import appeng.core.AppEng;
import appeng.integration.IntegrationType;
import appeng.integration.abstraction.IInvTweaks;

public class ItemSorters
{

	public static SortDir Direction = SortDir.ASCENDING;
	private static IInvTweaks api;

	public static void init()
	{
		if ( api != null )
			return;

		if ( AppEng.instance.isIntegrationEnabled( IntegrationType.InvTweaks ) )
			api = (IInvTweaks) AppEng.instance.getIntegration( IntegrationType.InvTweaks );
		else
			api = null;
	}

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

	public static int compareDouble(double a, double b)
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

	public static Comparator<IAEItemStack> ConfigBased_SortBySize = new Comparator<IAEItemStack>() {

		@Override
		public int compare(IAEItemStack o1, IAEItemStack o2)
		{
			if ( Direction == SortDir.ASCENDING )
				return compareLong( o2.getStackSize(), o1.getStackSize() );
			return compareLong( o1.getStackSize(), o2.getStackSize() );
		}
	};
	public static Comparator<IAEItemStack> ConfigBased_SortByInvTweaks = new Comparator<IAEItemStack>() {

		@Override
		public int compare(IAEItemStack o1, IAEItemStack o2)
		{
			if ( api == null )
				return ConfigBased_SortByName.compare( o1, o2 );

			int cmp = api.compareItems( o1.getItemStack(), o2.getItemStack() );

			if ( Direction == SortDir.ASCENDING )
				return cmp;
			return -cmp;
		}
	};

}
