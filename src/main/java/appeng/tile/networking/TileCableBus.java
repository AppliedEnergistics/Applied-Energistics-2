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

package appeng.tile.networking;


import java.io.IOException;
import java.util.List;
import java.util.Set;

import io.netty.buffer.ByteBuf;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import appeng.api.networking.IGridNode;
import appeng.api.parts.IFacadeContainer;
import appeng.api.parts.IPart;
import appeng.api.parts.LayerFlags;
import appeng.api.parts.SelectedPart;
import appeng.api.util.AECableType;
import appeng.api.util.AEColor;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import appeng.helpers.AEMultiTile;
import appeng.helpers.ICustomCollision;
import appeng.hooks.TickHandler;
import appeng.integration.IntegrationRegistry;
import appeng.integration.IntegrationType;
import appeng.integration.abstraction.IImmibisMicroblocks;
import appeng.parts.CableBusContainer;
import appeng.tile.AEBaseTile;
import appeng.tile.TileEvent;
import appeng.tile.events.TileEventType;
import appeng.util.Platform;


public class TileCableBus extends AEBaseTile implements AEMultiTile, ICustomCollision
{

	private CableBusContainer cb = new CableBusContainer( this );

	private int oldLV = -1; // on re-calculate light when it changes

	@TileEvent( TileEventType.WORLD_NBT_READ )
	public void readFromNBT_TileCableBus( final NBTTagCompound data )
	{
		this.getCableBus().readFromNBT( data );
	}

	@TileEvent( TileEventType.WORLD_NBT_WRITE )
	public void writeToNBT_TileCableBus( final NBTTagCompound data )
	{
		this.getCableBus().writeToNBT( data );
	}

	@TileEvent( TileEventType.NETWORK_READ )
	public boolean readFromStream_TileCableBus( final ByteBuf data ) throws IOException
	{
		final boolean ret = this.getCableBus().readFromStream( data );

		final int newLV = this.getCableBus().getLightValue();
		if( newLV != this.oldLV )
		{
			this.oldLV = newLV;
			this.worldObj.checkLight( this.pos );
		}

		return ret;
	}

	protected void copyFrom( final TileCableBus oldTile )
	{
		final CableBusContainer tmpCB = this.getCableBus();
		this.setCableBus( oldTile.getCableBus() );
		this.oldLV = oldTile.oldLV;
		oldTile.setCableBus( tmpCB );
	}

	@TileEvent( TileEventType.NETWORK_WRITE )
	public void writeToStream_TileCableBus( final ByteBuf data ) throws IOException
	{
		this.getCableBus().writeToStream( data );
	}

	@Override
	public double getMaxRenderDistanceSquared()
	{
		return 900.0;
	}

	@Override
	public void invalidate()
	{
		super.invalidate();
		this.getCableBus().removeFromWorld();
	}

	@Override
	public void validate()
	{
		super.validate();
		TickHandler.INSTANCE.addInit( this );
	}

	@Override
	public IGridNode getGridNode( final AEPartLocation dir )
	{
		return this.getCableBus().getGridNode( dir );
	}

	@Override
	public AECableType getCableConnectionType( final AEPartLocation side )
	{
		return this.getCableBus().getCableConnectionType( side );
	}

	@Override
	public void onChunkUnload()
	{
		super.onChunkUnload();
		this.getCableBus().removeFromWorld();
	}

	@Override
	public void markForUpdate()
	{
		if( this.worldObj == null )
		{
			return;
		}

		final int newLV = this.getCableBus().getLightValue();
		if( newLV != this.oldLV )
		{
			this.oldLV = newLV;
			this.worldObj.getLight( this.pos );
			// worldObj.updateAllLightTypes( xCoord, yCoord, zCoord );
		}

		super.markForUpdate();
	}

	@Override
	public boolean canBeRotated()
	{
		return false;
	}

	@Override
	public void getDrops( final World w, final BlockPos pos, final List drops )
	{
		this.getCableBus().getDrops( drops );
	}

	@Override
	public void getNoDrops( final World w, final BlockPos pos, final List<ItemStack> drops )
	{
		this.getCableBus().getNoDrops( drops );
	}

