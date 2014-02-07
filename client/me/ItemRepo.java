package appeng.client.me;

import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Pattern;

import net.minecraft.item.ItemStack;
import appeng.api.config.Settings;
import appeng.api.config.SortOrder;
import appeng.api.config.YesNo;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.client.gui.widgets.IScrollSource;
import appeng.client.gui.widgets.ISortSource;
import appeng.core.Configuration;
import appeng.util.ItemSorters;
import appeng.util.Platform;
import appeng.util.item.ItemList;

public class ItemRepo
{

	final private IItemList<IAEItemStack> list = new ItemList();
	final private ArrayList<IAEItemStack> view = new ArrayList();
	final private ArrayList<ItemStack> dsp = new ArrayList();
	final private IScrollSource src;
	final private ISortSource sortSrc;

	public int rowSize = 9;

	public String searchString = "";

	public ItemRepo(IScrollSource src, ISortSource sortSrc) {
		this.src = src;
		this.sortSrc = sortSrc;
	}

	public IAEItemStack getRefrenceItem(int idx)
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

	public void updateView()
	{
		view.clear();
		dsp.clear();

		view.ensureCapacity( list.size() );
		dsp.ensureCapacity( list.size() );

		boolean terminalSearchToolTips = Configuration.instance.settings.getSetting( Settings.SEARCH_TOOLTIPS ) != YesNo.NO;
		// boolean terminalSearchMods = Configuration.instance.settings.getSetting( Settings.SEARCH_MODS ) != YesNo.NO;

		Pattern m = null;
		try
		{
			m = Pattern.compile( searchString.toLowerCase(), Pattern.CASE_INSENSITIVE );
		}
		catch (Throwable _)
		{
			try
			{
				m = Pattern.compile( Pattern.quote( searchString.toLowerCase() ), Pattern.CASE_INSENSITIVE );
			}
			catch (Throwable __)
			{
				return;
			}
		}

		boolean notDone = false;
		for (IAEItemStack is : list)
		{
			String dspName = Platform.getItemDisplayName( is );
			notDone = true;

			if ( m.matcher( dspName ).find() )
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

		if ( SortBy == SortOrder.AMOUNT )
			Collections.sort( view, ItemSorters.ConfigBased_SortBySize );
		// else if ( SortBy == SortOrder.MOD )
		// Collections.sort( view, ItemSorters.ConfigBased_SortByMod );
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
}
