/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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

package appeng.parts.networking;


import java.util.EnumSet;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.util.AECableType;
import appeng.api.util.AEColor;
import appeng.client.texture.OffsetIcon;
import appeng.helpers.Reflected;
import appeng.util.Platform;


public final class PartCableCovered extends PartCable
{
	@Reflected
	public PartCableCovered( ItemStack is )
	{
		super( is );
	}

	@MENetworkEventSubscribe
	public void channelUpdated( MENetworkChannelsChanged c )
	{
		this.getHost().markForUpdate();
	}

	@MENetworkEventSubscribe
	public void powerRender( MENetworkPowerStatusChange c )
	{
		this.getHost().markForUpdate();
	}

	@Override
	public final AECableType getCableConnectionType()
	{
		return AECableType.COVERED;
	}

	@Override
	public final void getBoxes( IPartCollisionHelper bch )
	{
		bch.addBox( 5.0, 5.0, 5.0, 11.0, 11.0, 11.0 );

		if( Platform.isServer() )
		{
			IGridNode n = this.getGridNode();
			if( n != null )
			{
				this.connections = n.getConnectedSides();
			}
			else
			{
				this.connections.clear();
			}
		}

		for( ForgeDirection of : this.connections )
		{
			switch( of )
			{
				case DOWN:
					bch.addBox( 5.0, 0.0, 5.0, 11.0, 5.0, 11.0 );
					break;
				case EAST:
					bch.addBox( 11.0, 5.0, 5.0, 16.0, 11.0, 11.0 );
					break;
				case NORTH:
					bch.addBox( 5.0, 5.0, 0.0, 11.0, 11.0, 5.0 );
					break;
				case SOUTH:
					bch.addBox( 5.0, 5.0, 11.0, 11.0, 11.0, 16.0 );
					break;
				case UP:
					bch.addBox( 5.0, 11.0, 5.0, 11.0, 16.0, 11.0 );
					break;
				case WEST:
					bch.addBox( 0.0, 5.0, 5.0, 5.0, 11.0, 11.0 );
					break;
				default:
			}
		}
	}

	@Override
	@SideOnly( Side.CLIENT )
	public final void renderInventory( IPartRenderHelper rh, RenderBlocks renderer )
	{
		GL11.glTranslated( -0.0, -0.0, 0.3 );

		rh.setBounds( 5.0f, 5.0f, 2.0f, 11.0f, 11.0f, 14.0f );
		float offU = 0;
		float offV = 9;

		OffsetIcon main = new OffsetIcon( this.getTexture( this.getCableColor() ), offU, offV );

		for( ForgeDirection side : EnumSet.of( ForgeDirection.UP, ForgeDirection.DOWN ) )
		{
			rh.renderInventoryFace( main, side, renderer );
		}

		offU = 9;
		offV = 0;
		main = new OffsetIcon( this.getTexture( this.getCableColor() ), offU, offV );

		for( ForgeDirection side : EnumSet.of( ForgeDirection.EAST, ForgeDirection.WEST ) )
		{
			rh.renderInventoryFace( main, side, renderer );
		}

		main = new OffsetIcon( this.getTexture( this.getCableColor() ), 0, 0 );

		for( ForgeDirection side : EnumSet.of( ForgeDirection.SOUTH, ForgeDirection.NORTH ) )
		{
			rh.renderInventoryFace( main, side, renderer );
		}

		rh.setTexture( null );
	}

	@Override
	public final IIcon getTexture( AEColor c )
	{
		return this.getCoveredTexture( c );
	}

	@Override
	@SideOnly( Side.CLIENT )
	public final void renderStatic( int x, int y, int z, IPartRenderHelper rh, RenderBlocks renderer )
	{
		this.renderCache = rh.useSimplifiedRendering( x, y, z, this, this.renderCache );
		rh.setTexture( this.getTexture( this.getCableColor() ) );

		EnumSet<ForgeDirection> sides = this.connections.clone();

		boolean hasBuses = false;
		IPartHost ph = this.getHost();
		for( ForgeDirection of : EnumSet.complementOf( this.connections ) )
		{
			IPart bp = ph.getPart( of );
			if( bp instanceof IGridHost )
			{
				if( of != ForgeDirection.UNKNOWN )
				{
					sides.add( of );
					hasBuses = true;
				}

				int len = bp.cableConnectionRenderTo();
				if( len < 8 )
				{
					switch( of )
					{
						case DOWN:
							rh.setBounds( 6, len, 6, 10, 5, 10 );
							break;
						case EAST:
							rh.setBounds( 11, 6, 6, 16 - len, 10, 10 );
							break;
						case NORTH:
							rh.setBounds( 6, 6, len, 10, 10, 5 );
							break;
						case SOUTH:
							rh.setBounds( 6, 6, 11, 10, 10, 16 - len );
							break;
						case UP:
							rh.setBounds( 6, 11, 6, 10, 16 - len, 10 );
							break;
						case WEST:
							rh.setBounds( len, 6, 6, 5, 10, 10 );
							break;
						default:
							continue;
					}
					rh.renderBlock( x, y, z, renderer );
				}
			}
		}

		if( sides.size() != 2 || !this.nonLinear( sides ) || hasBuses )
		{
			for( ForgeDirection of : this.connections )
			{
				this.renderCoveredConnection( x, y, z, rh, renderer, this.channelsOnSide[of.ordinal()], of );
			}

			rh.setTexture( this.getTexture( this.getCableColor() ) );
			rh.setBounds( 5, 5, 5, 11, 11, 11 );
			rh.renderBlock( x, y, z, renderer );
		}
		else
		{
			IIcon def = this.getTexture( this.getCableColor() );
			IIcon off = new OffsetIcon( def, 0, -12 );
			for( ForgeDirection of : this.connections )
			{
				switch( of )
				{
					case DOWN:
					case UP:
						rh.setTexture( def, def, off, off, off, off );
						renderer.setRenderBounds( 5 / 16.0, 0, 5 / 16.0, 11 / 16.0, 16 / 16.0, 11 / 16.0 );
						break;
					case EAST:
					case WEST:
						rh.setTexture( off, off, off, off, def, def );
						renderer.uvRotateEast = renderer.uvRotateWest = 1;
						renderer.uvRotateBottom = renderer.uvRotateTop = 1;
						renderer.setRenderBounds( 0, 5 / 16.0, 5 / 16.0, 16 / 16.0, 11 / 16.0, 11 / 16.0 );
						break;
					case NORTH:
					case SOUTH:
						rh.setTexture( off, off, def, def, off, off );
						renderer.uvRotateNorth = renderer.uvRotateSouth = 1;
						renderer.setRenderBounds( 5 / 16.0, 5 / 16.0, 0, 11 / 16.0, 11 / 16.0, 16 / 16.0 );
						break;
					default:
				}
			}

			rh.renderBlockCurrentBounds( x, y, z, renderer );
		}

		renderer.uvRotateBottom = renderer.uvRotateEast = renderer.uvRotateNorth = renderer.uvRotateSouth = renderer.uvRotateTop = renderer.uvRotateWest = 0;
		rh.setTexture( null );
	}
}
