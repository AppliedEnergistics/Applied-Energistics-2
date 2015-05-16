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
import java.util.List;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import appeng.api.config.SearchBoxMode;
import appeng.api.config.Settings;
import appeng.api.config.TerminalStyle;
import appeng.api.implementations.guiobjects.IPortableCell;
import appeng.api.implementations.tiles.IMEChest;
import appeng.api.implementations.tiles.IViewCellStorage;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.client.gui.AEBaseMEGui;
import appeng.client.gui.widgets.GuiImgButton;
import appeng.client.gui.widgets.GuiScrollbar;
import appeng.client.gui.widgets.GuiTabButton;
import appeng.client.gui.widgets.ISortSource;
import appeng.client.gui.widgets.MEGuiTextField;
import appeng.client.me.InternalSlotME;
import appeng.client.me.ItemRepo;
import appeng.container.implementations.ContainerMEMonitorable;
import appeng.container.slot.AppEngSlot;
import appeng.container.slot.SlotCraftingMatrix;
import appeng.container.slot.SlotFakeCraftingMatrix;
import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.core.AppEng;
import appeng.core.localization.GuiText;
import appeng.core.sync.GuiBridge;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketSwitchGuis;
import appeng.core.sync.packets.PacketValueConfig;
import appeng.helpers.WirelessTerminalGuiObject;
import appeng.integration.IntegrationType;
import appeng.parts.reporting.PartTerminal;
import appeng.tile.misc.TileSecurity;
import appeng.util.IConfigManagerHost;
import appeng.util.Platform;


public class GuiMEMonitorable extends AEBaseMEGui implements ISortSource, IConfigManagerHost
{

	public static int CraftingGridOffsetX;
	public static int CraftingGridOffsetY;
	private static String memoryText = "";
	final ItemRepo repo;
	final int offsetX = 9;
	final int lowerTextureOffset = 0;
	final IConfigManager configSrc;
	final boolean viewCell;
	final ItemStack[] myCurrentViewCells = new ItemStack[5];
	final ContainerMEMonitorable monitorableContainer;
	GuiTabButton craftingStatusBtn;
	MEGuiTextField searchField;
	GuiText myName;
	int perRow = 9;
	int reservedSpace = 0;
	boolean customSortOrder = true;
	int rows = 0;
	int maxRows = Integer.MAX_VALUE;
	int standardSize;
	GuiImgButton ViewBox;
	GuiImgButton SortByBox;
	GuiImgButton SortDirBox;
	GuiImgButton searchBoxSettings;
	GuiImgButton terminalStyleBox;

	public GuiMEMonitorable( InventoryPlayer inventoryPlayer, ITerminalHost te )
	{
		this( inventoryPlayer, te, new ContainerMEMonitorable( inventoryPlayer, te ) );
	}

	public GuiMEMonitorable( InventoryPlayer inventoryPlayer, ITerminalHost te, ContainerMEMonitorable c )
	{

		super( c );
		this.myScrollBar = new GuiScrollbar();
		this.repo = new ItemRepo( this.myScrollBar, this );

		this.xSize = 185;
		this.ySize = 204;

		if( te instanceof IViewCellStorage )
		{
			this.xSize += 33;
		}

		this.standardSize = this.xSize;

		this.configSrc = ( (IConfigurableObject) this.inventorySlots ).getConfigManager();
		( this.monitorableContainer = (ContainerMEMonitorable) this.inventorySlots ).gui = this;

		this.viewCell = te instanceof IViewCellStorage;

		if( te instanceof TileSecurity )
		{
			this.myName = GuiText.Security;
		}
		else if( te instanceof WirelessTerminalGuiObject )
		{
			this.myName = GuiText.WirelessTerminal;
		}
		else if( te instanceof IPortableCell )
		{
			this.myName = GuiText.PortableCell;
		}
		else if( te instanceof IMEChest )
		{
			this.myName = GuiText.Chest;
		}
		else if( te instanceof PartTerminal )
		{
			this.myName = GuiText.Terminal;
		}
	}

