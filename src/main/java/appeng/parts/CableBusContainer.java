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

package appeng.parts;


import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import appeng.api.AEApi;
import appeng.api.config.YesNo;
import appeng.api.exceptions.FailedConnection;
import appeng.api.implementations.parts.IPartCable;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.parts.IFacadeContainer;
import appeng.api.parts.IFacadePart;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartItem;
import appeng.api.parts.LayerFlags;
import appeng.api.parts.PartItemStack;
import appeng.api.parts.SelectedPart;
import appeng.api.util.AECableType;
import appeng.api.util.AEColor;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import appeng.client.render.CableRenderHelper;
import appeng.core.AELog;
import appeng.facade.FacadeContainer;
import appeng.helpers.AEMultiTile;
import appeng.integration.IntegrationRegistry;
import appeng.integration.IntegrationType;
import appeng.integration.abstraction.ICLApi;
import appeng.me.GridConnection;
import appeng.util.Platform;


public class CableBusContainer extends CableBusStorage implements AEMultiTile, ICableBusContainer
{

	private static final ThreadLocal<Boolean> IS_LOADING = new ThreadLocal<Boolean>();
	private final EnumSet<LayerFlags> myLayerFlags = EnumSet.noneOf( LayerFlags.class );
	public YesNo hasRedstone = YesNo.UNDECIDED;
	public IPartHost tcb;
	public boolean requiresDynamicRender = false;
	boolean inWorld = false;

	public CableBusContainer( IPartHost host )
	{
		this.tcb = host;
	}

	public static boolean isLoading()
	{
		Boolean is = IS_LOADING.get();
		return is != null && is;
	}

	public void setHost( IPartHost host )
	{
		this.tcb.clearContainer();
		this.tcb = host;
	}

	public void rotateLeft()
	{
		IPart[] newSides = new IPart[6];

		newSides[AEPartLocation.UP.ordinal()] = this.getSide( AEPartLocation.UP );
		newSides[AEPartLocation.DOWN.ordinal()] = this.getSide( AEPartLocation.DOWN );

		newSides[AEPartLocation.EAST.ordinal()] = this.getSide( AEPartLocation.NORTH );
		newSides[AEPartLocation.SOUTH.ordinal()] = this.getSide( AEPartLocation.EAST );
		newSides[AEPartLocation.WEST.ordinal()] = this.getSide( AEPartLocation.SOUTH );
		newSides[AEPartLocation.NORTH.ordinal()] = this.getSide( AEPartLocation.WEST );

		for( AEPartLocation dir : AEPartLocation.SIDE_LOCATIONS )
		{
			this.setSide( dir, newSides[dir.ordinal()] );
		}

		this.getFacadeContainer().rotateLeft();
	}

	@Override
	public IFacadeContainer getFacadeContainer()
	{
		return new FacadeContainer( this );
	}

	@Override
	public boolean canAddPart( ItemStack is, AEPartLocation side )
	{
		if( PartPlacement.isFacade( is, side ) != null )
		{
			return true;
		}

		if( is.getItem() instanceof IPartItem )
		{
			IPartItem bi = (IPartItem) is.getItem();

			is = is.copy();
			is.stackSize = 1;

			IPart bp = bi.createPartFromItemStack( is );
			if( bp != null )
			{
				if( bp instanceof IPartCable )
				{
					boolean canPlace = true;
					for( AEPartLocation d : AEPartLocation.SIDE_LOCATIONS )
					{
						if( this.getPart( d ) != null && !this.getPart( d ).canBePlacedOn( ( (IPartCable) bp ).supportsBuses() ) )
						{
							canPlace = false;
						}
					}

					if( !canPlace )
					{
						return false;
					}

					return this.getPart( AEPartLocation.INTERNAL ) == null;
				}
				else if( !( bp instanceof IPartCable ) && side != AEPartLocation.INTERNAL )
				{
					IPart cable = this.getPart( AEPartLocation.INTERNAL );
					if( cable != null && !bp.canBePlacedOn( ( (IPartCable) cable ).supportsBuses() ) )
					{
						return false;
					}

					return this.getPart( side ) == null;
				}
			}
		}
		return false;
	}

