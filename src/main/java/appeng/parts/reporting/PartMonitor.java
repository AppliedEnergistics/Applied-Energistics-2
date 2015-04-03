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

package appeng.parts.reporting;


import java.io.IOException;

import io.netty.buffer.ByteBuf;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import appeng.api.implementations.IPowerChannelState;
import appeng.api.implementations.parts.IPartMonitor;
import appeng.api.networking.GridFlags;
import appeng.api.networking.events.MENetworkBootingStatusChange;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartRenderHelper;
import appeng.client.texture.CableBusTextures;
import appeng.me.GridAccessException;
import appeng.parts.AEBasePart;
import appeng.util.Platform;


public class PartMonitor extends AEBasePart implements IPartMonitor, IPowerChannelState
{

	final int POWERED_FLAG = 4;
	final int BOOTING_FLAG = 8;
	final int CHANNEL_FLAG = 16;
	// CableBusTextures frontSolid = CableBusTextures.PartMonitor_Solid;
	CableBusTextures frontDark = CableBusTextures.PartMonitor_Colored;
	CableBusTextures frontBright = CableBusTextures.PartMonitor_Bright;
	CableBusTextures frontColored = CableBusTextures.PartMonitor_Colored;
	boolean notLightSource = !this.getClass().equals( PartMonitor.class );
	byte spin = 0; // 0-3
	int clientFlags = 0; // sent as byte.
	float opacity = -1;

	public PartMonitor( ItemStack is )
	{
		this( is, false );
	}

	protected PartMonitor( ItemStack is, boolean requireChannel )
	{
		super( is );

		if( requireChannel )
		{
			this.proxy.setFlags( GridFlags.REQUIRE_CHANNEL );
			this.proxy.setIdlePowerUsage( 1.0 / 2.0 );
		}
		else
			this.proxy.setIdlePowerUsage( 1.0 / 16.0 ); // lights drain a little bit.
	}

	@MENetworkEventSubscribe
	public void bootingRender( MENetworkBootingStatusChange c )
	{
		if( this.notLightSource )
			this.getHost().markForUpdate();
	}

	@MENetworkEventSubscribe
	public void powerRender( MENetworkPowerStatusChange c )
	{
		this.getHost().markForUpdate();
	}

	@Override
	public void getBoxes( IPartCollisionHelper bch )
	{
		bch.addBox( 2, 2, 14, 14, 14, 16 );
		bch.addBox( 4, 4, 13, 12, 12, 14 );
	}

	@Override
	@SideOnly( Side.CLIENT )
	public void renderInventory( IPartRenderHelper rh, RenderBlocks renderer )
	{
		rh.setBounds( 2, 2, 14, 14, 14, 16 );

		rh.setTexture( CableBusTextures.PartMonitorSides.getIcon(), CableBusTextures.PartMonitorSides.getIcon(), CableBusTextures.PartMonitorBack.getIcon(), this.is.getIconIndex(), CableBusTextures.PartMonitorSides.getIcon(), CableBusTextures.PartMonitorSides.getIcon() );
		rh.renderInventoryBox( renderer );

		rh.setInvColor( this.getColor().whiteVariant );
		rh.renderInventoryFace( this.frontBright.getIcon(), ForgeDirection.SOUTH, renderer );

		rh.setInvColor( this.getColor().mediumVariant );
		rh.renderInventoryFace( this.frontDark.getIcon(), ForgeDirection.SOUTH, renderer );

		rh.setInvColor( this.getColor().blackVariant );
		rh.renderInventoryFace( this.frontColored.getIcon(), ForgeDirection.SOUTH, renderer );

		rh.setBounds( 4, 4, 13, 12, 12, 14 );
		rh.renderInventoryBox( renderer );
	}

