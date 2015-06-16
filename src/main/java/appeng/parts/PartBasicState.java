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

package appeng.parts;


import io.netty.buffer.ByteBuf;

import java.io.IOException;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import appeng.api.implementations.IPowerChannelState;
import appeng.api.networking.GridFlags;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.parts.IPartRenderHelper;
import appeng.client.render.IRenderHelper;
import appeng.client.texture.CableBusTextures;
import appeng.me.GridAccessException;


public abstract class PartBasicState extends AEBasePart implements IPowerChannelState
{

	protected static final int POWERED_FLAG = 1;
	protected static final int CHANNEL_FLAG = 2;

	protected int clientFlags = 0; // sent as byte.

	public PartBasicState( ItemStack is )
	{
		super( is );
		this.proxy.setFlags( GridFlags.REQUIRE_CHANNEL );
	}

	@MENetworkEventSubscribe
	public void chanRender( MENetworkChannelsChanged c )
	{
		this.getHost().markForUpdate();
	}

	@MENetworkEventSubscribe
	public void powerRender( MENetworkPowerStatusChange c )
	{
		this.getHost().markForUpdate();
	}

	@SideOnly( Side.CLIENT )
	public void renderLights( BlockPos pos, IPartRenderHelper rh, IRenderHelper renderer )
	{
		this.setColors( renderer, ( this.clientFlags & ( this.POWERED_FLAG | this.CHANNEL_FLAG ) ) == ( this.POWERED_FLAG | this.CHANNEL_FLAG ), ( this.clientFlags & this.POWERED_FLAG ) == this.POWERED_FLAG );
		rh.renderFace( pos, CableBusTextures.PartMonitorSidesStatusLights.getIcon(), EnumFacing.EAST, renderer );
		rh.renderFace( pos, CableBusTextures.PartMonitorSidesStatusLights.getIcon(), EnumFacing.WEST, renderer );
		rh.renderFace( pos, CableBusTextures.PartMonitorSidesStatusLights.getIcon(), EnumFacing.UP, renderer );
		rh.renderFace( pos, CableBusTextures.PartMonitorSidesStatusLights.getIcon(), EnumFacing.DOWN, renderer );
	}

	public void setColors( IRenderHelper renderer, boolean hasChan, boolean hasPower )
	{
		if( hasChan )
		{
			int l = 14;
			renderer.setBrightness( l << 20 | l << 4 );
			renderer.setColorOpaque_I( this.getColor().blackVariant );
		}
		else if( hasPower )
		{
			int l = 9;
			renderer.setBrightness( l << 20 | l << 4 );
			renderer.setColorOpaque_I( this.getColor().whiteVariant );
		}
		else
		{
			renderer.setBrightness( 0 );
			renderer.setColorOpaque_I( 0x000000 );
		}
	}

	@Override
	public void writeToStream( ByteBuf data ) throws IOException
	{
		super.writeToStream( data );

		this.clientFlags = 0;

		try
		{
			if( this.proxy.getEnergy().isNetworkPowered() )
			{
				this.clientFlags |= this.POWERED_FLAG;
			}

			if( this.proxy.getNode().meetsChannelRequirements() )
			{
				this.clientFlags |= this.CHANNEL_FLAG;
			}

			this.clientFlags = this.populateFlags( this.clientFlags );
		}
		catch( GridAccessException e )
		{
			// meh
		}

		data.writeByte( (byte) this.clientFlags );
	}

	protected int populateFlags( int cf )
	{
		return cf;
	}

	@Override
	public boolean readFromStream( ByteBuf data ) throws IOException
	{
		boolean eh = super.readFromStream( data );

		int old = this.clientFlags;
		this.clientFlags = data.readByte();

		return eh || old != this.clientFlags;
	}

	@Override
	@SideOnly( Side.CLIENT )
	public TextureAtlasSprite getBreakingTexture( IRenderHelper renderer )
	{
		return CableBusTextures.PartTransitionPlaneBack.getIcon().getAtlas();
	}

	@Override
	public boolean isPowered()
	{
		return ( this.clientFlags & this.POWERED_FLAG ) == this.POWERED_FLAG;
	}

	@Override
	public boolean isActive()
	{
		return ( this.clientFlags & this.CHANNEL_FLAG ) == this.CHANNEL_FLAG;
	}
}
