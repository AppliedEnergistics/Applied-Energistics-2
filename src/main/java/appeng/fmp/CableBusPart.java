package appeng.fmp;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.implementations.parts.IPartCable;
import appeng.api.networking.IGridNode;
import appeng.api.parts.IFacadeContainer;
import appeng.api.parts.IFacadePart;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartItem;
import appeng.api.parts.LayerFlags;
import appeng.api.parts.PartItemStack;
import appeng.api.parts.SelectedPart;
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
import codechicken.multipart.IRedstonePart;
import codechicken.multipart.JCuboidPart;
import codechicken.multipart.JNormalOcclusion;
import codechicken.multipart.NormalOcclusionTest;
import codechicken.multipart.NormallyOccludedPart;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.scalatraits.TIInventoryTile;

/**
 * Implementing these might help improve visuals for hollow covers
 * 
 * TSlottedPart,ISidedHollowConnect
 */
public class CableBusPart extends JCuboidPart implements JNormalOcclusion, IRedstonePart, AEMultiTile
{

	final static Cuboid6 sideTests[] = new Cuboid6[] {

	new Cuboid6( 6.0 / 16.0, 0, 6.0 / 16.0, 10.0 / 16.0, 6.0 / 16.0, 10.0 / 16.0 ), // DOWN(0, -1, 0),

			new Cuboid6( 6.0 / 16.0, 10.0 / 16.0, 6.0 / 16.0, 10.0 / 16.0, 1.0, 10.0 / 16.0 ), // UP(0, 1, 0),

			new Cuboid6( 6.0 / 16.0, 6.0 / 16.0, 0.0, 10.0 / 16.0, 10.0 / 16.0, 6.0 / 16.0 ),// NORTH(0, 0, -1),

			new Cuboid6( 6.0 / 16.0, 6.0 / 16.0, 10.0 / 16.0, 10.0 / 16.0, 10.0 / 16.0, 1.0 ),// SOUTH(0, 0, 1),

			new Cuboid6( 0.0, 6.0 / 16.0, 6.0 / 16.0, 6.0 / 16.0, 10.0 / 16.0, 10.0 / 16.0 ),// WEST(-1, 0, 0),

			new Cuboid6( 10.0 / 16.0, 6.0 / 16.0, 6.0 / 16.0, 1.0, 10.0 / 16.0, 10.0 / 16.0 ),// EAST(1, 0, 0),
	};

	public static final ThreadLocal<Boolean> disableFacadeOcclusion = new ThreadLocal<Boolean>();
	public CableBusContainer cb = new CableBusContainer( this );

	@Override
	public boolean isInWorld()
	{
		return cb.isInWorld();
	}

	@Override
	public boolean doesTick()
	{
		return false;
	}

	@Override
	public AECableType getCableConnectionType(ForgeDirection dir)
	{
		return cb.getCableConnectionType( dir );
	}

	@Override
	public boolean recolourBlock(ForgeDirection side, AEColor colour, EntityPlayer who)
	{
		return cb.recolourBlock( side, colour, who );
	}

	@Override
	public AEColor getColor()
	{
		return cb.getColor();
	}

	@Override
	public void save(NBTTagCompound tag)
	{
		cb.writeToNBT( tag );
	}

	@Override
	public void load(NBTTagCompound tag)
	{
		cb.readFromNBT( tag );
	}

	@Override
	public void writeDesc(MCDataOutput packet)
	{
		ByteBuf stream = Unpooled.buffer();

		try
		{
			cb.writeToStream( stream );
			packet.writeInt( stream.readableBytes() );
			stream.capacity( stream.readableBytes() );
			packet.writeByteArray( stream.array() );
		}
		catch (IOException e)
		{
			AELog.error( e );
		}

	}

	@Override
	public void readDesc(MCDataInput packet)
	{
		int len = packet.readInt();
		byte data[] = packet.readByteArray( len );

		try
		{
			if ( len > 0 )
			{
				ByteBuf byteBuffer = Unpooled.wrappedBuffer( data );
				cb.readFromStream( byteBuffer );
			}
		}
		catch (IOException e)
		{
			AELog.error( e );
		}
	}

