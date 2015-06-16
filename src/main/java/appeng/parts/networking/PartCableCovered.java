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

package appeng.parts.networking;


import java.util.EnumSet;

import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;

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
import appeng.api.util.AEPartLocation;
import appeng.client.render.IRenderHelper;
import appeng.client.texture.IAESprite;
import appeng.client.texture.OffsetIcon;
import appeng.helpers.Reflected;
import appeng.util.Platform;


public class PartCableCovered extends PartCable
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
	public AECableType getCableConnectionType()
	{
		return AECableType.COVERED;
	}

	@Override
	public void getBoxes( IPartCollisionHelper bch )
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

		for( AEPartLocation of : this.connections )
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
	public void renderInventory( IPartRenderHelper rh, IRenderHelper renderer )
	{
		GL11.glTranslated( -0.0, -0.0, 0.3 );

		rh.setBounds( 5.0f, 5.0f, 2.0f, 11.0f, 11.0f, 14.0f );
		float offU = 0;
		float offV = 9;

		OffsetIcon main = new OffsetIcon( this.getTexture( this.getCableColor(), renderer ), offU, offV );

		for( EnumFacing side : EnumSet.of( EnumFacing.UP, EnumFacing.DOWN ) )
		{
			rh.renderInventoryFace( main, side, renderer );
		}

		offU = 9;
		offV = 0;
		main = new OffsetIcon( this.getTexture( this.getCableColor(), renderer ), offU, offV );

		for( EnumFacing side : EnumSet.of( EnumFacing.EAST, EnumFacing.WEST ) )
		{
			rh.renderInventoryFace( main, side, renderer );
		}

		main = new OffsetIcon( this.getTexture( this.getCableColor(), renderer ), 0, 0 );

		for( EnumFacing side : EnumSet.of( EnumFacing.SOUTH, EnumFacing.NORTH ) )
		{
			rh.renderInventoryFace( main, side, renderer );
		}

		rh.setTexture( null );
	}

	@Override
	public IAESprite getTexture( AEColor c, IRenderHelper renderer )
	{
		return this.getCoveredTexture( c, renderer );
	}

	@Override
	@SideOnly( Side.CLIENT )
	public void renderStatic( BlockPos pos, IPartRenderHelper rh, IRenderHelper renderer )
	{
		rh.setTexture( this.getTexture( this.getCableColor(), renderer ) );

		EnumSet<AEPartLocation> sides = this.connections.clone();

		boolean hasBuses = false;
		IPartHost ph = this.getHost();
		for( AEPartLocation of : EnumSet.complementOf( this.connections ) )
		{
			IPart bp = ph.getPart( of );
			if( bp instanceof IGridHost )
			{
				if( of != AEPartLocation.INTERNAL )
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
					rh.renderBlock( pos, renderer );
				}
			}
		}

		if( sides.size() != 2 || !this.nonLinear( sides ) || hasBuses )
		{
			for( AEPartLocation of : this.connections )
			{
				this.renderCoveredConnection( pos, rh, renderer, this.channelsOnSide[of.ordinal()], of );
			}

			rh.setTexture( this.getTexture( this.getCableColor(), renderer ) );
			rh.setBounds( 5, 5, 5, 11, 11, 11 );
			rh.renderBlock( pos, renderer );
		}
		else
		{
			IAESprite def = this.getTexture( this.getCableColor(), renderer );
			IAESprite off = new OffsetIcon( def, 0, -12 );
			for( AEPartLocation of : this.connections )
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

			rh.renderBlockCurrentBounds( pos, renderer );
		}

		renderer.uvRotateBottom = renderer.uvRotateEast = renderer.uvRotateNorth = renderer.uvRotateSouth = renderer.uvRotateTop = renderer.uvRotateWest = 0;
		rh.setTexture( null );
	}
}
