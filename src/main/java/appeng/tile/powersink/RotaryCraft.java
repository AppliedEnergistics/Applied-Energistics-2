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

package appeng.tile.powersink;


import Reika.RotaryCraft.API.Interfaces.Transducerable;
import Reika.RotaryCraft.API.Power.AdvancedShaftPowerReceiver;
import appeng.api.config.PowerUnits;
import appeng.integration.IntegrationType;
import appeng.tile.TileEvent;
import appeng.tile.events.TileEventType;
import appeng.transformer.annotations.Integration.Interface;
import appeng.transformer.annotations.Integration.InterfaceList;
import appeng.transformer.annotations.Integration.Method;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.ArrayList;


@InterfaceList( value = { @Interface( iname = IntegrationType.RotaryCraft, iface = "Reika.RotaryCraft.API.Power.AdvancedShaftPowerReceiver" ), @Interface( iname = IntegrationType.RotaryCraft, iface = "Reika.RotaryCraft.API.Interfaces.Transducerable" ) } )
public abstract class RotaryCraft extends IC2 implements AdvancedShaftPowerReceiver, Transducerable
{

	private int omega = 0;
	private int torque = 0;
	private long power = 0;
	private int alpha = 0;

	private long currentPower = 0;

	@TileEvent( TileEventType.TICK )
	@Method( iname = IntegrationType.RotaryCraft )
	public void Tick_RotaryCraft()
	{
		if( this.worldObj != null && !this.worldObj.isRemote && this.currentPower > 0 )
		{
			this.injectExternalPower( PowerUnits.WA, this.currentPower );
			this.currentPower = 0;
		}
	}

	@Override
	public final boolean addPower( final int torque, final int omega, final long power, final ForgeDirection side )
	{
		this.omega = omega;
		this.torque = torque;
		this.power = power;

		this.currentPower += power;

		return true;

	}

	@Override
	public final int getOmega()
	{
		return this.omega;
	}

	@Override
	public final int getTorque()
	{
		return this.torque;
	}

	@Override
	public final long getPower()
	{
		return this.power;
	}

	@Override
	public final String getName()
	{
		return "AE";
	}

	@Override
	public final int getIORenderAlpha()
	{
		return this.alpha;
	}

	@Override
	public final void setIORenderAlpha( final int io )
	{
		this.alpha = io;
	}

	public final boolean canReadFromBlock( final int x, final int y, final int z )
	{
		ForgeDirection side = ForgeDirection.UNKNOWN;

		if( x == this.xCoord - 1 )
		{
			side = ForgeDirection.WEST;
		}
		else if( x == this.xCoord + 1 )
		{
			side = ForgeDirection.EAST;
		}
		else if( z == this.zCoord - 1 )
		{
			side = ForgeDirection.NORTH;
		}
		else if( z == this.zCoord + 1 )
		{
			side = ForgeDirection.SOUTH;
		}
		else if( y == this.yCoord - 1 )
		{
			side = ForgeDirection.DOWN;
		}
		else if( y == this.yCoord + 1 )
		{
			side = ForgeDirection.UP;
		}

		return this.getPowerSides().contains( side );
	}

	@Override
	public final boolean canReadFrom( final ForgeDirection side )
	{
		return this.getPowerSides().contains( side );
	}

	@Override
	public final boolean isReceiving()
	{
		return true;
	}

	@Override
	public final int getMinTorque( final int available )
	{
		return 1;
	}

	@Override
	public final ArrayList<String> getMessages( final World world, final int x, final int y, final int z, final int side )
	{
		final String out;
		if( this.power >= 1000000000 )
		{
			out = String.format( "Receiving %.3f GW @ %d rad/s.", this.power / 1000000000.0D, this.omega );
		}
		else if( this.power >= 1000000 )
		{
			out = String.format( "Receiving %.3f MW @ %d rad/s.", this.power / 1000000.0D, this.omega );
		}
		else if( this.power >= 1000 )
		{
			out = String.format( "Receiving %.3f kW @ %d rad/s.", this.power / 1000.0D, this.omega );
		}
		else
		{
			out = String.format( "Receiving %d W @ %d rad/s.", this.power, this.omega );
		}

		final ArrayList<String> messages = new ArrayList<String>( 1 );
		messages.add( out );
		return messages;
	}
}
