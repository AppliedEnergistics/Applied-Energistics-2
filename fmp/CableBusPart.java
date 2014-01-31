package appeng.fmp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import powercrystals.minefactoryreloaded.api.rednet.RedNetConnectionType;
import appeng.api.networking.IGridNode;
import appeng.api.parts.IFacadeContainer;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartCollsionHelper;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartItem;
import appeng.api.parts.PartItemStack;
import appeng.api.parts.SelectedPart;
import appeng.api.util.AECableType;
import appeng.api.util.AEColor;
import appeng.api.util.DimensionalCoord;
import appeng.helpers.AEMultiTile;
import appeng.parts.BusCollisionHelper;
import appeng.parts.CableBusContainer;
import appeng.tile.networking.TileCableBus;
import appeng.util.Platform;
import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.lighting.LazyLightMatrix;
import codechicken.lib.raytracer.IndexedCuboid6;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Vector3;
import codechicken.multipart.IRedstonePart;
import codechicken.multipart.JCuboidPart;
import codechicken.multipart.JNormalOcclusion;
import codechicken.multipart.NormalOcclusionTest;
import codechicken.multipart.NormallyOccludedPart;
import codechicken.multipart.TMultiPart;

//TFacePart, 
public class CableBusPart extends JCuboidPart implements JNormalOcclusion, IRedstonePart, IPartHost, AEMultiTile
{

	final static Cuboid6 sideTests[] = new Cuboid6[] {

	new Cuboid6( 6.0 / 16.0, 0, 6.0 / 16.0, 10.0 / 16.0, 6.0 / 16.0, 10.0 / 16.0 ), // DOWN(0, -1, 0),

			new Cuboid6( 6.0 / 16.0, 10.0 / 16.0, 6.0 / 16.0, 10.0 / 16.0, 1.0, 10.0 / 16.0 ), // UP(0, 1, 0),

			new Cuboid6( 6.0 / 16.0, 6.0 / 16.0, 0.0, 10.0 / 16.0, 10.0 / 16.0, 6.0 / 16.0 ),// NORTH(0, 0, -1),

			new Cuboid6( 6.0 / 16.0, 6.0 / 16.0, 10.0 / 16.0, 10.0 / 16.0, 10.0 / 16.0, 1.0 ),// SOUTH(0, 0, 1),

			new Cuboid6( 0.0, 6.0 / 16.0, 6.0 / 16.0, 6.0 / 16.0, 10.0 / 16.0, 10.0 / 16.0 ),// WEST(-1, 0, 0),

			new Cuboid6( 10.0 / 16.0, 6.0 / 16.0, 6.0 / 16.0, 1.0, 10.0 / 16.0, 10.0 / 16.0 ),// EAST(1, 0, 0),
	};

	public CableBusContainer cb = new CableBusContainer( this );

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
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream stream = new DataOutputStream( bytes );

		try
		{
			cb.writeToStream( stream );
			packet.writeInt( bytes.size() );
			packet.writeByteArray( bytes.toByteArray() );
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

	}

	@Override
	public void readDesc(MCDataInput packet)
	{
		int len = packet.readInt();
		byte data[] = packet.readByteArray( len );

		DataInputStream stream = new DataInputStream( new ByteArrayInputStream( data ) );

		try
		{
			cb.readFromStream( stream );
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public Cuboid6 getBounds()
	{
		AxisAlignedBB b = null;

		for (AxisAlignedBB bx : cb.getSelectedBoundingBoxsFromPool( false, null, true ))
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
	};

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
		if ( pass == 0 )
		{
			cb.renderDynamic( pos.x, pos.y, pos.z );
		}
	}

	@Override
	public void renderStatic(Vector3 pos, LazyLightMatrix olm, int pass)
	{
		if ( pass == 0 )
		{
			cb.renderStatic( pos.x, pos.y, pos.z );
		}
	}

	@Override
	public int getLightValue()
	{
		return cb.getLightValue();
	}

	@Override
	public boolean canAddPart(ItemStack is, ForgeDirection side)
	{
		if ( is.getItem() instanceof IPartItem )
		{
			IPartItem bi = (IPartItem) is.getItem();

			is = is.copy();
			is.stackSize = 1;

			IPart bp = bi.createPartFromItemStack( is );
			if ( !(side == null || side == ForgeDirection.UNKNOWN || tile() == null) )
			{
				List<AxisAlignedBB> boxes = new ArrayList();
				IPartCollsionHelper bch = new BusCollisionHelper( boxes, side, null, true );
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
	public void removePart(ForgeDirection side, boolean supressUpdate)
	{
		cb.removePart( side, supressUpdate );
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
		return new DimensionalCoord( getTile() );
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
	public boolean occlusionTest(TMultiPart npart)
	{
		return NormalOcclusionTest.apply( this, npart );
	}

	@Override
	public Iterable<Cuboid6> getCollisionBoxes()
	{
		LinkedList l = new LinkedList();
		for (AxisAlignedBB b : cb.getSelectedBoundingBoxsFromPool( false, null, false ))
		{
			l.add( new Cuboid6( b.minX, b.minY, b.minZ, b.maxX, b.maxY, b.maxZ ) );
		}
		return l;

	}

	@Override
	public Iterable<IndexedCuboid6> getSubParts()
	{
		LinkedList<IndexedCuboid6> l = new LinkedList();
		for (Cuboid6 c : getCollisionBoxes())
		{
			l.add( new IndexedCuboid6( 0, c ) );
		}
		return l;
	}

	@Override
	public Iterable<Cuboid6> getOcclusionBoxes()
	{
		LinkedList l = new LinkedList();
		for (AxisAlignedBB b : cb.getSelectedBoundingBoxsFromPool( true, null, true ))
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
		return !tile().canAddPart( new NormallyOccludedPart( sideTests[side.ordinal()] ) );
	}

	@Override
	public SelectedPart selectPart(Vec3 pos)
	{
		return cb.selectPart( pos );
	}

	@Override
	public void PartChanged()
	{
		// nothing!
	}

	@Override
	public void markForSave()
	{
		this.getTile().onInventoryChanged();
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
	public RedNetConnectionType getConnectionType(World world, int x, int y, int z, ForgeDirection side)
	{
		return cb.getConnectionType( world, x, y, z, side );
	}

	@Override
	public int[] getOutputValues(World world, int x, int y, int z, ForgeDirection side)
	{
		return cb.getOutputValues( world, x, y, z, side );
	}

	@Override
	public int getOutputValue(World world, int x, int y, int z, ForgeDirection side, int subnet)
	{
		return cb.getOutputValue( world, x, y, z, side, subnet );
	}

	@Override
	public void onInputsChanged(World world, int x, int y, int z, ForgeDirection side, int[] inputValues)
	{
		cb.onInputsChanged( world, x, y, z, side, inputValues );
	}

	@Override
	public void onInputChanged(World world, int x, int y, int z, ForgeDirection side, int inputValue)
	{
		cb.onInputChanged( world, x, y, z, side, inputValue );
	}

	@Override
	public boolean isEmpty()
	{
		return cb.isEmpty();
	}

}
