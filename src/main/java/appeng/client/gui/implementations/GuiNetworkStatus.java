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


import java.util.List;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import appeng.api.config.Settings;
import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.config.ViewItems;
import appeng.api.implementations.guiobjects.INetworkTool;
import appeng.api.storage.data.IAEItemStack;
import appeng.client.gui.AEBaseGui;
import appeng.client.gui.widgets.GuiImgButton;
import appeng.client.gui.widgets.GuiScrollbar;
import appeng.client.gui.widgets.ISortSource;
import appeng.client.me.ItemRepo;
import appeng.client.me.SlotME;
import appeng.container.implementations.ContainerNetworkStatus;
import appeng.core.AEConfig;
import appeng.core.localization.GuiText;
import appeng.util.Platform;

public class GuiNetworkStatus extends AEBaseGui implements ISortSource
{

	final ItemRepo repo;
	GuiImgButton units;

	final int rows = 4;

	public GuiNetworkStatus(InventoryPlayer inventoryPlayer, INetworkTool te) {
		super( new ContainerNetworkStatus( inventoryPlayer, te ) );
		this.ySize = 153;
		this.xSize = 195;
		this.myScrollBar = new GuiScrollbar();
		this.repo = new ItemRepo( this.myScrollBar, this );
		this.repo.rowSize = 5;
	}

	@Override
	protected void actionPerformed(GuiButton btn)
	{
		super.actionPerformed( btn );

		boolean backwards = Mouse.isButtonDown( 1 );

		if ( btn == this.units )
		{
			AEConfig.instance.nextPowerUnit( backwards );
			this.units.set( AEConfig.instance.selectedPowerUnit() );
		}
	}

	@Override
	public void initGui()
	{
		super.initGui();

		this.units = new GuiImgButton( this.guiLeft - 18, this.guiTop + 8, Settings.POWER_UNITS, AEConfig.instance.selectedPowerUnit() );
		this.buttonList.add( this.units );
	}

	public void postUpdate(List<IAEItemStack> list)
	{
		this.repo.clear();

		for (IAEItemStack is : list)
			this.repo.postUpdate( is );

		this.repo.updateView();
		this.setScrollBar();
	}

	private void setScrollBar()
	{
		int size = this.repo.size();
		this.myScrollBar.setTop( 39 ).setLeft( 175 ).setHeight( 78 );
		this.myScrollBar.setRange( 0, (size + 4) / 5 - this.rows, 1 );
	}

