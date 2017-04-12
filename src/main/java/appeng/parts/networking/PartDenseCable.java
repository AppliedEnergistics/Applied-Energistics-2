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


import appeng.api.AEApi;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.parts.BusSupport;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.util.AECableType;
import appeng.api.util.AEColor;
import appeng.block.AEBaseBlock;
import appeng.client.texture.CableBusTextures;
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
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;
import org.lwjgl.opengl.GL11;

import java.util.EnumSet;


public class PartDenseCable extends PartCable
{
	@Reflected
	public PartDenseCable( final ItemStack is )
	{
		super( is );

		this.getProxy().setFlags( GridFlags.DENSE_CAPACITY, GridFlags.PREFERRED );
	}

	@Override
	public BusSupport supportsBuses()
	{
		return BusSupport.DENSE_CABLE;
	}

	@Override
	public AECableType getCableConnectionType()
	{
		return AECableType.DENSE;
	}

	@Override
	public void getBoxes( final IPartCollisionHelper bch )
	{
		final boolean noLadder = !bch.isBBCollision();
		final double min = noLadder ? 3.0 : 4.9;
		final double max = noLadder ? 13.0 : 11.1;

		bch.addBox( min, min, min, max, max, max );

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
			if( this.isDense( of ) )
			{
				switch( of )
				{
					case DOWN:
						bch.addBox( min, 0.0, min, max, min, max );
						break;
					case EAST:
						bch.addBox( max, min, min, 16.0, max, max );
						break;
					case NORTH:
						bch.addBox( min, min, 0.0, max, max, min );
						break;
					case SOUTH:
						bch.addBox( min, min, max, max, max, 16.0 );
						break;
					case UP:
						bch.addBox( min, max, min, max, 16.0, max );
						break;
					case WEST:
						bch.addBox( 0.0, min, min, min, max, max );
						break;
					default:
				}
			}
			else
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
	}

	@Override
	@SideOnly( Side.CLIENT )
	public void renderInventory( final IPartRenderHelper rh, final RenderBlocks renderer )
	{
		GL11.glTranslated( -0.0, -0.0, 0.3 );
		rh.setBounds( 4.0f, 4.0f, 2.0f, 12.0f, 12.0f, 14.0f );

		float offU = 0;
		float offV = 9;

		OffsetIcon main = new OffsetIcon( this.getTexture( this.getCableColor() ), offU, offV );
		OffsetIcon ch1 = new OffsetIcon( this.getChannelTex( 4, false ).getIcon(), offU, offV );
		OffsetIcon ch2 = new OffsetIcon( this.getChannelTex( 4, true ).getIcon(), offU, offV );

		for( final ForgeDirection side : EnumSet.of( ForgeDirection.UP, ForgeDirection.DOWN ) )
		{
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
			rh.renderInventoryFace( main, side, renderer );
			rh.renderInventoryFace( ch1, side, renderer );
			rh.renderInventoryFace( ch2, side, renderer );
		}

		main = new OffsetIcon( this.getTexture( this.getCableColor() ), 0, 0 );
		ch1 = new OffsetIcon( this.getChannelTex( 4, false ).getIcon(), 0, 0 );
		ch2 = new OffsetIcon( this.getChannelTex( 4, true ).getIcon(), 0, 0 );

		for( final ForgeDirection side : EnumSet.of( ForgeDirection.SOUTH, ForgeDirection.NORTH ) )
		{
			rh.renderInventoryFace( main, side, renderer );
			rh.renderInventoryFace( ch1, side, renderer );
			rh.renderInventoryFace( ch2, side, renderer );
		}

		rh.setTexture( null );
	}