	@Override
	@SideOnly( Side.CLIENT )
	public void renderStatic( int x, int y, int z, IPartRenderHelper rh, RenderBlocks renderer )
	{
		this.renderCache = rh.useSimplifiedRendering( x, y, z, this, this.renderCache );

		rh.setTexture( CableBusTextures.PartMonitorSides.getIcon(), CableBusTextures.PartMonitorSides.getIcon(), CableBusTextures.PartMonitorBack.getIcon(), this.is.getIconIndex(), CableBusTextures.PartMonitorSides.getIcon(), CableBusTextures.PartMonitorSides.getIcon() );

		rh.setBounds( 2, 2, 14, 14, 14, 16 );
		rh.renderBlock( x, y, z, renderer );

		if( this.getLightLevel() > 0 )
		{
			int l = 13;
			Tessellator.instance.setBrightness( l << 20 | l << 4 );
		}

		renderer.uvRotateBottom = renderer.uvRotateEast = renderer.uvRotateNorth = renderer.uvRotateSouth = renderer.uvRotateTop = renderer.uvRotateWest = this.spin;

		Tessellator.instance.setColorOpaque_I( this.getColor().whiteVariant );
		rh.renderFace( x, y, z, this.frontBright.getIcon(), ForgeDirection.SOUTH, renderer );

		Tessellator.instance.setColorOpaque_I( this.getColor().mediumVariant );
		rh.renderFace( x, y, z, this.frontDark.getIcon(), ForgeDirection.SOUTH, renderer );

		Tessellator.instance.setColorOpaque_I( this.getColor().blackVariant );
		rh.renderFace( x, y, z, this.frontColored.getIcon(), ForgeDirection.SOUTH, renderer );

		renderer.uvRotateBottom = renderer.uvRotateEast = renderer.uvRotateNorth = renderer.uvRotateSouth = renderer.uvRotateTop = renderer.uvRotateWest = 0;

		if( this.notLightSource )
		{
			rh.setTexture( CableBusTextures.PartMonitorSidesStatus.getIcon(), CableBusTextures.PartMonitorSidesStatus.getIcon(), CableBusTextures.PartMonitorBack.getIcon(), this.is.getIconIndex(), CableBusTextures.PartMonitorSidesStatus.getIcon(), CableBusTextures.PartMonitorSidesStatus.getIcon() );
		}

		rh.setBounds( 4, 4, 13, 12, 12, 14 );
		rh.renderBlock( x, y, z, renderer );

		if( this.notLightSource )
		{
			boolean hasChan = ( this.clientFlags & ( this.POWERED_FLAG | this.CHANNEL_FLAG ) ) == ( this.POWERED_FLAG | this.CHANNEL_FLAG );
			boolean hasPower = ( this.clientFlags & this.POWERED_FLAG ) == this.POWERED_FLAG;

			if( hasChan )
			{
				int l = 14;
				Tessellator.instance.setBrightness( l << 20 | l << 4 );
				Tessellator.instance.setColorOpaque_I( this.getColor().blackVariant );
			}
			else if( hasPower )
			{
				int l = 9;
				Tessellator.instance.setBrightness( l << 20 | l << 4 );
				Tessellator.instance.setColorOpaque_I( this.getColor().whiteVariant );
			}
			else
			{
				Tessellator.instance.setBrightness( 0 );
				Tessellator.instance.setColorOpaque_I( 0x000000 );
			}

			rh.renderFace( x, y, z, CableBusTextures.PartMonitorSidesStatusLights.getIcon(), ForgeDirection.EAST, renderer );
			rh.renderFace( x, y, z, CableBusTextures.PartMonitorSidesStatusLights.getIcon(), ForgeDirection.WEST, renderer );
			rh.renderFace( x, y, z, CableBusTextures.PartMonitorSidesStatusLights.getIcon(), ForgeDirection.UP, renderer );
			rh.renderFace( x, y, z, CableBusTextures.PartMonitorSidesStatusLights.getIcon(), ForgeDirection.DOWN, renderer );
		}
	}

