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
import org.lwjgl.opengl.GL11;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

import appeng.api.AEApi;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.client.gui.AEBaseGui;
import appeng.client.gui.widgets.GuiScrollbar;
import appeng.container.implementations.ContainerCraftConfirm;
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

	final ContainerCraftConfirm ccc;

	final int rows = 5;

	final IItemList<IAEItemStack> storage = AEApi.instance().storage().createItemList();
	final IItemList<IAEItemStack> pending = AEApi.instance().storage().createItemList();
	final IItemList<IAEItemStack> missing = AEApi.instance().storage().createItemList();

	final List<IAEItemStack> visual = new ArrayList<IAEItemStack>();

	GuiBridge OriginalGui;

	boolean isAutoStart()
	{
		return ((ContainerCraftConfirm) this.inventorySlots).autoStart;
	}

	boolean isSimulation()
	{
		return ((ContainerCraftConfirm) this.inventorySlots).simulation;
	}

	public GuiCraftConfirm(InventoryPlayer inventoryPlayer, ITerminalHost te) {
		super( new ContainerCraftConfirm( inventoryPlayer, te ) );
		this.xSize = 238;
		this.ySize = 206;
		this.myScrollBar = new GuiScrollbar();

		this.ccc = (ContainerCraftConfirm) this.inventorySlots;

		if ( te instanceof WirelessTerminalGuiObject )
			this.OriginalGui = GuiBridge.GUI_WIRELESS_TERM;

		if ( te instanceof PartTerminal )
			this.OriginalGui = GuiBridge.GUI_ME;

		if ( te instanceof PartCraftingTerminal )
			this.OriginalGui = GuiBridge.GUI_CRAFTING_TERMINAL;

		if ( te instanceof PartPatternTerminal )
			this.OriginalGui = GuiBridge.GUI_PATTERN_TERMINAL;

	}

	GuiButton cancel;
	GuiButton start;
	GuiButton selectCPU;

	@Override
	public void initGui()
	{
		super.initGui();

		this.start = new GuiButton( 0, this.guiLeft + 162, this.guiTop + this.ySize - 25, 50, 20, GuiText.Start.getLocal() );
		this.start.enabled = false;
		this.buttonList.add( this.start );

		this.selectCPU = new GuiButton( 0, this.guiLeft + (219 - 180) / 2, this.guiTop + this.ySize - 68, 180, 20, GuiText.CraftingCPU.getLocal() + ": "
				+ GuiText.Automatic );
		this.selectCPU.enabled = false;
		this.buttonList.add( this.selectCPU );

		if ( this.OriginalGui != null )
			this.cancel = new GuiButton( 0, this.guiLeft + 6, this.guiTop + this.ySize - 25, 50, 20, GuiText.Cancel.getLocal() );

		this.buttonList.add( this.cancel );
	}

	private void updateCPUButtonText()
	{
		String btnTextText = GuiText.CraftingCPU.getLocal() + ": " + GuiText.Automatic.getLocal();
		if ( this.ccc.selectedCpu >= 0 )// && status.selectedCpu < status.cpus.size() )
		{
			if ( this.ccc.myName.length() > 0 )
			{
				String name = this.ccc.myName.substring( 0, Math.min( 20, this.ccc.myName.length() ) );
				btnTextText = GuiText.CraftingCPU.getLocal() + ": " + name;
			}
			else
				btnTextText = GuiText.CraftingCPU.getLocal() + ": #" + this.ccc.selectedCpu;
		}

		if ( this.ccc.noCPU )
			btnTextText = GuiText.NoCraftingCPUs.getLocal();

		this.selectCPU.displayString = btnTextText;
	}

	@Override
	protected void actionPerformed(GuiButton btn)
	{
		super.actionPerformed( btn );

		boolean backwards = Mouse.isButtonDown( 1 );

		if ( btn == this.selectCPU )
		{
			try
			{
				NetworkHandler.instance.sendToServer( new PacketValueConfig( "Terminal.Cpu", backwards ? "Prev" : "Next" ) );
			}
			catch (IOException e)
			{
				AELog.error( e );
			}
		}

		if ( btn == this.cancel )
		{
			NetworkHandler.instance.sendToServer( new PacketSwitchGuis( this.OriginalGui ) );
		}

		if ( btn == this.start )
		{
			try
			{
				NetworkHandler.instance.sendToServer( new PacketValueConfig( "Terminal.Start", "Start" ) );
			}
			catch (Throwable e)
			{
				AELog.error( e );
			}
		}

	}

	private long getTotal(IAEItemStack is)
	{
		IAEItemStack a = this.storage.findPrecise( is );
		IAEItemStack c = this.pending.findPrecise( is );
		IAEItemStack m = this.missing.findPrecise( is );

		long total = 0;

		if ( a != null )
			total += a.getStackSize();

		if ( c != null )
			total += c.getStackSize();

		if ( m != null )
			total += m.getStackSize();

		return total;
	}

	public void postUpdate(List<IAEItemStack> list, byte ref)
	{
		switch (ref)
		{
		case 0:
			for (IAEItemStack l : list)
				this.handleInput( this.storage, l );
			break;

		case 1:
			for (IAEItemStack l : list)
				this.handleInput( this.pending, l );
			break;

		case 2:
			for (IAEItemStack l : list)
				this.handleInput( this.missing, l );
			break;
		}

		for (IAEItemStack l : list)
		{
			long amt = this.getTotal( l );

			if ( amt <= 0 )
				this.deleteVisualStack( l );
			else
			{
				IAEItemStack is = this.findVisualStack( l );
				is.setStackSize( amt );
			}
		}

		this.setScrollBar();
	}

	private void handleInput(IItemList<IAEItemStack> s, IAEItemStack l)
	{
		IAEItemStack a = s.findPrecise( l );

		if ( l.getStackSize() <= 0 )
		{
			if ( a != null )
				a.reset();
		}
		else
		{
			if ( a == null )
			{
				s.add( l.copy() );
				a = s.findPrecise( l );
			}

			if ( a != null )
				a.setStackSize( l.getStackSize() );
		}
	}

	private IAEItemStack findVisualStack(IAEItemStack l)
	{
		for (IAEItemStack o : this.visual)
		{
			if ( o.equals( l ) )
			{
				return o;
			}
		}

		IAEItemStack stack = l.copy();
		this.visual.add( stack );
		return stack;
	}

	private void deleteVisualStack(IAEItemStack l)
	{
		Iterator<IAEItemStack> i = this.visual.iterator();
		while (i.hasNext())
		{
			IAEItemStack o = i.next();
			if ( o.equals( l ) )
			{
				i.remove();
				return;
			}
		}
	}

	private void setScrollBar()
	{
		int size = this.visual.size();

		this.myScrollBar.setTop( 19 ).setLeft( 218 ).setHeight( 114 );
		this.myScrollBar.setRange( 0, (size + 2) / 3 - this.rows, 1 );
	}

	@Override
	protected void keyTyped(char character, int key)
	{
		if ( !this.checkHotbarKeys( key ) )
		{
			if ( key == 28 )
			{
				this.actionPerformed( this.start );
			}
			super.keyTyped( character, key );
		}
	}

	@Override
	public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY)
	{
		this.setScrollBar();
		this.bindTexture( "guis/craftingreport.png" );
		this.drawTexturedModalRect( offsetX, offsetY, 0, 0, this.xSize, this.ySize );
	}

	int tooltip = -1;

	@Override
	public void drawScreen(int mouse_x, int mouse_y, float btn)
	{
		this.updateCPUButtonText();

		this.start.enabled = !(this.ccc.noCPU || this.isSimulation());
		this.selectCPU.enabled = !this.isSimulation();

		int x = 0;
		int y = 0;

		int gx = (this.width - this.xSize) / 2;
		int gy = (this.height - this.ySize) / 2;
		int offY = 23;

		this.tooltip = -1;

		for (int z = 0; z <= 4 * 5; z++)
		{
			int minX = gx + 9 + x * 67;
			int minY = gy + 22 + y * offY;

			if ( minX < mouse_x && minX + 67 > mouse_x )
			{
				if ( minY < mouse_y && minY + offY - 2 > mouse_y )
				{
					this.tooltip = z;
					break;
				}

			}

			x++;

			if ( x > 2 )
			{
				y++;
				x = 0;
			}
		}

		super.drawScreen( mouse_x, mouse_y, btn );
	}

	@Override
	public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY)
	{
		long BytesUsed = this.ccc.bytesUsed;
		String byteUsed = NumberFormat.getInstance().format( BytesUsed );
		String Add = BytesUsed > 0 ? (byteUsed + ' ' + GuiText.BytesUsed.getLocal()) : GuiText.CalculatingWait.getLocal();
		this.fontRendererObj.drawString( GuiText.CraftingPlan.getLocal() + " - " + Add, 8, 7, 4210752 );

		String dsp = null;

		if ( this.isSimulation() )
			dsp = GuiText.Simulation.getLocal();
		else
			dsp = this.ccc.cpuBytesAvail > 0 ? (GuiText.Bytes.getLocal() + ": " + this.ccc.cpuBytesAvail + " : " + GuiText.CoProcessors.getLocal() + ": " + this.ccc.cpuCoProcessors)
					: GuiText.Bytes.getLocal() + ": N/A : " + GuiText.CoProcessors.getLocal() + ": N/A";

		int offset = (219 - this.fontRendererObj.getStringWidth( dsp )) / 2;
		this.fontRendererObj.drawString( dsp, offset, 165, 4210752 );

		int sectionLength = 67;

		int x = 0;
		int y = 0;
		int xo = 9;
		int yo = 22;
		int viewStart = this.myScrollBar.getCurrentScroll() * 3;
		int viewEnd = viewStart + 3 * this.rows;

		String dspToolTip = "";
		List<String> lineList = new LinkedList<String>();
		int toolPosX = 0;
		int toolPosY = 0;

		int offY = 23;

		for (int z = viewStart; z < Math.min( viewEnd, this.visual.size() ); z++)
		{
			IAEItemStack refStack = this.visual.get( z );// repo.getReferenceItem( z );
			if ( refStack != null )
			{
				GL11.glPushMatrix();
				GL11.glScaled( 0.5, 0.5, 0.5 );

				IAEItemStack stored = this.storage.findPrecise( refStack );
				IAEItemStack pendingStack = this.pending.findPrecise( refStack );
				IAEItemStack missingStack = this.missing.findPrecise( refStack );

				int lines = 0;

				if ( stored != null && stored.getStackSize() > 0 )
					lines++;
				if ( pendingStack != null && pendingStack.getStackSize() > 0 )
					lines++;
				if ( pendingStack != null && pendingStack.getStackSize() > 0 )
					lines++;

				int negY = ((lines - 1) * 5) / 2;
				int downY = 0;
				boolean red = false;

				if ( stored != null && stored.getStackSize() > 0 )
				{
					String str = Long.toString( stored.getStackSize() );
					if ( stored.getStackSize() >= 10000 )
						str = Long.toString( stored.getStackSize() / 1000 ) + 'k';
					if ( stored.getStackSize() >= 10000000 )
						str = Long.toString( stored.getStackSize() / 1000000 ) + 'm';

					str = GuiText.FromStorage.getLocal() + ": " + str;
					int w = 4 + this.fontRendererObj.getStringWidth( str );
					this.fontRendererObj.drawString( str, (int) ((x * (1 + sectionLength) + xo + sectionLength - 19 - (w * 0.5)) * 2), (y * offY + yo
							+ 6 - negY + downY) * 2, 4210752 );

					if ( this.tooltip == z - viewStart )
						lineList.add( GuiText.FromStorage.getLocal() + ": " + Long.toString( stored.getStackSize() ) );

					downY += 5;
				}

				if ( missingStack != null && missingStack.getStackSize() > 0 )
				{
					String str = Long.toString( missingStack.getStackSize() );
					if ( missingStack.getStackSize() >= 10000 )
						str = Long.toString( missingStack.getStackSize() / 1000 ) + 'k';
					if ( missingStack.getStackSize() >= 10000000 )
						str = Long.toString( missingStack.getStackSize() / 1000000 ) + 'm';

					str = GuiText.Missing.getLocal() + ": " + str;
					int w = 4 + this.fontRendererObj.getStringWidth( str );
					this.fontRendererObj.drawString( str, (int) ((x * (1 + sectionLength) + xo + sectionLength - 19 - (w * 0.5)) * 2), (y * offY + yo
							+ 6 - negY + downY) * 2, 4210752 );

					if ( this.tooltip == z - viewStart )
						lineList.add( GuiText.Missing.getLocal() + ": " + Long.toString( missingStack.getStackSize() ) );

					red = true;
					downY += 5;
				}

				if ( pendingStack != null && pendingStack.getStackSize() > 0 )
				{
					String str = Long.toString( pendingStack.getStackSize() );
					if ( pendingStack.getStackSize() >= 10000 )
						str = Long.toString( pendingStack.getStackSize() / 1000 ) + 'k';
					if ( pendingStack.getStackSize() >= 10000000 )
						str = Long.toString( pendingStack.getStackSize() / 1000000 ) + 'm';

					str = GuiText.ToCraft.getLocal() + ": " + str;
					int w = 4 + this.fontRendererObj.getStringWidth( str );
					this.fontRendererObj.drawString( str, (int) ((x * (1 + sectionLength) + xo + sectionLength - 19 - (w * 0.5)) * 2), (y * offY + yo
							+ 6 - negY + downY) * 2, 4210752 );

					if ( this.tooltip == z - viewStart )
						lineList.add( GuiText.ToCraft.getLocal() + ": " + Long.toString( pendingStack.getStackSize() ) );

				}

				GL11.glPopMatrix();
				int posX = x * (1 + sectionLength) + xo + sectionLength - 19;
				int posY = y * offY + yo;

				ItemStack is = refStack.copy().getItemStack();

				if ( this.tooltip == z - viewStart )
				{
					dspToolTip = Platform.getItemDisplayName( is );

					if ( lineList.size() > 0 )
						dspToolTip = dspToolTip + '\n' + Joiner.on( "\n" ).join( lineList );

					toolPosX = x * (1 + sectionLength) + xo + sectionLength - 8;
					toolPosY = y * offY + yo;
				}

				this.drawItem( posX, posY, is );

				if ( red )
				{
					int startX = x * (1 + sectionLength) + xo;
					int startY = posY - 4;
					drawRect( startX, startY, startX + sectionLength, startY + offY, 0x1AFF0000 );
				}

				x++;

				if ( x > 2 )
				{
					y++;
					x = 0;
				}
			}

		}

		if ( this.tooltip >= 0 && dspToolTip.length() > 0 )
		{
			GL11.glPushAttrib( GL11.GL_ALL_ATTRIB_BITS );
			this.drawTooltip( toolPosX, toolPosY + 10, 0, dspToolTip );
			GL11.glPopAttrib();
		}

	}

}
