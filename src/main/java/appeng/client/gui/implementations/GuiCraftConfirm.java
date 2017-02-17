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
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.google.common.base.Joiner;

import org.lwjgl.input.Mouse;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

import appeng.api.AEApi;
import appeng.api.config.Settings;
import appeng.api.config.TerminalStyle;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.client.gui.AEBaseGui;
import appeng.client.gui.widgets.GuiImgButton;
import appeng.client.gui.widgets.GuiScrollbar;
import appeng.container.implementations.ContainerCraftConfirm;
import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.core.localization.GuiText;
import appeng.core.sync.GuiBridge;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketSwitchGuis;
import appeng.core.sync.packets.PacketValueConfig;
import appeng.helpers.WirelessTerminalGuiObject;
import appeng.parts.reporting.PartCraftingTerminal;
import appeng.parts.reporting.PartPatternTerminal;
import appeng.parts.reporting.PartTerminal;
import appeng.util.Platform;


public class GuiCraftConfirm extends AEBaseGui
{

	private final ContainerCraftConfirm ccc;

	private int rows = 5;
	private GuiImgButton terminalStyleBox;

	private static final int GUI_HEIGHT = 206;
	private static final int GUI_WIDTH = 238;

	private static final int SECTION_LENGTH = 67;
	private static final int SECTION_HEIGHT = 23;

	private static final int CPU_TOP_OFFSET = 68;

	private static final int ITEMSTACK_LEFT_OFFSET = 9;
	private static final int ITEMSTACK_TOP_OFFSET = 22;

	private final IItemList<IAEItemStack> storage = AEApi.instance().storage().createItemList();
	private final IItemList<IAEItemStack> pending = AEApi.instance().storage().createItemList();
	private final IItemList<IAEItemStack> missing = AEApi.instance().storage().createItemList();

	private final List<IAEItemStack> visual = new ArrayList<IAEItemStack>();

	private GuiBridge OriginalGui;
	private GuiButton cancel;
	private GuiButton start;
	private GuiButton selectCPU;
	private int tooltip = -1;

	public GuiCraftConfirm( final InventoryPlayer inventoryPlayer, final ITerminalHost te )
	{
		super( new ContainerCraftConfirm( inventoryPlayer, te ) );
		this.xSize = GUI_WIDTH;
		this.ySize = GUI_HEIGHT;

		final GuiScrollbar scrollbar = new GuiScrollbar();
		this.setScrollBar( scrollbar );

		this.ccc = (ContainerCraftConfirm) this.inventorySlots;

		if( te instanceof WirelessTerminalGuiObject )
		{
			this.OriginalGui = GuiBridge.GUI_WIRELESS_TERM;
		}

		if( te instanceof PartTerminal )
		{
			this.OriginalGui = GuiBridge.GUI_ME;
		}

		if( te instanceof PartCraftingTerminal )
		{
			this.OriginalGui = GuiBridge.GUI_CRAFTING_TERMINAL;
		}

		if( te instanceof PartPatternTerminal )
		{
			this.OriginalGui = GuiBridge.GUI_PATTERN_TERMINAL;
		}
	}

	boolean isAutoStart()
	{
		return ( (ContainerCraftConfirm) this.inventorySlots ).isAutoStart();
	}

	private void reinitalize()
	{
		this.buttonList.clear();
		this.initGui();
	}