	@Override
	public Cuboid6 getBounds()
	{
		AxisAlignedBB b = null;

		for (AxisAlignedBB bx : cb.getSelectedBoundingBoxesFromPool( false, true, null, true ))
		{
			if ( b == null )
				b = bx;
			else
			{
				double minX = Math.min( b.minX, bx.minX );
				double minY = Math.min( b.minY, bx.minY );
				double minZ = Math.min( b.minZ, bx.minZ );
				double maxX = Math.max( b.maxX, bx.maxX );
				double maxY = Math.max( b.maxY, bx.maxY );
				double maxZ = Math.max( b.maxZ, bx.maxZ );
				b.setBounds( minX, minY, minZ, maxX, maxY, maxZ );
			}
		}

		if ( b == null )
			return new Cuboid6( 0.0, 0.0, 0.0, 1.0, 1.0, 1.0 );

		return new Cuboid6( b.minX, b.minY, b.minZ, b.maxX, b.maxY, b.maxZ );
	}

	@Override
	public String getType()
	{
		return PartRegistry.CableBusPart.getName();
	}

	@Override
	public void onPartChanged(TMultiPart part)
	{
		cb.updateConnections();
	}

	@Override
	public ItemStack pickItem(MovingObjectPosition hit)
	{
		Vec3 v3 = hit.hitVec.addVector( -hit.blockX, -hit.blockY, -hit.blockZ );
		SelectedPart sp = cb.selectPart( v3 );
		if ( sp != null )
		{
			if ( sp.part != null )
				return sp.part.getItemStack( PartItemStack.Break );
			if ( sp.facade != null )
				return sp.facade.getItemStack();
		}
		return null;
	}

	@Override
	public Iterable<ItemStack> getDrops()
	{
		return cb.getDrops( new ArrayList() );
	}

	@Override
	public void onEntityCollision(Entity entity)
	{
		cb.onEntityCollision( entity );
	}

	@Override
	public void onWorldJoin()
	{
		canUpdate = true;
		cb.updateConnections();
		cb.addToWorld();
	}

	@Override
	public void onWorldSeparate()
	{
		canUpdate = false;
		cb.removeFromWorld();
	}

	@Override
	public boolean canConnectRedstone(int side)
	{
		return cb.canConnectRedstone( EnumSet.of( ForgeDirection.getOrientation( side ) ) );
	}

	@Override
	public int strongPowerLevel(int side)
	{
		return cb.isProvidingStrongPower( ForgeDirection.getOrientation( side ) );
	}

	@Override
	public int weakPowerLevel(int side)
	{
		return cb.isProvidingWeakPower( ForgeDirection.getOrientation( side ) );
	}

	@Override
	public void onNeighborChanged()
	{
		cb.onNeighborChanged();
	}

	@Override
	public boolean activate(EntityPlayer player, MovingObjectPosition hit, ItemStack item)
	{
		return cb.activate( player, hit.hitVec.addVector( -hit.blockX, -hit.blockY, -hit.blockZ ) );
	}

	@Override
	public void renderDynamic(Vector3 pos, float frame, int pass)
	{
		if ( pass == 0 || (pass == 1 && AEConfig.instance.isFeatureEnabled( AEFeature.AlphaPass )) )
		{
			BusRenderHelper.instance.setPass( pass );
			cb.renderDynamic( pos.x, pos.y, pos.z );
		}
	}

	@Override
	public boolean renderStatic(Vector3 pos, int pass)
	{
		if ( pass == 0 || (pass == 1 && AEConfig.instance.isFeatureEnabled( AEFeature.AlphaPass )) )
		{
			BusRenderHelper.instance.setPass( pass );
			BusRenderer.instance.renderer.renderAllFaces = true;
			BusRenderer.instance.renderer.blockAccess = world();
			BusRenderer.instance.renderer.overrideBlockTexture = null;
			cb.renderStatic( pos.x, pos.y, pos.z );
			return BusRenderHelper.instance.getItemsRendered() > 0;
		}
		return false;
	}

	@Override
	public int getLightValue()
	{
		return cb.getLightValue();
	}