	public void postUpdate( List<IAEItemStack> list )
	{
		for( IAEItemStack is : list )
		{
			this.repo.postUpdate( is );
		}

		this.repo.updateView();
		this.setScrollBar();
	}

	private void setScrollBar()
	{
		this.myScrollBar.setTop( 18 ).setLeft( 175 ).setHeight( this.rows * 18 - 2 );
		this.myScrollBar.setRange( 0, ( this.repo.size() + this.perRow - 1 ) / this.perRow - this.rows, Math.max( 1, this.rows / 6 ) );
	}

	@Override
	protected void actionPerformed( GuiButton btn )
	{
		if( btn == this.craftingStatusBtn )
		{
			NetworkHandler.instance.sendToServer( new PacketSwitchGuis( GuiBridge.GUI_CRAFTING_STATUS ) );
		}

		if( btn instanceof GuiImgButton )
		{
			boolean backwards = Mouse.isButtonDown( 1 );

			GuiImgButton iBtn = (GuiImgButton) btn;
			if( iBtn.getSetting() != Settings.ACTIONS )
			{
				Enum cv = iBtn.getCurrentValue();
				Enum next = Platform.rotateEnum( cv, backwards, iBtn.getSetting().getPossibleValues() );

				if( btn == this.terminalStyleBox )
				{
					AEConfig.instance.settings.putSetting( iBtn.getSetting(), next );
				}
				else if( btn == this.searchBoxSettings )
				{
					AEConfig.instance.settings.putSetting( iBtn.getSetting(), next );
				}
				else
				{
					try
					{
						NetworkHandler.instance.sendToServer( new PacketValueConfig( iBtn.getSetting().name(), next.name() ) );
					}
					catch( IOException e )
					{
						AELog.error( e );
					}
				}

				iBtn.set( next );

				if( next.getClass() == SearchBoxMode.class || next.getClass() == TerminalStyle.class )
				{
					this.re_init();
				}
			}
		}
	}

	public void re_init()
	{
		this.buttonList.clear();
		this.initGui();
	}