	@Override
	public void initGui()
	{
		final int staticSpace = ITEMSTACK_TOP_OFFSET + CPU_TOP_OFFSET;

		calculateRows( staticSpace );

		super.initGui();

		this.ySize = staticSpace + this.rows * SECTION_HEIGHT;
		final int unusedSpace = this.height - this.ySize;
		this.guiTop = (int) Math.floor( unusedSpace / ( unusedSpace < 0 ? 3.8f : 2.0f ) );
		int offset = this.guiTop + 8;
		this.setScrollBar();

		this.start = new GuiButton( 0, this.guiLeft + 162, this.guiTop + this.ySize - 25, 50, 20, GuiText.Start.getLocal() );
		this.start.enabled = false;
		this.buttonList.add( this.start );

		this.selectCPU = new GuiButton( 0, this.guiLeft + ( 219 - 180 ) / 2, this.guiTop + this.ySize - CPU_TOP_OFFSET, 180, 20, GuiText.CraftingCPU.getLocal() + ": " + GuiText.Automatic );
		this.selectCPU.enabled = false;
		this.buttonList.add( this.selectCPU );

		if( this.OriginalGui != null )
		{
			this.cancel = new GuiButton( 0, this.guiLeft + 6, this.guiTop + this.ySize - 25, 50, 20, GuiText.Cancel.getLocal() );
		}

		this.buttonList.add( this.cancel );

		this.terminalStyleBox = new GuiImgButton( this.guiLeft - 18, offset, Settings.TERMINAL_STYLE, AEConfig.instance().getConfigManager().getSetting( Settings.TERMINAL_STYLE ) );
		this.buttonList.add( this.terminalStyleBox );
	}

	private void calculateRows( final int height )
	{
		final int maxRows = AEConfig.instance().getConfigManager().getSetting( Settings.TERMINAL_STYLE ) == TerminalStyle.SMALL ? 5 : Integer.MAX_VALUE;

		final double extraSpace = (double) this.height - height;

		this.rows = (int) Math.floor( extraSpace / SECTION_HEIGHT );
		if( this.rows > maxRows )
		{
			this.rows = maxRows;
		}

		if( this.rows < 5 )
		{
			this.rows = 5;
		}
 	}

	@Override
	public void drawScreen( final int mouseX, final int mouseY, final float btn )
	{
		this.updateCPUButtonText();

		this.start.enabled = !( this.ccc.hasNoCPU() || this.isSimulation() );
		this.selectCPU.enabled = !this.isSimulation();

		final int gx = ( this.width - this.xSize ) / 2;
		final int gy = ( this.height - this.ySize ) / 2;

		this.tooltip = -1;

		final int offY = SECTION_HEIGHT;
		int y = 0;
		int x = 0;
		for( int z = 0; z <= 4 * 5; z++ )
		{
			final int minX = gx + 9 + x * 67;
			final int minY = gy + 22 + y * offY;

			if( minX < mouseX && minX + 67 > mouseX )
			{
				if( minY < mouseY && minY + offY - 2 > mouseY )
				{
					this.tooltip = z;
					break;
				}
			}

			x++;

			if( x > 2 )
			{
				y++;
				x = 0;
			}
		}

		super.drawScreen( mouseX, mouseY, btn );
	}

	private void updateCPUButtonText()
	{
		String btnTextText = GuiText.CraftingCPU.getLocal() + ": " + GuiText.Automatic.getLocal();
		if( this.ccc.getSelectedCpu() >= 0 )// && status.selectedCpu < status.cpus.size() )
		{
			if( this.ccc.getName().length() > 0 )
			{
				final String name = this.ccc.getName().substring( 0, Math.min( 20, this.ccc.getName().length() ) );
				btnTextText = GuiText.CraftingCPU.getLocal() + ": " + name;
			}
			else
			{
				btnTextText = GuiText.CraftingCPU.getLocal() + ": #" + this.ccc.getSelectedCpu();
			}
		}

		if( this.ccc.hasNoCPU() )
		{
			btnTextText = GuiText.NoCraftingCPUs.getLocal();
		}

		this.selectCPU.displayString = btnTextText;
	}

	private boolean isSimulation()
	{
		return ( (ContainerCraftConfirm) this.inventorySlots ).isSimulation();
	}