	@Override
	public AEPartLocation addPart( ItemStack is, AEPartLocation side, EntityPlayer player )
	{
		if( this.canAddPart( is, side ) )
		{
			if( is.getItem() instanceof IPartItem )
			{
				IPartItem bi = (IPartItem) is.getItem();

				is = is.copy();
				is.stackSize = 1;

				IPart bp = bi.createPartFromItemStack( is );
				if( bp instanceof IPartCable )
				{
					boolean canPlace = true;
					for( AEPartLocation d : AEPartLocation.SIDE_LOCATIONS )
					{
						if( this.getPart( d ) != null && !this.getPart( d ).canBePlacedOn( ( (IPartCable) bp ).supportsBuses() ) )
						{
							canPlace = false;
						}
					}

					if( !canPlace )
					{
						return null;
					}

					if( this.getPart( AEPartLocation.INTERNAL ) != null )
					{
						return null;
					}

					this.setCenter( (IPartCable) bp );
					bp.setPartHostInfo( AEPartLocation.INTERNAL, this, this.tcb.getTile() );

					if( player != null )
					{
						bp.onPlacement( player, is, side );
					}

					if( this.inWorld )
					{
						bp.addToWorld();
					}

					IGridNode cn = this.getCenter().getGridNode();
					if( cn != null )
					{
						for( AEPartLocation ins : AEPartLocation.SIDE_LOCATIONS )
						{
							IPart sbp = this.getPart( ins );
							if( sbp != null )
							{
								IGridNode sn = sbp.getGridNode();
								if( sn != null )
								{
									try
									{
										new GridConnection( cn, sn, AEPartLocation.INTERNAL );
									}
									catch( FailedConnection e )
									{
										// ekk!

										bp.removeFromWorld();
										this.setCenter( null );
										return null;
									}
								}
							}
						}
					}

					this.updateConnections();
					this.markForUpdate();
					this.markForSave();
					this.partChanged();
					return AEPartLocation.INTERNAL;
				}
				else if( bp != null && !( bp instanceof IPartCable ) && side != AEPartLocation.INTERNAL )
				{
					IPart cable = this.getPart( AEPartLocation.INTERNAL );
					if( cable != null && !bp.canBePlacedOn( ( (IPartCable) cable ).supportsBuses() ) )
					{
						return null;
					}

					this.setSide( side, bp );
					bp.setPartHostInfo( side, this, this.getTile() );

					if( player != null )
					{
						bp.onPlacement( player, is, side );
					}

					if( this.inWorld )
					{
						bp.addToWorld();
					}

					if( this.getCenter() != null )
					{
						IGridNode cn = this.getCenter().getGridNode();
						IGridNode sn = bp.getGridNode();

						if( cn != null && sn != null )
						{
							try
							{
								new GridConnection( cn, sn, AEPartLocation.INTERNAL );
							}
							catch( FailedConnection e )
							{
								// ekk!

								bp.removeFromWorld();
								this.setSide( side, null );
								return null;
							}
						}
					}

					this.updateDynamicRender();
					this.updateConnections();
					this.markForUpdate();
					this.markForSave();
					this.partChanged();
					return side;
				}
			}
		}
		return null;
	}

	@Override
	public IPart getPart( AEPartLocation partLocation )
	{
		if( partLocation == AEPartLocation.INTERNAL )
		{
			return this.getCenter();
		}
		return this.getSide( partLocation );
	}

	@Override
	public IPart getPart( EnumFacing side )
	{
		return this.getSide( AEPartLocation.fromFacing( side ) );
	}
	
	@Override
	public void removePart( AEPartLocation side, boolean suppressUpdate )
	{
		if( side == AEPartLocation.INTERNAL )
		{
			if( this.getCenter() != null )
			{
				this.getCenter().removeFromWorld();
			}
			this.setCenter( null );
		}
		else
		{
			if( this.getSide( side ) != null )
			{
				this.getSide( side ).removeFromWorld();
			}
			this.setSide( side, null );
		}

		if( !suppressUpdate )
		{
			this.updateDynamicRender();
			this.updateConnections();
			this.markForUpdate();
			this.markForSave();
			this.partChanged();
		}
	}

	@Override
	public void markForUpdate()
	{
		this.tcb.markForUpdate();
	}

	@Override
	public DimensionalCoord getLocation()
	{
		return this.tcb.getLocation();
	}

	@Override
	public TileEntity getTile()
	{
		return this.tcb.getTile();
	}

	@Override
	public AEColor getColor()
	{
		if( this.getCenter() != null )
		{
			IPartCable c = this.getCenter();
			return c.getCableColor();
		}
		return AEColor.Transparent;
	}