	@Override
	public boolean canAddPart(ItemStack is, ForgeDirection side)
	{
		IFacadePart fp = PartPlacement.isFacade( is, side );
		if ( fp != null )
		{
			if ( !(side == null || side == ForgeDirection.UNKNOWN || tile() == null) )
			{
				List<AxisAlignedBB> boxes = new ArrayList<AxisAlignedBB>();
				IPartCollisionHelper bch = new BusCollisionHelper( boxes, side, null, true );
				fp.getBoxes( bch, null );
				for (AxisAlignedBB bb : boxes)
				{
					disableFacadeOcclusion.set( true );
					boolean canAdd = tile().canAddPart( new NormallyOccludedPart( new Cuboid6( bb ) ) );
					disableFacadeOcclusion.remove();
					if ( !canAdd )
					{
						return false;
					}
				}
			}
			return true;
		}

		if ( is.getItem() instanceof IPartItem )
		{
			IPartItem bi = (IPartItem) is.getItem();

			is = is.copy();
			is.stackSize = 1;

			IPart bp = bi.createPartFromItemStack( is );
			if ( !(side == null || side == ForgeDirection.UNKNOWN || tile() == null) )
			{
				List<AxisAlignedBB> boxes = new ArrayList<AxisAlignedBB>();
				IPartCollisionHelper bch = new BusCollisionHelper( boxes, side, null, true );
				bp.getBoxes( bch );
				for (AxisAlignedBB bb : boxes)
				{
					if ( !tile().canAddPart( new NormallyOccludedPart( new Cuboid6( bb ) ) ) )
					{
						return false;
					}
				}
			}
		}

		return cb.canAddPart( is, side );
	}

	@Override
	public ForgeDirection addPart(ItemStack is, ForgeDirection side, EntityPlayer owner)
	{
		return cb.addPart( is, side, owner );
	}

	@Override
	public IPart getPart(ForgeDirection side)
	{
		return cb.getPart( side );
	}

	@Override
	public void removePart(ForgeDirection side, boolean suppressUpdate)
	{
		cb.removePart( side, suppressUpdate );
	}

	boolean canUpdate = false;

	@Override
	public void markForUpdate()
	{
		if ( Platform.isServer() && canUpdate )
			sendDescUpdate();
	}

	@Override
	public DimensionalCoord getLocation()
	{
		return new DimensionalCoord( this.tile() );
	}

	@Override
	public void invalidateConvertedTile()
	{
		cb.setHost( this );
	}

	public void convertFromTile(TileEntity blockTileEntity)
	{
		TileCableBus tcb = (TileCableBus) blockTileEntity;
		cb = tcb.cb;
	}

	@Override
	public boolean occlusionTest(TMultiPart part)
	{
		return NormalOcclusionTest.apply( this, part );
	}

	@Override
	public Iterable<Cuboid6> getCollisionBoxes()
	{
		LinkedList<Cuboid6> l = new LinkedList<Cuboid6>();
		for (AxisAlignedBB b : cb.getSelectedBoundingBoxesFromPool( false, true, null, false ))
		{
			l.add( new Cuboid6( b.minX, b.minY, b.minZ, b.maxX, b.maxY, b.maxZ ) );
		}
		return l;

	}

	@Override
	public Iterable<IndexedCuboid6> getSubParts()
	{
		LinkedList<IndexedCuboid6> l = new LinkedList<IndexedCuboid6>();
		for (Cuboid6 c : getCollisionBoxes())
		{
			l.add( new IndexedCuboid6( 0, c ) );
		}
		return l;
	}

	@Override
	public Iterable<Cuboid6> getOcclusionBoxes()
	{
		LinkedList<Cuboid6> l = new LinkedList<Cuboid6>();
		for (AxisAlignedBB b : cb.getSelectedBoundingBoxesFromPool( true, disableFacadeOcclusion.get() == null, null, true ))
		{
			l.add( new Cuboid6( b.minX, b.minY, b.minZ, b.maxX, b.maxY, b.maxZ ) );
		}
		return l;
	}

	@Override
	public IGridNode getGridNode(ForgeDirection dir)
	{
		return cb.getGridNode( dir );
	}

