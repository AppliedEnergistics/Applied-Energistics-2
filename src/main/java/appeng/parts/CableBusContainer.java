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


import appeng.api.AEApi;
import appeng.api.config.YesNo;
import appeng.api.exceptions.FailedConnection;
import appeng.api.implementations.parts.IPartCable;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.parts.*;
import appeng.api.util.AECableType;
import appeng.api.util.AEColor;
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
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.io.IOException;
import java.util.*;


public class CableBusContainer extends CableBusStorage implements AEMultiTile, ICableBusContainer
{

	private static final ThreadLocal<Boolean> IS_LOADING = new ThreadLocal<Boolean>();
	private final EnumSet<LayerFlags> myLayerFlags = EnumSet.noneOf( LayerFlags.class );
	private YesNo hasRedstone = YesNo.UNDECIDED;
	private IPartHost tcb;
	private boolean requiresDynamicRender = false;
	private boolean inWorld = false;

	public CableBusContainer( final IPartHost host )
	{
		this.tcb = host;
	}

	public static boolean isLoading()
	{
		final Boolean is = IS_LOADING.get();
		return is != null && is;
	}

	public void setHost( final IPartHost host )
	{
		this.tcb.clearContainer();
		this.tcb = host;
	}

	public void rotateLeft()
	{
		final IPart[] newSides = new IPart[6];

		newSides[ForgeDirection.UP.ordinal()] = this.getSide( ForgeDirection.UP );
		newSides[ForgeDirection.DOWN.ordinal()] = this.getSide( ForgeDirection.DOWN );

		newSides[ForgeDirection.EAST.ordinal()] = this.getSide( ForgeDirection.NORTH );
		newSides[ForgeDirection.SOUTH.ordinal()] = this.getSide( ForgeDirection.EAST );
		newSides[ForgeDirection.WEST.ordinal()] = this.getSide( ForgeDirection.SOUTH );
		newSides[ForgeDirection.NORTH.ordinal()] = this.getSide( ForgeDirection.WEST );

		for( final ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS )
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
	public boolean canAddPart( ItemStack is, final ForgeDirection side )
	{
		if( PartPlacement.isFacade( is, side ) != null )
		{
			return true;
		}

		if( is.getItem() instanceof IPartItem )
		{
			final IPartItem bi = (IPartItem) is.getItem();

			is = is.copy();
			is.stackSize = 1;

			final IPart bp = bi.createPartFromItemStack( is );
			if( bp != null )
			{
				if( bp instanceof IPartCable )
				{
					boolean canPlace = true;
					for( final ForgeDirection d : ForgeDirection.VALID_DIRECTIONS )
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

					return this.getPart( ForgeDirection.UNKNOWN ) == null;
				}
				else if( !( bp instanceof IPartCable ) && side != ForgeDirection.UNKNOWN )
				{
					final IPart cable = this.getPart( ForgeDirection.UNKNOWN );
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
	public ForgeDirection addPart( ItemStack is, final ForgeDirection side, final EntityPlayer player )
	{
		if( this.canAddPart( is, side ) )
		{
			if( is.getItem() instanceof IPartItem )
			{
				final IPartItem bi = (IPartItem) is.getItem();

				is = is.copy();
				is.stackSize = 1;

				final IPart bp = bi.createPartFromItemStack( is );
				if( bp instanceof IPartCable )
				{
					boolean canPlace = true;
					for( final ForgeDirection d : ForgeDirection.VALID_DIRECTIONS )
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

					if( this.getPart( ForgeDirection.UNKNOWN ) != null )
					{
						return null;
					}

					this.setCenter( (IPartCable) bp );
					bp.setPartHostInfo( ForgeDirection.UNKNOWN, this, this.tcb.getTile() );

					if( player != null )
					{
						bp.onPlacement( player, is, side );
					}

					if( this.inWorld )
					{
						bp.addToWorld();
					}

					final IGridNode cn = this.getCenter().getGridNode();
					if( cn != null )
					{
						for( final ForgeDirection ins : ForgeDirection.VALID_DIRECTIONS )
						{
							final IPart sbp = this.getPart( ins );
							if( sbp != null )
							{
								final IGridNode sn = sbp.getGridNode();
								if( sn != null )
								{
									try
									{
										new GridConnection( cn, sn, ForgeDirection.UNKNOWN );
									}
									catch( final FailedConnection e )
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
					return ForgeDirection.UNKNOWN;
				}
				else if( bp != null && !( bp instanceof IPartCable ) && side != ForgeDirection.UNKNOWN )
				{
					final IPart cable = this.getPart( ForgeDirection.UNKNOWN );
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
						final IGridNode cn = this.getCenter().getGridNode();
						final IGridNode sn = bp.getGridNode();

						if( cn != null && sn != null )
						{
							try
							{
								new GridConnection( cn, sn, ForgeDirection.UNKNOWN );
							}
							catch( final FailedConnection e )
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
	public IPart getPart( final ForgeDirection side )
	{
		if( side == ForgeDirection.UNKNOWN )
		{
			return this.getCenter();
		}
		return this.getSide( side );
	}

	@Override
	public void removePart( final ForgeDirection side, final boolean suppressUpdate )
	{
		if( side == ForgeDirection.UNKNOWN )
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
			final IPartCable c = this.getCenter();
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
	public boolean isBlocked( final ForgeDirection side )
	{
		return this.tcb.isBlocked( side );
	}

	@Override
	public SelectedPart selectPart( final Vec3 pos )
	{
		for( final ForgeDirection side : ForgeDirection.values() )
		{
			final IPart p = this.getPart( side );
			if( p != null )
			{
				final List<AxisAlignedBB> boxes = new LinkedList<AxisAlignedBB>();

				final IPartCollisionHelper bch = new BusCollisionHelper( boxes, side, null, true );
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
			final IFacadeContainer fc = this.getFacadeContainer();
			for( final ForgeDirection side : ForgeDirection.VALID_DIRECTIONS )
			{
				final IFacadePart p = fc.getFacade( side );
				if( p != null )
				{
					final List<AxisAlignedBB> boxes = new LinkedList<AxisAlignedBB>();

					final IPartCollisionHelper bch = new BusCollisionHelper( boxes, side, null, true );
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
			final List<ItemStack> facades = new LinkedList<ItemStack>();

			final IFacadeContainer fc = this.getFacadeContainer();
			for( final ForgeDirection d : ForgeDirection.VALID_DIRECTIONS )
			{
				final IFacadePart fp = fc.getFacade( d );
				if( fp != null )
				{
					facades.add( fp.getItemStack() );
					fc.removeFacade( this.tcb, d );
				}
			}

			if( !facades.isEmpty() )
			{
				final TileEntity te = this.tcb.getTile();
				Platform.spawnDrops( te.getWorldObj(), te.xCoord, te.yCoord, te.zCoord, facades );
			}
		}

		this.tcb.partChanged();
	}

	@Override
	public boolean hasRedstone( final ForgeDirection side )
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
		final IFacadeContainer fc = this.getFacadeContainer();
		for( final ForgeDirection s : ForgeDirection.values() )
		{
			final IPart part = this.getPart( s );
			if( part != null )
			{
				return false;
			}

			if( s != ForgeDirection.UNKNOWN )
			{
				final IFacadePart fp = fc.getFacade( s );
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
		final TileEntity te = this.getTile();
		this.hasRedstone = te.getWorldObj().isBlockIndirectlyGettingPowered( te.xCoord, te.yCoord, te.zCoord ) ? YesNo.YES : YesNo.NO;
	}

	private void updateDynamicRender()
	{
		this.setRequiresDynamicRender( false );
		for( final ForgeDirection s : ForgeDirection.VALID_DIRECTIONS )
		{
			final IPart p = this.getPart( s );
			if( p != null )
			{
				this.setRequiresDynamicRender( this.isRequiresDynamicRender() || p.requireDynamicRender() );
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
			final EnumSet<ForgeDirection> sides = EnumSet.allOf( ForgeDirection.class );

			for( final ForgeDirection s : ForgeDirection.VALID_DIRECTIONS )
			{
				if( this.getPart( s ) != null || this.isBlocked( s ) )
				{
					sides.remove( s );
				}
			}

			this.getCenter().setValidSides( sides );
			final IGridNode n = this.getCenter().getGridNode();
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

		final TileEntity te = this.getTile();

		// start with the center, then install the side parts into the grid.
		for( int x = 6; x >= 0; x-- )
		{
			final ForgeDirection s = ForgeDirection.getOrientation( x );
			final IPart part = this.getPart( s );

			if( part != null )
			{
				part.setPartHostInfo( s, this, te );
				part.addToWorld();

				if( s != ForgeDirection.UNKNOWN )
				{
					final IGridNode sn = part.getGridNode();
					if( sn != null )
					{
						// this is a really stupid if statement, why was this
						// here?
						// if ( !sn.getConnections().iterator().hasNext() )

						final IPart center = this.getPart( ForgeDirection.UNKNOWN );
						if( center != null )
						{
							final IGridNode cn = center.getGridNode();
							if( cn != null )
							{
								try
								{
									AEApi.instance().createGridConnection( cn, sn );
								}
								catch( final FailedConnection e )
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

		for( final ForgeDirection s : ForgeDirection.values() )
		{
			final IPart part = this.getPart( s );
			if( part != null )
			{
				part.removeFromWorld();
			}
		}

		this.partChanged();
	}

	@Override
	public IGridNode getGridNode( final ForgeDirection side )
	{
		final IPart part = this.getPart( side );
		if( part != null )
		{
			final IGridNode n = part.getExternalFacingNode();
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
	public AECableType getCableConnectionType( final ForgeDirection dir )
	{
		final IPart part = this.getPart( dir );
		if( part instanceof IGridHost )
		{
			final AECableType t = ( (IGridHost) part ).getCableConnectionType( dir );
			if( t != null && t != AECableType.NONE )
			{
				return t;
			}
		}

		if( this.getCenter() != null )
		{
			final IPartCable c = this.getCenter();
			return c.getCableConnectionType();
		}
		return AECableType.NONE;
	}

	@Override
	public void securityBreak()
	{
		for( final ForgeDirection d : ForgeDirection.values() )
		{
			final IPart p = this.getPart( d );
			if( p instanceof IGridHost )
			{
				( (IGridHost) p ).securityBreak();
			}
		}
	}

	public Iterable<AxisAlignedBB> getSelectedBoundingBoxesFromPool( final boolean ignoreConnections, final boolean includeFacades, final Entity e, final boolean visual )
	{
		final List<AxisAlignedBB> boxes = new LinkedList<AxisAlignedBB>();

		final IFacadeContainer fc = this.getFacadeContainer();
		for( final ForgeDirection s : ForgeDirection.values() )
		{
			final IPartCollisionHelper bch = new BusCollisionHelper( boxes, s, e, visual );

			final IPart part = this.getPart( s );
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
				if( includeFacades && s != null && s != ForgeDirection.UNKNOWN )
				{
					final IFacadePart fp = fc.getFacade( s );
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
	public int isProvidingStrongPower( final ForgeDirection side )
	{
		final IPart part = this.getPart( side );
		return part != null ? part.isProvidingStrongPower() : 0;
	}

	@Override
	public int isProvidingWeakPower( final ForgeDirection side )
	{
		final IPart part = this.getPart( side );
		return part != null ? part.isProvidingWeakPower() : 0;
	}

	@Override
	public boolean canConnectRedstone( final EnumSet<ForgeDirection> enumSet )
	{
		for( final ForgeDirection dir : enumSet )
		{
			final IPart part = this.getPart( dir );
			if( part != null && part.canConnectRedstone() )
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public void onEntityCollision( final Entity entity )
	{
		for( final ForgeDirection s : ForgeDirection.values() )
		{
			final IPart part = this.getPart( s );
			if( part != null )
			{
				part.onEntityCollision( entity );
			}
		}
	}

	@Override
	public boolean activate( final EntityPlayer player, final Vec3 pos )
	{
		final SelectedPart p = this.selectPart( pos );
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

		for( final ForgeDirection s : ForgeDirection.values() )
		{
			final IPart part = this.getPart( s );
			if( part != null )
			{
				part.onNeighborChanged();
			}
		}
	}

	@Override
	public boolean isSolidOnSide( final ForgeDirection side )
	{
		if( side == null || side == ForgeDirection.UNKNOWN )
		{
			return false;
		}

		// facades are solid..
		final IFacadePart fp = this.getFacadeContainer().getFacade( side );
		if( fp != null )
		{
			return true;
		}

		// buses can be too.
		final IPart part = this.getPart( side );
		return part != null && part.isSolid();
	}

	@Override
	public boolean isLadder( final EntityLivingBase entity )
	{
		for( final ForgeDirection side : ForgeDirection.values() )
		{
			final IPart p = this.getPart( side );
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
	public void randomDisplayTick( final World world, final int x, final int y, final int z, final Random r )
	{
		for( final ForgeDirection side : ForgeDirection.values() )
		{
			final IPart p = this.getPart( side );
			if( p != null )
			{
				p.randomDisplayTick( world, x, y, z, r );
			}
		}
	}

	@Override
	public int getLightValue()
	{
		int light = 0;

		for( final ForgeDirection d : ForgeDirection.values() )
		{
			final IPart p = this.getPart( d );
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
	public void renderStatic( final double x, final double y, final double z )
	{
		CableRenderHelper.getInstance().renderStatic( this, this.getFacadeContainer() );
	}

	@SideOnly( Side.CLIENT )
	public void renderDynamic( final double x, final double y, final double z )
	{
		CableRenderHelper.getInstance().renderDynamic( this, x, y, z );
	}

	public void writeToStream( final ByteBuf data ) throws IOException
	{
		int sides = 0;
		for( int x = 0; x < 7; x++ )
		{
			final IPart p = this.getPart( ForgeDirection.getOrientation( x ) );
			if( p != null )
			{
				sides |= ( 1 << x );
			}
		}

		data.writeByte( (byte) sides );

		for( int x = 0; x < 7; x++ )
		{
			final IPart p = this.getPart( ForgeDirection.getOrientation( x ) );
			if( p != null )
			{
				final ItemStack is = p.getItemStack( PartItemStack.Network );

				data.writeShort( Item.getIdFromItem( is.getItem() ) );
				data.writeShort( is.getItemDamage() );

				p.writeToStream( data );
			}
		}

		this.getFacadeContainer().writeToStream( data );
	}

	public boolean readFromStream( final ByteBuf data ) throws IOException
	{
		final byte sides = data.readByte();

		boolean updateBlock = false;

		for( int x = 0; x < 7; x++ )
		{
			ForgeDirection side = ForgeDirection.getOrientation( x );
			if( ( ( sides & ( 1 << x ) ) == ( 1 << x ) ) )
			{
				IPart p = this.getPart( side );

				final short itemID = data.readShort();
				final short dmgValue = data.readShort();

				final Item myItem = Item.getItemById( itemID );

				final ItemStack current = p != null ? p.getItemStack( PartItemStack.Network ) : null;
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

	public void writeToNBT( final NBTTagCompound data )
	{
		data.setInteger( "hasRedstone", this.hasRedstone.ordinal() );

		final IFacadeContainer fc = this.getFacadeContainer();
		for( final ForgeDirection s : ForgeDirection.values() )
		{
			fc.writeToNBT( data );

			final IPart part = this.getPart( s );
			if( part != null )
			{
				final NBTTagCompound def = new NBTTagCompound();
				part.getItemStack( PartItemStack.World ).writeToNBT( def );

				final NBTTagCompound extra = new NBTTagCompound();
				part.writeToNBT( extra );

				data.setTag( "def:" + this.getSide( part ).ordinal(), def );
				data.setTag( "extra:" + this.getSide( part ).ordinal(), extra );
			}
		}
	}

	private ForgeDirection getSide( final IPart part )
	{
		if( this.getCenter() == part )
		{
			return ForgeDirection.UNKNOWN;
		}
		else
		{
			for( final ForgeDirection side : ForgeDirection.VALID_DIRECTIONS )
			{
				if( this.getSide( side ) == part )
				{
					return side;
				}
			}
		}

		throw new IllegalStateException( "Uhh Bad Part (" + part + ") on Side." );
	}

	public void readFromNBT( final NBTTagCompound data )
	{
		if( data.hasKey( "hasRedstone" ) )
		{
			this.hasRedstone = YesNo.values()[data.getInteger( "hasRedstone" )];
		}

		for( int x = 0; x < 7; x++ )
		{
			ForgeDirection side = ForgeDirection.getOrientation( x );

			final NBTTagCompound def = data.getCompoundTag( "def:" + side.ordinal() );
			final NBTTagCompound extra = data.getCompoundTag( "extra:" + side.ordinal() );
			if( def != null && extra != null )
			{
				IPart p = this.getPart( side );
				final ItemStack iss = ItemStack.loadItemStackFromNBT( def );
				if( iss == null )
				{
					continue;
				}

				final ItemStack current = p == null ? null : p.getItemStack( PartItemStack.World );

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
						AELog.warn( "Invalid NBT For CableBus Container: " + iss.getItem().getClass().getName() + " is not a valid part; it was ignored." );
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

	public List getDrops( final List drops )
	{
		for( final ForgeDirection s : ForgeDirection.values() )
		{
			final IPart part = this.getPart( s );
			if( part != null )
			{
				drops.add( part.getItemStack( PartItemStack.Break ) );
				part.getDrops( drops, false );
			}

			if( s != ForgeDirection.UNKNOWN )
			{
				final IFacadePart fp = this.getFacadeContainer().getFacade( s );
				if( fp != null )
				{
					drops.add( fp.getItemStack() );
				}
			}
		}

		return drops;
	}

	public List getNoDrops( final List drops )
	{
		for( final ForgeDirection s : ForgeDirection.values() )
		{
			final IPart part = this.getPart( s );
			if( part != null )
			{
				part.getDrops( drops, false );
			}
		}

		return drops;
	}

	@Override
	public boolean recolourBlock( final ForgeDirection side, final AEColor colour, final EntityPlayer who )
	{
		final IPart cable = this.getPart( ForgeDirection.UNKNOWN );
		if( cable != null )
		{
			final IPartCable pc = (IPartCable) cable;
			return pc.changeColor( colour, who );
		}
		return false;
	}

	public boolean isRequiresDynamicRender()
	{
		return this.requiresDynamicRender;
	}

	private void setRequiresDynamicRender( final boolean requiresDynamicRender )
	{
		this.requiresDynamicRender = requiresDynamicRender;
	}
}
