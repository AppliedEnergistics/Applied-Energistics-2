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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

import com.google.common.base.Joiner;

import appeng.api.AEApi;
import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.config.ViewItems;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.client.gui.AEBaseGui;
import appeng.client.gui.widgets.GuiScrollbar;
import appeng.client.gui.widgets.ISortSource;
import appeng.container.implementations.ContainerCraftingCPU;
import appeng.core.AELog;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketValueConfig;
import appeng.util.Platform;

public class GuiCraftingCPU extends AEBaseGui implements ISortSource
{

	final int rows = 6;

	IItemList<IAEItemStack> storage = AEApi.instance().storage().createItemList();
	IItemList<IAEItemStack> active = AEApi.instance().storage().createItemList();
	IItemList<IAEItemStack> pending = AEApi.instance().storage().createItemList();

	List<IAEItemStack> visual = new ArrayList<IAEItemStack>();

	public void clearItems()
	{
		storage = AEApi.instance().storage().createItemList();
		active = AEApi.instance().storage().createItemList();
		pending = AEApi.instance().storage().createItemList();
		visual = new ArrayList<IAEItemStack>();
	}

	protected GuiCraftingCPU(ContainerCraftingCPU container) {
		super( container );
		this.ySize = 184;
		this.xSize = 238;
		myScrollBar = new GuiScrollbar();
	}

	public GuiCraftingCPU(InventoryPlayer inventoryPlayer, Object te) {
		this( new ContainerCraftingCPU( inventoryPlayer, te ) );
	}

	GuiButton cancel;

	@Override
	protected void actionPerformed(GuiButton btn)
	{
		super.actionPerformed( btn );

		if ( cancel == btn )
		{
			try
			{
				NetworkHandler.instance.sendToServer( new PacketValueConfig( "TileCrafting.Cancel", "Cancel" ) );
			}
			catch (IOException e)
			{
				AELog.error( e );
			}
		}
	}

	@Override
	public void initGui()
	{
		super.initGui();
		setScrollBar();
		cancel = new GuiButton( 0, this.guiLeft + 163, this.guiTop + ySize - 25, 50, 20, GuiText.Cancel.getLocal() );
		buttonList.add( cancel );
	}

	private long getTotal(IAEItemStack is)
	{
		IAEItemStack a = storage.findPrecise( is );
		IAEItemStack b = active.findPrecise( is );
		IAEItemStack c = pending.findPrecise( is );

		long total = 0;

		if ( a != null )
			total += a.getStackSize();

		if ( b != null )
			total += b.getStackSize();

		if ( c != null )
			total += c.getStackSize();

		return total;
	}

	public void postUpdate(List<IAEItemStack> list, byte ref)
	{
		switch (ref)
		{
		case 0:
			for (IAEItemStack l : list)
				handleInput( storage, l );
			break;

		case 1:
			for (IAEItemStack l : list)
				handleInput( active, l );
			break;

		case 2:
			for (IAEItemStack l : list)
				handleInput( pending, l );
			break;
		}

		for (IAEItemStack l : list)
		{
			long amt = getTotal( l );

			if ( amt <= 0 )
				deleteVisualStack( l );
			else
			{
				IAEItemStack is = findVisualStack( l );
				is.setStackSize( amt );
			}
		}

		setScrollBar();
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
		for (IAEItemStack o : visual)
		{
			if ( o.equals( l ) )
			{
				return o;
			}
		}

		IAEItemStack stack = l.copy();
		visual.add( stack );
		return stack;
	}

	private void deleteVisualStack(IAEItemStack l)
	{
		Iterator<IAEItemStack> i = visual.iterator();
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
		int size = visual.size();

		myScrollBar.setTop( 19 ).setLeft( 218 ).setHeight( 137 );
		myScrollBar.setRange( 0, (size + 2) / 3 - rows, 1 );
	}