	@Override
	public void drawFG( final int offsetX, final int offsetY, final int mouseX, final int mouseY )
	{
		final long BytesUsed = this.ccc.getUsedBytes();
		final String byteUsed = NumberFormat.getInstance().format( BytesUsed );
		final String Add = BytesUsed > 0 ? ( byteUsed + ' ' + GuiText.BytesUsed.getLocal() ) : GuiText.CalculatingWait.getLocal();
		this.fontRendererObj.drawString( GuiText.CraftingPlan.getLocal() + " - " + Add, 8, 7, 4210752 );

		String dsp = null;

		if( this.isSimulation() )
		{
			dsp = GuiText.Simulation.getLocal();
		}
		else
		{
			dsp = this.ccc.getCpuAvailableBytes() > 0 ? ( GuiText.Bytes.getLocal() + ": " + this.ccc.getCpuAvailableBytes() + " : " + GuiText.CoProcessors.getLocal() + ": " + this.ccc.getCpuCoProcessors() ) : GuiText.Bytes.getLocal() + ": N/A : " + GuiText.CoProcessors.getLocal() + ": N/A";
		}

		final int offset = ( 219 - this.fontRendererObj.getStringWidth( dsp ) ) / 2;
		this.fontRendererObj.drawString( dsp, offset, this.ySize + 27 - CPU_TOP_OFFSET, 4210752 );

		int x = 0;
		int y = 0;
		final int viewStart = this.getScrollBar().getCurrentScroll() * 3;
		final int viewEnd = viewStart + 3 * this.rows;

		String dspToolTip = "";
		final List<String> lineList = new LinkedList<String>();
		int toolPosX = 0;
		int toolPosY = 0;

		final int offY = SECTION_HEIGHT;

		for( int z = viewStart; z < Math.min( viewEnd, this.visual.size() ); z++ )
		{
			final IAEItemStack refStack = this.visual.get( z );// repo.getReferenceItem( z );
			if( refStack != null )
			{
				GlStateManager.pushMatrix();
				GlStateManager.scale( 0.5, 0.5, 0.5 );

				final IAEItemStack stored = this.storage.findPrecise( refStack );
				final IAEItemStack pendingStack = this.pending.findPrecise( refStack );
				final IAEItemStack missingStack = this.missing.findPrecise( refStack );

				int lines = 0;

				if( stored != null && stored.getStackSize() > 0 )
				{
					lines++;
				}
				if( missingStack != null && missingStack.getStackSize() > 0 )
				{
					lines++;
				}
				if( pendingStack != null && pendingStack.getStackSize() > 0 )
				{
					lines++;
				}

				final int negY = ( ( lines - 1 ) * 5 ) / 2;
				int downY = 0;

				if( stored != null && stored.getStackSize() > 0 )
				{
					String str = Long.toString( stored.getStackSize() );
					if( stored.getStackSize() >= 10000 )
					{
						str = Long.toString( stored.getStackSize() / 1000 ) + 'k';
					}
					if( stored.getStackSize() >= 10000000 )
					{
						str = Long.toString( stored.getStackSize() / 1000000 ) + 'm';
					}

					str = GuiText.FromStorage.getLocal() + ": " + str;
					final int w = 4 + this.fontRendererObj.getStringWidth( str );
					this.fontRendererObj.drawString( str, (int) ( ( x * ( 1 + SECTION_LENGTH ) + ITEMSTACK_LEFT_OFFSET + SECTION_LENGTH - 19 - ( w * 0.5 ) ) * 2 ), ( y * offY + ITEMSTACK_TOP_OFFSET + 6 - negY + downY ) * 2, 4210752 );

					if( this.tooltip == z - viewStart )
					{
						lineList.add( GuiText.FromStorage.getLocal() + ": " + Long.toString( stored.getStackSize() ) );
					}

					downY += 5;
				}

				boolean red = false;
				if( missingStack != null && missingStack.getStackSize() > 0 )
				{
					String str = Long.toString( missingStack.getStackSize() );
					if( missingStack.getStackSize() >= 10000 )
					{
						str = Long.toString( missingStack.getStackSize() / 1000 ) + 'k';
					}
					if( missingStack.getStackSize() >= 10000000 )
					{
						str = Long.toString( missingStack.getStackSize() / 1000000 ) + 'm';
					}

					str = GuiText.Missing.getLocal() + ": " + str;
					final int w = 4 + this.fontRendererObj.getStringWidth( str );
					this.fontRendererObj.drawString( str, (int) ( ( x * ( 1 + SECTION_LENGTH ) + ITEMSTACK_LEFT_OFFSET + SECTION_LENGTH - 19 - ( w * 0.5 ) ) * 2 ), ( y * offY + ITEMSTACK_TOP_OFFSET + 6 - negY + downY ) * 2, 4210752 );

					if( this.tooltip == z - viewStart )
					{
						lineList.add( GuiText.Missing.getLocal() + ": " + Long.toString( missingStack.getStackSize() ) );
					}

					red = true;
					downY += 5;
				}

				if( pendingStack != null && pendingStack.getStackSize() > 0 )
				{
					String str = Long.toString( pendingStack.getStackSize() );
					if( pendingStack.getStackSize() >= 10000 )
					{
						str = Long.toString( pendingStack.getStackSize() / 1000 ) + 'k';
					}
					if( pendingStack.getStackSize() >= 10000000 )
					{
						str = Long.toString( pendingStack.getStackSize() / 1000000 ) + 'm';
					}

					str = GuiText.ToCraft.getLocal() + ": " + str;
					final int w = 4 + this.fontRendererObj.getStringWidth( str );
					this.fontRendererObj.drawString( str, (int) ( ( x * ( 1 + SECTION_LENGTH ) + ITEMSTACK_LEFT_OFFSET + SECTION_LENGTH - 19 - ( w * 0.5 ) ) * 2 ), ( y * offY + ITEMSTACK_TOP_OFFSET + 6 - negY + downY ) * 2, 4210752 );

					if( this.tooltip == z - viewStart )
					{
						lineList.add( GuiText.ToCraft.getLocal() + ": " + Long.toString( pendingStack.getStackSize() ) );
					}
				}

				GlStateManager.popMatrix();
				final int posX = x * ( 1 + SECTION_LENGTH ) + ITEMSTACK_LEFT_OFFSET + SECTION_LENGTH - 19;
				final int posY = y * offY + ITEMSTACK_TOP_OFFSET;

				final ItemStack is = refStack.copy().getItemStack();

				if( this.tooltip == z - viewStart )
				{
					dspToolTip = Platform.getItemDisplayName( is );

					if( lineList.size() > 0 )
					{
						dspToolTip = dspToolTip + '\n' + Joiner.on( "\n" ).join( lineList );
					}

					toolPosX = x * ( 1 + SECTION_LENGTH ) + ITEMSTACK_LEFT_OFFSET + SECTION_LENGTH - 8;
					toolPosY = y * offY + ITEMSTACK_TOP_OFFSET;
				}

				this.drawItem( posX, posY, is );

				if( red )
				{
					final int startX = x * ( 1 + SECTION_LENGTH ) + ITEMSTACK_LEFT_OFFSET;
					final int startY = posY - 4;
					drawRect( startX, startY, startX + SECTION_LENGTH, startY + offY, 0x1AFF0000 );
				}

				x++;

				if( x > 2 )
				{
					y++;
					x = 0;
				}
			}
		}

		if( this.tooltip >= 0 && !dspToolTip.isEmpty() )
		{
			this.drawTooltip( toolPosX, toolPosY + 10, dspToolTip );
		}
	}