	@Override
	public void clearContainer()
	{
		throw new UnsupportedOperationException( "Now that is silly!" );
	}

	@Override
	public boolean isBlocked( EnumFacing side )
	{
		return this.tcb.isBlocked( side );
	}

	@Override
	public SelectedPart selectPart( Vec3 pos )
	{
		for( AEPartLocation side : AEPartLocation.values() )
		{
			IPart p = this.getPart( side );
			if( p != null )
			{
				List<AxisAlignedBB> boxes = new LinkedList<AxisAlignedBB>();

				IPartCollisionHelper bch = new BusCollisionHelper( boxes, side, null, true );
				p.getBoxes( bch );
				for( AxisAlignedBB bb : boxes )
				{
					bb = bb.expand( 0.002, 0.002, 0.002 );
					if( bb.isVecInside( pos ) )
					{
						return new SelectedPart( p, side );
					}
				}
			}
		}

		if( AEApi.instance().partHelper().getCableRenderMode().opaqueFacades )
		{
			IFacadeContainer fc = this.getFacadeContainer();
			for( AEPartLocation side : AEPartLocation.SIDE_LOCATIONS )
			{
				IFacadePart p = fc.getFacade( side );
				if( p != null )
				{
					List<AxisAlignedBB> boxes = new LinkedList<AxisAlignedBB>();

					IPartCollisionHelper bch = new BusCollisionHelper( boxes, side, null, true );
					p.getBoxes( bch, null );
					for( AxisAlignedBB bb : boxes )
					{
						bb = bb.expand( 0.01, 0.01, 0.01 );
						if( bb.isVecInside( pos ) )
						{
							return new SelectedPart( p, side );
						}
					}
				}
			}
		}

		return new SelectedPart();
	}

	@Override
	public void markForSave()
	{
		this.tcb.markForSave();
	}

	@Override
	public void partChanged()
	{
		if( this.getCenter() == null )
		{
			List<ItemStack> facades = new LinkedList<ItemStack>();

			IFacadeContainer fc = this.getFacadeContainer();
			for( AEPartLocation d : AEPartLocation.SIDE_LOCATIONS )
			{
				IFacadePart fp = fc.getFacade( d );
				if( fp != null )
				{
					facades.add( fp.getItemStack() );
					fc.removeFacade( this.tcb, d );
				}
			}

			if( !facades.isEmpty() )
			{
				TileEntity te = this.tcb.getTile();
				Platform.spawnDrops( te.getWorld(), te.getPos(), facades );
			}
		}

		this.tcb.partChanged();
	}

	@Override
	public boolean hasRedstone( AEPartLocation side )
	{
		if( this.hasRedstone == YesNo.UNDECIDED )
		{
			this.updateRedstone();
		}

		return this.hasRedstone == YesNo.YES;
	}

