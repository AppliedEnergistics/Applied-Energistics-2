/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.util;

import java.util.Comparator;

import net.minecraft.item.Item;

import appeng.api.config.SortDir;
import appeng.api.storage.data.IAEItemStack;
import appeng.core.AppEng;
import appeng.integration.IntegrationType;
import appeng.integration.abstraction.IInvTweaks;
import appeng.util.item.AEItemStack;

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
	
	public static final Comparator<IAEItemStack> ConfigBased_SortByID = new Comparator<IAEItemStack>() {

		@Override
		public int compare(IAEItemStack o1, IAEItemStack o2)
		{
			int idx = Item.getIdFromItem(o1.getItem())-Item.getIdFromItem(o2.getItem());
			return Direction == SortDir.ASCENDING ? idx : -idx;
		}
	};

	public static final Comparator<IAEItemStack> ConfigBased_SortByName = new Comparator<IAEItemStack>() {

		@Override
		public int compare(IAEItemStack o1, IAEItemStack o2)
		{
			if ( Direction == SortDir.ASCENDING )
				return Platform.getItemDisplayName( o1 ).compareToIgnoreCase( Platform.getItemDisplayName( o2 ) );
			return Platform.getItemDisplayName( o2 ).compareToIgnoreCase( Platform.getItemDisplayName( o1 ) );
		}
	};

	public static final Comparator<IAEItemStack> ConfigBased_SortByMod = new Comparator<IAEItemStack>() {

		@Override
		public int compare(IAEItemStack o1, IAEItemStack o2)
		{
			AEItemStack op1 = (AEItemStack) o1;
			AEItemStack op2 = (AEItemStack) o2;

			if ( Direction == SortDir.ASCENDING )
				return secondarySort( op2.getModID().compareToIgnoreCase( op1.getModID() ), o1, o2 );
			return secondarySort( op1.getModID().compareToIgnoreCase( op2.getModID() ), o2, o1 );
		}

		private int secondarySort(int compareToIgnoreCase, IAEItemStack o1, IAEItemStack o2)
		{
			if ( compareToIgnoreCase == 0 )
				return Platform.getItemDisplayName( o2 ).compareToIgnoreCase( Platform.getItemDisplayName( o1 ) );

			return compareToIgnoreCase;
		}
	};

	public static final Comparator<IAEItemStack> ConfigBased_SortBySize = new Comparator<IAEItemStack>() {

		@Override
		public int compare(IAEItemStack o1, IAEItemStack o2)
		{
			if ( Direction == SortDir.ASCENDING )
				return compareLong( o2.getStackSize(), o1.getStackSize() );
			return compareLong( o1.getStackSize(), o2.getStackSize() );
		}
	};

	public static final Comparator<IAEItemStack> ConfigBased_SortByInvTweaks = new Comparator<IAEItemStack>() {

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
