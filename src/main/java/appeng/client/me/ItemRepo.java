package appeng.client.me;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Pattern;

import net.minecraft.item.ItemStack;
import appeng.api.AEApi;
import appeng.api.config.SearchBoxMode;
import appeng.api.config.Settings;
import appeng.api.config.SortOrder;
import appeng.api.config.ViewItems;
import appeng.api.config.YesNo;
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

public class ItemRepo
{

	final private IItemList<IAEItemStack> list = AEApi.instance().storage().createItemList();
	final private ArrayList<IAEItemStack> view = new ArrayList<IAEItemStack>();
	final private ArrayList<ItemStack> dsp = new ArrayList<ItemStack>();
	final private IScrollSource src;
	final private ISortSource sortSrc;

	public int rowSize = 9;

	public String searchString = "";
	private String innerSearch = "";

	public ItemRepo(IScrollSource src, ISortSource sortSrc)
	{
		this.src = src;
		this.sortSrc = sortSrc;
	}

	public IAEItemStack getReferenceItem(int idx)
	{
		idx += src.getCurrentScroll() * rowSize;

		if ( idx >= view.size() )
			return null;
		return view.get( idx );
	}

	public ItemStack getItem(int idx)
	{
		idx += src.getCurrentScroll() * rowSize;

		if ( idx >= dsp.size() )
			return null;
		return dsp.get( idx );
	}

	void setSearch(String search)
	{
		searchString = search == null ? "" : search;
	}

	public void postUpdate(IAEItemStack is)
	{
		IAEItemStack st = list.findPrecise( is );

		if ( st != null )
		{
			st.reset();
			st.add( is );
		}
		else
			list.add( is );
	}

	IPartitionList<IAEItemStack> myPartitionList;

	public void setViewCell(ItemStack[] list)
	{
		myPartitionList = ItemViewCell.createFilter( list );
		updateView();
	}

	private String NEIWord = null;

	private void updateNEI(String filter)
	{
		try
		{
			if ( NEIWord == null || !NEIWord.equals( filter ) )
			{
				Class c = ReflectionHelper.getClass( getClass().getClassLoader(), "codechicken.nei.LayoutManager" );
				Field fldSearchField = c.getField( "searchField" );
				Object searchField = fldSearchField.get( c );

				Method a = searchField.getClass().getMethod( "setText", String.class );
				Method b = searchField.getClass().getMethod( "onTextChange", String.class );

				NEIWord = filter;
				a.invoke( searchField, filter );
				b.invoke( searchField, "" );
			}
		}
		catch (Throwable ignore)
		{

		}
	}

	public void updateView()
	{
		view.clear();
		dsp.clear();

		view.ensureCapacity( list.size() );
		dsp.ensureCapacity( list.size() );

		Enum viewMode = sortSrc.getSortDisplay();
		Enum searchMode = AEConfig.instance.settings.getSetting( Settings.SEARCH_MODE );
		if ( searchMode == SearchBoxMode.NEI_AUTOSEARCH || searchMode == SearchBoxMode.NEI_MANUAL_SEARCH )
			updateNEI( searchString );

		innerSearch = searchString;
		boolean terminalSearchToolTips = AEConfig.instance.settings.getSetting( Settings.SEARCH_TOOLTIPS ) != YesNo.NO;
		// boolean terminalSearchMods = Configuration.instance.settings.getSetting( Settings.SEARCH_MODS ) != YesNo.NO;

		boolean searchMod = false;
		if ( innerSearch.startsWith( "@" ) )
		{
			searchMod = true;
			innerSearch = innerSearch.substring( 1 );
		}

		Pattern m = null;
		try
		{
			m = Pattern.compile( innerSearch.toLowerCase(), Pattern.CASE_INSENSITIVE );
		}
		catch (Throwable ignore)
		{
			try
			{
				m = Pattern.compile( Pattern.quote( innerSearch.toLowerCase() ), Pattern.CASE_INSENSITIVE );
			}
			catch (Throwable __)
			{
				return;
			}
		}

		boolean notDone = false;
		for (IAEItemStack is : list)
		{
			if ( myPartitionList != null )
			{
				if ( !myPartitionList.isListed( is ) )
					continue;
			}

			if ( viewMode == ViewItems.CRAFTABLE && !is.isCraftable() )
				continue;

			if ( viewMode == ViewItems.CRAFTABLE )
			{
				is = is.copy();
				is.setStackSize( 0 );
			}

			if ( viewMode == ViewItems.STORED && is.getStackSize() == 0 )
				continue;

			String dspName = searchMod ? Platform.getModId( is ) : Platform.getItemDisplayName( is );
			notDone = true;

			if ( m.matcher( dspName.toLowerCase() ).find() )
			{
				view.add( is );
				notDone = false;
			}

			if ( terminalSearchToolTips && notDone )
			{
				for (Object lp : Platform.getTooltip( is ))
					if ( lp instanceof String && m.matcher( (String) lp ).find() )
					{
						view.add( is );
						notDone = false;
						break;
					}
			}

			/*
			 * if ( terminalSearchMods && notDone ) { if ( m.matcher( Platform.getMod( is.getItemStack() ) ).find() ) {
			 * view.add( is ); notDone = false; } }
			 */
		}

		Enum SortBy = sortSrc.getSortBy();
		Enum SortDir = sortSrc.getSortDir();

		ItemSorters.Direction = (appeng.api.config.SortDir) SortDir;
		ItemSorters.init();

		if ( SortBy == SortOrder.MOD )
			Collections.sort( view, ItemSorters.ConfigBased_SortByMod );
		else if ( SortBy == SortOrder.AMOUNT )
			Collections.sort( view, ItemSorters.ConfigBased_SortBySize );
		else if ( SortBy == SortOrder.INVTWEAKS )
			Collections.sort( view, ItemSorters.ConfigBased_SortByInvTweaks );
		else
			Collections.sort( view, ItemSorters.ConfigBased_SortByName );

		for (IAEItemStack is : view)
			dsp.add( is.getItemStack() );
	}

	public int size()
	{
		return view.size();
	}

	public void clear()
	{
		list.resetStatus();
	}

	private boolean hasPower;

	public boolean hasPower()
	{
		return hasPower;
	}

	public void setPower(boolean hasPower)
	{
		this.hasPower = hasPower;
	}

}