	@Override
	public boolean isEmpty()
	{
		IFacadeContainer fc = this.getFacadeContainer();
		for( AEPartLocation s : AEPartLocation.values() )
		{
			IPart part = this.getPart( s );
			if( part != null )
			{
				return false;
			}

			if( s != AEPartLocation.INTERNAL )
			{
				IFacadePart fp = fc.getFacade( s );
				if( fp != null )
				{
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public Set<LayerFlags> getLayerFlags()
	{
		return this.myLayerFlags;
	}

	@Override
	public void cleanup()
	{
		this.tcb.cleanup();
	}

	@Override
	public void notifyNeighbors()
	{
		this.tcb.notifyNeighbors();
	}

	@Override
	public boolean isInWorld()
	{
		return this.inWorld;
	}

	private void updateRedstone()
	{
		TileEntity te = this.getTile();
		this.hasRedstone = te.getWorld().isBlockIndirectlyGettingPowered( te.getPos() ) != 0 ? YesNo.YES : YesNo.NO;
	}

	public void updateDynamicRender()
	{
		this.requiresDynamicRender = false;
		for( AEPartLocation s : AEPartLocation.SIDE_LOCATIONS )
		{
			IPart p = this.getPart( s );
			if( p != null )
			{
				this.requiresDynamicRender = this.requiresDynamicRender || p.requireDynamicRender();
			}
		}
	}

	/**
	 * use for FMP
	 */
	public void updateConnections()
	{
		if( this.getCenter() != null )
		{
			EnumSet<EnumFacing> sides = EnumSet.allOf( EnumFacing.class );

			for( EnumFacing s : EnumFacing.VALUES )
			{
				if( this.getPart( s ) != null || this.isBlocked( s ) )
				{
					sides.remove( s );
				}
			}

			this.getCenter().setValidSides( sides );
			IGridNode n = this.getCenter().getGridNode();
			if( n != null )
			{
				n.updateState();
			}
		}
	}

	public void addToWorld()
	{
		if( this.inWorld )
		{
			return;
		}

		this.inWorld = true;
		IS_LOADING.set( true );

		TileEntity te = this.getTile();

		// start with the center, then install the side parts into the grid.
		for( int x = 6; x >= 0; x-- )
		{
			AEPartLocation s = AEPartLocation.fromOrdinal( x );
			IPart part = this.getPart( s );

			if( part != null )
			{
				part.setPartHostInfo( s, this, te );
				part.addToWorld();

				if( s != AEPartLocation.INTERNAL )
				{
					IGridNode sn = part.getGridNode();
					if( sn != null )
					{
						// this is a really stupid if statement, why was this
						// here?
						// if ( !sn.getConnections().iterator().hasNext() )

						IPart center = this.getPart( AEPartLocation.INTERNAL );
						if( center != null )
						{
							IGridNode cn = center.getGridNode();
							if( cn != null )
							{
								try
								{
									AEApi.instance().createGridConnection( cn, sn );
								}
								catch( FailedConnection e )
								{
									// ekk
								}
							}
						}
					}
				}
			}
		}

		this.partChanged();

		IS_LOADING.set( false );
	}

	public void removeFromWorld()
	{
		if( !this.inWorld )
		{
			return;
		}

		this.inWorld = false;

		for( AEPartLocation s : AEPartLocation.values() )
		{
			IPart part = this.getPart( s );
			if( part != null )
			{
				part.removeFromWorld();
			}
		}

		this.partChanged();
	}

	@Override
	public IGridNode getGridNode( AEPartLocation side )
	{
		IPart part = this.getPart( side );
		if( part != null )
		{
			IGridNode n = part.getExternalFacingNode();
			if( n != null )
			{
				return n;
			}
		}

		if( this.getCenter() != null )
		{
			return this.getCenter().getGridNode();
		}

		return null;
	}

	@Override
	public AECableType getCableConnectionType( AEPartLocation dir )
	{
		IPart part = this.getPart( dir );
		if( part instanceof IGridHost )
		{
			AECableType t = ( (IGridHost) part ).getCableConnectionType( dir );
			if( t != null && t != AECableType.NONE )
			{
				return t;
			}
		}

		if( this.getCenter() != null )
		{
			IPartCable c = this.getCenter();
			return c.getCableConnectionType();
		}
		return AECableType.NONE;
	}

	@Override
	public void securityBreak()
	{
		for( AEPartLocation d : AEPartLocation.values() )
		{
			IPart p = this.getPart( d );
			if( p instanceof IGridHost )
			{
				( (IGridHost) p ).securityBreak();
			}
		}
	}

	public Iterable<AxisAlignedBB> getSelectedBoundingBoxesFromPool( boolean ignoreConnections, boolean includeFacades, Entity e, boolean visual )
	{
		List<AxisAlignedBB> boxes = new LinkedList<AxisAlignedBB>();

		IFacadeContainer fc = this.getFacadeContainer();
		for( AEPartLocation s : AEPartLocation.values() )
		{
			IPartCollisionHelper bch = new BusCollisionHelper( boxes, s, e, visual );

			IPart part = this.getPart( s );
			if( part != null )
			{
				if( ignoreConnections && part instanceof IPartCable )
				{
					bch.addBox( 6.0, 6.0, 6.0, 10.0, 10.0, 10.0 );
				}
				else
				{
					part.getBoxes( bch );
				}
			}

			if( AEApi.instance().partHelper().getCableRenderMode().opaqueFacades || !visual )
			{
				if( includeFacades && s != null && s != AEPartLocation.INTERNAL )
				{
					IFacadePart fp = fc.getFacade( s );
					if( fp != null )
					{
						fp.getBoxes( bch, e );
					}
				}
			}
		}

		return boxes;
	}

	@Override
	public int isProvidingStrongPower( EnumFacing side )
	{
		IPart part = this.getPart( side );
		return part != null ? part.isProvidingStrongPower() : 0;
	}

	@Override
	public int isProvidingWeakPower( EnumFacing side )
	{
		IPart part = this.getPart( side );
		return part != null ? part.isProvidingWeakPower() : 0;
	}

	@Override
	public boolean canConnectRedstone( EnumSet<EnumFacing> enumSet )
	{
		for( EnumFacing dir : enumSet )
		{
			IPart part = this.getPart( dir );
			if( part != null && part.canConnectRedstone() )
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public void onEntityCollision( Entity entity )
	{
		for( AEPartLocation s : AEPartLocation.values() )
		{
			IPart part = this.getPart( s );
			if( part != null )
			{
				part.onEntityCollision( entity );
			}
		}
	}

	@Override
	public boolean activate( EntityPlayer player, Vec3 pos )
	{
		SelectedPart p = this.selectPart( pos );
		if( p != null && p.part != null )
		{
			return p.part.onActivate( player, pos );
		}
		return false;
	}

	@Override
	public void onNeighborChanged()
	{
		this.hasRedstone = YesNo.UNDECIDED;

		for( AEPartLocation s : AEPartLocation.values() )
		{
			IPart part = this.getPart( s );
			if( part != null )
			{
				part.onNeighborChanged();
			}
		}
	}

	@Override
	public boolean isSolidOnSide( EnumFacing side )
	{
		if( side == null )
		{
			return false;
		}

		// facades are solid..
		IFacadePart fp = this.getFacadeContainer().getFacade( AEPartLocation.fromFacing( side ) );
		if( fp != null )
		{
			return true;
		}

		// buses can be too.
		IPart part = this.getPart( side );
		return part != null && part.isSolid();
	}

	@Override
	public boolean isLadder( EntityLivingBase entity )
	{
		for( AEPartLocation side : AEPartLocation.values() )
		{
			IPart p = this.getPart( side );
			if( p != null )
			{
				if( p.isLadder( entity ) )
				{
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public void randomDisplayTick( World world, BlockPos pos, Random r )
	{
		for( AEPartLocation side : AEPartLocation.values() )
		{
			IPart p = this.getPart( side );
			if( p != null )
			{
				p.randomDisplayTick( world, pos, r );
			}
		}
	}

	@Override
	public int getLightValue()
	{
		int light = 0;

		for( AEPartLocation d : AEPartLocation.values() )
		{
			IPart p = this.getPart( d );
			if( p != null )
			{
				light = Math.max( p.getLightLevel(), light );
			}
		}

		if( light > 0 && IntegrationRegistry.INSTANCE.isEnabled( IntegrationType.CLApi ) )
		{
			return ( (ICLApi) IntegrationRegistry.INSTANCE.getInstance( IntegrationType.CLApi ) ).colorLight( this.getColor(), light );
		}

		return light;
	}

	@SideOnly( Side.CLIENT )
	public void renderStatic()
	{
		CableRenderHelper.getInstance().renderStatic( this, this.getFacadeContainer() );
	}

	@SideOnly( Side.CLIENT )
	public void renderDynamic( double x, double y, double z )
	{
		CableRenderHelper.getInstance().renderDynamic( this, x, y, z );
	}

	public void writeToStream( ByteBuf data ) throws IOException
	{
		int sides = 0;
		for( int x = 0; x < 7; x++ )
		{
			IPart p = this.getPart( AEPartLocation.fromOrdinal( x ) );
			if( p != null )
			{
				sides |= ( 1 << x );
			}
		}

		data.writeByte( (byte) sides );

		for( int x = 0; x < 7; x++ )
		{
			ItemStack is = null;
			IPart p = this.getPart( AEPartLocation.fromOrdinal( x ) );
			if( p != null )
			{
				is = p.getItemStack( PartItemStack.Network );

				data.writeShort( Item.getIdFromItem( is.getItem() ) );
				data.writeShort( is.getItemDamage() );

				p.writeToStream( data );
			}
		}

		this.getFacadeContainer().writeToStream( data );
	}

	public boolean readFromStream( ByteBuf data ) throws IOException
	{
		byte sides = data.readByte();

		boolean updateBlock = false;

		for( int x = 0; x < 7; x++ )
		{
			AEPartLocation side = AEPartLocation.fromOrdinal( x );
			if( ( ( sides & ( 1 << x ) ) == ( 1 << x ) ) )
			{
				IPart p = this.getPart( side );

				short itemID = data.readShort();
				short dmgValue = data.readShort();

				Item myItem = Item.getItemById( itemID );

				ItemStack current = p != null ? p.getItemStack( PartItemStack.Network ) : null;
				if( current != null && current.getItem() == myItem && current.getItemDamage() == dmgValue )
				{
					if( p.readFromStream( data ) )
					{
						updateBlock = true;
					}
				}
				else
				{
					this.removePart( side, false );
					side = this.addPart( new ItemStack( myItem, 1, dmgValue ), side, null );
					if( side != null )
					{
						p = this.getPart( side );
						p.readFromStream( data );
					}
					else
					{
						throw new IllegalStateException( "Invalid Stream For CableBus Container." );
					}
				}
			}
			else if( this.getPart( side ) != null )
			{
				this.removePart( side, false );
			}
		}

		if( this.getFacadeContainer().readFromStream( data ) )
		{
			return true;
		}

		return updateBlock;
	}

	public void writeToNBT( NBTTagCompound data )
	{
		data.setInteger( "hasRedstone", this.hasRedstone.ordinal() );

		IFacadeContainer fc = this.getFacadeContainer();
		for( AEPartLocation s : AEPartLocation.values() )
		{
			fc.writeToNBT( data );

			IPart part = this.getPart( s );
			if( part != null )
			{
				NBTTagCompound def = new NBTTagCompound();
				part.getItemStack( PartItemStack.World ).writeToNBT( def );

				NBTTagCompound extra = new NBTTagCompound();
				part.writeToNBT( extra );

				data.setTag( "def:" + this.getSide( part ).ordinal(), def );
				data.setTag( "extra:" + this.getSide( part ).ordinal(), extra );
			}
		}
	}

	AEPartLocation getSide( IPart part )
	{
		if( this.getCenter() == part )
		{
			return AEPartLocation.INTERNAL;
		}
		else
		{
			for( AEPartLocation side : AEPartLocation.SIDE_LOCATIONS )
			{
				if( this.getSide( side ) == part )
				{
					return side;
				}
			}
		}

		throw new IllegalStateException( "Uhh Bad Part (" + part + ") on Side." );
	}

	public void readFromNBT( NBTTagCompound data )
	{
		if( data.hasKey( "hasRedstone" ) )
		{
			this.hasRedstone = YesNo.values()[data.getInteger( "hasRedstone" )];
		}

		for( int x = 0; x < 7; x++ )
		{
			AEPartLocation side = AEPartLocation.fromOrdinal( x );

			NBTTagCompound def = data.getCompoundTag( "def:" + side.ordinal() );
			NBTTagCompound extra = data.getCompoundTag( "extra:" + side.ordinal() );
			if( def != null && extra != null )
			{
				IPart p = this.getPart( side );
				ItemStack iss = ItemStack.loadItemStackFromNBT( def );
				if( iss == null )
				{
					continue;
				}

				ItemStack current = p == null ? null : p.getItemStack( PartItemStack.World );

				if( Platform.isSameItemType( iss, current ) )
				{
					p.readFromNBT( extra );
				}
				else
				{
					this.removePart( side, true );
					side = this.addPart( iss, side, null );
					if( side != null )
					{
						p = this.getPart( side );
						p.readFromNBT( extra );
					}
					else
					{
						AELog.warning( "Invalid NBT For CableBus Container: " + iss.getItem().getClass().getName() + " is not a valid part; it was ignored." );
					}
				}
			}
			else
			{
				this.removePart( side, false );
			}
		}

		this.getFacadeContainer().readFromNBT( data );
	}

	public List getDrops( List drops )
	{
		for( AEPartLocation s : AEPartLocation.values() )
		{
			IPart part = this.getPart( s );
			if( part != null )
			{
				drops.add( part.getItemStack( PartItemStack.Break ) );
				part.getDrops( drops, false );
			}

			if( s != AEPartLocation.INTERNAL )
			{
				IFacadePart fp = this.getFacadeContainer().getFacade( s );
				if( fp != null )
				{
					drops.add( fp.getItemStack() );
				}
			}
		}

		return drops;
	}

	public List getNoDrops( List drops )
	{
		for( AEPartLocation s : AEPartLocation.values() )
		{
			IPart part = this.getPart( s );
			if( part != null )
			{
				part.getDrops( drops, false );
			}
		}

		return drops;
	}

	@Override
	public boolean recolourBlock( EnumFacing side, AEColor colour, EntityPlayer who )
	{
		IPart cable = this.getPart( AEPartLocation.INTERNAL );
		if( cable != null )
		{
			IPartCable pc = (IPartCable) cable;
			return pc.changeColor( colour, who );
		}
		return false;
	}
}
