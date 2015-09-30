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
import appeng.block.AEBaseBlock;
import appeng.client.render.ModelGenerator;
import appeng.client.texture.FlippableIcon;
import appeng.client.texture.IAESprite;
import appeng.client.texture.OffsetIcon;
import appeng.client.texture.TaughtIcon;
import appeng.helpers.Reflected;
import appeng.util.Platform;


public class PartCableSmart extends PartCable
{
	@Reflected
	public PartCableSmart( final ItemStack is )
	{
		super( is );
	}

	@MENetworkEventSubscribe
	public void channelUpdated( final MENetworkChannelsChanged c )
	{
		this.getHost().markForUpdate();
	}

	@MENetworkEventSubscribe
	public void powerRender( final MENetworkPowerStatusChange c )
	{
		this.getHost().markForUpdate();
	}

	@Override
	public AECableType getCableConnectionType()
	{
		return AECableType.SMART;
	}

	@Override
	public void getBoxes( final IPartCollisionHelper bch )
	{
		bch.addBox( 5.0, 5.0, 5.0, 11.0, 11.0, 11.0 );

		if( Platform.isServer() )
		{
			final IGridNode n = this.getGridNode();
			if( n != null )
			{
				this.connections = n.getConnectedSides();
			}
			else
			{
				this.connections.clear();
			}
		}

		for( final AEPartLocation of : this.connections )
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
	public void renderInventory( final IPartRenderHelper rh, final ModelGenerator renderer )
	{
		GL11.glTranslated( -0.0, -0.0, 0.3 );

		float offU = 0;
		float offV = 9;

		OffsetIcon main = new OffsetIcon( this.getTexture( this.getCableColor(), renderer ), offU, offV );
		OffsetIcon ch1 = new OffsetIcon( this.getChannelTex( 4, false ).getIcon(), offU, offV );
		OffsetIcon ch2 = new OffsetIcon( this.getChannelTex( 4, true ).getIcon(), offU, offV );

		for( final EnumFacing side : EnumSet.of( EnumFacing.UP, EnumFacing.DOWN ) )
		{
			rh.setBounds( 5.0f, 5.0f, 2.0f, 11.0f, 11.0f, 14.0f );
			rh.renderInventoryFace( main, side, renderer );
			rh.renderInventoryFace( ch1, side, renderer );
			rh.renderInventoryFace( ch2, side, renderer );
		}

		offU = 9;
		offV = 0;
		main = new OffsetIcon( this.getTexture( this.getCableColor(), renderer ), offU, offV );
		ch1 = new OffsetIcon( this.getChannelTex( 4, false ).getIcon(), offU, offV );
		ch2 = new OffsetIcon( this.getChannelTex( 4, true ).getIcon(), offU, offV );

		for( final EnumFacing side : EnumSet.of( EnumFacing.EAST, EnumFacing.WEST ) )
		{
			rh.setBounds( 5.0f, 5.0f, 2.0f, 11.0f, 11.0f, 14.0f );
			rh.renderInventoryFace( main, side, renderer );
			rh.renderInventoryFace( ch1, side, renderer );
			rh.renderInventoryFace( ch2, side, renderer );
		}

		main = new OffsetIcon( this.getTexture( this.getCableColor(), renderer ), 0, 0 );
		ch1 = new OffsetIcon( this.getChannelTex( 4, false ).getIcon(), 0, 0 );
		ch2 = new OffsetIcon( this.getChannelTex( 4, true ).getIcon(), 0, 0 );

		for( final EnumFacing side : EnumSet.of( EnumFacing.SOUTH, EnumFacing.NORTH ) )
		{
			rh.setBounds( 5.0f, 5.0f, 2.0f, 11.0f, 11.0f, 14.0f );
			rh.renderInventoryFace( main, side, renderer );
			rh.renderInventoryFace( ch1, side, renderer );
			rh.renderInventoryFace( ch2, side, renderer );
		}

		rh.setTexture( null );
	}

	@Override
	public IAESprite getTexture( final AEColor c, final ModelGenerator renderer )
	{
		return this.getSmartTexture( c, renderer );
	}

	@Override
	@SideOnly( Side.CLIENT )
	public void renderStatic( final BlockPos pos, final IPartRenderHelper rh, final ModelGenerator renderer )
	{
		rh.setTexture( this.getTexture( this.getCableColor(), renderer ) );

		final EnumSet<AEPartLocation> sides = this.connections.clone();

		boolean hasBuses = false;
		final IPartHost ph = this.getHost();
		for( final AEPartLocation of : EnumSet.complementOf( this.connections ) )
		{
			final IPart bp = ph.getPart( of );
			if( bp instanceof IGridHost )
			{
				if( of != AEPartLocation.INTERNAL )
				{
					sides.add( of );
					hasBuses = true;
				}

				final int len = bp.cableConnectionRenderTo();
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

					this.setSmartConnectionRotations( of, renderer );
					final IAESprite firstIcon = new TaughtIcon( this.getChannelTex( this.channelsOnSide[of.ordinal()], false ).getIcon(), -0.2f );
					final IAESprite secondIcon = new TaughtIcon( this.getChannelTex( this.channelsOnSide[of.ordinal()], true ).getIcon(), -0.2f );

					if( of == AEPartLocation.EAST || of == AEPartLocation.WEST )
					{
						final AEBaseBlock blk = (AEBaseBlock) rh.getBlock();
						final FlippableIcon ico = blk.getRendererInstance().getTexture( AEPartLocation.EAST );
						ico.setFlip( false, true );
					}

					renderer.setBrightness( 15 << 20 | 15 << 4 );
					renderer.setColorOpaque_I( this.getCableColor().blackVariant );
					rh.setTexture( firstIcon, firstIcon, firstIcon, firstIcon, firstIcon, firstIcon );
					this.renderAllFaces( (AEBaseBlock) rh.getBlock(), pos, rh, renderer );

					renderer.setColorOpaque_I( this.getCableColor().whiteVariant );
					rh.setTexture( secondIcon, secondIcon, secondIcon, secondIcon, secondIcon, secondIcon );
					this.renderAllFaces( (AEBaseBlock) rh.getBlock(), pos, rh, renderer );

					renderer.uvRotateBottom = renderer.uvRotateEast = renderer.uvRotateNorth = renderer.uvRotateSouth = renderer.uvRotateTop = renderer.uvRotateWest = 0;

					rh.setTexture( this.getTexture( this.getCableColor(), renderer ) );
				}
			}
		}

		if( sides.size() != 2 || !this.nonLinear( sides ) || hasBuses )
		{
			for( final AEPartLocation of : this.connections )
			{
				this.renderSmartConnection( pos, rh, renderer, this.channelsOnSide[of.ordinal()], of );
			}

			rh.setTexture( this.getCoveredTexture( this.getCableColor(), renderer ) );
			rh.setBounds( 5, 5, 5, 11, 11, 11 );
			rh.renderBlock( pos, renderer );
		}
		else
		{
			AEPartLocation selectedSide = AEPartLocation.INTERNAL;

			for( final AEPartLocation of : this.connections )
			{
				selectedSide = of;
				break;
			}

			final int channels = this.channelsOnSide[selectedSide.ordinal()];
			final IAESprite def = this.getTexture( this.getCableColor(), renderer );
			final IAESprite off = new OffsetIcon( def, 0, -12 );

			final IAESprite firstTaughtIcon = new TaughtIcon( this.getChannelTex( channels, false ).getIcon(), -0.2f );
			final IAESprite firstOffsetIcon = new OffsetIcon( firstTaughtIcon, 0, -12 );

			final IAESprite secondTaughtIcon = new TaughtIcon( this.getChannelTex( channels, true ).getIcon(), -0.2f );
			final IAESprite secondOffsetIcon = new OffsetIcon( secondTaughtIcon, 0, -12 );

			switch( selectedSide )
			{
				case DOWN:
				case UP:
					renderer.setRenderBounds( 5 / 16.0, 0, 5 / 16.0, 11 / 16.0, 16 / 16.0, 11 / 16.0 );
					rh.setTexture( def, def, off, off, off, off );
					rh.renderBlockCurrentBounds( pos, renderer );

					renderer.uvRotateTop = 0;
					renderer.uvRotateBottom = 0;
					renderer.uvRotateSouth = 3;
					renderer.uvRotateEast = 3;

					renderer.setBrightness( 15 << 20 | 15 << 4 );

					renderer.setColorOpaque_I( this.getCableColor().blackVariant );
					rh.setTexture( firstTaughtIcon, firstTaughtIcon, firstOffsetIcon, firstOffsetIcon, firstOffsetIcon, firstOffsetIcon );
					this.renderAllFaces( (AEBaseBlock) rh.getBlock(), pos, rh, renderer );

					renderer.setColorOpaque_I( this.getCableColor().whiteVariant );
					rh.setTexture( secondTaughtIcon, secondTaughtIcon, secondOffsetIcon, secondOffsetIcon, secondOffsetIcon, secondOffsetIcon );
					this.renderAllFaces( (AEBaseBlock) rh.getBlock(), pos, rh, renderer );
					break;
				case EAST:
				case WEST:
					rh.setTexture( off, off, off, off, def, def );
					renderer.uvRotateEast = 2;
					renderer.uvRotateWest = 1;
					renderer.uvRotateBottom = 2;
					renderer.uvRotateTop = 1;
					renderer.uvRotateSouth = 0;
					renderer.uvRotateNorth = 0;

					final AEBaseBlock blk = (AEBaseBlock) rh.getBlock();
					final FlippableIcon ico = blk.getRendererInstance().getTexture( AEPartLocation.EAST );
					ico.setFlip( false, true );

					renderer.setRenderBounds( 0, 5 / 16.0, 5 / 16.0, 16 / 16.0, 11 / 16.0, 11 / 16.0 );
					rh.renderBlockCurrentBounds( pos, renderer );

					renderer.setBrightness( 15 << 20 | 15 << 4 );

					FlippableIcon fpA = new FlippableIcon( firstTaughtIcon );
					FlippableIcon fpB = new FlippableIcon( secondTaughtIcon );

					fpA = new FlippableIcon( firstTaughtIcon );
					fpB = new FlippableIcon( secondTaughtIcon );

					fpA.setFlip( true, false );
					fpB.setFlip( true, false );

					renderer.setColorOpaque_I( this.getCableColor().blackVariant );
					rh.setTexture( firstOffsetIcon, firstOffsetIcon, firstOffsetIcon, firstOffsetIcon, firstTaughtIcon, fpA );
					this.renderAllFaces( (AEBaseBlock) rh.getBlock(), pos, rh, renderer );

					renderer.setColorOpaque_I( this.getCableColor().whiteVariant );
					rh.setTexture( secondOffsetIcon, secondOffsetIcon, secondOffsetIcon, secondOffsetIcon, secondTaughtIcon, fpB );
					this.renderAllFaces( (AEBaseBlock) rh.getBlock(), pos, rh, renderer );
					break;
				case NORTH:
				case SOUTH:
					rh.setTexture( off, off, def, def, off, off );
					renderer.uvRotateTop = 3;
					renderer.uvRotateBottom = 3;
					renderer.uvRotateNorth = 1;
					renderer.uvRotateSouth = 2;
					renderer.uvRotateWest = 1;
					renderer.setRenderBounds( 5 / 16.0, 5 / 16.0, 0, 11 / 16.0, 11 / 16.0, 16 / 16.0 );
					rh.renderBlockCurrentBounds( pos, renderer );

					renderer.setBrightness( 15 << 20 | 15 << 4 );

					renderer.setColorOpaque_I( this.getCableColor().blackVariant );
					rh.setTexture( firstOffsetIcon, firstOffsetIcon, firstTaughtIcon, firstTaughtIcon, firstOffsetIcon, firstOffsetIcon );
					this.renderAllFaces( (AEBaseBlock) rh.getBlock(), pos, rh, renderer );

					renderer.setColorOpaque_I( this.getCableColor().whiteVariant );
					rh.setTexture( secondOffsetIcon, secondOffsetIcon, secondTaughtIcon, secondTaughtIcon, secondOffsetIcon, secondOffsetIcon );
					this.renderAllFaces( (AEBaseBlock) rh.getBlock(), pos, rh, renderer );
					break;
				default:
					break;
			}
		}

		renderer.uvRotateBottom = renderer.uvRotateEast = renderer.uvRotateNorth = renderer.uvRotateSouth = renderer.uvRotateTop = renderer.uvRotateWest = 0;
		rh.setTexture( null );
	}
}
