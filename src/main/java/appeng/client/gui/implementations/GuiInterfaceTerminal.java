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


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import com.google.common.collect.HashMultimap;

import org.lwjgl.input.Mouse;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import appeng.api.AEApi;
import appeng.api.config.SearchBoxMode;
import appeng.api.config.Settings;
import appeng.api.config.TerminalStyle;
import appeng.client.gui.AEBaseGui;
import appeng.client.gui.widgets.GuiImgButton;
import appeng.client.gui.widgets.GuiScrollbar;
import appeng.client.gui.widgets.MEGuiTextField;
import appeng.client.me.ClientDCInternalInv;
import appeng.client.me.SlotDisconnected;
import appeng.container.implementations.ContainerInterfaceTerminal;
import appeng.container.slot.AppEngSlot;
import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketValueConfig;
import appeng.integration.Integrations;
import appeng.parts.reporting.PartInterfaceTerminal;
import appeng.util.Platform;


public class GuiInterfaceTerminal extends AEBaseGui
{
	private static final int SECTION_HEIGHT = 18;

	// TODO: copied from GuiMEMonitorable. It looks not changed, maybe unneeded?
	private final int offsetX = 9;

	private final HashMap<Long, ClientDCInternalInv> byId = new HashMap<Long, ClientDCInternalInv>();
	private final HashMultimap<String, ClientDCInternalInv> byName = HashMultimap.create();
	private final ArrayList<String> names = new ArrayList<String>();
	private final ArrayList<Object> lines = new ArrayList<Object>();

	private final Map<String, Set<Object>> cachedSearches = new WeakHashMap<String, Set<Object>>();

	private boolean refreshList = false;
	private MEGuiTextField searchField;

	private int rows = 6;
	private GuiImgButton searchBoxSettings;
	private GuiImgButton terminalStyleBox;

	public GuiInterfaceTerminal( final InventoryPlayer inventoryPlayer, final PartInterfaceTerminal te )
	{
		super( new ContainerInterfaceTerminal( inventoryPlayer, te ) );

		final GuiScrollbar scrollbar = new GuiScrollbar();
		this.setScrollBar( scrollbar );
		this.xSize = 195;
		this.ySize = 222;
	}

	@Override
	protected void actionPerformed( final GuiButton btn ) throws IOException
	{
		super.actionPerformed( btn );

		if( btn instanceof GuiImgButton )
		{
			final boolean backwards = Mouse.isButtonDown( 1 );

			final GuiImgButton iBtn = (GuiImgButton) btn;
			if( iBtn.getSetting() != Settings.ACTIONS )
			{
				final Enum cv = iBtn.getCurrentValue();
				final Enum next = Platform.rotateEnum( cv, backwards, iBtn.getSetting().getPossibleValues() );

				if( btn == this.terminalStyleBox || btn == this.searchBoxSettings )
				{
					AEConfig.instance().getConfigManager().putSetting( iBtn.getSetting(), next );
				}
				else
				{
					try
					{
						NetworkHandler.instance().sendToServer( new PacketValueConfig( iBtn.getSetting().name(), next.name() ) );
					}
					catch( final IOException e )
					{
						AELog.debug( e );
					}
				}

				iBtn.set( next );

				if( next.getClass() == SearchBoxMode.class || next.getClass() == TerminalStyle.class )
				{
					this.reinitalize();
				}
			}
		}
	}

	private void reinitalize()
	{
		this.buttonList.clear();
		this.initGui();
		this.refreshList();
	}