	private int blockLight( int emit )
	{
		if( this.opacity < 0 )
		{
			TileEntity te = this.getTile();
			this.opacity = 255 - te.getWorldObj().getBlockLightOpacity( te.xCoord + this.side.offsetX, te.yCoord + this.side.offsetY, te.zCoord + this.side.offsetZ );
		}

		return (int) ( emit * ( this.opacity / 255.0f ) );
	}

	@Override
	public boolean isPowered()
	{
		try
		{
			if( Platform.isServer() )
				return this.proxy.getEnergy().isNetworkPowered();
			else
				return ( ( this.clientFlags & this.POWERED_FLAG ) == this.POWERED_FLAG );
		}
		catch( GridAccessException e )
		{
			return false;
		}
	}

	@Override
	public void onNeighborChanged()
	{
		this.opacity = -1;
		this.getHost().markForUpdate();
	}

	@Override
	public void readFromNBT( NBTTagCompound data )
	{
		super.readFromNBT( data );
		if( data.hasKey( "opacity" ) )
			this.opacity = data.getFloat( "opacity" );
		this.spin = data.getByte( "spin" );
	}

	@Override
	public void writeToNBT( NBTTagCompound data )
	{
		super.writeToNBT( data );
		data.setFloat( "opacity", this.opacity );
		data.setByte( "spin", this.spin );
	}

	@Override
	public void writeToStream( ByteBuf data ) throws IOException
	{
		super.writeToStream( data );
		this.clientFlags = this.spin & 3;

		try
		{
			if( this.proxy.getEnergy().isNetworkPowered() )
				this.clientFlags |= this.POWERED_FLAG;

			if( this.proxy.getPath().isNetworkBooting() )
				this.clientFlags |= this.BOOTING_FLAG;

			if( this.proxy.getNode().meetsChannelRequirements() )
				this.clientFlags |= this.CHANNEL_FLAG;
		}
		catch( GridAccessException e )
		{
			// um.. nothing.
		}

		data.writeByte( (byte) this.clientFlags );
	}

	@Override
	public boolean readFromStream( ByteBuf data ) throws IOException
	{
		super.readFromStream( data );
		int oldFlags = this.clientFlags;
		this.clientFlags = data.readByte();
		this.spin = (byte) ( this.clientFlags & 3 );
		if( this.clientFlags == oldFlags )
			return false;
		return true;
	}

	@Override
	public int getLightLevel()
	{
		return this.blockLight( this.isPowered() ? ( this.notLightSource ? 9 : 15 ) : 0 );
	}

	@Override
	public boolean onPartActivate( EntityPlayer player, Vec3 pos )
	{
		TileEntity te = this.getTile();

		if( !player.isSneaking() && Platform.isWrench( player, player.inventory.getCurrentItem(), te.xCoord, te.yCoord, te.zCoord ) )
		{
			if( Platform.isServer() )
			{
				if( this.spin > 3 )
					this.spin = 0;

				switch( this.spin )
				{
					case 0:
						this.spin = 1;
						break;
					case 1:
						this.spin = 3;
						break;
					case 2:
						this.spin = 0;
						break;
					case 3:
						this.spin = 2;
						break;
				}

				this.host.markForUpdate();
				this.saveChanges();
			}
			return true;
		}
		else
			return super.onPartActivate( player, pos );
	}

	@Override
	public void onPlacement( EntityPlayer player, ItemStack held, ForgeDirection side )
	{
		super.onPlacement( player, held, side );

		byte rotation = (byte) ( MathHelper.floor_double( ( player.rotationYaw * 4F ) / 360F + 2.5D ) & 3 );
		if( side == ForgeDirection.UP )
			this.spin = rotation;
		else if( side == ForgeDirection.DOWN )
			this.spin = rotation;
	}

	@Override
	public boolean isActive()
	{
		if( this.notLightSource )
			return ( ( this.clientFlags & ( this.CHANNEL_FLAG | this.POWERED_FLAG ) ) == ( this.CHANNEL_FLAG | this.POWERED_FLAG ) );
		else
			return this.isPowered();
	}
}