	@Override
	public void initGui()
	{
		Keyboard.enableRepeatEvents( true );

		this.maxRows = this.getMaxRows();
		this.perRow = AEConfig.instance.getConfigManager().getSetting( Settings.TERMINAL_STYLE ) != TerminalStyle.FULL ? 9 : 9 + ( ( this.width - this.standardSize ) / 18 );

		boolean hasNEI = AppEng.instance.isIntegrationEnabled( IntegrationType.NEI );

		int NEI = hasNEI ? 0 : 0;
		int top = hasNEI ? 22 : 0;

		int magicNumber = 114 + 1;
		int extraSpace = this.height - magicNumber - NEI - top - this.reservedSpace;

		this.rows = (int) Math.floor( extraSpace / 18 );
		if( this.rows > this.maxRows )
		{
			top += ( this.rows - this.maxRows ) * 18 / 2;
			this.rows = this.maxRows;
		}

		if( hasNEI )
		{
			this.rows--;
		}

		if( this.rows < 3 )
		{
			this.rows = 3;
		}

		this.meSlots.clear();
		for( int y = 0; y < this.rows; y++ )
		{
			for( int x = 0; x < this.perRow; x++ )
			{
				this.meSlots.add( new InternalSlotME( this.repo, x + y * this.perRow, this.offsetX + x * 18, 18 + y * 18 ) );
			}
		}

		if( AEConfig.instance.getConfigManager().getSetting( Settings.TERMINAL_STYLE ) != TerminalStyle.FULL )
		{
			this.xSize = this.standardSize + ( ( this.perRow - 9 ) * 18 );
		}
		else
		{
			this.xSize = this.standardSize;
		}

		super.initGui();
		// full size : 204
		// extra slots : 72
		// slot 18

		this.ySize = magicNumber + this.rows * 18 + this.reservedSpace;
		// this.guiTop = top;
		int unusedSpace = this.height - this.ySize;
		this.guiTop = (int) Math.floor( unusedSpace / ( unusedSpace < 0 ? 3.8f : 2.0f ) );

		int offset = this.guiTop + 8;

		if( this.customSortOrder )
		{
			this.buttonList.add( this.SortByBox = new GuiImgButton( this.guiLeft - 18, offset, Settings.SORT_BY, this.configSrc.getSetting( Settings.SORT_BY ) ) );
			offset += 20;
		}

		if( this.viewCell || this instanceof GuiWirelessTerm )
		{
			this.buttonList.add( this.ViewBox = new GuiImgButton( this.guiLeft - 18, offset, Settings.VIEW_MODE, this.configSrc.getSetting( Settings.VIEW_MODE ) ) );
			offset += 20;
		}

		this.buttonList.add( this.SortDirBox = new GuiImgButton( this.guiLeft - 18, offset, Settings.SORT_DIRECTION, this.configSrc.getSetting( Settings.SORT_DIRECTION ) ) );
		offset += 20;

		this.buttonList.add( this.searchBoxSettings = new GuiImgButton( this.guiLeft - 18, offset, Settings.SEARCH_MODE, AEConfig.instance.settings.getSetting( Settings.SEARCH_MODE ) ) );
		offset += 20;

		if( !( this instanceof GuiMEPortableCell ) || this instanceof GuiWirelessTerm )
		{
			this.buttonList.add( this.terminalStyleBox = new GuiImgButton( this.guiLeft - 18, offset, Settings.TERMINAL_STYLE, AEConfig.instance.settings.getSetting( Settings.TERMINAL_STYLE ) ) );
		}

		this.searchField = new MEGuiTextField( this.fontRendererObj, this.guiLeft + Math.max( 80, this.offsetX ), this.guiTop + 4, 90, 12 );
		this.searchField.setEnableBackgroundDrawing( false );
		this.searchField.setMaxStringLength( 25 );
		this.searchField.setTextColor( 0xFFFFFF );
		this.searchField.setVisible( true );

		if( this.viewCell || this instanceof GuiWirelessTerm )
		{
			this.buttonList.add( this.craftingStatusBtn = new GuiTabButton( this.guiLeft + 170, this.guiTop - 4, 2 + 11 * 16, GuiText.CraftingStatus.getLocal(), itemRender ) );
			this.craftingStatusBtn.hideEdge = 13;
		}

		// Enum setting = AEConfig.INSTANCE.getSetting( "Terminal", SearchBoxMode.class, SearchBoxMode.AUTOSEARCH );
		Enum setting = AEConfig.instance.settings.getSetting( Settings.SEARCH_MODE );
		this.searchField.setFocused( SearchBoxMode.AUTOSEARCH == setting || SearchBoxMode.NEI_AUTOSEARCH == setting );

		if( this.isSubGui() )
		{
			this.searchField.setText( memoryText );
			this.repo.searchString = memoryText;
			this.repo.updateView();
			this.setScrollBar();
		}

		CraftingGridOffsetX = Integer.MAX_VALUE;
		CraftingGridOffsetY = Integer.MAX_VALUE;

		for( Object s : this.inventorySlots.inventorySlots )
		{
			if( s instanceof AppEngSlot )
			{
				if( ( (Slot) s ).xDisplayPosition < 197 )
				{
					this.repositionSlot( (AppEngSlot) s );
				}
			}

			if( s instanceof SlotCraftingMatrix || s instanceof SlotFakeCraftingMatrix )
			{
				Slot g = (Slot) s;
				if( g.xDisplayPosition > 0 && g.yDisplayPosition > 0 )
				{
					CraftingGridOffsetX = Math.min( CraftingGridOffsetX, g.xDisplayPosition );
					CraftingGridOffsetY = Math.min( CraftingGridOffsetY, g.yDisplayPosition );
				}
			}
		}

		CraftingGridOffsetX -= 25;
		CraftingGridOffsetY -= 6;
	}

	@Override
	public void drawFG( int offsetX, int offsetY, int mouseX, int mouseY )
	{
		this.fontRendererObj.drawString( this.getGuiDisplayName( this.myName.getLocal() ), 8, 6, 4210752 );
		this.fontRendererObj.drawString( GuiText.inventory.getLocal(), 8, this.ySize - 96 + 3, 4210752 );
	}