	@Override
	public void drawBG( final int offsetX, final int offsetY, final int mouseX, final int mouseY )
	{
		this.bindTexture( "guis/craftingreport.png" );
		final int x_width = 238;
		this.drawTexturedModalRect( offsetX, offsetY, 0, 0, x_width, 19 );

		for( int x = 0; x < this.rows; x++ )
		{
			this.drawTexturedModalRect( offsetX, offsetY + 19 + x * SECTION_HEIGHT, 0, 19, x_width, SECTION_HEIGHT );
		}

		this.drawTexturedModalRect( offsetX, offsetY + 19 + this.rows * SECTION_HEIGHT - 1, 0, 133, x_width, 72 );
	}

	private void setScrollBar()
	{
		final int size = this.visual.size();

		this.getScrollBar().setTop( 19 ).setLeft( 218 ).setHeight( this.rows * SECTION_HEIGHT - 2 );
		this.getScrollBar().setRange( 0, ( size + 2 ) / 3 - this.rows, Math.max( 1, this.rows / 6 ) );
	}

	public void postUpdate( final List<IAEItemStack> list, final byte ref )
	{
		switch( ref )
		{
			case 0:
				for( final IAEItemStack l : list )
				{
					this.handleInput( this.storage, l );
				}
				break;

			case 1:
				for( final IAEItemStack l : list )
				{
					this.handleInput( this.pending, l );
				}
				break;

			case 2:
				for( final IAEItemStack l : list )
				{
					this.handleInput( this.missing, l );
				}
				break;
		}

		for( final IAEItemStack l : list )
		{
			final long amt = this.getTotal( l );

			if( amt <= 0 )
			{
				this.deleteVisualStack( l );
			}
			else
			{
				final IAEItemStack is = this.findVisualStack( l );
				is.setStackSize( amt );
			}
		}

		this.setScrollBar();
	}

