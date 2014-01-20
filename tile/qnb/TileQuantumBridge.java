package appeng.tile.qnb;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.EnumSet;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import appeng.api.AEApi;
import appeng.api.networking.GridFlags;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalCoord;
import appeng.me.GridAccessException;
import appeng.me.cluster.IAECluster;
import appeng.me.cluster.IAEMultiBlock;
import appeng.me.cluster.implementations.QuantumCalculator;
import appeng.me.cluster.implementations.QuantumCluster;
import appeng.tile.events.AETileEventHandler;
import appeng.tile.events.TileEventType;
import appeng.tile.grid.AENetworkInvTile;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.InvOperation;
import appeng.util.Platform;

public class TileQuantumBridge extends AENetworkInvTile implements IAEMultiBlock
{

	final int sidesRing[] = new int[] {};
	final int sidesLink[] = new int[] { 0 };

	AppEngInternalInventory inv = new AppEngInternalInventory( this, 1 );

	public final byte corner = 16;
	final byte hasSingularity = 32;
	final byte powered = 64;

	private QuantumCalculator calc = new QuantumCalculator( this );
	byte xdex = -1;

	QuantumCluster clust;
	public boolean bridgePowered;

	private boolean updateStatus = false;

	class QBridgeHandler extends AETileEventHandler
	{

		public QBridgeHandler() {
			super( EnumSet.of( TileEventType.NETWORK, TileEventType.TICK ) );
			gridProxy.setValidSides( EnumSet.noneOf( ForgeDirection.class ) );
			gridProxy.setFlags( GridFlags.TIER_2_CAPACITY );
			gridProxy.setIdlePowerUsage( 22 );
			inv.setMaxStackSize( 1 );
		}

		@Override
		public void Tick()
		{
			if ( updateStatus )
			{
				updateStatus = false;
				if ( clust != null )
					clust.updateStatus( true );
				markForUpdate();
			}
		}

		@Override
		public void writeToStream(DataOutputStream data) throws IOException
		{
			int out = xdex;

			if ( getStackInSlot( 0 ) != null && xdex != -1 )
				out = out | hasSingularity;

			if ( gridProxy.isActive() && xdex != -1 )
				out = out | powered;

			data.writeByte( (byte) out );
		}

		@Override
		public boolean readFromStream(DataInputStream data) throws IOException
		{
			int oldValue = xdex;
			xdex = data.readByte();
			bridgePowered = (xdex | powered) == powered;
			return xdex != oldValue;
		}

	};

	public TileQuantumBridge() {
		addNewHandler( new QBridgeHandler() );
		gridProxy.setFlags( GridFlags.TIER_2_CAPACITY );
	}

	public IInventory getInternalInventory()
	{
		return inv;
	}

	@MENetworkEventSubscribe
	public void PowerSwitch(MENetworkPowerStatusChange c)
	{
		updateStatus = true;
	}

	@Override
	public void onChangeInventory(IInventory inv, int slot, InvOperation mc, ItemStack removed, ItemStack added)
	{
		if ( clust != null )
			clust.updateStatus( true );
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int side)
	{
		if ( isCenter() )
			return sidesLink;
		return sidesRing;
	}

	@Override
	public void disconnect()
	{
		if ( clust != null )
			clust.destroy();
		clust = null;
		gridProxy.setValidSides( EnumSet.noneOf( ForgeDirection.class ) );
	}

	@Override
	public IAECluster getCluster()
	{
		return clust;
	}

	@Override
	public boolean isValid()
	{
		return !isInvalid();
	}

	public void updateStatus(QuantumCluster c, byte flags)
	{
		clust = c;

		if ( xdex != flags )
		{
			xdex = flags;
			markForUpdate();
		}

		if ( isCorner() || isCenter() )
		{
			gridProxy.setValidSides( getConnections() );
		}
		else
			gridProxy.setValidSides( EnumSet.allOf( ForgeDirection.class ) );
	}

	public long getQEDest()
	{
		ItemStack is = inv.getStackInSlot( 0 );
		if ( is != null )
		{
			NBTTagCompound c = is.getTagCompound();
			if ( c != null )
				return c.getLong( "freq" );
		}
		return 0;
	}

	public boolean isCenter()
	{
		return getBlockType() == AEApi.instance().blocks().blockQuantumLink.block();
	}

	public boolean isCorner()
	{
		return (xdex & corner) == corner && xdex != -1;
	}

	public boolean isPowered()
	{
		if ( Platform.isClient() )
			return (xdex & powered) == powered && xdex != -1;

		try
		{
			return gridProxy.getEnergy().isNetworkPowered();
		}
		catch (GridAccessException e)
		{
			// :P
		}

		return false;
	}

	public boolean isFormed()
	{
		return xdex != -1;
	}

	@Override
	public AECableType getCableConnectionType(ForgeDirection dir)
	{
		return AECableType.DENSE;
	}

	@Override
	public DimensionalCoord getLocation()
	{
		return new DimensionalCoord( this );
	}

	public void neighborUpdate()
	{
		calc.calculateMultiblock( worldObj, getLocation() );
	}

	public EnumSet<ForgeDirection> getConnections()
	{
		EnumSet<ForgeDirection> set = EnumSet.noneOf( ForgeDirection.class );

		for (ForgeDirection d : ForgeDirection.VALID_DIRECTIONS)
		{
			TileEntity te = worldObj.getBlockTileEntity( xCoord + d.offsetX, yCoord + d.offsetY, zCoord + d.offsetZ );
			if ( te instanceof TileQuantumBridge )
				set.add( d );
		}

		return set;
	}

	public boolean hasQES()
	{
		if ( xdex == -1 )
			return false;
		return (xdex & hasSingularity) == hasSingularity;
	}

}