	@Override
	protected void mouseClicked( int xCoord, int yCoord, int btn )
	{
		Enum searchMode = AEConfig.instance.settings.getSetting( Settings.SEARCH_MODE );

		if( searchMode != SearchBoxMode.AUTOSEARCH && searchMode != SearchBoxMode.NEI_AUTOSEARCH )
		{
			this.searchField.mouseClicked( xCoord, yCoord, btn );
		}

		if( btn == 1 && this.searchField.isMouseIn( xCoord, yCoord ) )
		{
			this.searchField.setText( "" );
			this.repo.searchString = "";
			this.repo.updateView();
			this.setScrollBar();
		}

		super.mouseClicked( xCoord, yCoord, btn );
	}

	@Override
	public void onGuiClosed()
	{
		super.onGuiClosed();
		Keyboard.enableRepeatEvents( false );
		memoryText = this.searchField.getText();
	}

	@Override
	public void drawBG( int offsetX, int offsetY, int mouseX, int mouseY )
	{
		int x_width = 197;

		this.bindTexture( this.getBackground() );
		this.drawTexturedModalRect( offsetX, offsetY, 0, 0, x_width, 18 );

		if( this.viewCell || ( this instanceof GuiSecurity ) )
		{
			this.drawTexturedModalRect( offsetX + x_width, offsetY, x_width, 0, 46, 128 );
		}

		for( int x = 0; x < this.rows; x++ )
		{
			this.drawTexturedModalRect( offsetX, offsetY + 18 + x * 18, 0, 18, x_width, 18 );
		}

		this.drawTexturedModalRect( offsetX, offsetY + 16 + this.rows * 18 + this.lowerTextureOffset, 0, 106 - 18 - 18, x_width, 99 + this.reservedSpace - this.lowerTextureOffset );

		if( this.viewCell )
		{
			boolean update = false;

			for( int i = 0; i < 5; i++ )
			{
				if( this.myCurrentViewCells[i] != this.monitorableContainer.cellView[i].getStack() )
				{
					update = true;
					this.myCurrentViewCells[i] = this.monitorableContainer.cellView[i].getStack();
				}
			}

			if( update )
			{
				this.repo.setViewCell( this.myCurrentViewCells );
			}
		}

		if( this.searchField != null )
		{
			this.searchField.drawTextBox();
		}
	}

	protected String getBackground()
	{
		return "guis/terminal.png";
	}

	@Override
	protected boolean isPowered()
	{
		return this.repo.hasPower();
	}

	int getMaxRows()
	{
		return AEConfig.instance.getConfigManager().getSetting( Settings.TERMINAL_STYLE ) == TerminalStyle.SMALL ? 6 : Integer.MAX_VALUE;
	}

	protected void repositionSlot( AppEngSlot s )
	{
		s.yDisplayPosition = s.defY + this.ySize - 78 - 5;
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
				this.repo.searchString = this.searchField.getText();
				this.repo.updateView();
				this.setScrollBar();
			}
			else
			{
				super.keyTyped( character, key );
			}
		}
	}

	@Override
	public void updateScreen()
	{
		this.repo.setPower( this.monitorableContainer.hasPower );
		super.updateScreen();
	}

	@Override
	public Enum getSortBy()
	{
		return this.configSrc.getSetting( Settings.SORT_BY );
	}

	@Override
	public Enum getSortDir()
	{
		return this.configSrc.getSetting( Settings.SORT_DIRECTION );
	}

	@Override
	public Enum getSortDisplay()
	{
		return this.configSrc.getSetting( Settings.VIEW_MODE );
	}

	@Override
	public void updateSetting( IConfigManager manager, Enum settingName, Enum newValue )
	{
		if( this.SortByBox != null )
		{
			this.SortByBox.set( this.configSrc.getSetting( Settings.SORT_BY ) );
		}

		if( this.SortDirBox != null )
		{
			this.SortDirBox.set( this.configSrc.getSetting( Settings.SORT_DIRECTION ) );
		}

		if( this.ViewBox != null )
		{
			this.ViewBox.set( this.configSrc.getSetting( Settings.VIEW_MODE ) );
		}

		this.repo.updateView();
	}
}
