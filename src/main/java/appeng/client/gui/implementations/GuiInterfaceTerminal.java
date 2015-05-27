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

package appeng.client.gui.implementations;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import com.google.common.collect.HashMultimap;

import org.lwjgl.opengl.GL11;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import appeng.api.AEApi;
import appeng.client.gui.AEBaseGui;
import appeng.client.gui.widgets.GuiScrollbar;
import appeng.client.gui.widgets.MEGuiTextField;
import appeng.client.me.ClientDCInternalInv;
import appeng.client.me.SlotDisconnected;
import appeng.container.implementations.ContainerInterfaceTerminal;
import appeng.core.localization.GuiText;
import appeng.parts.reporting.PartInterfaceTerminal;
import appeng.util.Platform;


public class GuiInterfaceTerminal extends AEBaseGui
{

	private static final int LINES_ON_PAGE = 6;

	// TODO: copied from GuiMEMonitorable. It looks not changed, maybe unneeded?
	final int offsetX = 9;

	private final HashMap<Long, ClientDCInternalInv> byId = new HashMap<Long, ClientDCInternalInv>();
	private final HashMultimap<String, ClientDCInternalInv> byName = HashMultimap.create();
	private final ArrayList<String> names = new ArrayList<String>();
	private final ArrayList<Object> lines = new ArrayList<Object>();

	private final Map<String, Set<Object>> cachedSearches = new WeakHashMap<String, Set<Object>>();

	private boolean refreshList = false;
	private MEGuiTextField searchField;

	public GuiInterfaceTerminal( InventoryPlayer inventoryPlayer, PartInterfaceTerminal te )
	{
		super( new ContainerInterfaceTerminal( inventoryPlayer, te ) );
		this.myScrollBar = new GuiScrollbar();
		this.xSize = 195;
		this.ySize = 222;
	}

	@Override
	public void initGui()
	{
		super.initGui();

		this.myScrollBar.setLeft( 175 );
		this.myScrollBar.setHeight( 106 );
		this.myScrollBar.setTop( 18 );

		this.searchField = new MEGuiTextField( this.fontRendererObj, this.guiLeft + Math.max( 104, this.offsetX ), this.guiTop + 4, 65, 12 );
		this.searchField.setEnableBackgroundDrawing( false );
		this.searchField.setMaxStringLength( 25 );
		this.searchField.setTextColor( 0xFFFFFF );
		this.searchField.setVisible( true );
		this.searchField.setFocused( true );
	}

	@Override
	public void drawFG( int offsetX, int offsetY, int mouseX, int mouseY )
	{
		this.fontRendererObj.drawString( this.getGuiDisplayName( GuiText.InterfaceTerminal.getLocal() ), 8, 6, 4210752 );
		this.fontRendererObj.drawString( GuiText.inventory.getLocal(), 8, this.ySize - 96 + 3, 4210752 );

		int offset = 17;
		int ex = this.myScrollBar.getCurrentScroll();

		Iterator<Object> o = this.inventorySlots.inventorySlots.iterator();
		while( o.hasNext() )
		{
			if( o.next() instanceof SlotDisconnected )
			{
				o.remove();
			}
		}

		for( int x = 0; x < LINES_ON_PAGE && ex + x < this.lines.size(); x++ )
		{
			Object lineObj = this.lines.get( ex + x );
			if( lineObj instanceof ClientDCInternalInv )
			{
				ClientDCInternalInv inv = (ClientDCInternalInv) lineObj;
				for( int z = 0; z < inv.inv.getSizeInventory(); z++ )
				{
					this.inventorySlots.inventorySlots.add( new SlotDisconnected( inv, z, z * 18 + 8, 1 + offset ) );
				}
			}
			else if( lineObj instanceof String )
			{
				String name = (String) lineObj;
				int rows = this.byName.get( name ).size();
				if( rows > 1 )
				{
					name = name + " (" + rows + ')';
				}

				while( name.length() > 2 && this.fontRendererObj.getStringWidth( name ) > 155 )
				{
					name = name.substring( 0, name.length() - 1 );
				}

				this.fontRendererObj.drawString( name, 10, 6 + offset, 4210752 );
			}
			offset += 18;
		}
	}

	@Override
	protected void mouseClicked( int xCoord, int yCoord, int btn )
	{
		this.searchField.mouseClicked( xCoord, yCoord, btn );

		if( btn == 1 && this.searchField.isMouseIn( xCoord, yCoord ) )
		{
			this.searchField.setText( "" );
			this.refreshList();
		}

		super.mouseClicked( xCoord, yCoord, btn );
	}

	@Override
	public void drawBG( int offsetX, int offsetY, int mouseX, int mouseY )
	{
		this.bindTexture( "guis/interfaceterminal.png" );
		this.drawTexturedModalRect( offsetX, offsetY, 0, 0, this.xSize, this.ySize );

		int offset = 17;
		int ex = this.myScrollBar.getCurrentScroll();

		for( int x = 0; x < LINES_ON_PAGE && ex + x < this.lines.size(); x++ )
		{
			Object lineObj = this.lines.get( ex + x );
			if( lineObj instanceof ClientDCInternalInv )
			{
				ClientDCInternalInv inv = (ClientDCInternalInv) lineObj;

				GL11.glColor4f( 1, 1, 1, 1 );
				int width = inv.inv.getSizeInventory() * 18;
				this.drawTexturedModalRect( offsetX + 7, offsetY + offset, 7, 139, width, 18 );
			}
			offset += 18;
		}

		if( this.searchField != null )
		{
			this.searchField.drawTextBox();
		}
	}

