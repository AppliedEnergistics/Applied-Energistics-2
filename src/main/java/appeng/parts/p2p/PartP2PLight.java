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

package appeng.parts.p2p;


import appeng.api.networking.IGridNode;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.core.settings.TickRates;
import appeng.me.GridAccessException;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import java.io.IOException;


public class PartP2PLight extends PartP2PTunnel<PartP2PLight> implements IGridTickable
{

	private int lastValue = 0;
	private float opacity = -1;

	public PartP2PLight( final ItemStack is )
	{
		super( is );
	}

	@Override
	public void chanRender( final MENetworkChannelsChanged c )
	{
		this.onTunnelNetworkChange();
		super.chanRender( c );
	}

	@Override
	public void powerRender( final MENetworkPowerStatusChange c )
	{
		this.onTunnelNetworkChange();
		super.powerRender( c );
	}

	@Override
	public void writeToStream( final ByteBuf data ) throws IOException
	{
		super.writeToStream( data );
		data.writeInt( this.isOutput() ? this.lastValue : 0 );
	}

	@Override
	public boolean readFromStream( final ByteBuf data ) throws IOException
	{
		super.readFromStream( data );
		this.lastValue = data.readInt();
		this.setOutput( this.lastValue > 0 );
		return false;
	}

	private boolean doWork()
	{
		if( this.isOutput() )
		{
			return false;
		}

		final TileEntity te = this.getTile();
		final World w = te.getWorldObj();

		final int newLevel = w.getBlockLightValue( te.xCoord + this.getSide().offsetX, te.yCoord + this.getSide().offsetY, te.zCoord + this.getSide().offsetZ );

		if( this.lastValue != newLevel && this.getProxy().isActive() )
		{
			this.lastValue = newLevel;
			try
			{
				for( final PartP2PLight out : this.getOutputs() )
				{
					out.setLightLevel( this.lastValue );
				}
			}
			catch( final GridAccessException e )
			{
				// :P
			}
			return true;
		}
		return false;
	}

	@Override
	public void onNeighborChanged()
	{
		this.opacity = -1;

		this.doWork();

		if( this.isOutput() )
		{
			this.getHost().markForUpdate();
		}
	}

	@Override
	public int getLightLevel()
	{
		if( this.isOutput() && this.isPowered() )
		{
			return this.blockLight( this.lastValue );
		}

		return 0;
	}

	private void setLightLevel( final int out )
	{
		this.lastValue = out;
		this.getHost().markForUpdate();
	}

	private int blockLight( final int emit )
	{
		if( this.opacity < 0 )
		{
			final TileEntity te = this.getTile();
			this.opacity = 255 - te.getWorldObj().getBlockLightOpacity( te.xCoord + this.getSide().offsetX, te.yCoord + this.getSide().offsetY, te.zCoord + this.getSide().offsetZ );
		}

		return (int) ( emit * ( this.opacity / 255.0f ) );
	}

	@Override
	@SideOnly( Side.CLIENT )
	public IIcon getTypeTexture()
	{
		return Blocks.quartz_block.getBlockTextureFromSide( 0 );
	}

	@Override
	public void readFromNBT( final NBTTagCompound tag )
	{
		super.readFromNBT( tag );
		if( tag.hasKey( "opacity" ) )
		{
			this.opacity = tag.getFloat( "opacity" );
		}
		this.lastValue = tag.getInteger( "lastValue" );
	}

	@Override
	public void writeToNBT( final NBTTagCompound tag )
	{
		super.writeToNBT( tag );
		tag.setFloat( "opacity", this.opacity );
		tag.setInteger( "lastValue", this.lastValue );
	}

	@Override
	public void onTunnelConfigChange()
	{
		this.onTunnelNetworkChange();
	}

	@Override
	public void onTunnelNetworkChange()
	{
		if( this.isOutput() )
		{
			final PartP2PLight src = this.getInput();
			if( src != null && src.getProxy().isActive() )
			{
				this.setLightLevel( src.lastValue );
			}
			else
			{
				this.getHost().markForUpdate();
			}
		}
		else
		{
			this.doWork();
		}
	}

	@Override
	public TickingRequest getTickingRequest( final IGridNode node )
	{
		return new TickingRequest( TickRates.LightTunnel.getMin(), TickRates.LightTunnel.getMax(), false, false );
	}

	@Override
	public TickRateModulation tickingRequest( final IGridNode node, final int ticksSinceLastCall )
	{
		return this.doWork() ? TickRateModulation.FASTER : TickRateModulation.SLOWER;
	}

	public float getPowerDrainPerTick()
	{
		return 0.5f;
	}
}
