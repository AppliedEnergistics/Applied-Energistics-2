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

package appeng.client.gui.implementations;


import java.awt.Color;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraftforge.fluids.FluidStack;

import appeng.api.storage.ITerminalHost;
import appeng.client.gui.AEBaseGui;
import appeng.container.implementations.ContainerFluidTerm;


/**
 * @author BrockWS
 * @version rv6 - 12/05/2018
 * @since rv6 12/05/2018
 */
public class GuiFluidTerm extends AEBaseGui
{
	protected ITerminalHost terminalHost;

	public GuiFluidTerm( InventoryPlayer inventoryPlayer, ITerminalHost terminalHost )
	{
		super( new ContainerFluidTerm( inventoryPlayer, terminalHost ) );
		this.terminalHost = terminalHost;
	}

	@Override
	public void drawFG( int offsetX, int offsetY, int mouseX, int mouseY )
	{
		int textColor = Color.RED.getRGB();
		int y = 0;

		ContainerFluidTerm c = (ContainerFluidTerm) this.inventorySlots;

		Type listType = new TypeToken<ArrayList<String>>()
		{
		}.getType();
		GsonBuilder gsonBuilder = new GsonBuilder();
		Gson gson = gsonBuilder.create();
		List<String> list = gson.fromJson( c.fluids, listType );

		this.fontRenderer.drawString( "Fluids", 0, y, textColor );
		if( list == null )
		{
			return;
		}
		y += 16;
		if( list.isEmpty() )
		{
			this.fontRenderer.drawString( "Storage Empty", 0, y, textColor );
		}
		else
		{
			for( String str : list )
			{
				FluidStack stack = null;
				try
				{
					stack = FluidStack.loadFluidStackFromNBT( JsonToNBT.getTagFromJson( str ) );
				}
				catch( NBTException e )
				{
					e.printStackTrace();
				}
				if( stack == null )
				{
					continue;
				}
				this.fontRenderer.drawString( stack.getLocalizedName() + ": " + stack.amount, 0, y, textColor );
				y += 16;
			}
		}
	}

	@Override
	public void drawBG( int offsetX, int offsetY, int mouseX, int mouseY )
	{

	}
}