	@Override
	public IIcon getTexture( final AEColor c )
	{
		if( c == AEColor.Transparent )
		{
			return AEApi.instance().definitions().parts().cableSmart().stack( AEColor.Transparent, 1 ).getIconIndex();
		}

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
		for( final ForgeDirection of : this.getConnections() )
		{
			if( !this.isDense( of ) )
			{
				hasBuses = true;
			}
		}

		if( sides.size() != 2 || !this.nonLinear( sides ) || hasBuses )
		{
			for( final ForgeDirection of : this.getConnections() )
			{
				if( this.isDense( of ) )
				{
					this.renderDenseConnection( x, y, z, rh, renderer, this.getChannelsOnSide()[of.ordinal()], of );
				}
				else if( this.isSmart( of ) )
				{
					this.renderSmartConnection( x, y, z, rh, renderer, this.getChannelsOnSide()[of.ordinal()], of );
				}
				else
				{
					this.renderCoveredConnection( x, y, z, rh, renderer, this.getChannelsOnSide()[of.ordinal()], of );
				}
			}

			rh.setTexture( this.getDenseTexture( this.getCableColor() ) );
			rh.setBounds( 3, 3, 3, 13, 13, 13 );
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

			final IIcon firstIcon = new TaughtIcon( this.getChannelTex( channels, false ).getIcon(), -0.2f );
			final IIcon firstOffset = new OffsetIcon( firstIcon, 0, -12 );

			final IIcon secondIcon = new TaughtIcon( this.getChannelTex( channels, true ).getIcon(), -0.2f );
			final IIcon secondOffset = new OffsetIcon( secondIcon, 0, -12 );

			switch( selectedSide )
			{
				case DOWN:
				case UP:
					renderer.setRenderBounds( 3 / 16.0, 0, 3 / 16.0, 13 / 16.0, 16 / 16.0, 13 / 16.0 );
					rh.setTexture( def, def, off, off, off, off );
					rh.renderBlockCurrentBounds( x, y, z, renderer );

					renderer.uvRotateTop = 0;
					renderer.uvRotateBottom = 0;
					renderer.uvRotateSouth = 3;
					renderer.uvRotateEast = 3;

					Tessellator.instance.setBrightness( 15 << 20 | 15 << 4 );

					Tessellator.instance.setColorOpaque_I( this.getCableColor().blackVariant );
					rh.setTexture( firstIcon, firstIcon, firstOffset, firstOffset, firstOffset, firstOffset );
					this.renderAllFaces( (AEBaseBlock) rh.getBlock(), x, y, z, rh, renderer );

					Tessellator.instance.setColorOpaque_I( this.getCableColor().whiteVariant );
					rh.setTexture( secondIcon, secondIcon, secondOffset, secondOffset, secondOffset, secondOffset );
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

					renderer.setRenderBounds( 0, 3 / 16.0, 3 / 16.0, 16 / 16.0, 13 / 16.0, 13 / 16.0 );
					rh.renderBlockCurrentBounds( x, y, z, renderer );

					Tessellator.instance.setBrightness( 15 << 20 | 15 << 4 );

					final FlippableIcon fpA = new FlippableIcon( firstIcon );
					final FlippableIcon fpB = new FlippableIcon( secondIcon );

					fpA.setFlip( true, false );
					fpB.setFlip( true, false );

					Tessellator.instance.setColorOpaque_I( this.getCableColor().blackVariant );
					rh.setTexture( firstOffset, firstOffset, firstOffset, firstOffset, firstIcon, fpA );
					this.renderAllFaces( (AEBaseBlock) rh.getBlock(), x, y, z, rh, renderer );

					Tessellator.instance.setColorOpaque_I( this.getCableColor().whiteVariant );
					rh.setTexture( secondOffset, secondOffset, secondOffset, secondOffset, secondIcon, fpB );
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
					renderer.setRenderBounds( 3 / 16.0, 3 / 16.0, 0, 13 / 16.0, 13 / 16.0, 16 / 16.0 );
					rh.renderBlockCurrentBounds( x, y, z, renderer );

					Tessellator.instance.setBrightness( 15 << 20 | 15 << 4 );

					Tessellator.instance.setColorOpaque_I( this.getCableColor().blackVariant );
					rh.setTexture( firstOffset, firstOffset, firstIcon, firstIcon, firstOffset, firstOffset );
					this.renderAllFaces( (AEBaseBlock) rh.getBlock(), x, y, z, rh, renderer );

					Tessellator.instance.setColorOpaque_I( this.getCableColor().whiteVariant );
					rh.setTexture( secondOffset, secondOffset, secondIcon, secondIcon, secondOffset, secondOffset );
					this.renderAllFaces( (AEBaseBlock) rh.getBlock(), x, y, z, rh, renderer );
					break;
				default:
					break;
			}
		}

		renderer.uvRotateBottom = renderer.uvRotateEast = renderer.uvRotateNorth = renderer.uvRotateSouth = renderer.uvRotateTop = renderer.uvRotateWest = 0;
		rh.setTexture( null );
	}