	@Override
	public void onReady()
	{
		super.onReady();
		if( this.getCableBus().isEmpty() )
		{
			if( this.worldObj.getTileEntity( this.pos ) == this )
			{
				this.worldObj.destroyBlock( this.pos, true );
			}
		}
		else
		{
			this.getCableBus().addToWorld();
		}
	}

	@Override
	public boolean requiresTESR()
	{
		return this.getCableBus().isRequiresDynamicRender();
	}

	@Override
	public IFacadeContainer getFacadeContainer()
	{
		return this.getCableBus().getFacadeContainer();
	}

	@Override
	public boolean canAddPart( final ItemStack is, final AEPartLocation side )
	{
		return this.getCableBus().canAddPart( is, side );
	}

	@Override
	public AEPartLocation addPart( final ItemStack is, final AEPartLocation side, final EntityPlayer player, final EnumHand hand )
	{
		return this.getCableBus().addPart( is, side, player, hand );
	}

	@Override
	public IPart getPart( final AEPartLocation side )
	{
		return this.cb.getPart( side );
	}

	@Override
	public IPart getPart( final EnumFacing side )
	{
		return this.getCableBus().getPart( side );
	}

	@Override
	public void removePart( final AEPartLocation side, final boolean suppressUpdate )
	{
		this.getCableBus().removePart( side, suppressUpdate );
	}

	@Override
	public DimensionalCoord getLocation()
	{
		return new DimensionalCoord( this );
	}

	@Override
	public AEColor getColor()
	{
		return this.getCableBus().getColor();
	}

	@Override
	public void clearContainer()
	{
		this.setCableBus( new CableBusContainer( this ) );
	}

	@Override
	public boolean isBlocked( final EnumFacing side )
	{
		//TODO 1.10.2-R - Stuff.
		return false;
	}

	@Override
	public Iterable<AxisAlignedBB> getSelectedBoundingBoxesFromPool( final World w, final BlockPos pos, final Entity e, final boolean visual )
	{
		return this.getCableBus().getSelectedBoundingBoxesFromPool( false, true, e, visual );
	}

	@Override
	public SelectedPart selectPart( final Vec3d pos )
	{
		return this.getCableBus().selectPart( pos );
	}

	@Override
	public void markForSave()
	{
		super.markDirty();
	}

	@Override
	public void partChanged()
	{
		this.notifyNeighbors();
	}

	@Override
	public boolean hasRedstone( final AEPartLocation side )
	{
		return this.getCableBus().hasRedstone( side );
	}

	@Override
	public boolean isEmpty()
	{
		return this.getCableBus().isEmpty();
	}

	@Override
	public Set<LayerFlags> getLayerFlags()
	{
		return this.getCableBus().getLayerFlags();
	}

	@Override
	public void cleanup()
	{
		if( IntegrationRegistry.INSTANCE.isEnabled( IntegrationType.ImmibisMicroblocks ) )
		{
			final IImmibisMicroblocks imb = (IImmibisMicroblocks) IntegrationRegistry.INSTANCE.getInstance( IntegrationType.ImmibisMicroblocks );
			if( imb != null && imb.leaveParts( this ) )
			{
				return;
			}
		}

		this.getWorld().setBlockToAir( this.pos );
	}

	@Override
	public void addCollidingBlockToList( final World w, final BlockPos pos, final AxisAlignedBB bb, final List<AxisAlignedBB> out, final Entity e )
	{
		for( final AxisAlignedBB bx : this.getSelectedBoundingBoxesFromPool( w, pos, e, false ) )
		{
			out.add( new AxisAlignedBB( bx.minX, bx.minY, bx.minZ, bx.maxX, bx.maxY, bx.maxZ ) );
		}
	}

	@Override
	public void notifyNeighbors()
	{
		if( this.worldObj != null && this.worldObj.isBlockLoaded( this.pos ) && !CableBusContainer.isLoading() )
		{
			Platform.notifyBlocksOfNeighbors( this.worldObj, this.pos );
		}
	}

	@Override
	public boolean isInWorld()
	{
		return this.getCableBus().isInWorld();
	}

	@Override
	public boolean recolourBlock( final EnumFacing side, final AEColor colour, final EntityPlayer who )
	{
		return this.getCableBus().recolourBlock( side, colour, who );
	}

	public CableBusContainer getCableBus()
	{
		return this.cb;
	}

	private void setCableBus( final CableBusContainer cb )
	{
		this.cb = cb;
	}

}