	@Override
	public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY)
	{
		this.bindTexture( "guis/networkstatus.png" );
		this.drawTexturedModalRect( offsetX, offsetY, 0, 0, this.xSize, this.ySize );
	}

	int tooltip = -1;

	@Override
	public void drawScreen(int mouse_x, int mouse_y, float btn)
	{
		int x = 0;
		int y = 0;

		int gx = (this.width - this.xSize) / 2;
		int gy = (this.height - this.ySize) / 2;

		this.tooltip = -1;

		for (int z = 0; z <= 4 * 5; z++)
		{
			int minX = gx + 14 + x * 31;
			int minY = gy + 41 + y * 18;

			if ( minX < mouse_x && minX + 28 > mouse_x )
			{
				if ( minY < mouse_y && minY + 20 > mouse_y )
				{
					this.tooltip = z;
					break;
				}

			}

			x++;

			if ( x > 4 )
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
		ContainerNetworkStatus ns = (ContainerNetworkStatus) this.inventorySlots;

		this.fontRendererObj.drawString( GuiText.NetworkDetails.getLocal(), 8, 6, 4210752 );

		this.fontRendererObj.drawString( GuiText.StoredPower.getLocal() + ": " + Platform.formatPowerLong( ns.currentPower, false ), 13, 16, 4210752 );
		this.fontRendererObj.drawString( GuiText.MaxPower.getLocal() + ": " + Platform.formatPowerLong( ns.maxPower, false ), 13, 26, 4210752 );

		this.fontRendererObj.drawString( GuiText.PowerInputRate.getLocal() + ": " + Platform.formatPowerLong( ns.avgAddition, true ), 13, 143 - 10, 4210752 );
		this.fontRendererObj.drawString( GuiText.PowerUsageRate.getLocal() + ": " + Platform.formatPowerLong( ns.powerUsage, true ), 13, 143 - 20, 4210752 );

		int sectionLength = 30;

		int x = 0;
		int y = 0;
		int xo = 12;
		int yo = 42;
		int viewStart = 0;// myScrollBar.getCurrentScroll() * 5;
		int viewEnd = viewStart + 5 * 4;

		String ToolTip = "";
		int toolPosX = 0;
		int toolPosY = 0;

		for (int z = viewStart; z < Math.min( viewEnd, this.repo.size() ); z++)
		{
			IAEItemStack refStack = this.repo.getReferenceItem( z );
			if ( refStack != null )
			{
				GL11.glPushMatrix();
				GL11.glScaled( 0.5, 0.5, 0.5 );

				String str = Long.toString( refStack.getStackSize() );
				if ( refStack.getStackSize() >= 10000 )
					str = Long.toString( refStack.getStackSize() / 1000 ) + 'k';

				int w = this.fontRendererObj.getStringWidth( str );
				this.fontRendererObj.drawString( str, (int) ((x * sectionLength + xo + sectionLength - 19 - (w * 0.5)) * 2), (y * 18 + yo + 6) * 2,
						4210752 );

				GL11.glPopMatrix();
				int posX = x * sectionLength + xo + sectionLength - 18;
				int posY = y * 18 + yo;

				if ( this.tooltip == z - viewStart )
				{
					ToolTip = Platform.getItemDisplayName( this.repo.getItem( z ) );

					ToolTip = ToolTip + ( '\n' + GuiText.Installed.getLocal() + ": " + (refStack.getStackSize()));
					if ( refStack.getCountRequestable() > 0 )
						ToolTip = ToolTip + ( '\n' + GuiText.EnergyDrain.getLocal() + ": " + Platform.formatPowerLong( refStack.getCountRequestable(), true ));

					toolPosX = x * sectionLength + xo + sectionLength - 8;
					toolPosY = y * 18 + yo;
				}

				this.drawItem( posX, posY, this.repo.getItem( z ) );

				x++;

				if ( x > 4 )
				{
					y++;
					x = 0;
				}
			}

		}

		if ( this.tooltip >= 0 && ToolTip.length() > 0 )
		{
			GL11.glPushAttrib( GL11.GL_ALL_ATTRIB_BITS );
			this.drawTooltip( toolPosX, toolPosY + 10, 0, ToolTip );
			GL11.glPopAttrib();
		}

	}

	// @Override - NEI
	public List<String> handleItemTooltip(ItemStack stack, int mouseX, int mouseY, List<String> currentToolTip)
	{
		if ( stack != null )
		{
			Slot s = this.getSlot( mouseX, mouseY );
			if ( s instanceof SlotME )
			{
				IAEItemStack myStack = null;

				try
				{
					SlotME theSlotField = (SlotME) s;
					myStack = theSlotField.getAEStack();
				}
				catch (Throwable ignore)
				{
				}

				if ( myStack != null )
				{
					while (currentToolTip.size() > 1)
						currentToolTip.remove( 1 );

				}
			}
		}
		return currentToolTip;
	}

	// Vanilla version...
	protected void drawItemStackTooltip(ItemStack stack, int x, int y)
	{
		Slot s = this.getSlot( x, y );
		if ( s instanceof SlotME && stack != null )
		{
			IAEItemStack myStack = null;

			try
			{
				SlotME theSlotField = (SlotME) s;
				myStack = theSlotField.getAEStack();
			}
			catch (Throwable ignore)
			{
			}

			if ( myStack != null )
			{
				List currentToolTip = stack.getTooltip( this.mc.thePlayer, this.mc.gameSettings.advancedItemTooltips );

				while (currentToolTip.size() > 1)
					currentToolTip.remove( 1 );

				currentToolTip.add( GuiText.Installed.getLocal() + ": " + (myStack.getStackSize()) );
				currentToolTip.add( GuiText.EnergyDrain.getLocal() + ": " + Platform.formatPowerLong( myStack.getCountRequestable(), true ) );

				this.drawTooltip( x, y, 0, join( currentToolTip, "\n" ) );
			}
		}
		// super.drawItemStackTooltip( stack, x, y );
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
