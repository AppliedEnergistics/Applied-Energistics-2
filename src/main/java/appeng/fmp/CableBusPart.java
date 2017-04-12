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

package appeng.fmp;


import appeng.api.implementations.parts.IPartCable;
import appeng.api.networking.IGridNode;
import appeng.api.parts.*;
import appeng.api.util.AECableType;
import appeng.api.util.AEColor;
import appeng.api.util.DimensionalCoord;
import appeng.client.render.BusRenderHelper;
import appeng.client.render.BusRenderer;
import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.core.features.AEFeature;
import appeng.helpers.AEMultiTile;
import appeng.parts.BusCollisionHelper;
import appeng.parts.CableBusContainer;
import appeng.parts.PartPlacement;
import appeng.tile.networking.TileCableBus;
import appeng.util.Platform;
import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.raytracer.IndexedCuboid6;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Vector3;
import codechicken.multipart.*;
import codechicken.multipart.scalatraits.TIInventoryTile;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;

import java.io.IOException;
import java.util.*;


/**
 * Implementing these might help improve visuals for hollow covers
 * <p>
 * TSlottedPart,ISidedHollowConnect
 */
public class CableBusPart extends JCuboidPart implements JNormalOcclusion, IMaskedRedstonePart, AEMultiTile
{
	private static final ThreadLocal<Boolean> DISABLE_FACADE_OCCLUSION = new ThreadLocal<Boolean>();
	private static final double SHORTER = 6.0 / 16.0;
	private static final double LONGER = 10.0 / 16.0;
	private static final double MIN_DIRECTION = 0;
	private static final double MAX_DIRECTION = 1.0;
	private static final Cuboid6[] SIDE_TESTS = {

			// DOWN(0, -1, 0),
			new Cuboid6( SHORTER, MIN_DIRECTION, SHORTER, LONGER, SHORTER, LONGER ),

			// UP(0, 1, 0),
			new Cuboid6( SHORTER, LONGER, SHORTER, LONGER, MAX_DIRECTION, LONGER ),

			// NORTH(0, 0, -1),
			new Cuboid6( SHORTER, SHORTER, MIN_DIRECTION, LONGER, LONGER, SHORTER ),

			// SOUTH(0, 0, 1),
			new Cuboid6( SHORTER, SHORTER, LONGER, LONGER, LONGER, MAX_DIRECTION ),

			// WEST(-1, 0, 0),
			new Cuboid6( MIN_DIRECTION, SHORTER, SHORTER, SHORTER, LONGER, LONGER ),

			// EAST(1, 0, 0),
			new Cuboid6( LONGER, SHORTER, SHORTER, MAX_DIRECTION, LONGER, LONGER ),
	};

	/**
	 * Mask for {@link IMaskedRedstonePart#getConnectionMask(int)}
	 * <p>
	 * the bits are derived from the rotation, where 4 is the center
	 */
	private static final int CONNECTION_MASK = 0x000010;
	private CableBusContainer cb = new CableBusContainer( this );
	private boolean canUpdate = false;

	@Override
	public boolean recolourBlock( final ForgeDirection side, final AEColor colour, final EntityPlayer who )
	{
		return this.getCableBus().recolourBlock( side, colour, who );
	}

	@Override
	public Cuboid6 getBounds()
	{
		AxisAlignedBB b = null;

		for( final AxisAlignedBB bx : this.getCableBus().getSelectedBoundingBoxesFromPool( false, true, null, true ) )
		{
			if( b == null )
			{
				b = bx;
			}
			else
			{
				final double minX = Math.min( b.minX, bx.minX );
				final double minY = Math.min( b.minY, bx.minY );
				final double minZ = Math.min( b.minZ, bx.minZ );
				final double maxX = Math.max( b.maxX, bx.maxX );
				final double maxY = Math.max( b.maxY, bx.maxY );
				final double maxZ = Math.max( b.maxZ, bx.maxZ );
				b.setBounds( minX, minY, minZ, maxX, maxY, maxZ );
			}
		}

		if( b == null )
		{
			return new Cuboid6( 0.0, 0.0, 0.0, 1.0, 1.0, 1.0 );
		}

		return new Cuboid6( b.minX, b.minY, b.minZ, b.maxX, b.maxY, b.maxZ );
	}

	@Override
	public String getType()
	{
		return PartRegistry.CableBusPart.getName();
	}

	@Override
	public int getLightValue()
	{
		return this.getCableBus().getLightValue();
	}

	@Override
	public void onWorldJoin()
	{
		this.canUpdate = true;
		this.getCableBus().updateConnections();
		this.getCableBus().addToWorld();
	}