	@Override
	public void initGui()
	{
		final int staticSpace = 18 + 97;

		calculateRows( staticSpace );

		super.initGui();

		this.ySize = staticSpace + this.rows * SECTION_HEIGHT;
		final int unusedSpace = this.height - this.ySize;
		this.guiTop = (int) Math.floor( unusedSpace / ( unusedSpace < 0 ? 3.8f : 2.0f ) );
		int offset = this.guiTop + 8;

		this.getScrollBar().setLeft( 175 );
		this.getScrollBar().setHeight( this.rows * SECTION_HEIGHT - 2 );
		this.getScrollBar().setTop( 18 );
		this.getScrollBar().setRange( 0, this.lines.size() - rows, 2 );

		this.searchBoxSettings = new GuiImgButton( this.guiLeft - 18, offset, Settings.SEARCH_MODE, AEConfig.instance().getConfigManager().getSetting( Settings.SEARCH_MODE ) );
		this.buttonList.add( this.searchBoxSettings );
		offset += 20;

		this.terminalStyleBox = new GuiImgButton( this.guiLeft - 18, offset, Settings.TERMINAL_STYLE, AEConfig.instance().getConfigManager().getSetting( Settings.TERMINAL_STYLE ) );
		this.buttonList.add( this.terminalStyleBox );

		this.searchField = new MEGuiTextField( this.fontRendererObj, this.guiLeft + Math.max( 104, this.offsetX ), this.guiTop + 4, 65, 12 );
		this.searchField.setEnableBackgroundDrawing( false );
		this.searchField.setMaxStringLength( 25 );
		this.searchField.setTextColor( 0xFFFFFF );
		this.searchField.setVisible( true );
		this.searchField.setFocused( true );

		final Enum setting = AEConfig.instance().getConfigManager().getSetting( Settings.SEARCH_MODE );
		this.searchField.setFocused( SearchBoxMode.AUTOSEARCH == setting || SearchBoxMode.JEI_AUTOSEARCH == setting );

		for( final Object s : this.inventorySlots.inventorySlots )
		{
			if( s instanceof AppEngSlot && ( (Slot) s ).xDisplayPosition < 197 )
			{
				this.repositionSlot( (AppEngSlot) s );
			}
		}
	}

	private void calculateRows( final int height )
	{
		final int maxRows = AEConfig.instance().getConfigManager().getSetting( Settings.TERMINAL_STYLE ) == TerminalStyle.SMALL ? 6 : Integer.MAX_VALUE;

		final double extraSpace = (double) this.height - height;

		this.rows = (int) Math.floor( extraSpace / SECTION_HEIGHT );
		if( this.rows > maxRows )
		{
			this.rows = maxRows;
		}

		if( this.rows < 6 )
		{
			this.rows = 6;
		}
	}