	private void handleInput( final IItemList<IAEItemStack> s, final IAEItemStack l )
	{
		IAEItemStack a = s.findPrecise( l );

		if( l.getStackSize() <= 0 )
		{
			if( a != null )
			{
				a.reset();
			}
		}
		else
		{
			if( a == null )
			{
				s.add( l.copy() );
				a = s.findPrecise( l );
			}

			if( a != null )
			{
				a.setStackSize( l.getStackSize() );
			}
		}
	}

	private long getTotal( final IAEItemStack is )
	{
		final IAEItemStack a = this.storage.findPrecise( is );
		final IAEItemStack c = this.pending.findPrecise( is );
		final IAEItemStack m = this.missing.findPrecise( is );

		long total = 0;

		if( a != null )
		{
			total += a.getStackSize();
		}

		if( c != null )
		{
			total += c.getStackSize();
		}

		if( m != null )
		{
			total += m.getStackSize();
		}

		return total;
	}

	private void deleteVisualStack( final IAEItemStack l )
	{
		final Iterator<IAEItemStack> i = this.visual.iterator();
		while( i.hasNext() )
		{
			final IAEItemStack o = i.next();
			if( o.equals( l ) )
			{
				i.remove();
				return;
			}
		}
	}

	private IAEItemStack findVisualStack( final IAEItemStack l )
	{
		for( final IAEItemStack o : this.visual )
		{
			if( o.equals( l ) )
			{
				return o;
			}
		}

		final IAEItemStack stack = l.copy();
		this.visual.add( stack );
		return stack;
	}

	@Override
	protected void keyTyped( final char character, final int key ) throws IOException
	{
		if( !this.checkHotbarKeys( key ) )
		{
			if( key == 28 )
			{
				this.actionPerformed( this.start );
			}
			super.keyTyped( character, key );
		}
	}

	@Override
	protected void actionPerformed( final GuiButton btn ) throws IOException
	{
		super.actionPerformed( btn );

		final boolean backwards = Mouse.isButtonDown( 1 );

		if( btn == this.selectCPU )
		{
			try
			{
				NetworkHandler.instance().sendToServer( new PacketValueConfig( "Terminal.Cpu", backwards ? "Prev" : "Next" ) );
			}
			catch( final IOException e )
			{
				AELog.debug( e );
			}
		}

		if( btn == this.cancel )
		{
			NetworkHandler.instance().sendToServer( new PacketSwitchGuis( this.OriginalGui ) );
		}

		if( btn == this.start )
		{
			try
			{
				NetworkHandler.instance().sendToServer( new PacketValueConfig( "Terminal.Start", "Start" ) );
			}
			catch( final Throwable e )
			{
				AELog.debug( e );
			}
		}

		if( btn instanceof GuiImgButton )
		{
			final GuiImgButton iBtn = (GuiImgButton) btn;
			if( iBtn.getSetting() != Settings.ACTIONS )
			{
				final Enum cv = iBtn.getCurrentValue();
				final Enum next = Platform.rotateEnum( cv, backwards, iBtn.getSetting().getPossibleValues() );

				if( btn == this.terminalStyleBox )
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

				if( next.getClass() == TerminalStyle.class )
				{
					this.reinitalize();
				}
			}
		}
	}
}