	@Override
	public boolean occlusionTest( final TMultiPart part )
	{
		return NormalOcclusionTest.apply( this, part );
	}

	@Override
	public boolean renderStatic( final Vector3 pos, final int pass )
	{
		if( pass == 0 || ( pass == 1 && AEConfig.instance.isFeatureEnabled( AEFeature.AlphaPass ) ) )
		{
			BusRenderHelper.INSTANCE.setPass( pass );
			BusRenderer.INSTANCE.getRenderer().renderAllFaces = true;
			BusRenderer.INSTANCE.getRenderer().blockAccess = this.world();
			BusRenderer.INSTANCE.getRenderer().overrideBlockTexture = null;
			this.getCableBus().renderStatic( pos.x, pos.y, pos.z );
			return BusRenderHelper.INSTANCE.getItemsRendered() > 0;
		}
		return false;
	}

	@Override
	public void renderDynamic( final Vector3 pos, final float frame, final int pass )
	{
		if( pass == 0 || ( pass == 1 && AEConfig.instance.isFeatureEnabled( AEFeature.AlphaPass ) ) )
		{
			BusRenderHelper.INSTANCE.setPass( pass );
			this.getCableBus().renderDynamic( pos.x, pos.y, pos.z );
		}
	}

	@Override
	public void onPartChanged( final TMultiPart part )
	{
		this.getCableBus().updateConnections();
	}

	@Override
	public void onEntityCollision( final Entity entity )
	{
		this.getCableBus().onEntityCollision( entity );
	}

	@Override
	public boolean activate( final EntityPlayer player, final MovingObjectPosition hit, final ItemStack item )
	{
		return this.getCableBus().activate( player, hit.hitVec.addVector( -hit.blockX, -hit.blockY, -hit.blockZ ) );
	}

	@Override
	public void load( final NBTTagCompound tag )
	{
		this.getCableBus().readFromNBT( tag );
	}

	@Override
	public void onWorldSeparate()
	{
		this.canUpdate = false;
		this.getCableBus().removeFromWorld();
	}

	@Override
	public void save( final NBTTagCompound tag )
	{
		this.getCableBus().writeToNBT( tag );
	}

	@Override
	public void writeDesc( final MCDataOutput packet )
	{
		final ByteBuf stream = Unpooled.buffer();

		try
		{
			this.getCableBus().writeToStream( stream );
			packet.writeInt( stream.readableBytes() );
			stream.capacity( stream.readableBytes() );
			packet.writeByteArray( stream.array() );
		}
		catch( final IOException e )
		{
			AELog.debug( e );
		}
	}

	@Override
	public ItemStack pickItem( final MovingObjectPosition hit )
	{
		final Vec3 v3 = hit.hitVec.addVector( -hit.blockX, -hit.blockY, -hit.blockZ );
		final SelectedPart sp = this.getCableBus().selectPart( v3 );
		if( sp != null )
		{
			if( sp.part != null )
			{
				return sp.part.getItemStack( PartItemStack.Break );
			}
			if( sp.facade != null )
			{
				return sp.facade.getItemStack();
			}
		}
		return null;
	}

	@Override
	public Iterable<ItemStack> getDrops()
	{
		return this.getCableBus().getDrops( new ArrayList() );
	}

	@Override
	public void onNeighborChanged()
	{
		this.getCableBus().onNeighborChanged();
	}

	@Override
	public boolean doesTick()
	{
		return false;
	}

	@Override
	public void invalidateConvertedTile()
	{
		this.getCableBus().setHost( this );
	}

	@Override
	public void readDesc( final MCDataInput packet )
	{
		final int len = packet.readInt();
		final byte[] data = packet.readByteArray( len );

		try
		{
			if( len > 0 )
			{
				final ByteBuf byteBuffer = Unpooled.wrappedBuffer( data );
				this.getCableBus().readFromStream( byteBuffer );
			}
		}
		catch( final IOException e )
		{
			AELog.debug( e );
		}
	}

	@Override
	public boolean canConnectRedstone( final int side )
	{
		return this.getCableBus().canConnectRedstone( EnumSet.of( ForgeDirection.getOrientation( side ) ) );
	}

	@Override
	public int weakPowerLevel( final int side )
	{
		return this.getCableBus().isProvidingWeakPower( ForgeDirection.getOrientation( side ) );
	}

	@Override
	public int strongPowerLevel( final int side )
	{
		return this.getCableBus().isProvidingStrongPower( ForgeDirection.getOrientation( side ) );
	}

