/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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

import appeng.container.implementations.ContainerCraftingStatus;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.util.InputMappings;
import net.minecraft.inventory.container.Slot;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.client.gui.GuiUtils;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;

import appeng.api.config.SearchBoxMode;
import appeng.api.config.Settings;
import appeng.api.config.TerminalStyle;
import appeng.api.implementations.guiobjects.IPortableCell;
import appeng.api.implementations.tiles.IMEChest;
import appeng.api.implementations.tiles.IViewCellStorage;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.client.ActionKey;
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

import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketSwitchGuis;
import appeng.core.sync.packets.PacketValueConfig;
import appeng.helpers.WirelessTerminalGuiObject;
import appeng.parts.reporting.AbstractPartTerminal;
import appeng.tile.misc.TileSecurityStation;
import appeng.util.IConfigManagerHost;
import appeng.util.Platform;
import org.lwjgl.glfw.GLFW;


public class GuiMEMonitorable<T extends ContainerMEMonitorable> extends AEBaseMEGui<T> implements ISortSource, IConfigManagerHost
{

	private static int craftingGridOffsetX;
	private static int craftingGridOffsetY;

	private static String memoryText = "";
	private final ItemRepo repo;
	private final int offsetX = 9;
	private final int lowerTextureOffset = 0;
	private final IConfigManager configSrc;
	private final boolean viewCell;
	private final ItemStack[] myCurrentViewCells = new ItemStack[5];
	private GuiTabButton craftingStatusBtn;
	private MEGuiTextField searchField;
	private GuiText myName;
	private int perRow = 9;
	private int reservedSpace = 0;
	private boolean customSortOrder = true;
	private int rows = 0;
	private int maxRows = Integer.MAX_VALUE;
	private int standardSize;
	private GuiImgButton ViewBox;
	private GuiImgButton SortByBox;
	private GuiImgButton SortDirBox;
	private GuiImgButton searchBoxSettings;
	private GuiImgButton terminalStyleBox;
	private boolean isAutoFocus = false;
	private int currentMouseX = 0;
	private int currentMouseY = 0;

	public GuiMEMonitorable(T container, PlayerInventory playerInventory, ITextComponent title) {
		super(container, playerInventory, title);

		final GuiScrollbar scrollbar = new GuiScrollbar();
		this.setScrollBar( scrollbar );
		this.repo = new ItemRepo( scrollbar, this );

		this.xSize = 185;
		this.ySize = 204;

		Object te = container.getTarget();
		if( te instanceof IViewCellStorage )
		{
			this.xSize += 33;
		}

		this.standardSize = this.xSize;

		this.configSrc = ( (IConfigurableObject) this.container ).getConfigManager();
		this.container.setGui( this );

		this.viewCell = te instanceof IViewCellStorage;

		if( te instanceof TileSecurityStation )
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
		else if( te instanceof AbstractPartTerminal )
		{
			this.myName = GuiText.Terminal;
		}
	}

	public void postUpdate( final List<IAEItemStack> list )
	{
		for( final IAEItemStack is : list )
		{
			this.repo.postUpdate( is );
		}

		this.repo.updateView();
		this.setScrollBar();
	}

	private void setScrollBar()
	{
		this.getScrollBar().setTop( 18 ).setLeft( 175 ).setHeight( this.rows * 18 - 2 );
		this.getScrollBar().setRange( 0, ( this.repo.size() + this.perRow - 1 ) / this.perRow - this.rows, Math.max( 1, this.rows / 6 ) );
	}

	private void showCraftingStatus() {
		NetworkHandler.instance().sendToServer( new PacketSwitchGuis(ContainerCraftingStatus.TYPE) );
	}

