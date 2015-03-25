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

package appeng.container.implementations;


import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.tileentity.TileEntity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import appeng.api.config.SecurityPermissions;
import appeng.api.parts.IPart;
import appeng.container.AEBaseContainer;
import appeng.container.guisync.GuiSync;
import appeng.helpers.IPriorityHost;
import appeng.util.Platform;


public class ContainerPriority extends AEBaseContainer
{

	final IPriorityHost priHost;

	@SideOnly( Side.CLIENT )
	public GuiTextField textField;
	@GuiSync( 2 )
	public long PriorityValue = -1;

	public ContainerPriority( InventoryPlayer ip, IPriorityHost te )
	{
		super( ip, (TileEntity) ( te instanceof TileEntity ? te : null ), (IPart) ( te instanceof IPart ? te : null ) );
		this.priHost = te;
	}

	@SideOnly( Side.CLIENT )
	public void setTextField( GuiTextField level )
	{
		this.textField = level;
		this.textField.setText( String.valueOf( this.PriorityValue ) );
	}

	public void setPriority( int newValue, EntityPlayer player )
	{
		this.priHost.setPriority( newValue );
		this.PriorityValue = newValue;
	}

	@Override
	public void detectAndSendChanges()
	{
		super.detectAndSendChanges();
		this.verifyPermissions( SecurityPermissions.BUILD, false );

		if( Platform.isServer() )
		{
			this.PriorityValue = this.priHost.getPriority();
		}
	}

	@Override
	public void onUpdate( String field, Object oldValue, Object newValue )
	{
		if( field.equals( "PriorityValue" ) )
		{
			if( this.textField != null )
				this.textField.setText( String.valueOf( this.PriorityValue ) );
		}

		super.onUpdate( field, oldValue, newValue );
	}
}
