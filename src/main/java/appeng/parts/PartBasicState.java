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


import java.io.IOException;

import io.netty.buffer.ByteBuf;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import appeng.api.implementations.IPowerChannelState;
import appeng.api.networking.GridFlags;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.parts.IPartRenderHelper;
import appeng.client.texture.CableBusTextures;
import appeng.me.GridAccessException;


public abstract class PartBasicState extends AEBasePart implements IPowerChannelState
{

	protected static final int POWERED_FLAG = 1;
	protected static final int CHANNEL_FLAG = 2;

	protected int clientFlags = 0; // sent as byte.

	public PartBasicState( final ItemStack is )
	{
		super( is );
		this.proxy.setFlags( GridFlags.REQUIRE_CHANNEL );
	}

	@MENetworkEventSubscribe
	public void chanRender( final MENetworkChannelsChanged c )
	{
		this.getHost().markForUpdate();
	}

	@MENetworkEventSubscribe
	public void powerRender( final MENetworkPowerStatusChange c )
	{
		this.getHost().markForUpdate();
	}

	@SideOnly( Side.CLIENT )
	public void renderLights( final int x, final int y, final int z, final IPartRenderHelper rh, final RenderBlocks renderer )
	{
		rh.normalRendering();
		this.setColors( ( this.clientFlags & ( POWERED_FLAG | CHANNEL_FLAG ) ) == ( POWERED_FLAG | CHANNEL_FLAG ), ( this.clientFlags & POWERED_FLAG ) == POWERED_FLAG );
		rh.renderFace( x, y, z, CableBusTextures.PartMonitorSidesStatusLights.getIcon(), ForgeDirection.EAST, renderer );
		rh.renderFace( x, y, z, CableBusTextures.PartMonitorSidesStatusLights.getIcon(), ForgeDirection.WEST, renderer );
		rh.renderFace( x, y, z, CableBusTextures.PartMonitorSidesStatusLights.getIcon(), ForgeDirection.UP, renderer );
		rh.renderFace( x, y, z, CableBusTextures.PartMonitorSidesStatusLights.getIcon(), ForgeDirection.DOWN, renderer );
	}

	public void setColors( final boolean hasChan, final boolean hasPower )
	{
		if( hasChan )
		{
			final int l = 14;
			Tessellator.instance.setBrightness( l << 20 | l << 4 );
			Tessellator.instance.setColorOpaque_I( this.getColor().blackVariant );
		}
		else if( hasPower )
		{
			final int l = 9;
			Tessellator.instance.setBrightness( l << 20 | l << 4 );
			Tessellator.instance.setColorOpaque_I( this.getColor().whiteVariant );
		}
		else
		{
			Tessellator.instance.setBrightness( 0 );
			Tessellator.instance.setColorOpaque_I( 0x000000 );
		}
	}

	@Override
	public void writeToStream( final ByteBuf data ) throws IOException
	{
		super.writeToStream( data );

		this.clientFlags = 0;

		try
		{
			if( this.proxy.getEnergy().isNetworkPowered() )
			{
				this.clientFlags |= POWERED_FLAG;
			}

			if( this.proxy.getNode().meetsChannelRequirements() )
			{
				this.clientFlags |= CHANNEL_FLAG;
			}

			this.clientFlags = this.populateFlags( this.clientFlags );
		}
		catch( final GridAccessException e )
		{
			// meh
		}

		data.writeByte( (byte) this.clientFlags );
	}

	protected int populateFlags( final int cf )
	{
		return cf;
	}

	@Override
	public boolean readFromStream( final ByteBuf data ) throws IOException
	{
		final boolean eh = super.readFromStream( data );

		final int old = this.clientFlags;
		this.clientFlags = data.readByte();

		return eh || old != this.clientFlags;
	}

	@Override
	@SideOnly( Side.CLIENT )
	public IIcon getBreakingTexture()
	{
		return CableBusTextures.PartTransitionPlaneBack.getIcon();
	}

	@Override
	public boolean isPowered()
	{
		return ( this.clientFlags & POWERED_FLAG ) == POWERED_FLAG;
	}

	@Override
	public boolean isActive()
	{
		return ( this.clientFlags & CHANNEL_FLAG ) == CHANNEL_FLAG;
	}
}