	@SideOnly( Side.CLIENT )
	private void renderDenseConnection( final int x, final int y, final int z, final IPartRenderHelper rh, final RenderBlocks renderer, final int channels, final ForgeDirection of )
	{
		final TileEntity te = this.getTile().getWorldObj().getTileEntity( x + of.offsetX, y + of.offsetY, z + of.offsetZ );
		final IPartHost partHost = te instanceof IPartHost ? (IPartHost) te : null;
		final IGridHost ghh = te instanceof IGridHost ? (IGridHost) te : null;
		AEColor myColor = this.getCableColor();
		/*
		 * ( ghh != null && partHost != null && ghh.getCableConnectionType( of ) == AECableType.GLASS &&
		 * partHost.getPart(
		 * of.getOpposite() ) == null ) { isGlass = true; rh.setTexture( getGlassTexture( myColor = partHost.getColor()
		 * ) );
		 * } else if ( partHost == null && ghh != null && ghh.getCableConnectionType( of ) != AECableType.GLASS ) {
		 * rh.setTexture( getSmartTexture( myColor ) ); switch (of) { case DOWN: rh.setBounds( 3, 0, 3, 13, 4, 13 );
		 * break; case EAST: rh.setBounds( 12, 3, 3, 16, 13, 13 ); break; case NORTH: rh.setBounds( 3, 3, 0, 13, 13, 4
		 * ); break; case SOUTH: rh.setBounds( 3, 3, 12, 13, 13, 16 ); break; case UP: rh.setBounds( 3, 12, 3, 13, 16,
		 * 13 ); break; case WEST: rh.setBounds( 0, 3, 3, 4, 13, 13 ); break; default: return; } rh.renderBlock( x, y,
		 * z, renderer );
		 * if ( true ) { setSmartConnectionRotations( of, renderer ); IIcon firstIcon = new TaughtIcon( getChannelTex(
		 * channels, false ).getIcon(), -0.2f ); IIcon secondIcon = new TaughtIcon( getChannelTex( channels, true
		 * ).getIcon(),
		 * -0.2f );
		 * if ( of == ForgeDirection.EAST || of == ForgeDirection.WEST ) { AEBaseBlock blk = (AEBaseBlock)
		 * rh.getBlock(); FlippableIcon ico = blk.getRendererInstance().getTexture( ForgeDirection.EAST ); ico.setFlip(
		 * false, true ); }
		 * Tessellator.INSTANCE.setBrightness( 15 << 20 | 15 << 5 ); Tessellator.INSTANCE.setColorOpaque_I(
		 * myColor.mediumVariant ); rh.setTexture( firstIcon, firstIcon, firstIcon, firstIcon, firstIcon, firstIcon );
		 * renderAllFaces( (AEBaseBlock)
		 * rh.getBlock(), x, y, z, renderer );
		 * Tessellator.INSTANCE.setColorOpaque_I( myColor.whiteVariant ); rh.setTexture( secondIcon, secondIcon,
		 * secondIcon, secondIcon, secondIcon,
		 * secondIcon ); renderAllFaces( (AEBaseBlock) rh.getBlock(), x, y, z, renderer );
		 * renderer.uvRotateBottom = renderer.uvRotateEast = renderer.uvRotateNorth = renderer.uvRotateSouth =
		 * renderer.uvRotateTop = renderer.uvRotateWest = 0; }
		 * rh.setTexture( getTexture( getCableColor() ) ); }
		 */

		rh.setFacesToRender( EnumSet.complementOf( EnumSet.of( of, of.getOpposite() ) ) );
		if( ghh != null && partHost != null && ghh.getCableConnectionType( of ) != AECableType.GLASS && partHost.getColor() != AEColor.Transparent && partHost.getPart( of.getOpposite() ) == null )
		{
			rh.setTexture( this.getTexture( myColor = partHost.getColor() ) );
		}
		else
		{
			rh.setTexture( this.getTexture( this.getCableColor() ) );
		}

		switch( of )
		{
			case DOWN:
				rh.setBounds( 4, 0, 4, 12, 5, 12 );
				break;
			case EAST:
				rh.setBounds( 11, 4, 4, 16, 12, 12 );
				break;
			case NORTH:
				rh.setBounds( 4, 4, 0, 12, 12, 5 );
				break;
			case SOUTH:
				rh.setBounds( 4, 4, 11, 12, 12, 16 );
				break;
			case UP:
				rh.setBounds( 4, 11, 4, 12, 16, 12 );
				break;
			case WEST:
				rh.setBounds( 0, 4, 4, 5, 12, 12 );
				break;
			default:
				return;
		}

		rh.renderBlock( x, y, z, renderer );

		rh.setFacesToRender( EnumSet.allOf( ForgeDirection.class ) );
		final boolean isGlass = false;
		if( !isGlass )
		{
			this.setSmartConnectionRotations( of, renderer );

			final IIcon firstIcon = new TaughtIcon( this.getChannelTex( channels, false ).getIcon(), -0.2f );
			final IIcon secondIcon = new TaughtIcon( this.getChannelTex( channels, true ).getIcon(), -0.2f );

			Tessellator.instance.setBrightness( 15 << 20 | 15 << 4 );
			Tessellator.instance.setColorOpaque_I( myColor.blackVariant );
			rh.setTexture( firstIcon, firstIcon, firstIcon, firstIcon, firstIcon, firstIcon );
			this.renderAllFaces( (AEBaseBlock) rh.getBlock(), x, y, z, rh, renderer );

			Tessellator.instance.setColorOpaque_I( myColor.whiteVariant );
			rh.setTexture( secondIcon, secondIcon, secondIcon, secondIcon, secondIcon, secondIcon );
			this.renderAllFaces( (AEBaseBlock) rh.getBlock(), x, y, z, rh, renderer );

			renderer.uvRotateBottom = renderer.uvRotateEast = renderer.uvRotateNorth = renderer.uvRotateSouth = renderer.uvRotateTop = renderer.uvRotateWest = 0;
		}
	}

