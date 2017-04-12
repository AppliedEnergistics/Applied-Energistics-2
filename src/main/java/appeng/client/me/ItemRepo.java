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

package appeng.client.me;


import appeng.api.AEApi;
import appeng.api.config.*;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.client.gui.widgets.IScrollSource;
import appeng.client.gui.widgets.ISortSource;
import appeng.core.AEConfig;
import appeng.items.storage.ItemViewCell;
import appeng.util.ItemSorters;
import appeng.util.Platform;
import appeng.util.prioitylist.IPartitionList;
import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Pattern;


public class ItemRepo
{

	private final IItemList<IAEItemStack> list = AEApi.instance().storage().createItemList();
	private final ArrayList<IAEItemStack> view = new ArrayList<IAEItemStack>();
	private final ArrayList<ItemStack> dsp = new ArrayList<ItemStack>();
	private final IScrollSource src;
	private final ISortSource sortSrc;

	private int rowSize = 9;

	private String searchString = "";
	private IPartitionList<IAEItemStack> myPartitionList;
	private String innerSearch = "";
	private String NEIWord = null;
	private boolean hasPower;

	public ItemRepo( final IScrollSource src, final ISortSource sortSrc )
	{
		this.src = src;
		this.sortSrc = sortSrc;
	}

	public IAEItemStack getReferenceItem( int idx )
	{
		idx += this.src.getCurrentScroll() * this.rowSize;

		if( idx >= this.view.size() )
		{
			return null;
		}
		return this.view.get( idx );
	}

	public ItemStack getItem( int idx )
	{
		idx += this.src.getCurrentScroll() * this.rowSize;

		if( idx >= this.dsp.size() )
		{
			return null;
		}
		return this.dsp.get( idx );
	}

	void setSearch( final String search )
	{
		this.searchString = search == null ? "" : search;
	}

	public void postUpdate( final IAEItemStack is )
	{
		final IAEItemStack st = this.list.findPrecise( is );

		if( st != null )
		{
			st.reset();
			st.add( is );
		}
		else
		{
			this.list.add( is );
		}
	}

	public void setViewCell( final ItemStack[] list )
	{
		this.myPartitionList = ItemViewCell.createFilter( list );
		this.updateView();
	}

	public void updateView()
	{
		this.view.clear();
		this.dsp.clear();

		this.view.ensureCapacity( this.list.size() );
		this.dsp.ensureCapacity( this.list.size() );

		final Enum viewMode = this.sortSrc.getSortDisplay();
		final Enum searchMode = AEConfig.instance.settings.getSetting( Settings.SEARCH_MODE );
		if( searchMode == SearchBoxMode.NEI_AUTOSEARCH || searchMode == SearchBoxMode.NEI_MANUAL_SEARCH )
		{
			this.updateNEI( this.searchString );
		}

		this.innerSearch = this.searchString;
		final boolean terminalSearchToolTips = AEConfig.instance.settings.getSetting( Settings.SEARCH_TOOLTIPS ) != YesNo.NO;
		// boolean terminalSearchMods = Configuration.INSTANCE.settings.getSetting( Settings.SEARCH_MODS ) != YesNo.NO;

		boolean searchMod = false;
		if( this.innerSearch.startsWith( "@" ) )
		{
			searchMod = true;
			this.innerSearch = this.innerSearch.substring( 1 );
		}

		Pattern m = null;
		try
		{
			m = Pattern.compile( this.innerSearch.toLowerCase(), Pattern.CASE_INSENSITIVE );
		}
		catch( final Throwable ignore )
		{
			try
			{
				m = Pattern.compile( Pattern.quote( this.innerSearch.toLowerCase() ), Pattern.CASE_INSENSITIVE );
			}
			catch( final Throwable __ )
			{
				return;
			}
		}

		boolean notDone = false;
		for( IAEItemStack is : this.list )
		{
			if( this.myPartitionList != null )
			{
				if( !this.myPartitionList.isListed( is ) )
				{
					continue;
				}
			}

			if( viewMode == ViewItems.CRAFTABLE && !is.isCraftable() )
			{
				continue;
			}

			if( viewMode == ViewItems.CRAFTABLE )
			{
				is = is.copy();
				is.setStackSize( 0 );
			}

			if( viewMode == ViewItems.STORED && is.getStackSize() == 0 )
			{
				continue;
			}

			final String dspName = searchMod ? Platform.getModId( is ) : Platform.getItemDisplayName( is );
			notDone = true;

			if( m.matcher( dspName.toLowerCase() ).find() )
			{
				this.view.add( is );
				notDone = false;
			}

			if( terminalSearchToolTips && notDone )
			{
				for( final Object lp : Platform.getTooltip( is ) )
				{
					if( lp instanceof String && m.matcher( (CharSequence) lp ).find() )
					{
						this.view.add( is );
						notDone = false;
						break;
					}
				}
			}

			/*
			 * if ( terminalSearchMods && notDone ) { if ( m.matcher( Platform.getMod( is.getItemStack() ) ).find() ) {
			 * view.add( is ); notDone = false; } }
			 */
		}

		final Enum SortBy = this.sortSrc.getSortBy();
		final Enum SortDir = this.sortSrc.getSortDir();

		ItemSorters.setDirection( (appeng.api.config.SortDir) SortDir );
		ItemSorters.init();

		if( SortBy == SortOrder.MOD )
		{
			Collections.sort( this.view, ItemSorters.CONFIG_BASED_SORT_BY_MOD );
		}
		else if( SortBy == SortOrder.AMOUNT )
		{
			Collections.sort( this.view, ItemSorters.CONFIG_BASED_SORT_BY_SIZE );
		}
		else if( SortBy == SortOrder.INVTWEAKS )
		{
			Collections.sort( this.view, ItemSorters.CONFIG_BASED_SORT_BY_INV_TWEAKS );
		}
		else
		{
			Collections.sort( this.view, ItemSorters.CONFIG_BASED_SORT_BY_NAME );
		}

		for( final IAEItemStack is : this.view )
		{
			this.dsp.add( is.getItemStack() );
		}
	}

	private void updateNEI( final String filter )
	{
		try
		{
			if( this.NEIWord == null || !this.NEIWord.equals( filter ) )
			{
				final Class c = ReflectionHelper.getClass( this.getClass().getClassLoader(), "codechicken.nei.LayoutManager" );
				final Field fldSearchField = c.getField( "searchField" );
				final Object searchField = fldSearchField.get( c );

				final Method a = searchField.getClass().getMethod( "setText", String.class );
				final Method b = searchField.getClass().getMethod( "onTextChange", String.class );

				this.NEIWord = filter;
				a.invoke( searchField, filter );
				b.invoke( searchField, "" );
			}
		}
		catch( final Throwable ignore )
		{

		}
	}

	public int size()
	{
		return this.view.size();
	}

	public void clear()
	{
		this.list.resetStatus();
	}

	public boolean hasPower()
	{
		return this.hasPower;
	}

	public void setPower( final boolean hasPower )
	{
		this.hasPower = hasPower;
	}

	public int getRowSize()
	{
		return this.rowSize;
	}

	public void setRowSize( final int rowSize )
	{
		this.rowSize = rowSize;
	}

	public String getSearchString()
	{
		return this.searchString;
	}

	public void setSearchString( @Nonnull final String searchString )
	{
		this.searchString = searchString;
	}
}