	protected void actionPerformed( final Button btn )
	{
		if( btn instanceof GuiImgButton )
		{
			final boolean backwards = minecraft.mouseHelper.isRightDown();

			final GuiImgButton iBtn = (GuiImgButton) btn;
			if( iBtn.getSetting() != Settings.ACTIONS )
			{
				final Enum cv = iBtn.getCurrentValue();
				final Enum next = Platform.rotateEnum( cv, backwards, iBtn.getSetting().getPossibleValues() );

				if( btn == this.terminalStyleBox )
				{
					AEConfig.instance().getConfigManager().putSetting( iBtn.getSetting(), next );
				}
				else if( btn == this.searchBoxSettings )
				{
					AEConfig.instance().getConfigManager().putSetting( iBtn.getSetting(), next );
				}
				else
				{
					NetworkHandler.instance().sendToServer( new PacketValueConfig( iBtn.getSetting().name(), next.name() ) );
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
		this.buttons.clear();
		this.init();
	}

	@Override
	public void init()
	{
		minecraft.keyboardListener.enableRepeatEvents( true );

		this.maxRows = this.getMaxRows();
		this.perRow = AEConfig.instance()
				.getConfigManager()
				.getSetting(
						Settings.TERMINAL_STYLE ) != TerminalStyle.FULL ? 9 : 9 + ( ( this.width - this.standardSize ) / 18 );

		final int magicNumber = 114 + 1;
		final int extraSpace = this.height - magicNumber - this.reservedSpace;

		this.rows = (int) Math.floor( extraSpace / 18 );
		if( this.rows > this.maxRows )
		{
			this.rows = this.maxRows;
		}

		if( this.rows < 3 )
		{
			this.rows = 3;
		}

		this.getMeSlots().clear();
		for( int y = 0; y < this.rows; y++ )
		{
			for( int x = 0; x < this.perRow; x++ )
			{
				this.getMeSlots().add( new InternalSlotME( this.repo, x + y * this.perRow, this.offsetX + x * 18, 18 + y * 18 ) );
			}
		}

		if( AEConfig.instance().getConfigManager().getSetting( Settings.TERMINAL_STYLE ) != TerminalStyle.FULL )
		{
			this.xSize = this.standardSize + ( ( this.perRow - 9 ) * 18 );
		}
		else
		{
			this.xSize = this.standardSize;
		}

		super.init();
		// full size : 204
		// extra slots : 72
		// slot 18

		this.ySize = magicNumber + this.rows * 18 + this.reservedSpace;
		// this.guiTop = top;
		final int unusedSpace = this.height - this.ySize;
		this.guiTop = (int) Math.floor( unusedSpace / ( unusedSpace < 0 ? 3.8f : 2.0f ) );

		int offset = this.guiTop + 8;

		if( this.customSortOrder )
		{
			this.SortByBox = this.addButton( new GuiImgButton( this.guiLeft - 18, offset, Settings.SORT_BY, this.configSrc.getSetting( Settings.SORT_BY ), this::actionPerformed ) );
			offset += 20;
		}

		if( this.viewCell || this instanceof GuiWirelessTerm )
		{
			this.ViewBox = this.addButton( new GuiImgButton( this.guiLeft - 18, offset, Settings.VIEW_MODE, this.configSrc.getSetting( Settings.VIEW_MODE ), this::actionPerformed ) );
			offset += 20;
		}

		this.addButton( this.SortDirBox = new GuiImgButton( this.guiLeft - 18, offset, Settings.SORT_DIRECTION, this.configSrc
				.getSetting( Settings.SORT_DIRECTION ), this::actionPerformed ) );
		offset += 20;

		this.addButton(
				this.searchBoxSettings = new GuiImgButton( this.guiLeft - 18, offset, Settings.SEARCH_MODE, AEConfig.instance()
						.getConfigManager()
						.getSetting(
								Settings.SEARCH_MODE ), this::actionPerformed ) );

		offset += 20;

		if( !( this instanceof GuiMEPortableCell ) || this instanceof GuiWirelessTerm )
		{
			this.addButton( this.terminalStyleBox = new GuiImgButton( this.guiLeft - 18, offset, Settings.TERMINAL_STYLE, AEConfig.instance()
					.getConfigManager()
					.getSetting( Settings.TERMINAL_STYLE ), this::actionPerformed ) );
		}

		this.searchField = new MEGuiTextField( this.font, this.guiLeft + Math.max( 80, this.offsetX ), this.guiTop + 4, 90, 12 );
		this.searchField.setEnableBackgroundDrawing( false );
		this.searchField.setMaxStringLength( 25 );
		this.searchField.setTextColor( 0xFFFFFF );
		this.searchField.setSelectionColor( 0xFF008000 );
		this.searchField.setVisible( true );

		if( this.viewCell || this instanceof GuiWirelessTerm )
		{
			this.craftingStatusBtn = this.addButton( new GuiTabButton( this.guiLeft + 170, this.guiTop - 4, 2 + 11 * 16, GuiText.CraftingStatus
					.getLocal(), this.itemRenderer, btn -> showCraftingStatus() ) );
			this.craftingStatusBtn.setHideEdge( 13 );
		}

		final Enum searchModeSetting = AEConfig.instance().getConfigManager().getSetting( Settings.SEARCH_MODE );

		this.isAutoFocus = SearchBoxMode.AUTOSEARCH == searchModeSetting || SearchBoxMode.JEI_AUTOSEARCH == searchModeSetting || SearchBoxMode.AUTOSEARCH_KEEP == searchModeSetting || SearchBoxMode.JEI_AUTOSEARCH_KEEP == searchModeSetting;
		final boolean isKeepFilter = SearchBoxMode.AUTOSEARCH_KEEP == searchModeSetting || SearchBoxMode.JEI_AUTOSEARCH_KEEP == searchModeSetting || SearchBoxMode.MANUAL_SEARCH_KEEP == searchModeSetting || SearchBoxMode.JEI_MANUAL_SEARCH_KEEP == searchModeSetting;
		final boolean isJEIEnabled = SearchBoxMode.JEI_AUTOSEARCH == searchModeSetting || SearchBoxMode.JEI_MANUAL_SEARCH == searchModeSetting;

		this.searchField.setFocused2( this.isAutoFocus );

		if( isJEIEnabled )
		{
			memoryText = null; // FIXME Integrations.jei().getSearchText();
		}

		if( isKeepFilter && memoryText != null && !memoryText.isEmpty() )
		{
			this.searchField.setText( memoryText );
			this.searchField.selectAll();
			this.repo.setSearchString( memoryText );
			this.repo.updateView();
			this.setScrollBar();
		}

		craftingGridOffsetX = Integer.MAX_VALUE;
		craftingGridOffsetY = Integer.MAX_VALUE;

		for( final Object s : this.container.inventorySlots )
		{
			if( s instanceof AppEngSlot )
			{
				if( ( (Slot) s ).xPos < 197 )
				{
					this.repositionSlot( (AppEngSlot) s );
				}
			}

			if( s instanceof SlotCraftingMatrix || s instanceof SlotFakeCraftingMatrix )
			{
				final Slot g = (Slot) s;
				if( g.xPos > 0 && g.yPos > 0 )
				{
					craftingGridOffsetX = Math.min( craftingGridOffsetX, g.xPos );
					craftingGridOffsetY = Math.min( craftingGridOffsetY, g.yPos );
				}
			}
		}

		craftingGridOffsetX -= 25;
		craftingGridOffsetY -= 6;

	}

	@Override
	public void drawFG( final int offsetX, final int offsetY, final int mouseX, final int mouseY )
	{
		this.font.drawString( this.getGuiDisplayName( this.myName.getLocal() ), 8, 6, 4210752 );
		this.font.drawString( GuiText.inventory.getLocal(), 8, this.ySize - 96 + 3, 4210752 );

		this.currentMouseX = mouseX;
		this.currentMouseY = mouseY;
	}

	@Override
	public boolean mouseClicked( final double xCoord, final double yCoord, final int btn )
	{
		if (this.searchField.mouseClicked( xCoord, yCoord, btn )) {
			if (btn == 1 && this.searchField.isMouseOver(xCoord, yCoord)) {
				this.searchField.setText("");
				this.repo.setSearchString("");
				this.repo.updateView();
				this.setScrollBar();
			}
			return true;
		}

		return super.mouseClicked( xCoord, yCoord, btn );
	}

	@Override
	public void removed()
	{
		super.removed();
		minecraft.keyboardListener.enableRepeatEvents( false );
		memoryText = this.searchField.getText();
	}

	@Override
	public void drawBG(final int offsetX, final int offsetY, final int mouseX, final int mouseY, float partialTicks)
	{

		this.bindTexture( this.getBackground() );
		final int x_width = 197;
		GuiUtils.drawTexturedModalRect( offsetX, offsetY, 0, 0, x_width, 18, 0 /* FIXME this.zlevel was used */ );

		if( this.viewCell || ( this instanceof GuiSecurityStation ) )
		{
			GuiUtils.drawTexturedModalRect( offsetX + x_width, offsetY, x_width, 0, 46, 128, 0 /* FIXME this.zlevel was used */ );
		}

		for( int x = 0; x < this.rows; x++ )
		{
			GuiUtils.drawTexturedModalRect( offsetX, offsetY + 18 + x * 18, 0, 18, x_width, 18, 0 /* FIXME this.zlevel was used */ );
		}

		GuiUtils.drawTexturedModalRect( offsetX, offsetY + 16 + this.rows * 18 + this.lowerTextureOffset, 0, 106 - 18 - 18, x_width,
				99 + this.reservedSpace - this.lowerTextureOffset, 0 /* FIXME this.zlevel was used */ );

		if( this.viewCell )
		{
			boolean update = false;

			for( int i = 0; i < 5; i++ )
			{
				if( this.myCurrentViewCells[i] != this.container.getCellViewSlot( i ).getStack() )
				{
					update = true;
					this.myCurrentViewCells[i] = this.container.getCellViewSlot( i ).getStack();
				}
			}

			if( update )
			{
				this.repo.setViewCell( this.myCurrentViewCells );
			}
		}

		if( this.searchField != null )
		{
			this.searchField.render(mouseX, mouseY, partialTicks);
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
		return AEConfig.instance().getConfigManager().getSetting( Settings.TERMINAL_STYLE ) == TerminalStyle.SMALL ? 6 : Integer.MAX_VALUE;
	}

	protected void repositionSlot( final AppEngSlot s )
	{
		// FIXME .... it's final now, WHAT DO WE DO ARGH
		//  s.yPos = s.getY() + this.ySize - 78 - 5;
	}

	@Override
	public boolean charTyped(char character, int p_charTyped_2_) {
		if( character == ' ' && this.searchField.getText().isEmpty() )
		{
			return true;
		}

		final boolean mouseInGui = this.isMouseOver( this.currentMouseX, this.currentMouseY );
		if( this.isAutoFocus && !this.searchField.isFocused() && mouseInGui )
		{
			this.searchField.setFocused2( true );
		}

		if (this.searchField.charTyped(character, p_charTyped_2_)) {
			this.repo.setSearchString( this.searchField.getText() );
			this.repo.updateView();
			this.setScrollBar();
			return true;
		}

		return false;
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int p_keyPressed_3_)
	{

		InputMappings.Input input = InputMappings.getInputByCode(keyCode, scanCode);

		if( !this.checkHotbarKeys(input) )
		{
			if( AppEng.proxy.isActionKey( ActionKey.TOGGLE_FOCUS, input ) )
			{
				this.searchField.setFocused2( !this.searchField.isFocused() );
				return true;
			}

			if( this.searchField.isFocused() && keyCode == GLFW.GLFW_KEY_ENTER )
			{
				this.searchField.setFocused2( false );
				return true;
			}

			if( this.searchField.keyPressed( keyCode, scanCode, p_keyPressed_3_ ) )
			{
				this.repo.setSearchString( this.searchField.getText() );
				this.repo.updateView();
				this.setScrollBar();
				// tell forge the key event is handled and should not be sent out
				return true;
			}
		}

		return super.keyPressed(keyCode, scanCode, p_keyPressed_3_);
	}

	@Override
	public void tick()
	{
		this.repo.setPower( this.container.isPowered() );
		super.tick();
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
	public void updateSetting( final IConfigManager manager, final Enum settingName, final Enum newValue )
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

	int getReservedSpace()
	{
		return this.reservedSpace;
	}

	void setReservedSpace( final int reservedSpace )
	{
		this.reservedSpace = reservedSpace;
	}

	public boolean isCustomSortOrder()
	{
		return this.customSortOrder;
	}

	void setCustomSortOrder( final boolean customSortOrder )
	{
		this.customSortOrder = customSortOrder;
	}

	public int getStandardSize()
	{
		return this.standardSize;
	}

	void setStandardSize( final int standardSize )
	{
		this.standardSize = standardSize;
	}
}