	public void convertFromTile( final TileEntity blockTileEntity )
	{
		final TileCableBus tcb = (TileCableBus) blockTileEntity;
		this.setCableBus( tcb.getCableBus() );
	}

	@Override
	public Iterable<Cuboid6> getOcclusionBoxes()
	{
		final LinkedList<Cuboid6> l = new LinkedList<Cuboid6>();
		for( final AxisAlignedBB b : this.getCableBus().getSelectedBoundingBoxesFromPool( true, DISABLE_FACADE_OCCLUSION.get() == null, null, true ) )
		{
			l.add( new Cuboid6( b.minX, b.minY, b.minZ, b.maxX, b.maxY, b.maxZ ) );
		}
		return l;
	}

	@Override
	public IGridNode getGridNode( final ForgeDirection dir )
	{
		return this.getCableBus().getGridNode( dir );
	}

	@Override
	public AECableType getCableConnectionType( final ForgeDirection dir )
	{
		return this.getCableBus().getCableConnectionType( dir );
	}

	@Override
	public void securityBreak()
	{
		this.getCableBus().securityBreak();
	}

	// @Override
	public int getHollowSize( final int side )
	{
		final IPartCable cable = (IPartCable) this.getPart( ForgeDirection.UNKNOWN );

		final ForgeDirection dir = ForgeDirection.getOrientation( side );
		if( cable != null && cable.isConnected( dir ) )
		{
			final List<AxisAlignedBB> boxes = new ArrayList<AxisAlignedBB>();

			final BusCollisionHelper bch = new BusCollisionHelper( boxes, ForgeDirection.EAST, ForgeDirection.UP, ForgeDirection.SOUTH, null, true );

			for( final ForgeDirection whichSide : ForgeDirection.values() )
			{
				final IPart fPart = this.getPart( whichSide );

				if( fPart != null )
				{
					fPart.getBoxes( bch );
				}
			}

			AxisAlignedBB b = null;
			final AxisAlignedBB pb = Platform.getPrimaryBox( dir, 2 );

			for( final AxisAlignedBB bb : boxes )
			{
				if( bb.intersectsWith( pb ) )
				{
					if( b == null )
					{
						b = bb;
					}
					else
					{
						b.maxX = Math.max( b.maxX, bb.maxX );
						b.maxY = Math.max( b.maxY, bb.maxY );
						b.maxZ = Math.max( b.maxZ, bb.maxZ );
						b.minX = Math.min( b.minX, bb.minX );
						b.minY = Math.min( b.minY, bb.minY );
						b.minZ = Math.min( b.minZ, bb.minZ );
					}
				}
			}

			if( b == null )
			{
				return 0;
			}

			switch( dir )
			{
				case WEST:
				case EAST:
					return this.getSize( b.minZ, b.maxZ, b.minY, b.maxY );
				case DOWN:
				case NORTH:
					return this.getSize( b.minX, b.maxX, b.minZ, b.maxZ );
				case SOUTH:
				case UP:
					return this.getSize( b.minX, b.maxX, b.minY, b.maxY );
				default:
			}
		}

		return 12;
	}

	private int getSize( final double a, final double b, final double c, final double d )
	{
		double r = Math.abs( a - 0.5 );
		r = Math.max( Math.abs( b - 0.5 ), r );
		r = Math.max( Math.abs( c - 0.5 ), r );
		return ( 8 * (int) Math.max( Math.abs( d - 0.5 ), r ) );
	}

	// @Override
	public int getSlotMask()
	{
		int mask = 0;

		for( final ForgeDirection side : ForgeDirection.values() )
		{
			if( this.getPart( side ) != null )
			{
				mask |= 1 << side.ordinal();
			}
			else if( side != ForgeDirection.UNKNOWN && this.getFacadeContainer().getFacade( side ) != null )
			{
				mask |= 1 << side.ordinal();
			}
		}

		return mask;
	}

	@Override
	public IFacadeContainer getFacadeContainer()
	{
		return this.getCableBus().getFacadeContainer();
	}

