/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2018, AlgorithmX2, All rights reserved.
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

package appeng.fluids.client.gui;


import appeng.api.config.Settings;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.util.IConfigManager;
import appeng.client.gui.AEBaseMEGui;
import appeng.client.gui.widgets.GuiImgButton;
import appeng.client.gui.widgets.GuiScrollbar;
import appeng.client.gui.widgets.ISortSource;
import appeng.client.gui.widgets.MEGuiTextField;
import appeng.client.me.FluidRepo;
import appeng.client.me.InternalFluidSlotME;
import appeng.client.me.SlotFluidME;
import appeng.core.AELog;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketInventoryAction;
import appeng.core.sync.packets.PacketValueConfig;
import appeng.fluids.container.ContainerFluidTerminal;
import appeng.fluids.container.slots.IMEFluidSlot;
import appeng.helpers.InventoryAction;
import appeng.util.IConfigManagerHost;
import appeng.util.Platform;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.client.gui.GuiUtils;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;


/**
 * @author BrockWS
 * @version rv6 - 12/05/2018
 * @since rv6 12/05/2018
 */
public class GuiFluidTerminal extends AEBaseMEGui<ContainerFluidTerminal> implements ISortSource, IConfigManagerHost
{
	private final List<SlotFluidME> meFluidSlots = new LinkedList<>();
	private final FluidRepo repo;
	private final IConfigManager configSrc;
	private final int offsetX = 9;
	private int rows = 6;
	private int perRow = 9;

	private MEGuiTextField searchField;
	private GuiImgButton sortByBox;
	private GuiImgButton sortDirBox;

	public GuiFluidTerminal(ContainerFluidTerminal container, PlayerInventory playerInventory, ITextComponent title) {
		super(container, playerInventory, title);
		this.xSize = 185;
		this.ySize = 222;
		final GuiScrollbar scrollbar = new GuiScrollbar();
		this.setScrollBar( scrollbar );
		this.repo = new FluidRepo( scrollbar, this );
		this.configSrc = container.getConfigManager();
		this.container.setGui( this );
	}

	@Override
	public void init()
	{
		this.guiLeft = ( this.width - this.xSize ) / 2;
		this.guiTop = ( this.height - this.ySize ) / 2;

		this.searchField = new MEGuiTextField( this.font, this.guiLeft + Math.max( 80, this.offsetX ), this.guiTop + 4, 90, 12 );
		this.searchField.setEnableBackgroundDrawing( false );
		this.searchField.setMaxStringLength( 25 );
		this.searchField.setTextColor( 0xFFFFFF );
		this.searchField.setSelectionColor( 0xFF99FF99 );
		this.searchField.setVisible( true );

		int offset = this.guiTop;

		this.sortByBox = this.addButton( new GuiImgButton( this.guiLeft - 18, offset, Settings.SORT_BY, this.configSrc.getSetting( Settings.SORT_BY ), this::actionPerformed ) );
		offset += 20;

		this.sortDirBox = this.addButton( new GuiImgButton( this.guiLeft - 18, offset, Settings.SORT_DIRECTION, this.configSrc
				.getSetting( Settings.SORT_DIRECTION ), this::actionPerformed ) );

		for( int y = 0; y < this.rows; y++ )
		{
			for( int x = 0; x < this.perRow; x++ )
			{
				SlotFluidME slot = new SlotFluidME( new InternalFluidSlotME( this.repo, x + y * this.perRow, this.offsetX + x * 18, 18 + y * 18 ) );
				this.getMeFluidSlots().add( slot );
				this.container.inventorySlots.add( slot );
			}
		}
		this.setScrollBar();
	}

	@Override
	public void drawFG( int offsetX, int offsetY, int mouseX, int mouseY )
	{
		this.font.drawString( this.getGuiDisplayName( "Fluid Terminal" ), 8, 6, 4210752 );
		this.font.drawString( GuiText.inventory.getLocal(), 8, this.ySize - 96 + 3, 4210752 );
	}