	@Override
	public IFacadeContainer getFacadeContainer()
	{
		return cb.getFacadeContainer();
	}

	@Override
	public void clearContainer()
	{
		cb = new CableBusContainer( this );
	}

	@Override
	public boolean isBlocked(ForgeDirection side)
	{
		if ( side == null || side == ForgeDirection.UNKNOWN || tile() == null )
			return false;

		disableFacadeOcclusion.set( true );
		boolean blocked = !tile().canAddPart( new NormallyOccludedPart( sideTests[side.ordinal()] ) );
		disableFacadeOcclusion.remove();

		return blocked;
	}

	@Override
	public SelectedPart selectPart(Vec3 pos)
	{
		return cb.selectPart( pos );
	}

	@Override
	public void partChanged()
	{
		if ( isInWorld() )
			notifyNeighbors();
	}

	@Override
	public Set<LayerFlags> getLayerFlags()
	{
		return cb.getLayerFlags();
	}

	@Override
	public void markForSave()
	{
		// mark the chunk for save...
		TileEntity te = this.tile();
		if ( te != null && te.getWorldObj() != null )
			te.getWorldObj().getChunkFromBlockCoords( x(), z() ).isModified = true;
	}

	@Override
	public boolean hasRedstone(ForgeDirection side)
	{
		return cb.hasRedstone( side );
	}

	@Override
	public void securityBreak()
	{
		cb.securityBreak();
	}

	@Override
	public boolean isEmpty()
	{
		return cb.isEmpty();
	}

	@Override
	public void cleanup()
	{
		tile().remPart( this );
	}

	@Override
	public void notifyNeighbors()
	{
		if ( tile() instanceof TIInventoryTile )
			((TIInventoryTile) tile()).rebuildSlotMap();

		if ( world() != null && world().blockExists( x(), y(), z() ) && !CableBusContainer.isLoading() )
			Platform.notifyBlocksOfNeighbors(world(), x(), y(), z() );
	}

	// @Override
	public int getHollowSize(int side)
	{
		IPartCable cable = (IPartCable) getPart( ForgeDirection.UNKNOWN );

		ForgeDirection dir = ForgeDirection.getOrientation( side );
		if ( cable != null && cable.isConnected( dir ) )
		{
			List<AxisAlignedBB> boxes = new ArrayList<AxisAlignedBB>();

			BusCollisionHelper bch = new BusCollisionHelper( boxes, ForgeDirection.EAST, ForgeDirection.UP, ForgeDirection.SOUTH, null, true );

			for (ForgeDirection whichSide : ForgeDirection.values())
			{
				IPart fPart = getPart( whichSide );

				if ( fPart != null )
					fPart.getBoxes( bch );
			}

			AxisAlignedBB b = null;
			AxisAlignedBB pb = Platform.getPrimaryBox( dir, 2 );

			for (AxisAlignedBB bb : boxes)
			{
				if ( bb.intersectsWith( pb ) )
				{
					if ( b == null )
						b = bb;
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

			if ( b == null )
				return 0;

			switch (dir)
			{
			case WEST:
			case EAST:
				return getSize( b.minZ, b.maxZ, b.minY, b.maxY );
			case DOWN:
			case NORTH:
				return getSize( b.minX, b.maxX, b.minZ, b.maxZ );
			case SOUTH:
			case UP:
				return getSize( b.minX, b.maxX, b.minY, b.maxY );
			default:
			}
		}

		return 12;
	}

	int getSize(double a, double b, double c, double d)
	{
		double r = Math.abs( a - 0.5 );
		r = Math.max( Math.abs( b - 0.5 ), r );
		r = Math.max( Math.abs( c - 0.5 ), r );
		return (8 * (int) Math.max( Math.abs( d - 0.5 ), r ));
	}

	// @Override
	public int getSlotMask()
	{
		int mask = 0;

		for (ForgeDirection side : ForgeDirection.values())
		{
			if ( getPart( side ) != null )
				mask |= 1 << side.ordinal();
			else if ( side != ForgeDirection.UNKNOWN && getFacadeContainer().getFacade( side ) != null )
				mask |= 1 << side.ordinal();
		}

		return mask;
	}

}
