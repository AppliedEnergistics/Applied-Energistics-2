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
import appeng.block.AEBaseBlock;
import appeng.client.texture.FlippableIcon;
import appeng.client.texture.OffsetIcon;
import appeng.client.texture.TaughtIcon;
import appeng.helpers.Reflected;
import appeng.util.Platform;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;
import org.lwjgl.opengl.GL11;

import java.util.EnumSet;


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
				this.setConnections( n.getConnectedSides() );
			}
			else
			{
				this.getConnections().clear();
			}
		}

		for( final ForgeDirection of : this.getConnections() )
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
	public void renderInventory( final IPartRenderHelper rh, final RenderBlocks renderer )
	{
		GL11.glTranslated( -0.0, -0.0, 0.3 );

		float offU = 0;
		float offV = 9;

		OffsetIcon main = new OffsetIcon( this.getTexture( this.getCableColor() ), offU, offV );
		OffsetIcon ch1 = new OffsetIcon( this.getChannelTex( 4, false ).getIcon(), offU, offV );
		OffsetIcon ch2 = new OffsetIcon( this.getChannelTex( 4, true ).getIcon(), offU, offV );

		for( final ForgeDirection side : EnumSet.of( ForgeDirection.UP, ForgeDirection.DOWN ) )
		{
			rh.setBounds( 5.0f, 5.0f, 2.0f, 11.0f, 11.0f, 14.0f );
			rh.renderInventoryFace( main, side, renderer );
			rh.renderInventoryFace( ch1, side, renderer );
			rh.renderInventoryFace( ch2, side, renderer );
		}

		offU = 9;
		offV = 0;
		main = new OffsetIcon( this.getTexture( this.getCableColor() ), offU, offV );
		ch1 = new OffsetIcon( this.getChannelTex( 4, false ).getIcon(), offU, offV );
		ch2 = new OffsetIcon( this.getChannelTex( 4, true ).getIcon(), offU, offV );

		for( final ForgeDirection side : EnumSet.of( ForgeDirection.EAST, ForgeDirection.WEST ) )
		{
			rh.setBounds( 5.0f, 5.0f, 2.0f, 11.0f, 11.0f, 14.0f );
			rh.renderInventoryFace( main, side, renderer );
			rh.renderInventoryFace( ch1, side, renderer );
			rh.renderInventoryFace( ch2, side, renderer );
		}

		main = new OffsetIcon( this.getTexture( this.getCableColor() ), 0, 0 );
		ch1 = new OffsetIcon( this.getChannelTex( 4, false ).getIcon(), 0, 0 );
		ch2 = new OffsetIcon( this.getChannelTex( 4, true ).getIcon(), 0, 0 );

		for( final ForgeDirection side : EnumSet.of( ForgeDirection.SOUTH, ForgeDirection.NORTH ) )
		{
			rh.setBounds( 5.0f, 5.0f, 2.0f, 11.0f, 11.0f, 14.0f );
			rh.renderInventoryFace( main, side, renderer );
			rh.renderInventoryFace( ch1, side, renderer );
			rh.renderInventoryFace( ch2, side, renderer );
		}

		rh.setTexture( null );
	}

	@Override
	public IIcon getTexture( final AEColor c )
	{
		return this.getSmartTexture( c );
	}

	@Override
	@SideOnly( Side.CLIENT )
	public void renderStatic( final int x, final int y, final int z, final IPartRenderHelper rh, final RenderBlocks renderer )
	{
		this.setRenderCache( rh.useSimplifiedRendering( x, y, z, this, this.getRenderCache() ) );
		rh.setTexture( this.getTexture( this.getCableColor() ) );

		final EnumSet<ForgeDirection> sides = this.getConnections().clone();

		boolean hasBuses = false;
		final IPartHost ph = this.getHost();
		for( final ForgeDirection of : EnumSet.complementOf( this.getConnections() ) )
		{
			final IPart bp = ph.getPart( of );
			if( bp instanceof IGridHost )
			{
				if( of != ForgeDirection.UNKNOWN )
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
					rh.renderBlock( x, y, z, renderer );

					this.setSmartConnectionRotations( of, renderer );
					final IIcon firstIcon = new TaughtIcon( this.getChannelTex( this.getChannelsOnSide()[of.ordinal()], false ).getIcon(), -0.2f );
					final IIcon secondIcon = new TaughtIcon( this.getChannelTex( this.getChannelsOnSide()[of.ordinal()], true ).getIcon(), -0.2f );

					if( of == ForgeDirection.EAST || of == ForgeDirection.WEST )
					{
						final AEBaseBlock blk = (AEBaseBlock) rh.getBlock();
						final FlippableIcon ico = blk.getRendererInstance().getTexture( ForgeDirection.EAST );
						ico.setFlip( false, true );
					}

					Tessellator.instance.setBrightness( 15 << 20 | 15 << 4 );
					Tessellator.instance.setColorOpaque_I( this.getCableColor().blackVariant );
					rh.setTexture( firstIcon, firstIcon, firstIcon, firstIcon, firstIcon, firstIcon );
					this.renderAllFaces( (AEBaseBlock) rh.getBlock(), x, y, z, rh, renderer );

					Tessellator.instance.setColorOpaque_I( this.getCableColor().whiteVariant );
					rh.setTexture( secondIcon, secondIcon, secondIcon, secondIcon, secondIcon, secondIcon );
					this.renderAllFaces( (AEBaseBlock) rh.getBlock(), x, y, z, rh, renderer );

					renderer.uvRotateBottom = renderer.uvRotateEast = renderer.uvRotateNorth = renderer.uvRotateSouth = renderer.uvRotateTop = renderer.uvRotateWest = 0;

					rh.setTexture( this.getTexture( this.getCableColor() ) );
				}
			}
		}

		if( sides.size() != 2 || !this.nonLinear( sides ) || hasBuses )
		{
			for( final ForgeDirection of : this.getConnections() )
			{
				this.renderSmartConnection( x, y, z, rh, renderer, this.getChannelsOnSide()[of.ordinal()], of );
			}

			rh.setTexture( this.getCoveredTexture( this.getCableColor() ) );
			rh.setBounds( 5, 5, 5, 11, 11, 11 );
			rh.renderBlock( x, y, z, renderer );
		}
		else
		{
			ForgeDirection selectedSide = ForgeDirection.UNKNOWN;

			for( final ForgeDirection of : this.getConnections() )
			{
				selectedSide = of;
				break;
			}

			final int channels = this.getChannelsOnSide()[selectedSide.ordinal()];
			final IIcon def = this.getTexture( this.getCableColor() );
			final IIcon off = new OffsetIcon( def, 0, -12 );

			final IIcon firstTaughtIcon = new TaughtIcon( this.getChannelTex( channels, false ).getIcon(), -0.2f );
			final IIcon firstOffsetIcon = new OffsetIcon( firstTaughtIcon, 0, -12 );

			final IIcon secondTaughtIcon = new TaughtIcon( this.getChannelTex( channels, true ).getIcon(), -0.2f );
			final IIcon secondOffsetIcon = new OffsetIcon( secondTaughtIcon, 0, -12 );

			switch( selectedSide )
			{
				case DOWN:
				case UP:
					renderer.setRenderBounds( 5 / 16.0, 0, 5 / 16.0, 11 / 16.0, 16 / 16.0, 11 / 16.0 );
					rh.setTexture( def, def, off, off, off, off );
					rh.renderBlockCurrentBounds( x, y, z, renderer );

					renderer.uvRotateTop = 0;
					renderer.uvRotateBottom = 0;
					renderer.uvRotateSouth = 3;
					renderer.uvRotateEast = 3;

					Tessellator.instance.setBrightness( 15 << 20 | 15 << 4 );

					Tessellator.instance.setColorOpaque_I( this.getCableColor().blackVariant );
					rh.setTexture( firstTaughtIcon, firstTaughtIcon, firstOffsetIcon, firstOffsetIcon, firstOffsetIcon, firstOffsetIcon );
					this.renderAllFaces( (AEBaseBlock) rh.getBlock(), x, y, z, rh, renderer );

					Tessellator.instance.setColorOpaque_I( this.getCableColor().whiteVariant );
					rh.setTexture( secondTaughtIcon, secondTaughtIcon, secondOffsetIcon, secondOffsetIcon, secondOffsetIcon, secondOffsetIcon );
					this.renderAllFaces( (AEBaseBlock) rh.getBlock(), x, y, z, rh, renderer );
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
					final FlippableIcon ico = blk.getRendererInstance().getTexture( ForgeDirection.EAST );
					ico.setFlip( false, true );

					renderer.setRenderBounds( 0, 5 / 16.0, 5 / 16.0, 16 / 16.0, 11 / 16.0, 11 / 16.0 );
					rh.renderBlockCurrentBounds( x, y, z, renderer );

					Tessellator.instance.setBrightness( 15 << 20 | 15 << 4 );

					FlippableIcon fpA = new FlippableIcon( firstTaughtIcon );
					FlippableIcon fpB = new FlippableIcon( secondTaughtIcon );

					fpA = new FlippableIcon( firstTaughtIcon );
					fpB = new FlippableIcon( secondTaughtIcon );

					fpA.setFlip( true, false );
					fpB.setFlip( true, false );

					Tessellator.instance.setColorOpaque_I( this.getCableColor().blackVariant );
					rh.setTexture( firstOffsetIcon, firstOffsetIcon, firstOffsetIcon, firstOffsetIcon, firstTaughtIcon, fpA );
					this.renderAllFaces( (AEBaseBlock) rh.getBlock(), x, y, z, rh, renderer );

					Tessellator.instance.setColorOpaque_I( this.getCableColor().whiteVariant );
					rh.setTexture( secondOffsetIcon, secondOffsetIcon, secondOffsetIcon, secondOffsetIcon, secondTaughtIcon, fpB );
					this.renderAllFaces( (AEBaseBlock) rh.getBlock(), x, y, z, rh, renderer );
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
					rh.renderBlockCurrentBounds( x, y, z, renderer );

					Tessellator.instance.setBrightness( 15 << 20 | 15 << 4 );

					Tessellator.instance.setColorOpaque_I( this.getCableColor().blackVariant );
					rh.setTexture( firstOffsetIcon, firstOffsetIcon, firstTaughtIcon, firstTaughtIcon, firstOffsetIcon, firstOffsetIcon );
					this.renderAllFaces( (AEBaseBlock) rh.getBlock(), x, y, z, rh, renderer );

					Tessellator.instance.setColorOpaque_I( this.getCableColor().whiteVariant );
					rh.setTexture( secondOffsetIcon, secondOffsetIcon, secondTaughtIcon, secondTaughtIcon, secondOffsetIcon, secondOffsetIcon );
					this.renderAllFaces( (AEBaseBlock) rh.getBlock(), x, y, z, rh, renderer );
					break;
				default:
					break;
			}
		}

		renderer.uvRotateBottom = renderer.uvRotateEast = renderer.uvRotateNorth = renderer.uvRotateSouth = renderer.uvRotateTop = renderer.uvRotateWest = 0;
		rh.setTexture( null );
	}
}