	@Override
	public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY)
	{
		bindTexture( "guis/craftingcpu.png" );
		this.drawTexturedModalRect( offsetX, offsetY, 0, 0, xSize, ySize );
	}

	int tooltip = -1;

	@Override
	public void drawScreen(int mouse_x, int mouse_y, float btn)
	{
		cancel.enabled = !visual.isEmpty();

		int x = 0;
		int y = 0;

		int gx = (width - xSize) / 2;
		int gy = (height - ySize) / 2;
		int offY = 23;

		tooltip = -1;

		for (int z = 0; z <= 4 * 5; z++)
		{
			int minX = gx + 9 + x * 67;
			int minY = gy + 22 + y * offY;

			if ( minX < mouse_x && minX + 67 > mouse_x )
			{
				if ( minY < mouse_y && minY + offY - 2 > mouse_y )
				{
					tooltip = z;
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
		fontRendererObj.drawString( getGuiDisplayName( GuiText.CraftingStatus.getLocal() ), 8, 7, 4210752 );

		int sectionLength = 67;

		int x = 0;
		int y = 0;
		int xo = 9;
		int yo = 22;
		int viewStart = myScrollBar.getCurrentScroll() * 3;
		int viewEnd = viewStart + 3 * 6;

		String dspToolTip = "";
		List<String> lineList = new LinkedList<String>();
		int toolPosX = 0;
		int toolPosY = 0;

		int offY = 23;

		for (int z = viewStart; z < Math.min( viewEnd, visual.size() ); z++)
		{
			IAEItemStack refStack = visual.get( z );// repo.getReferenceItem( z );
			if ( refStack != null )
			{
				GL11.glPushMatrix();
				GL11.glScaled( 0.5, 0.5, 0.5 );

				IAEItemStack stored = storage.findPrecise( refStack );
				IAEItemStack activeStack = active.findPrecise( refStack );
				IAEItemStack pendingStack = pending.findPrecise( refStack );

				int lines = 0;

				if ( stored != null && stored.getStackSize() > 0 )
					lines++;
				if ( activeStack != null && activeStack.getStackSize() > 0 )
					lines++;
				if ( pendingStack != null && pendingStack.getStackSize() > 0 )
					lines++;

				int negY = ((lines - 1) * 5) / 2;
				int downY = 0;

				if ( stored != null && stored.getStackSize() > 0 )
				{
					String str = Long.toString( stored.getStackSize() );
					if ( stored.getStackSize() >= 10000 )
						str = Long.toString( stored.getStackSize() / 1000 ) + 'k';
					if ( stored.getStackSize() >= 10000000 )
						str = Long.toString( stored.getStackSize() / 1000000 ) + 'm';

					str = GuiText.Stored.getLocal() + ": " + str;
					int w = 4 + fontRendererObj.getStringWidth( str );
					fontRendererObj.drawString( str, (int) ((x * (1 + sectionLength) + xo + sectionLength - 19 - (w * 0.5)) * 2), (y * offY + yo
							+ 6 - negY + downY) * 2, 4210752 );

					if ( tooltip == z - viewStart )
						lineList.add( GuiText.Stored.getLocal() + ": " + Long.toString( stored.getStackSize() ) );

					downY += 5;
				}

				if ( activeStack != null && activeStack.getStackSize() > 0 )
				{
					String str = Long.toString( activeStack.getStackSize() );
					if ( activeStack.getStackSize() >= 10000 )
						str = Long.toString( activeStack.getStackSize() / 1000 ) + 'k';
					if ( activeStack.getStackSize() >= 10000000 )
						str = Long.toString( activeStack.getStackSize() / 1000000 ) + 'm';

					str = GuiText.Crafting.getLocal() + ": " + str;
					int w = 4 + fontRendererObj.getStringWidth( str );
					fontRendererObj.drawString( str, (int) ((x * (1 + sectionLength) + xo + sectionLength - 19 - (w * 0.5)) * 2), (y * offY + yo
							+ 6 - negY + downY) * 2, 4210752 );

					if ( tooltip == z - viewStart )
						lineList.add( GuiText.Crafting.getLocal() + ": " + Long.toString( activeStack.getStackSize() ) );

					downY += 5;
				}

				if ( pendingStack != null && pendingStack.getStackSize() > 0 )
				{
					String str = Long.toString( pendingStack.getStackSize() );
					if ( pendingStack.getStackSize() >= 10000 )
						str = Long.toString( pendingStack.getStackSize() / 1000 ) + 'k';
					if ( pendingStack.getStackSize() >= 10000000 )
						str = Long.toString( pendingStack.getStackSize() / 1000000 ) + 'm';

					str = GuiText.Scheduled.getLocal() + ": " + str;
					int w = 4 + fontRendererObj.getStringWidth( str );
					fontRendererObj.drawString( str, (int) ((x * (1 + sectionLength) + xo + sectionLength - 19 - (w * 0.5)) * 2), (y * offY + yo
							+ 6 - negY + downY) * 2, 4210752 );

					if ( tooltip == z - viewStart )
						lineList.add( GuiText.Scheduled.getLocal() + ": " + Long.toString( pendingStack.getStackSize() ) );

				}

				GL11.glPopMatrix();
				int posX = x * (1 + sectionLength) + xo + sectionLength - 19;
				int posY = y * offY + yo;

				ItemStack is = refStack.copy().getItemStack();

				if ( tooltip == z - viewStart )
				{
					dspToolTip = Platform.getItemDisplayName( is );

					if ( lineList.size() > 0 )
						dspToolTip = dspToolTip + '\n' + Joiner.on( "\n" ).join( lineList );

					toolPosX = x * (1 + sectionLength) + xo + sectionLength - 8;
					toolPosY = y * offY + yo;
				}

				drawItem( posX, posY, is );

				x++;

				if ( x > 2 )
				{
					y++;
					x = 0;
				}
			}

		}

		if ( tooltip >= 0 && dspToolTip.length() > 0 )
		{
			GL11.glPushAttrib( GL11.GL_ALL_ATTRIB_BITS );
			drawTooltip( toolPosX, toolPosY + 10, 0, dspToolTip );
			GL11.glPopAttrib();
		}

	}

	@Override
	public Enum getSortBy()
	{
		return SortOrder.NAME;
	}

	@Override
	public Enum getSortDir()
	{
		return SortDir.ASCENDING;
	}

	@Override
	public Enum getSortDisplay()
	{
		return ViewItems.ALL;
	}

}