	@Override
	public boolean canAddPart( ItemStack is, final ForgeDirection side )
	{
		final IFacadePart fp = PartPlacement.isFacade( is, side );
		if( fp != null )
		{
			if( !( side == null || side == ForgeDirection.UNKNOWN || this.tile() == null ) )
			{
				final List<AxisAlignedBB> boxes = new ArrayList<AxisAlignedBB>();
				final IPartCollisionHelper bch = new BusCollisionHelper( boxes, side, null, true );
				fp.getBoxes( bch, null );
				for( final AxisAlignedBB bb : boxes )
				{
					DISABLE_FACADE_OCCLUSION.set( true );
					final boolean canAdd = this.tile().canAddPart( new NormallyOccludedPart( new Cuboid6( bb ) ) );
					DISABLE_FACADE_OCCLUSION.remove();
					if( !canAdd )
					{
						return false;
					}
				}
			}
			return true;
		}

		if( is.getItem() instanceof IPartItem )
		{
			final IPartItem bi = (IPartItem) is.getItem();

			is = is.copy();
			is.stackSize = 1;

			final IPart bp = bi.createPartFromItemStack( is );
			if( !( side == null || side == ForgeDirection.UNKNOWN || this.tile() == null ) )
			{
				final List<AxisAlignedBB> boxes = new ArrayList<AxisAlignedBB>();
				final IPartCollisionHelper bch = new BusCollisionHelper( boxes, side, null, true );
				if( bp != null )
				{
					bp.getBoxes( bch );
				}
				for( final AxisAlignedBB bb : boxes )
				{
					if( !this.tile().canAddPart( new NormallyOccludedPart( new Cuboid6( bb ) ) ) )
					{
						return false;
					}
				}
			}
		}

		return this.getCableBus().canAddPart( is, side );
	}

	@Override
	public ForgeDirection addPart( final ItemStack is, final ForgeDirection side, final EntityPlayer owner )
	{
		return this.getCableBus().addPart( is, side, owner );
	}

	@Override
	public IPart getPart( final ForgeDirection side )
	{
		return this.getCableBus().getPart( side );
	}

	@Override
	public void removePart( final ForgeDirection side, final boolean suppressUpdate )
	{
		this.getCableBus().removePart( side, suppressUpdate );
	}

	@Override
	public void markForUpdate()
	{
		if( Platform.isServer() && this.canUpdate )
		{
			this.sendDescUpdate();
		}
	}

	@Override
	public DimensionalCoord getLocation()
	{
		return new DimensionalCoord( this.tile() );
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
	public boolean isBlocked( final ForgeDirection side )
	{
		if( side == null || side == ForgeDirection.UNKNOWN || this.tile() == null )
		{
			return false;
		}

		DISABLE_FACADE_OCCLUSION.set( true );

		final int ordinal = side.ordinal();
		final Cuboid6 sideTest = SIDE_TESTS[ordinal];
		final NormallyOccludedPart occludedPart = new NormallyOccludedPart( sideTest );
		final boolean blocked = !this.tile().canAddPart( occludedPart );
		DISABLE_FACADE_OCCLUSION.remove();

		return blocked;
	}

	@Override
	public SelectedPart selectPart( final Vec3 pos )
	{
		return this.getCableBus().selectPart( pos );
	}

	@Override
	public void markForSave()
	{
		// mark the chunk for save...
		final TileEntity te = this.tile();
		if( te != null && te.getWorldObj() != null )
		{
			te.getWorldObj().getChunkFromBlockCoords( this.x(), this.z() ).isModified = true;
		}
	}

	@Override
	public void partChanged()
	{
		if( this.isInWorld() )
		{
			this.notifyNeighbors();
		}
	}

	@Override
	public boolean hasRedstone( final ForgeDirection side )
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
		this.tile().remPart( this );
	}

	@Override
	public void notifyNeighbors()
	{
		if( this.tile() instanceof TIInventoryTile )
		{
			( (TIInventoryTile) this.tile() ).rebuildSlotMap();
		}

		if( this.world() != null && this.world().blockExists( this.x(), this.y(), this.z() ) && !CableBusContainer.isLoading() )
		{
			Platform.notifyBlocksOfNeighbors( this.world(), this.x(), this.y(), this.z() );
		}
	}

	@Override
	public boolean isInWorld()
	{
		return this.getCableBus().isInWorld();
	}

	@Override
	public Iterable<Cuboid6> getCollisionBoxes()
	{
		final LinkedList<Cuboid6> l = new LinkedList<Cuboid6>();
		for( final AxisAlignedBB b : this.getCableBus().getSelectedBoundingBoxesFromPool( false, true, null, true ) )
		{
			l.add( new Cuboid6( b.minX, b.minY, b.minZ, b.maxX, b.maxY, b.maxZ ) );
		}
		return l;
	}

	@Override
	public Iterable<IndexedCuboid6> getSubParts()
	{
		final LinkedList<IndexedCuboid6> l = new LinkedList<IndexedCuboid6>();
		for( final Cuboid6 c : this.getCollisionBoxes() )
		{
			l.add( new IndexedCuboid6( 0, c ) );
		}
		return l;
	}

	@Override
	public int getConnectionMask( final int side )
	{
		return CONNECTION_MASK;
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
