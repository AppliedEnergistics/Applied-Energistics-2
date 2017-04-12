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


import appeng.api.AEApi;
import appeng.api.definitions.IBlocks;
import appeng.api.definitions.IDefinitions;
import appeng.api.definitions.IParts;
import appeng.client.gui.AEBaseGui;
import appeng.client.gui.widgets.GuiNumberBox;
import appeng.client.gui.widgets.GuiTabButton;
import appeng.container.AEBaseContainer;
import appeng.container.implementations.ContainerPriority;
import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.core.localization.GuiText;
import appeng.core.sync.GuiBridge;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketSwitchGuis;
import appeng.core.sync.packets.PacketValueConfig;
import appeng.helpers.IPriorityHost;
import appeng.parts.automation.PartFormationPlane;
import appeng.parts.misc.PartInterface;
import appeng.parts.misc.PartStorageBus;
import appeng.tile.misc.TileInterface;
import appeng.tile.storage.TileChest;
import appeng.tile.storage.TileDrive;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

import java.io.IOException;


public class GuiPriority extends AEBaseGui
{

	private GuiNumberBox priority;
	private GuiTabButton originalGuiBtn;

	private GuiButton plus1;
	private GuiButton plus10;
	private GuiButton plus100;
	private GuiButton plus1000;
	private GuiButton minus1;
	private GuiButton minus10;
	private GuiButton minus100;
	private GuiButton minus1000;

	private GuiBridge OriginalGui;

	public GuiPriority( final InventoryPlayer inventoryPlayer, final IPriorityHost te )
	{
		super( new ContainerPriority( inventoryPlayer, te ) );
	}

	@Override
	public void initGui()
	{
		super.initGui();

		final int a = AEConfig.instance.priorityByStacksAmounts( 0 );
		final int b = AEConfig.instance.priorityByStacksAmounts( 1 );
		final int c = AEConfig.instance.priorityByStacksAmounts( 2 );
		final int d = AEConfig.instance.priorityByStacksAmounts( 3 );

		this.buttonList.add( this.plus1 = new GuiButton( 0, this.guiLeft + 20, this.guiTop + 32, 22, 20, "+" + a ) );
		this.buttonList.add( this.plus10 = new GuiButton( 0, this.guiLeft + 48, this.guiTop + 32, 28, 20, "+" + b ) );
		this.buttonList.add( this.plus100 = new GuiButton( 0, this.guiLeft + 82, this.guiTop + 32, 32, 20, "+" + c ) );
		this.buttonList.add( this.plus1000 = new GuiButton( 0, this.guiLeft + 120, this.guiTop + 32, 38, 20, "+" + d ) );

		this.buttonList.add( this.minus1 = new GuiButton( 0, this.guiLeft + 20, this.guiTop + 69, 22, 20, "-" + a ) );
		this.buttonList.add( this.minus10 = new GuiButton( 0, this.guiLeft + 48, this.guiTop + 69, 28, 20, "-" + b ) );
		this.buttonList.add( this.minus100 = new GuiButton( 0, this.guiLeft + 82, this.guiTop + 69, 32, 20, "-" + c ) );
		this.buttonList.add( this.minus1000 = new GuiButton( 0, this.guiLeft + 120, this.guiTop + 69, 38, 20, "-" + d ) );

		ItemStack myIcon = null;
		final Object target = ( (AEBaseContainer) this.inventorySlots ).getTarget();
		final IDefinitions definitions = AEApi.instance().definitions();
		final IParts parts = definitions.parts();
		final IBlocks blocks = definitions.blocks();

		if( target instanceof PartStorageBus )
		{
			for( final ItemStack storageBusStack : parts.storageBus().maybeStack( 1 ).asSet() )
			{
				myIcon = storageBusStack;
			}
			this.OriginalGui = GuiBridge.GUI_STORAGEBUS;
		}

		if( target instanceof PartFormationPlane )
		{
			for( final ItemStack formationPlaneStack : parts.formationPlane().maybeStack( 1 ).asSet() )
			{
				myIcon = formationPlaneStack;
			}
			this.OriginalGui = GuiBridge.GUI_FORMATION_PLANE;
		}

		if( target instanceof TileDrive )
		{
			for( final ItemStack driveStack : blocks.drive().maybeStack( 1 ).asSet() )
			{
				myIcon = driveStack;
			}

			this.OriginalGui = GuiBridge.GUI_DRIVE;
		}

		if( target instanceof TileChest )
		{
			for( final ItemStack chestStack : blocks.chest().maybeStack( 1 ).asSet() )
			{
				myIcon = chestStack;
			}

			this.OriginalGui = GuiBridge.GUI_CHEST;
		}

		if( target instanceof TileInterface )
		{
			for( final ItemStack interfaceStack : blocks.iface().maybeStack( 1 ).asSet() )
			{
				myIcon = interfaceStack;
			}

			this.OriginalGui = GuiBridge.GUI_INTERFACE;
		}

		if( target instanceof PartInterface )
		{
			for( final ItemStack interfaceStack : parts.iface().maybeStack( 1 ).asSet() )
			{
				myIcon = interfaceStack;
			}
			this.OriginalGui = GuiBridge.GUI_INTERFACE;
		}

		if( this.OriginalGui != null && myIcon != null )
		{
			this.buttonList.add( this.originalGuiBtn = new GuiTabButton( this.guiLeft + 154, this.guiTop, myIcon, myIcon.getDisplayName(), itemRender ) );
		}

		this.priority = new GuiNumberBox( this.fontRendererObj, this.guiLeft + 62, this.guiTop + 57, 59, this.fontRendererObj.FONT_HEIGHT, Long.class );
		this.priority.setEnableBackgroundDrawing( false );
		this.priority.setMaxStringLength( 16 );
		this.priority.setTextColor( 0xFFFFFF );
		this.priority.setVisible( true );
		this.priority.setFocused( true );
		( (ContainerPriority) this.inventorySlots ).setTextField( this.priority );
	}

