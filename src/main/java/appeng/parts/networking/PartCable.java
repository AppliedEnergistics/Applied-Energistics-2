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


import java.io.IOException;
import java.util.EnumSet;

import org.lwjgl.opengl.GL11;

import io.netty.buffer.ByteBuf;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.api.AEApi;
import appeng.api.config.SecurityPermissions;
import appeng.api.definitions.IParts;
import appeng.api.implementations.parts.IPartCable;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridConnection;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.parts.BusSupport;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.util.AECableType;
import appeng.api.util.AEColor;
import appeng.api.util.AEColoredItemDefinition;
import appeng.api.util.AEPartLocation;
import appeng.api.util.IReadOnlyCollection;
import appeng.block.AEBaseBlock;
import appeng.client.render.ModelGenerator;
import appeng.client.texture.CableBusTextures;
import appeng.client.texture.FlippableIcon;
import appeng.client.texture.IAESprite;
import appeng.client.texture.TaughtIcon;
import appeng.items.parts.ItemMultiPart;
import appeng.me.GridAccessException;
import appeng.parts.AEBasePart;
import appeng.util.Platform;


public class PartCable extends AEBasePart implements IPartCable
{

	private final int[] channelsOnSide = { 0, 0, 0, 0, 0, 0 };

	private EnumSet<AEPartLocation> connections = EnumSet.noneOf( AEPartLocation.class );
	private boolean powered = false;

	public PartCable( final ItemStack is )
	{
		super( is );
		this.getProxy().setFlags( GridFlags.PREFERRED );
		this.getProxy().setIdlePowerUsage( 0.0 );
		this.getProxy().setColor( AEColor.values()[( (ItemMultiPart) is.getItem() ).variantOf( is.getItemDamage() )] );
	}

	@Override
	public BusSupport supportsBuses()
	{
		return BusSupport.CABLE;
	}

	@Override
	public AEColor getCableColor()
	{
		return this.getProxy().getColor();
	}

	@Override
	public AECableType getCableConnectionType()
	{
		return AECableType.GLASS;
	}

	@Override
	public boolean changeColor( final AEColor newColor, final EntityPlayer who )
	{
		if( this.getCableColor() != newColor )
		{
			ItemStack newPart = null;

			final IParts parts = AEApi.instance().definitions().parts();

			if( this.getCableConnectionType() == AECableType.GLASS )
			{
				newPart = parts.cableGlass().stack( newColor, 1 );
			}
			else if( this.getCableConnectionType() == AECableType.COVERED )
			{
				newPart = parts.cableCovered().stack( newColor, 1 );
			}
			else if( this.getCableConnectionType() == AECableType.SMART )
			{
				newPart = parts.cableSmart().stack( newColor, 1 );
			}
			else if( this.getCableConnectionType() == AECableType.DENSE )
			{
				newPart = parts.cableDense().stack( newColor, 1 );
			}

			boolean hasPermission = true;

			try
			{
				hasPermission = this.getProxy().getSecurity().hasPermission( who, SecurityPermissions.BUILD );
			}
			catch( final GridAccessException e )
			{
				// :P
			}

			if( newPart != null && hasPermission )
			{
				if( Platform.isClient() )
				{
					return true;
				}

				this.getHost().removePart( AEPartLocation.INTERNAL, true );
				this.getHost().addPart( newPart, AEPartLocation.INTERNAL, who );
				return true;
			}
		}
		return false;
	}

	@Override
	public void setValidSides( final EnumSet<EnumFacing> sides )
	{
		this.getProxy().setValidSides( sides );
	}

	@Override
	public boolean isConnected( final EnumFacing side )
	{
		return this.getConnections().contains( AEPartLocation.fromFacing( side ) );
	}

	public void markForUpdate()
	{
		this.getHost().markForUpdate();
	}

