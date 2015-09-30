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


import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
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
import appeng.block.networking.BlockCableBus;
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

	public CableBusContainer cb = new CableBusContainer( this );
	/**
	 * Immibis MB Support
	 */

	boolean ImmibisMicroblocks_TransformableTileEntityMarker = true;
	private int oldLV = -1; // on re-calculate light when it changes

	@TileEvent( TileEventType.WORLD_NBT_READ )
	public void readFromNBT_TileCableBus( final NBTTagCompound data )
	{
		this.cb.readFromNBT( data );
	}

	@TileEvent( TileEventType.WORLD_NBT_WRITE )
	public void writeToNBT_TileCableBus( final NBTTagCompound data )
	{
		this.cb.writeToNBT( data );
	}

	@TileEvent( TileEventType.NETWORK_READ )
	public boolean readFromStream_TileCableBus( final ByteBuf data ) throws IOException
	{
		final boolean ret = this.cb.readFromStream( data );

		final int newLV = this.cb.getLightValue();
		if( newLV != this.oldLV )
		{
			this.oldLV = newLV;
			this.worldObj.checkLight( pos );
		}

		this.updateTileSetting();
		return ret;
	}

	protected void updateTileSetting()
	{
		if( this.cb.requiresDynamicRender )
		{
			try
			{
				final TileCableBus tcb = (TileCableBus) BlockCableBus.tesrTile.newInstance();
				tcb.copyFrom( this );
				this.getWorld().setTileEntity( pos, tcb );
			}
			catch( final Throwable ignored )
			{

			}
		}
	}

	protected void copyFrom( final TileCableBus oldTile )
	{
		final CableBusContainer tmpCB = this.cb;
		this.cb = oldTile.cb;
		this.oldLV = oldTile.oldLV;
		oldTile.cb = tmpCB;
	}

	@TileEvent( TileEventType.NETWORK_WRITE )
	public void writeToStream_TileCableBus( final ByteBuf data ) throws IOException
	{
		this.cb.writeToStream( data );
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
		this.cb.removeFromWorld();
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
		return this.cb.getGridNode( dir );
	}

	@Override
	public AECableType getCableConnectionType( final AEPartLocation side )
	{
		return this.cb.getCableConnectionType( side );
	}

	@Override
	public void onChunkUnload()
	{
		super.onChunkUnload();
		this.cb.removeFromWorld();
	}

	@Override
	public void markForUpdate()
	{
		if( this.worldObj == null )
		{
			return;
		}

		final int newLV = this.cb.getLightValue();
		if( newLV != this.oldLV )
		{
			this.oldLV = newLV;
			this.worldObj.getLight( pos );
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
		this.cb.getDrops( drops );
	}

	@Override
	public void getNoDrops( final World w, final BlockPos pos, final List<ItemStack> drops )
	{
		this.cb.getNoDrops( drops );
	}

	@Override
	public void onReady()
	{
		super.onReady();
		if( this.cb.isEmpty() )
		{
			if( this.worldObj.getTileEntity( pos ) == this )
			{
				this.worldObj.destroyBlock( pos, true );
			}
		}
		else
		{
			this.cb.addToWorld();
		}
	}

	@Override
	public boolean requiresTESR()
	{
		return this.cb.requiresDynamicRender;
	}

	@Override
	public IFacadeContainer getFacadeContainer()
	{
		return this.cb.getFacadeContainer();
	}

	@Override
	public boolean canAddPart( final ItemStack is, final AEPartLocation side )
	{
		return this.cb.canAddPart( is, side );
	}

	@Override
	public AEPartLocation addPart( final ItemStack is, final AEPartLocation side, final EntityPlayer player )
	{
		return this.cb.addPart( is, side, player );
	}

	@Override
	public IPart getPart( final AEPartLocation side )
	{
		return this.cb.getPart( side );
	}

	@Override
	public IPart getPart( final EnumFacing side )
	{
		return this.cb.getPart( side );
	}

	@Override
	public void removePart( final AEPartLocation side, final boolean suppressUpdate )
	{
		this.cb.removePart( side, suppressUpdate );
	}

	@Override
	public DimensionalCoord getLocation()
	{
		return new DimensionalCoord( this );
	}

	@Override
	public AEColor getColor()
	{
		return this.cb.getColor();
	}

	@Override
	public void clearContainer()
	{
		this.cb = new CableBusContainer( this );
	}

	@Override
	public boolean isBlocked( final EnumFacing side )
	{
		return !this.ImmibisMicroblocks_isSideOpen( side );
	}
	
	@Override
	public Iterable<AxisAlignedBB> getSelectedBoundingBoxesFromPool( final World w, final BlockPos pos, final Entity e, final boolean visual )
	{
		return this.cb.getSelectedBoundingBoxesFromPool( false, true, e, visual );
	}

	@Override
	public SelectedPart selectPart( final Vec3 pos )
	{
		return this.cb.selectPart( pos );
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
		return this.cb.hasRedstone( side );
	}

	@Override
	public boolean isEmpty()
	{
		return this.cb.isEmpty();
	}

	@Override
	public Set<LayerFlags> getLayerFlags()
	{
		return this.cb.getLayerFlags();
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

		this.getWorld().setBlockToAir( pos );
	}
	
	@Override
	public void addCollidingBlockToList(
			final World w,
			final BlockPos pos,
			final AxisAlignedBB bb,
			final List<AxisAlignedBB> out,
			final Entity e )
	{
		for( final AxisAlignedBB bx : this.getSelectedBoundingBoxesFromPool( w, pos, e, false ) )
		{
			out.add( AxisAlignedBB.fromBounds( bx.minX, bx.minY, bx.minZ, bx.maxX, bx.maxY, bx.maxZ ) );
		}
	}

	@Override
	public void notifyNeighbors()
	{
		if( this.worldObj != null && this.worldObj.isBlockLoaded( pos ) && !CableBusContainer.isLoading() )
		{
			Platform.notifyBlocksOfNeighbors( this.worldObj, pos );
		}
	}

	@Override
	public boolean isInWorld()
	{
		return this.cb.isInWorld();
	}

	public boolean ImmibisMicroblocks_isSideOpen( final EnumFacing side )
	{
		return true;
	}

	public void ImmibisMicroblocks_onMicroblocksChanged()
	{
		this.cb.updateConnections();
	}

	@Override
	public boolean recolourBlock(
			final EnumFacing side,
			final AEColor colour,
			final EntityPlayer who )
	{
		return this.cb.recolourBlock( side, colour, who );
	}




}