	@Override
	public void drawFG( final int offsetX, final int offsetY, final int mouseX, final int mouseY )
	{
		this.fontRendererObj.drawString( GuiText.Priority.getLocal(), 8, 6, 4210752 );
	}

	@Override
	public void drawBG( final int offsetX, final int offsetY, final int mouseX, final int mouseY )
	{
		this.bindTexture( "guis/priority.png" );
		this.drawTexturedModalRect( offsetX, offsetY, 0, 0, this.xSize, this.ySize );

		this.priority.drawTextBox();
	}

	@Override
	protected void actionPerformed( final GuiButton btn )
	{
		super.actionPerformed( btn );

		if( btn == this.originalGuiBtn )
		{
			NetworkHandler.instance.sendToServer( new PacketSwitchGuis( this.OriginalGui ) );
		}

		final boolean isPlus = btn == this.plus1 || btn == this.plus10 || btn == this.plus100 || btn == this.plus1000;
		final boolean isMinus = btn == this.minus1 || btn == this.minus10 || btn == this.minus100 || btn == this.minus1000;

		if( isPlus || isMinus )
		{
			this.addQty( this.getQty( btn ) );
		}
	}

	private void addQty( final int i )
	{
		try
		{
			String out = this.priority.getText();

			boolean fixed = false;
			while( out.startsWith( "0" ) && out.length() > 1 )
			{
				out = out.substring( 1 );
				fixed = true;
			}

			if( fixed )
			{
				this.priority.setText( out );
			}

			if( out.isEmpty() )
			{
				out = "0";
			}

			long result = Long.parseLong( out );
			result += i;

			this.priority.setText( out = Long.toString( result ) );

			NetworkHandler.instance.sendToServer( new PacketValueConfig( "PriorityHost.Priority", out ) );
		}
		catch( final NumberFormatException e )
		{
			// nope..
			this.priority.setText( "0" );
		}
		catch( final IOException e )
		{
			AELog.debug( e );
		}
	}

	@Override
	protected void keyTyped( final char character, final int key )
	{
		if( !this.checkHotbarKeys( key ) )
		{
			if( ( key == 211 || key == 205 || key == 203 || key == 14 || character == '-' || Character.isDigit( character ) ) && this.priority.textboxKeyTyped( character, key ) )
			{
				try
				{
					String out = this.priority.getText();

					boolean fixed = false;
					while( out.startsWith( "0" ) && out.length() > 1 )
					{
						out = out.substring( 1 );
						fixed = true;
					}

					if( fixed )
					{
						this.priority.setText( out );
					}

					if( out.isEmpty() )
					{
						out = "0";
					}

					NetworkHandler.instance.sendToServer( new PacketValueConfig( "PriorityHost.Priority", out ) );
				}
				catch( final IOException e )
				{
					AELog.debug( e );
				}
			}
			else
			{
				super.keyTyped( character, key );
			}
		}
	}

	protected String getBackground()
	{
		return "guis/priority.png";
	}
}