	@Override
	public void getBoxes( final IPartCollisionHelper bch )
	{
		bch.addBox( 6.0, 6.0, 6.0, 10.0, 10.0, 10.0 );

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

		final IPartHost ph = this.getHost();
		if( ph != null )
		{
			for( final AEPartLocation dir : AEPartLocation.SIDE_LOCATIONS )
			{
				final IPart p = ph.getPart( dir );
				if( p instanceof IGridHost )
				{
					final double dist = p.cableConnectionRenderTo();

					if( dist > 8 )
					{
						continue;
					}

					switch( dir )
					{
						case DOWN:
							bch.addBox( 6.0, dist, 6.0, 10.0, 6.0, 10.0 );
							break;
						case EAST:
							bch.addBox( 10.0, 6.0, 6.0, 16.0 - dist, 10.0, 10.0 );
							break;
						case NORTH:
							bch.addBox( 6.0, 6.0, dist, 10.0, 10.0, 6.0 );
							break;
						case SOUTH:
							bch.addBox( 6.0, 6.0, 10.0, 10.0, 10.0, 16.0 - dist );
							break;
						case UP:
							bch.addBox( 6.0, 10.0, 6.0, 10.0, 16.0 - dist, 10.0 );
							break;
						case WEST:
							bch.addBox( dist, 6.0, 6.0, 6.0, 10.0, 10.0 );
							break;
						default:
					}
				}
			}
		}

		for( final AEPartLocation of : this.getConnections() )
		{
			switch( of )
			{
				case DOWN:
					bch.addBox( 6.0, 0.0, 6.0, 10.0, 6.0, 10.0 );
					break;
				case EAST:
					bch.addBox( 10.0, 6.0, 6.0, 16.0, 10.0, 10.0 );
					break;
				case NORTH:
					bch.addBox( 6.0, 6.0, 0.0, 10.0, 10.0, 6.0 );
					break;
				case SOUTH:
					bch.addBox( 6.0, 6.0, 10.0, 10.0, 10.0, 16.0 );
					break;
				case UP:
					bch.addBox( 6.0, 10.0, 6.0, 10.0, 16.0, 10.0 );
					break;
				case WEST:
					bch.addBox( 0.0, 6.0, 6.0, 6.0, 10.0, 10.0 );
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

		rh.setTexture( this.getTexture( this.getCableColor(), renderer ) );
		rh.setBounds( 6.0f, 6.0f, 2.0f, 10.0f, 10.0f, 14.0f );
		rh.renderInventoryBox( renderer );
		rh.setTexture( null );
	}

	public IAESprite getTexture( final AEColor c, final ModelGenerator renderer )
	{
		return this.getGlassTexture( c, renderer );
	}

	public IAESprite getGlassTexture( final AEColor c, final ModelGenerator renderer )
	{
		switch( c )
		{
			case Black:
				return CableBusTextures.MECable_Black.getIcon();
			case Blue:
				return CableBusTextures.MECable_Blue.getIcon();
			case Brown:
				return CableBusTextures.MECable_Brown.getIcon();
			case Cyan:
				return CableBusTextures.MECable_Cyan.getIcon();
			case Gray:
				return CableBusTextures.MECable_Grey.getIcon();
			case Green:
				return CableBusTextures.MECable_Green.getIcon();
			case LightBlue:
				return CableBusTextures.MECable_LightBlue.getIcon();
			case LightGray:
				return CableBusTextures.MECable_LightGrey.getIcon();
			case Lime:
				return CableBusTextures.MECable_Lime.getIcon();
			case Magenta:
				return CableBusTextures.MECable_Magenta.getIcon();
			case Orange:
				return CableBusTextures.MECable_Orange.getIcon();
			case Pink:
				return CableBusTextures.MECable_Pink.getIcon();
			case Purple:
				return CableBusTextures.MECable_Purple.getIcon();
			case Red:
				return CableBusTextures.MECable_Red.getIcon();
			case White:
				return CableBusTextures.MECable_White.getIcon();
			case Yellow:
				return CableBusTextures.MECable_Yellow.getIcon();
			default:
		}

		final AEColoredItemDefinition glassCable = AEApi.instance().definitions().parts().cableGlass();
		final ItemStack glassCableStack = glassCable.stack( AEColor.Transparent, 1 );

		return renderer.getIcon( glassCableStack );
	}

	@Override
	@SideOnly( Side.CLIENT )
	public void renderStatic( final BlockPos pos, final IPartRenderHelper rh, final ModelGenerator renderer )
	{
		boolean useCovered = false;
		boolean requireDetailed = false;

		for( final AEPartLocation dir : AEPartLocation.SIDE_LOCATIONS )
		{
			final IPart p = this.getHost().getPart( dir );
			if( p instanceof IGridHost )
			{
				final IGridHost igh = (IGridHost) p;
				final AECableType type = igh.getCableConnectionType( dir.getOpposite() );
				if( type == AECableType.COVERED || type == AECableType.SMART )
				{
					useCovered = true;
					break;
				}
			}
			else if( this.getConnections().contains( dir ) )
			{
				final TileEntity te = this.getTile().getWorld().getTileEntity( pos.offset( dir.getFacing() ) );
				final IPartHost partHost = te instanceof IPartHost ? (IPartHost) te : null;
				final IGridHost gh = te instanceof IGridHost ? (IGridHost) te : null;
				if( partHost == null && gh != null && gh.getCableConnectionType( dir ) != AECableType.GLASS )
				{
					requireDetailed = true;
				}
			}
		}

		if( useCovered )
		{
			rh.setTexture( this.getCoveredTexture( this.getCableColor(), renderer ) );
		}
		else
		{
			rh.setTexture( this.getTexture( this.getCableColor(), renderer ) );
		}

		final IPartHost ph = this.getHost();
		for( final AEPartLocation of : EnumSet.complementOf( this.getConnections() ) )
		{
			final IPart bp = ph.getPart( of );
			if( bp instanceof IGridHost )
			{
				final int len = bp.cableConnectionRenderTo();
				if( len < 8 )
				{
					switch( of )
					{
						case DOWN:
							rh.setBounds( 6, len, 6, 10, 6, 10 );
							break;
						case EAST:
							rh.setBounds( 10, 6, 6, 16 - len, 10, 10 );
							break;
						case NORTH:
							rh.setBounds( 6, 6, len, 10, 10, 6 );
							break;
						case SOUTH:
							rh.setBounds( 6, 6, 10, 10, 10, 16 - len );
							break;
						case UP:
							rh.setBounds( 6, 10, 6, 10, 16 - len, 10 );
							break;
						case WEST:
							rh.setBounds( len, 6, 6, 6, 10, 10 );
							break;
						default:
							continue;
					}
					rh.renderBlock( pos, renderer );
				}
			}
		}

		if( this.getConnections().size() != 2 || !this.nonLinear( this.getConnections() ) || useCovered || requireDetailed )
		{
			if( useCovered )
			{
				rh.setBounds( 5, 5, 5, 11, 11, 11 );
				rh.renderBlock( pos, renderer );
			}
			else
			{
				rh.setBounds( 6, 6, 6, 10, 10, 10 );
				rh.renderBlock( pos, renderer );
			}

			for( final AEPartLocation of : this.getConnections() )
			{
				this.renderGlassConnection( pos, rh, renderer, of );
			}
		}
		else
		{
			final IAESprite def = this.getTexture( this.getCableColor(), renderer );
			rh.setTexture( def );

			for( final AEPartLocation of : this.getConnections() )
			{
				rh.setFacesToRender( EnumSet.complementOf( EnumSet.of( of.getFacing(), of.getFacing().getOpposite() ) ) );
				switch( of )
				{
					case DOWN:
					case UP:
						renderer.setRenderBounds( 6 / 16.0, 0, 6 / 16.0, 10 / 16.0, 16 / 16.0, 10 / 16.0 );
						break;
					case EAST:
					case WEST:
						renderer.setUvRotateEast( renderer.setUvRotateWest( 1 ) );
						renderer.setUvRotateBottom( renderer.setUvRotateTop( 1 ) );
						renderer.setRenderBounds( 0, 6 / 16.0, 6 / 16.0, 16 / 16.0, 10 / 16.0, 10 / 16.0 );
						break;
					case NORTH:
					case SOUTH:
						renderer.setUvRotateNorth( renderer.setUvRotateSouth( 1 ) );
						renderer.setRenderBounds( 6 / 16.0, 6 / 16.0, 0, 10 / 16.0, 10 / 16.0, 16 / 16.0 );
						break;
					default:
				}
			}

			rh.renderBlockCurrentBounds( pos, renderer );
		}

		rh.setFacesToRender( EnumSet.allOf( EnumFacing.class ) );
		rh.setTexture( null );
	}

	@Override
	public void writeToNBT( final NBTTagCompound data )
	{
		super.writeToNBT( data );

		if( Platform.isServer() )
		{
			final IGridNode node = this.getGridNode();

			if( node != null )
			{
				int howMany = 0;
				for( final IGridConnection gc : node.getConnections() )
				{
					howMany = Math.max( gc.getUsedChannels(), howMany );
				}

				data.setByte( "usedChannels", (byte) howMany );
			}
		}
	}

	@Override
	public void writeToStream( final ByteBuf data ) throws IOException
	{
		int cs = 0;
		int sideOut = 0;

		final IGridNode n = this.getGridNode();
		if( n != null )
		{
			for( final AEPartLocation thisSide : AEPartLocation.SIDE_LOCATIONS )
			{
				final IPart part = this.getHost().getPart( thisSide );
				if( part != null )
				{
					if( part.getGridNode() != null )
					{
						final IReadOnlyCollection<IGridConnection> set = part.getGridNode().getConnections();
						for( final IGridConnection gc : set )
						{
							if( this.getProxy().getNode().hasFlag( GridFlags.DENSE_CAPACITY ) && gc.getOtherSide( this.getProxy().getNode() ).hasFlag( GridFlags.DENSE_CAPACITY ) )
							{
								sideOut |= ( gc.getUsedChannels() / 4 ) << ( 4 * thisSide.ordinal() );
							}
							else
							{
								sideOut |= ( gc.getUsedChannels() ) << ( 4 * thisSide.ordinal() );
							}
						}
					}
				}
			}

			for( final IGridConnection gc : n.getConnections() )
			{
				final AEPartLocation side = gc.getDirection( n );
				if( side != AEPartLocation.INTERNAL )
				{
					final boolean isTier2a = this.getProxy().getNode().hasFlag( GridFlags.DENSE_CAPACITY );
					final boolean isTier2b = gc.getOtherSide( this.getProxy().getNode() ).hasFlag( GridFlags.DENSE_CAPACITY );

					if( isTier2a && isTier2b )
					{
						sideOut |= ( gc.getUsedChannels() / 4 ) << ( 4 * side.ordinal() );
					}
					else
					{
						sideOut |= gc.getUsedChannels() << ( 4 * side.ordinal() );
					}
					cs |= ( 1 << side.ordinal() );
				}
			}
		}

		try
		{
			if( this.getProxy().getEnergy().isNetworkPowered() )
			{
				cs |= ( 1 << AEPartLocation.INTERNAL.ordinal() );
			}
		}
		catch( final GridAccessException e )
		{
			// aww...
		}

		data.writeByte( (byte) cs );
		data.writeInt( sideOut );
	}

	@Override
	public boolean readFromStream( final ByteBuf data ) throws IOException
	{
		final int cs = data.readByte();
		final int sideOut = data.readInt();

		final EnumSet<AEPartLocation> myC = this.getConnections().clone();
		final boolean wasPowered = this.powered;
		this.powered = false;
		boolean channelsChanged = false;

		for( final AEPartLocation d : AEPartLocation.values() )
		{
			if( d != AEPartLocation.INTERNAL )
			{
				final int ch = ( sideOut >> ( d.ordinal() * 4 ) ) & 0xF;
				if( ch != this.getChannelsOnSide( d.ordinal() ) )
				{
					channelsChanged = true;
					this.setChannelsOnSide( d.ordinal(), ch );
				}
			}

			if( d == AEPartLocation.INTERNAL )
			{
				final int id = 1 << d.ordinal();
				if( id == ( cs & id ) )
				{
					this.powered = true;
				}
			}
			else
			{
				final int id = 1 << d.ordinal();
				if( id == ( cs & id ) )
				{
					this.getConnections().add( d );
				}
				else
				{
					this.getConnections().remove( d );
				}
			}
		}

		return !myC.equals( this.getConnections() ) || wasPowered != this.powered || channelsChanged;
	}

	@Override
	@SideOnly( Side.CLIENT )
	public TextureAtlasSprite getBreakingTexture( final ModelGenerator renderer )
	{
		return this.getTexture( this.getCableColor(), renderer ).getAtlas();
	}

	public IAESprite getCoveredTexture( final AEColor c, final ModelGenerator renderer )
	{
		switch( c )
		{
			case Black:
				return CableBusTextures.MECovered_Black.getIcon();
			case Blue:
				return CableBusTextures.MECovered_Blue.getIcon();
			case Brown:
				return CableBusTextures.MECovered_Brown.getIcon();
			case Cyan:
				return CableBusTextures.MECovered_Cyan.getIcon();
			case Gray:
				return CableBusTextures.MECovered_Gray.getIcon();
			case Green:
				return CableBusTextures.MECovered_Green.getIcon();
			case LightBlue:
				return CableBusTextures.MECovered_LightBlue.getIcon();
			case LightGray:
				return CableBusTextures.MECovered_LightGrey.getIcon();
			case Lime:
				return CableBusTextures.MECovered_Lime.getIcon();
			case Magenta:
				return CableBusTextures.MECovered_Magenta.getIcon();
			case Orange:
				return CableBusTextures.MECovered_Orange.getIcon();
			case Pink:
				return CableBusTextures.MECovered_Pink.getIcon();
			case Purple:
				return CableBusTextures.MECovered_Purple.getIcon();
			case Red:
				return CableBusTextures.MECovered_Red.getIcon();
			case White:
				return CableBusTextures.MECovered_White.getIcon();
			case Yellow:
				return CableBusTextures.MECovered_Yellow.getIcon();
			default:
		}

		final AEColoredItemDefinition coveredCable = AEApi.instance().definitions().parts().cableCovered();
		final ItemStack coveredCableStack = coveredCable.stack( AEColor.Transparent, 1 );

		return renderer.getIcon( coveredCableStack );
	}

	protected boolean nonLinear( final EnumSet<AEPartLocation> sides )
	{
		return ( sides.contains( AEPartLocation.EAST ) && sides.contains( AEPartLocation.WEST ) ) || ( sides.contains( AEPartLocation.NORTH ) && sides.contains( AEPartLocation.SOUTH ) ) || ( sides.contains( AEPartLocation.UP ) && sides.contains( AEPartLocation.DOWN ) );
	}

	@SideOnly( Side.CLIENT )
	private void renderGlassConnection( final BlockPos pos, final IPartRenderHelper rh, final ModelGenerator renderer, final AEPartLocation of )
	{
		final TileEntity te = this.getTile().getWorld().getTileEntity( pos.offset( of.getFacing() ) );
		final IPartHost partHost = te instanceof IPartHost ? (IPartHost) te : null;
		final IGridHost gh = te instanceof IGridHost ? (IGridHost) te : null;

		rh.setFacesToRender( EnumSet.complementOf( EnumSet.of( of.getFacing() ) ) );

		if( gh != null && partHost != null && gh.getCableConnectionType( of.getOpposite() ) == AECableType.GLASS && partHost.getColor() != AEColor.Transparent && partHost.getPart( of.getOpposite() ) == null )
		{
			rh.setTexture( this.getTexture( partHost.getColor(), renderer ) );
		}
		else if( partHost == null && gh != null && gh.getCableConnectionType( of.getOpposite() ) != AECableType.GLASS )
		{
			rh.setTexture( this.getCoveredTexture( this.getCableColor(), renderer ) );
			switch( of )
			{
				case DOWN:
					rh.setBounds( 5, 0, 5, 11, 4, 11 );
					break;
				case EAST:
					rh.setBounds( 12, 5, 5, 16, 11, 11 );
					break;
				case NORTH:
					rh.setBounds( 5, 5, 0, 11, 11, 4 );
					break;
				case SOUTH:
					rh.setBounds( 5, 5, 12, 11, 11, 16 );
					break;
				case UP:
					rh.setBounds( 5, 12, 5, 11, 16, 11 );
					break;
				case WEST:
					rh.setBounds( 0, 5, 5, 4, 11, 11 );
					break;
				default:
					return;
			}

			rh.renderBlock( pos, renderer );
			rh.setTexture( this.getTexture( this.getCableColor(), renderer ) );
		}
		else
		{
			rh.setTexture( this.getTexture( this.getCableColor(), renderer ) );
		}

		switch( of )
		{
			case DOWN:
				rh.setBounds( 6, 0, 6, 10, 6, 10 );
				break;
			case EAST:
				rh.setBounds( 10, 6, 6, 16, 10, 10 );
				break;
			case NORTH:
				rh.setBounds( 6, 6, 0, 10, 10, 6 );
				break;
			case SOUTH:
				rh.setBounds( 6, 6, 10, 10, 10, 16 );
				break;
			case UP:
				rh.setBounds( 6, 10, 6, 10, 16, 10 );
				break;
			case WEST:
				rh.setBounds( 0, 6, 6, 6, 10, 10 );
				break;
			default:
				return;
		}

		rh.renderBlock( pos, renderer );
		rh.setFacesToRender( EnumSet.allOf( EnumFacing.class ) );
	}

	@SideOnly( Side.CLIENT )
	protected void renderCoveredConnection( final BlockPos pos, final IPartRenderHelper rh, final ModelGenerator renderer, final int channels, final AEPartLocation of )
	{
		final TileEntity te = this.getTile().getWorld().getTileEntity( pos.offset( of.getFacing() ) );
		final IPartHost partHost = te instanceof IPartHost ? (IPartHost) te : null;
		final IGridHost ghh = te instanceof IGridHost ? (IGridHost) te : null;

		rh.setFacesToRender( EnumSet.complementOf( EnumSet.of( of.getFacing() ) ) );
		if( ghh != null && partHost != null && ghh.getCableConnectionType( of.getOpposite() ) == AECableType.GLASS && partHost.getPart( of.getOpposite() ) == null && partHost.getColor() != AEColor.Transparent )
		{
			rh.setTexture( this.getGlassTexture( partHost.getColor(), renderer ) );
		}
		else if( partHost == null && ghh != null && ghh.getCableConnectionType( of.getOpposite() ) != AECableType.GLASS )
		{
			rh.setTexture( this.getCoveredTexture( this.getCableColor(), renderer ) );
			switch( of )
			{
				case DOWN:
					rh.setBounds( 5, 0, 5, 11, 4, 11 );
					break;
				case EAST:
					rh.setBounds( 12, 5, 5, 16, 11, 11 );
					break;
				case NORTH:
					rh.setBounds( 5, 5, 0, 11, 11, 4 );
					break;
				case SOUTH:
					rh.setBounds( 5, 5, 12, 11, 11, 16 );
					break;
				case UP:
					rh.setBounds( 5, 12, 5, 11, 16, 11 );
					break;
				case WEST:
					rh.setBounds( 0, 5, 5, 4, 11, 11 );
					break;
				default:
					return;
			}

			rh.renderBlock( pos, renderer );

			rh.setTexture( this.getTexture( this.getCableColor(), renderer ) );
		}
		else if( ghh != null && partHost != null && ghh.getCableConnectionType( of.getOpposite() ) == AECableType.COVERED && partHost.getColor() != AEColor.Transparent && partHost.getPart( of.getOpposite() ) == null )
		{
			rh.setTexture( this.getCoveredTexture( partHost.getColor(), renderer ) );
		}
		else
		{
			rh.setTexture( this.getCoveredTexture( this.getCableColor(), renderer ) );
		}

		switch( of )
		{
			case DOWN:
				rh.setBounds( 6, 0, 6, 10, 5, 10 );
				break;
			case EAST:
				rh.setBounds( 11, 6, 6, 16, 10, 10 );
				break;
			case NORTH:
				rh.setBounds( 6, 6, 0, 10, 10, 5 );
				break;
			case SOUTH:
				rh.setBounds( 6, 6, 11, 10, 10, 16 );
				break;
			case UP:
				rh.setBounds( 6, 11, 6, 10, 16, 10 );
				break;
			case WEST:
				rh.setBounds( 0, 6, 6, 5, 10, 10 );
				break;
			default:
				return;
		}

		rh.renderBlock( pos, renderer );
		rh.setFacesToRender( EnumSet.allOf( EnumFacing.class ) );
		rh.setFacesToRender( EnumSet.allOf( EnumFacing.class ) );
	}

	@SideOnly( Side.CLIENT )
	protected void renderSmartConnection( final BlockPos pos, final IPartRenderHelper rh, final ModelGenerator renderer, final int channels, final AEPartLocation of )
	{
		final TileEntity te = this.getTile().getWorld().getTileEntity( pos.offset( of.getFacing() ) );
		final IPartHost partHost = te instanceof IPartHost ? (IPartHost) te : null;
		final IGridHost ghh = te instanceof IGridHost ? (IGridHost) te : null;
		AEColor myColor = this.getCableColor();

		rh.setFacesToRender( EnumSet.complementOf( EnumSet.of( of.getFacing() ) ) );

		boolean isGlass = false;
		if( ghh != null && partHost != null && ghh.getCableConnectionType( of.getOpposite() ) == AECableType.GLASS && partHost.getPart( of.getOpposite() ) == null && partHost.getColor() != AEColor.Transparent )
		{
			isGlass = true;
			rh.setTexture( this.getGlassTexture( myColor = partHost.getColor(), renderer ) );
		}
		else if( partHost == null && ghh != null && ghh.getCableConnectionType( of.getOpposite() ) != AECableType.GLASS )
		{
			rh.setTexture( this.getSmartTexture( myColor, renderer ) );
			switch( of )
			{
				case DOWN:
					rh.setBounds( 5, 0, 5, 11, 4, 11 );
					break;
				case EAST:
					rh.setBounds( 12, 5, 5, 16, 11, 11 );
					break;
				case NORTH:
					rh.setBounds( 5, 5, 0, 11, 11, 4 );
					break;
				case SOUTH:
					rh.setBounds( 5, 5, 12, 11, 11, 16 );
					break;
				case UP:
					rh.setBounds( 5, 12, 5, 11, 16, 11 );
					break;
				case WEST:
					rh.setBounds( 0, 5, 5, 4, 11, 11 );
					break;
				default:
					return;
			}
			rh.renderBlock( pos, renderer );

			this.setSmartConnectionRotations( of, renderer );
			final IAESprite firstIcon = new TaughtIcon( this.getChannelTex( channels, false ).getIcon(), -0.2f );
			final IAESprite secondIcon = new TaughtIcon( this.getChannelTex( channels, true ).getIcon(), -0.2f );

			if( of == AEPartLocation.EAST || of == AEPartLocation.WEST )
			{
				final AEBaseBlock blk = (AEBaseBlock) rh.getBlock();
				final FlippableIcon ico = blk.getRendererInstance().getTexture( AEPartLocation.EAST );
				ico.setFlip( false, true );
			}

			renderer.setBrightness( 15 << 20 | 15 << 4 );
			renderer.setColorOpaque_I( myColor.blackVariant );
			rh.setTexture( firstIcon, firstIcon, firstIcon, firstIcon, firstIcon, firstIcon );
			this.renderAllFaces( (AEBaseBlock) rh.getBlock(), pos, rh, renderer );

			renderer.setColorOpaque_I( myColor.whiteVariant );
			rh.setTexture( secondIcon, secondIcon, secondIcon, secondIcon, secondIcon, secondIcon );
			this.renderAllFaces( (AEBaseBlock) rh.getBlock(), pos, rh, renderer );

			renderer.setUvRotateBottom( renderer.setUvRotateEast( renderer.setUvRotateNorth( renderer.setUvRotateSouth( renderer.setUvRotateTop( renderer.setUvRotateWest( 0 ) ) ) ) ) );

			rh.setTexture( this.getTexture( this.getCableColor(), renderer ) );
		}

		else if( ghh != null && partHost != null && ghh.getCableConnectionType( of.getOpposite() ) != AECableType.GLASS && partHost.getColor() != AEColor.Transparent && partHost.getPart( of.getOpposite() ) == null )
		{
			rh.setTexture( this.getSmartTexture( myColor = partHost.getColor(), renderer ) );
		}
		else
		{
			rh.setTexture( this.getSmartTexture( this.getCableColor(), renderer ) );
		}

		switch( of )
		{
			case DOWN:
				rh.setBounds( 6, 0, 6, 10, 5, 10 );
				break;
			case EAST:
				rh.setBounds( 11, 6, 6, 16, 10, 10 );
				break;
			case NORTH:
				rh.setBounds( 6, 6, 0, 10, 10, 5 );
				break;
			case SOUTH:
				rh.setBounds( 6, 6, 11, 10, 10, 16 );
				break;
			case UP:
				rh.setBounds( 6, 11, 6, 10, 16, 10 );
				break;
			case WEST:
				rh.setBounds( 0, 6, 6, 5, 10, 10 );
				break;
			default:
				return;
		}

		rh.renderBlock( pos, renderer );
		rh.setFacesToRender( EnumSet.allOf( EnumFacing.class ) );

		if( !isGlass )
		{
			this.setSmartConnectionRotations( of, renderer );

			final IAESprite firstIcon = new TaughtIcon( this.getChannelTex( channels, false ).getIcon(), -0.2f );
			final IAESprite secondIcon = new TaughtIcon( this.getChannelTex( channels, true ).getIcon(), -0.2f );

			renderer.setBrightness( 15 << 20 | 15 << 4 );
			renderer.setColorOpaque_I( myColor.blackVariant );
			rh.setTexture( firstIcon, firstIcon, firstIcon, firstIcon, firstIcon, firstIcon );
			this.renderAllFaces( (AEBaseBlock) rh.getBlock(), pos, rh, renderer );

			renderer.setColorOpaque_I( myColor.whiteVariant );
			rh.setTexture( secondIcon, secondIcon, secondIcon, secondIcon, secondIcon, secondIcon );
			this.renderAllFaces( (AEBaseBlock) rh.getBlock(), pos, rh, renderer );

			renderer.setUvRotateBottom( renderer.setUvRotateEast( renderer.setUvRotateNorth( renderer.setUvRotateSouth( renderer.setUvRotateTop( renderer.setUvRotateWest( 0 ) ) ) ) ) );
		}
	}

	public IAESprite getSmartTexture( final AEColor c, final ModelGenerator renderer )
	{
		switch( c )
		{
			case Black:
				return CableBusTextures.MESmart_Black.getIcon();
			case Blue:
				return CableBusTextures.MESmart_Blue.getIcon();
			case Brown:
				return CableBusTextures.MESmart_Brown.getIcon();
			case Cyan:
				return CableBusTextures.MESmart_Cyan.getIcon();
			case Gray:
				return CableBusTextures.MESmart_Gray.getIcon();
			case Green:
				return CableBusTextures.MESmart_Green.getIcon();
			case LightBlue:
				return CableBusTextures.MESmart_LightBlue.getIcon();
			case LightGray:
				return CableBusTextures.MESmart_LightGrey.getIcon();
			case Lime:
				return CableBusTextures.MESmart_Lime.getIcon();
			case Magenta:
				return CableBusTextures.MESmart_Magenta.getIcon();
			case Orange:
				return CableBusTextures.MESmart_Orange.getIcon();
			case Pink:
				return CableBusTextures.MESmart_Pink.getIcon();
			case Purple:
				return CableBusTextures.MESmart_Purple.getIcon();
			case Red:
				return CableBusTextures.MESmart_Red.getIcon();
			case White:
				return CableBusTextures.MESmart_White.getIcon();
			case Yellow:
				return CableBusTextures.MESmart_Yellow.getIcon();
			default:
		}

		final IParts parts = AEApi.instance().definitions().parts();
		final ItemStack smartCableStack = parts.cableSmart().stack( AEColor.Transparent, 1 );

		return renderer.getIcon( smartCableStack );
	}

	@SideOnly( Side.CLIENT )
	protected void setSmartConnectionRotations( final AEPartLocation of, final ModelGenerator renderer )
	{
		switch( of )
		{
			case UP:
			case DOWN:
				renderer.setUvRotateTop( 0 );
				renderer.setUvRotateBottom( 0 );
				renderer.setUvRotateSouth( 3 );
				renderer.setUvRotateEast( 3 );
				break;
			case NORTH:
			case SOUTH:
				renderer.setUvRotateTop( 3 );
				renderer.setUvRotateBottom( 3 );
				renderer.setUvRotateNorth( 1 );
				renderer.setUvRotateSouth( 2 );
				renderer.setUvRotateWest( 1 );
				break;
			case EAST:
			case WEST:
				renderer.setUvRotateEast( 2 );
				renderer.setUvRotateWest( 1 );
				renderer.setUvRotateBottom( 2 );
				renderer.setUvRotateTop( 1 );
				renderer.setUvRotateSouth( 3 );
				renderer.setUvRotateNorth( 0 );
				break;
			default:
				break;
		}
	}

	protected CableBusTextures getChannelTex( int i, final boolean b )
	{
		if( !this.powered )
		{
			i = 0;
		}

		if( b )
		{
			switch( i )
			{
				default:
					return CableBusTextures.Channels10;
				case 5:
					return CableBusTextures.Channels11;
				case 6:
					return CableBusTextures.Channels12;
				case 7:
					return CableBusTextures.Channels13;
				case 8:
					return CableBusTextures.Channels14;
			}
		}
		else
		{
			switch( i )
			{
				case 0:
					return CableBusTextures.Channels00;
				case 1:
					return CableBusTextures.Channels01;
				case 2:
					return CableBusTextures.Channels02;
				case 3:
					return CableBusTextures.Channels03;
				default:
					return CableBusTextures.Channels04;
			}
		}
	}

	@SideOnly( Side.CLIENT )
	protected void renderAllFaces( final AEBaseBlock blk, final BlockPos pos, final IPartRenderHelper rh, final ModelGenerator renderer )
	{
		rh.setBounds( (float) renderer.getRenderMinX() * 16.0f, (float) renderer.getRenderMinY() * 16.0f, (float) renderer.getRenderMinZ() * 16.0f, (float) renderer.getRenderMaxX() * 16.0f, (float) renderer.getRenderMaxY() * 16.0f, (float) renderer.getRenderMaxZ() * 16.0f );
		rh.renderFace( pos, blk.getRendererInstance().getTexture( AEPartLocation.WEST ), EnumFacing.WEST, renderer );
		rh.renderFace( pos, blk.getRendererInstance().getTexture( AEPartLocation.EAST ), EnumFacing.EAST, renderer );
		rh.renderFace( pos, blk.getRendererInstance().getTexture( AEPartLocation.NORTH ), EnumFacing.NORTH, renderer );
		rh.renderFace( pos, blk.getRendererInstance().getTexture( AEPartLocation.SOUTH ), EnumFacing.SOUTH, renderer );
		rh.renderFace( pos, blk.getRendererInstance().getTexture( AEPartLocation.DOWN ), EnumFacing.DOWN, renderer );
		rh.renderFace( pos, blk.getRendererInstance().getTexture( AEPartLocation.UP ), EnumFacing.UP, renderer );
	}

	int getChannelsOnSide( final int i )
	{
		return this.channelsOnSide[i];
	}

	void setChannelsOnSide( final int i, final int channels )
	{
		this.channelsOnSide[i] = channels;
	}

	EnumSet<AEPartLocation> getConnections()
	{
		return this.connections;
	}

	void setConnections( final EnumSet<AEPartLocation> connections )
	{
		this.connections = connections;
	}
}