	private boolean isSmart( final ForgeDirection of )
	{
		final TileEntity te = this.getTile().getWorldObj().getTileEntity( this.getTile().xCoord + of.offsetX, this.getTile().yCoord + of.offsetY, this.getTile().zCoord + of.offsetZ );
		if( te instanceof IGridHost )
		{
			final AECableType t = ( (IGridHost) te ).getCableConnectionType( of.getOpposite() );
			return t == AECableType.SMART;
		}
		return false;
	}

	private IIcon getDenseTexture( final AEColor c )
	{
		switch( c )
		{
			case Black:
				return CableBusTextures.MEDense_Black.getIcon();
			case Blue:
				return CableBusTextures.MEDense_Blue.getIcon();
			case Brown:
				return CableBusTextures.MEDense_Brown.getIcon();
			case Cyan:
				return CableBusTextures.MEDense_Cyan.getIcon();
			case Gray:
				return CableBusTextures.MEDense_Gray.getIcon();
			case Green:
				return CableBusTextures.MEDense_Green.getIcon();
			case LightBlue:
				return CableBusTextures.MEDense_LightBlue.getIcon();
			case LightGray:
				return CableBusTextures.MEDense_LightGrey.getIcon();
			case Lime:
				return CableBusTextures.MEDense_Lime.getIcon();
			case Magenta:
				return CableBusTextures.MEDense_Magenta.getIcon();
			case Orange:
				return CableBusTextures.MEDense_Orange.getIcon();
			case Pink:
				return CableBusTextures.MEDense_Pink.getIcon();
			case Purple:
				return CableBusTextures.MEDense_Purple.getIcon();
			case Red:
				return CableBusTextures.MEDense_Red.getIcon();
			case White:
				return CableBusTextures.MEDense_White.getIcon();
			case Yellow:
				return CableBusTextures.MEDense_Yellow.getIcon();
			default:
		}

		return this.getItemStack().getIconIndex();
	}

	private boolean isDense( final ForgeDirection of )
	{
		final TileEntity te = this.getTile().getWorldObj().getTileEntity( this.getTile().xCoord + of.offsetX, this.getTile().yCoord + of.offsetY, this.getTile().zCoord + of.offsetZ );
		if( te instanceof IGridHost )
		{
			final AECableType t = ( (IGridHost) te ).getCableConnectionType( of.getOpposite() );
			return t == AECableType.DENSE;
		}
		return false;
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
}