	@Override
	public void drawFG( final int offsetX, final int offsetY, final int mouseX, final int mouseY )
	{
		this.fontRendererObj.drawString( this.getGuiDisplayName( GuiText.InterfaceTerminal.getLocal() ), 8, 6, 4210752 );
		this.fontRendererObj.drawString( GuiText.inventory.getLocal(), 8, this.ySize - 96 + 3, 4210752 );

		final int ex = this.getScrollBar().getCurrentScroll();

		final Iterator<Slot> o = this.inventorySlots.inventorySlots.iterator();
		while( o.hasNext() )
		{
			if( o.next() instanceof SlotDisconnected )
			{
				o.remove();
			}
		}

		int offset = 17;
		for( int x = 0; x < rows && ex + x < this.lines.size(); x++ )
		{
			final Object lineObj = this.lines.get( ex + x );
			if( lineObj instanceof ClientDCInternalInv )
			{
				final ClientDCInternalInv inv = (ClientDCInternalInv) lineObj;
				for( int z = 0; z < inv.getInventory().getSizeInventory(); z++ )
				{
					this.inventorySlots.inventorySlots.add( new SlotDisconnected( inv, z, z * 18 + 8, 1 + offset ) );
				}
			}
			else if( lineObj instanceof String )
			{
				String name = (String) lineObj;
				final int rows = this.byName.get( name ).size();
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
			offset += SECTION_HEIGHT;
		}
	}

	@Override
	protected void mouseClicked( final int xCoord, final int yCoord, final int btn ) throws IOException
	{
		final Enum searchMode = AEConfig.instance().getConfigManager().getSetting( Settings.SEARCH_MODE );

		if( searchMode != SearchBoxMode.AUTOSEARCH && searchMode != SearchBoxMode.JEI_AUTOSEARCH )
		{
			this.searchField.mouseClicked( xCoord, yCoord, btn );
		}
		
		if( btn == 1 && this.searchField.isMouseIn( xCoord, yCoord ) )
		{
			this.searchField.setText( "" );
			this.refreshList();
		}

		super.mouseClicked( xCoord, yCoord, btn );
	}

	@Override
	public void drawBG( final int offsetX, final int offsetY, final int mouseX, final int mouseY )
	{
		this.bindTexture( "guis/interfaceterminal.png" );
		this.drawTexturedModalRect( offsetX, offsetY, 0, 0, this.xSize, 18 );

		for( int x = 0; x < rows; x++ )
		{
			this.drawTexturedModalRect( offsetX, offsetY + 18 + x * SECTION_HEIGHT, 0, 18, this.xSize, SECTION_HEIGHT );
		}

		this.drawTexturedModalRect( offsetX, offsetY + 18 + rows * SECTION_HEIGHT - 2, 0, 124, this.xSize, 96 );

		int offset = 17;
		final int ex = this.getScrollBar().getCurrentScroll();

		for( int x = 0; x < rows && ex + x < this.lines.size(); x++ )
		{
			final Object lineObj = this.lines.get( ex + x );
			if( lineObj instanceof ClientDCInternalInv )
			{
				final ClientDCInternalInv inv = (ClientDCInternalInv) lineObj;

				GlStateManager.color( 1, 1, 1, 1 );
				final int width = inv.getInventory().getSizeInventory() * 18;
				this.drawTexturedModalRect( offsetX + 7, offsetY + offset, 7, 139, width, SECTION_HEIGHT );
			}
			offset += SECTION_HEIGHT;
		}

		if( this.searchField != null )
		{
			this.searchField.drawTextBox();
		}
	}

	private void repositionSlot( final AppEngSlot s )
	{
		s.yDisplayPosition = s.getY() + this.ySize - 78 - 5;
	}

	@Override
	protected void keyTyped( final char character, final int key ) throws IOException
	{
		if( !this.checkHotbarKeys( key ) )
		{
			if( character == ' ' && this.searchField.getText().isEmpty() )
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

	public void postUpdate( final NBTTagCompound in )
	{
		if( in.getBoolean( "clear" ) )
		{
			this.byId.clear();
			this.refreshList = true;
		}

		for( final Object oKey : in.getKeySet() )
		{
			final String key = (String) oKey;
			if( key.startsWith( "=" ) )
			{
				try
				{
					final long id = Long.parseLong( key.substring( 1 ), Character.MAX_RADIX );
					final NBTTagCompound invData = in.getCompoundTag( key );
					final ClientDCInternalInv current = this.getById( id, invData.getLong( "sortBy" ), invData.getString( "un" ) );

					for( int x = 0; x < current.getInventory().getSizeInventory(); x++ )
					{
						final String which = Integer.toString( x );
						if( invData.hasKey( which ) )
						{
							current.getInventory().setInventorySlotContents( x, ItemStack.loadItemStackFromNBT( invData.getCompoundTag( which ) ) );
						}
					}
				}
				catch( final NumberFormatException ignored )
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

		// Since someone searching in an interface terminal will rarely find it useful to sync with JEI, 
		//  should we maybe just ignore this setting? 
		final Enum searchMode = AEConfig.instance().getConfigManager().getSetting( Settings.SEARCH_MODE );
		if( searchMode == SearchBoxMode.JEI_AUTOSEARCH || searchMode == SearchBoxMode.JEI_MANUAL_SEARCH )
		{
			Integrations.jei().setSearchText( this.searchField.getText() );
		}

		final String searchFilterLowerCase = this.searchField.getText().toLowerCase();

		final Set<Object> cachedSearch = this.getCacheForSearchTerm( searchFilterLowerCase );
		final boolean rebuild = cachedSearch.isEmpty();

		for( final ClientDCInternalInv entry : this.byId.values() )
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
				for( final ItemStack itemStack : entry.getInventory() )
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

		for( final String n : this.names )
		{
			this.lines.add( n );

			final ArrayList<ClientDCInternalInv> clientInventories = new ArrayList<ClientDCInternalInv>();
			clientInventories.addAll( this.byName.get( n ) );

			Collections.sort( clientInventories );
			this.lines.addAll( clientInventories );
		}

		this.getScrollBar().setRange( 0, this.lines.size() - rows, 2 );
	}

	private boolean itemStackMatchesSearchTerm( final ItemStack itemStack, final String searchTerm )
	{
		if( itemStack == null )
		{
			return false;
		}

		final NBTTagCompound encodedValue = itemStack.getTagCompound();

		if( encodedValue == null )
		{
			return false;
		}

		// Potential later use to filter by input
		// NBTTagList inTag = encodedValue.getTagList( "in", 10 );
		final NBTTagList outTag = encodedValue.getTagList( "out", 10 );

		for( int i = 0; i < outTag.tagCount(); i++ )
		{

			final ItemStack parsedItemStack = ItemStack.loadItemStackFromNBT( outTag.getCompoundTagAt( i ) );
			if( parsedItemStack != null )
			{
				final String displayName = Platform.getItemDisplayName( AEApi.instance().storage().createItemStack( parsedItemStack ) ).toLowerCase();
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
	private Set<Object> getCacheForSearchTerm( final String searchTerm )
	{
		if( !this.cachedSearches.containsKey( searchTerm ) )
		{
			this.cachedSearches.put( searchTerm, new HashSet<Object>() );
		}

		final Set<Object> cache = this.cachedSearches.get( searchTerm );

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

	private ClientDCInternalInv getById( final long id, final long sortBy, final String string )
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