	@Override
	public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY, float partialTicks)
	{
		this.bindTexture( this.getBackground() );
		final int x_width = 197;
		GuiUtils.drawTexturedModalRect( offsetX, offsetY, 0, 0, x_width, 18, 0 /* FIXME ZINDEX */ );

		for( int x = 0; x < 6; x++ )
		{
			GuiUtils.drawTexturedModalRect( offsetX, offsetY + 18 + x * 18, 0, 18, x_width, 18, 0 /* FIXME ZINDEX */ );
		}

		GuiUtils.drawTexturedModalRect( offsetX, offsetY + 16 + 6 * 18, 0, 106 - 18 - 18, x_width, 99 + 77, 0 /* FIXME ZINDEX */ );

		if( this.searchField != null )
		{
			this.searchField.render(mouseX, mouseY, partialTicks);
		}
	}

	@Override
	public void tick()
	{
		this.repo.setPower( this.container.isPowered() );
		super.tick();
	}

	@Override
	protected void renderHoveredToolTip( int mouseX, int mouseY )
	{
		final Slot slot = this.getSlot( mouseX, mouseY );

		if( slot instanceof IMEFluidSlot && slot.isEnabled() )
		{
			final IMEFluidSlot fluidSlot = (IMEFluidSlot) slot;

			if( fluidSlot.getAEFluidStack() != null && fluidSlot.shouldRenderAsFluid() )
			{
				final IAEFluidStack fluidStack = fluidSlot.getAEFluidStack();
				final String formattedAmount = NumberFormat.getNumberInstance( Locale.US ).format( fluidStack.getStackSize() / 1000.0 ) + " B";

				// FIXME: Move getting the mod name to platform
				final String modName = "" + TextFormatting.BLUE + TextFormatting.ITALIC + ModList.get()
						.getModContainerById( Platform.getModId( fluidStack ) )
						.map(mc -> mc.getModInfo().getDisplayName())
						.orElse(null);

				final List<String> list = new ArrayList<>();

				list.add( fluidStack.getFluidStack().getDisplayName().getFormattedText() );
				list.add( formattedAmount );
				list.add( modName );

				this.renderTooltip( list, mouseX, mouseY );

				return;
			}
		}
		super.renderHoveredToolTip( mouseX, mouseY );
	}

	protected void actionPerformed( Button btn )
	{
		if( btn instanceof GuiImgButton )
		{
			final boolean backwards = minecraft.mouseHelper.isRightDown();
			final GuiImgButton iBtn = (GuiImgButton) btn;

			if( iBtn.getSetting() != Settings.ACTIONS )
			{
				final Enum cv = iBtn.getCurrentValue();
				final Enum next = Platform.rotateEnum( cv, backwards, iBtn.getSetting().getPossibleValues() );

				NetworkHandler.instance().sendToServer( new PacketValueConfig( iBtn.getSetting().name(), next.name() ) );

				iBtn.set( next );
			}
		}
	}

	@Override
	protected void handleMouseClick( Slot slot, int slotIdx, int mouseButton, ClickType clickType )
	{
		if( slot instanceof SlotFluidME )
		{
			final SlotFluidME meSlot = (SlotFluidME) slot;

			if( clickType == ClickType.PICKUP )
			{
				// TODO: Allow more options
				if( mouseButton == 0 && meSlot.getHasStack() )
				{
					this.container.setTargetStack( meSlot.getAEFluidStack() );
					AELog.debug( "mouse0 GUI STACK SIZE %s", meSlot.getAEFluidStack().getStackSize() );
					NetworkHandler.instance().sendToServer( new PacketInventoryAction( InventoryAction.FILL_ITEM, slot.slotNumber, 0 ) );
				}
				else
				{
					this.container.setTargetStack( meSlot.getAEFluidStack() );
					if( meSlot.getAEFluidStack() != null )
					{
						AELog.debug( "mouse1 GUI STACK SIZE %s", meSlot.getAEFluidStack().getStackSize() );
					}
					NetworkHandler.instance().sendToServer( new PacketInventoryAction( InventoryAction.EMPTY_ITEM, slot.slotNumber, 0 ) );
				}
			}
			return;
		}
		super.handleMouseClick( slot, slotIdx, mouseButton, clickType );
	}

	@Override
	public boolean charTyped(char character, int keyCode) {
		if (character == ' ' && this.searchField.getText().isEmpty())
		{
			// Swallow spaces if the text field is still empty
			return true;
		}

		if (this.searchField.charTyped(character, keyCode)) {
			this.repo.setSearchString( this.searchField.getText() );
			this.repo.updateView();
			this.setScrollBar();
			return true;
		}

		return super.charTyped(character, keyCode);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int p_keyPressed_3_)
	{
		InputMappings.Input input = InputMappings.getInputByCode(keyCode, scanCode);
		if( !this.checkHotbarKeys(input) )
		{
			if( this.searchField.keyPressed( keyCode, scanCode, p_keyPressed_3_ ) )
			{
				this.repo.setSearchString( this.searchField.getText() );
				this.repo.updateView();
				this.setScrollBar();
				return true;
			}
		}

		return super.keyPressed(keyCode, scanCode, p_keyPressed_3_);
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

	public void postUpdate( final List<IAEFluidStack> list )
	{
		for( final IAEFluidStack is : list )
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
		if( this.sortByBox != null )
		{
			this.sortByBox.set( this.configSrc.getSetting( Settings.SORT_BY ) );
		}

		if( this.sortDirBox != null )
		{
			this.sortDirBox.set( this.configSrc.getSetting( Settings.SORT_DIRECTION ) );
		}

		this.repo.updateView();
	}

	protected List<SlotFluidME> getMeFluidSlots()
	{
		return this.meFluidSlots;
	}

	@Override
	protected boolean isPowered()
	{
		return this.repo.hasPower();
	}

	protected String getBackground()
	{
		return "guis/terminal.png";
	}
}