	@Override
	protected void keyTyped( char character, int key )
	{
		if( !this.checkHotbarKeys( key ) )
		{
			if( character == ' ' && this.searchField.getText().length() == 0 )
			{
				return;
			}

			if( this.searchField.textboxKeyTyped( character, key ) )
			{
				this.refreshList();
			}
			else
			{
				super.keyTyped( character, key );
			}
		}
	}

	public void postUpdate( NBTTagCompound in )
	{
		if( in.getBoolean( "clear" ) )
		{
			this.byId.clear();
			this.refreshList = true;
		}

		for( Object oKey : in.func_150296_c() )
		{
			String key = (String) oKey;
			if( key.startsWith( "=" ) )
			{
				try
				{
					long id = Long.parseLong( key.substring( 1 ), Character.MAX_RADIX );
					NBTTagCompound invData = in.getCompoundTag( key );
					ClientDCInternalInv current = this.getById( id, invData.getLong( "sortBy" ), invData.getString( "un" ) );

					for( int x = 0; x < current.inv.getSizeInventory(); x++ )
					{
						String which = Integer.toString( x );
						if( invData.hasKey( which ) )
						{
							current.inv.setInventorySlotContents( x, ItemStack.loadItemStackFromNBT( invData.getCompoundTag( which ) ) );
						}
					}
				}
				catch( NumberFormatException ignored )
				{
				}
			}
		}

		if( this.refreshList )
		{
			this.refreshList = false;
			// invalid caches on refresh
			this.cachedSearches.clear();
			this.refreshList();
		}
	}

	/**
	 * Rebuilds the list of interfaces.
	 *
	 * Respects a search term if present (ignores case) and adding only matching patterns.
	 */
	private void refreshList()
	{
		this.byName.clear();

		final String searchFilterLowerCase = this.searchField.getText().toLowerCase();

		final Set<Object> cachedSearch = this.getCacheForSearchTerm( searchFilterLowerCase );
		final boolean rebuild = cachedSearch.isEmpty();

		for( ClientDCInternalInv entry : this.byId.values() )
		{
			// ignore inventory if not doing a full rebuild or cache already marks it as miss.
			if( !rebuild && !cachedSearch.contains( entry ) )
			{
				continue;
			}

			// Shortcut to skip any filter if search term is ""/empty
			boolean found = searchFilterLowerCase.isEmpty();

			// Search if the current inventory holds a pattern containing the search term.
			if( !found && !searchFilterLowerCase.isEmpty() )
			{
				for( ItemStack itemStack : entry.inv )
				{
					found = this.itemStackMatchesSearchTerm( itemStack, searchFilterLowerCase );
					if( found )
					{
						break;
					}
				}
			}

			// if found, filter skipped or machine name matching the search term, add it
			if( found || entry.getName().toLowerCase().contains( searchFilterLowerCase ) )
			{
				this.byName.put( entry.getName(), entry );
				cachedSearch.add( entry );
			}
			else
			{
				cachedSearch.remove( entry );
			}
		}

		this.names.clear();
		this.names.addAll( this.byName.keySet() );

		Collections.sort( this.names );

		this.lines.clear();
		this.lines.ensureCapacity( this.getMaxRows() );

		for( String n : this.names )
		{
			this.lines.add( n );

			ArrayList<ClientDCInternalInv> clientInventories = new ArrayList<ClientDCInternalInv>();
			clientInventories.addAll( this.byName.get( n ) );

			Collections.sort( clientInventories );
			this.lines.addAll( clientInventories );
		}

		this.myScrollBar.setRange( 0, this.lines.size() - LINES_ON_PAGE, 2 );
	}

	private boolean itemStackMatchesSearchTerm( ItemStack itemStack, String searchTerm )
	{
		if( itemStack == null )
		{
			return false;
		}

		NBTTagCompound encodedValue = itemStack.getTagCompound();

		if( encodedValue == null )
		{
			return false;
		}

		// Potential later use to filter by input
		// NBTTagList inTag = encodedValue.getTagList( "in", 10 );
		NBTTagList outTag = encodedValue.getTagList( "out", 10 );

		for( int i = 0; i < outTag.tagCount(); i++ )
		{

			ItemStack parsedItemStack = ItemStack.loadItemStackFromNBT( outTag.getCompoundTagAt( i ) );
			if( parsedItemStack != null )
			{
				String displayName = Platform.getItemDisplayName( AEApi.instance().storage().createItemStack( parsedItemStack ) ).toLowerCase();
				if( displayName.contains( searchTerm ) )
				{
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Tries to retrieve a cache for a with search term as keyword.
	 *
	 * If this cache should be empty, it will populate it with an earlier cache if available or at least the cache for
	 * the empty string.
	 *
	 * @param searchTerm the corresponding search
	 *
	 * @return a Set matching a superset of the search term
	 */
	private Set<Object> getCacheForSearchTerm( String searchTerm )
	{
		if( !this.cachedSearches.containsKey( searchTerm ) )
		{
			this.cachedSearches.put( searchTerm, new HashSet<Object>() );
		}

		Set<Object> cache = this.cachedSearches.get( searchTerm );

		if( cache.isEmpty() && searchTerm.length() > 1 )
		{
			cache.addAll( this.getCacheForSearchTerm( searchTerm.substring( 0, searchTerm.length() - 1 ) ) );
			return cache;
		}

		return cache;
	}

	/**
	 * The max amount of unique names and each inv row. Not affected by the filtering.
	 *
	 * @return max amount of unique names and each inv row
	 */
	private int getMaxRows()
	{
		return this.names.size() + this.byId.size();
	}

	private ClientDCInternalInv getById( long id, long sortBy, String string )
	{
		ClientDCInternalInv o = this.byId.get( id );

		if( o == null )
		{
			this.byId.put( id, o = new ClientDCInternalInv( 9, id, sortBy, string ) );
			this.refreshList = true;
		}

		return o;
	}
}
