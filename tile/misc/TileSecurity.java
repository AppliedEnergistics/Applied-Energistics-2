package appeng.tile.misc;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.EnumSet;
import java.util.HashMap;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;
import appeng.api.AEApi;
import appeng.api.config.SecurityPermissions;
import appeng.api.features.IPlayerRegistry;
import appeng.api.implementations.items.IBiometricCard;
import appeng.api.networking.GridFlags;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.events.MENetworkSecurityChange;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IStorageMonitorable;
import appeng.api.storage.MEMonitorHandler;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalCoord;
import appeng.me.GridAccessException;
import appeng.me.storage.SecurityInventory;
import appeng.tile.events.AETileEventHandler;
import appeng.tile.events.TileEventType;
import appeng.tile.grid.AENetworkTile;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;

public class TileSecurity extends AENetworkTile implements IStorageMonitorable
{

	private static int diffrence = 0;

	private SecurityInventory inventory = new SecurityInventory( this );
	private MEMonitorHandler<IAEItemStack> securityMonitor = new MEMonitorHandler<IAEItemStack>( inventory );

	private boolean isActive = false;

	public long securityKey;

	IMEInventoryHandler<IAEItemStack> getSecurityInventory()
	{
		return inventory;
	}

	@Override
	public void onReady()
	{
		super.onReady();
		if ( Platform.isServer() )
			isActive = true;
	}

	@Override
	public void onChunkUnload()
	{
		super.onChunkUnload();
		isActive = false;
	}

	@Override
	public void invalidate()
	{
		super.invalidate();
		isActive = false;
	}

	class SecurityHandler extends AETileEventHandler
	{

		public SecurityHandler() {
			super( TileEventType.WORLD_NBT, TileEventType.NETWORK );
		}

		@Override
		public boolean readFromStream(DataInputStream data) throws IOException
		{
			boolean wasActive = isActive;
			isActive = data.readBoolean();

			return wasActive != isActive;
		}

		@Override
		public void writeToStream(DataOutputStream data) throws IOException
		{
			data.writeBoolean( gridProxy.isActive() );
		}

		@Override
		public void writeToNBT(NBTTagCompound data)
		{
			data.setLong( "securityKey", securityKey );
			NBTTagCompound storedItems = new NBTTagCompound();

			int offset = 0;
			for (IAEItemStack ais : inventory.storedItems)
			{
				NBTTagCompound it = new NBTTagCompound();
				ais.getItemStack().writeToNBT( it );
				storedItems.setCompoundTag( "" + (offset++), it );
			}

			data.setCompoundTag( "storedItems", storedItems );
		}

		@Override
		public void readFromNBT(NBTTagCompound data)
		{
			securityKey = data.getLong( "securityKey" );
			NBTTagCompound storedItems = data.getCompoundTag( "storedItems" );
			for (Object obj : storedItems.getTags())
			{
				if ( obj instanceof NBTTagCompound )
				{
					inventory.storedItems.add( AEItemStack.create( ItemStack.loadItemStackFromNBT( (NBTTagCompound) obj ) ) );
				}
			}
		}
	};

	public void inventoryChanged()
	{
		try
		{
			saveChanges();
			gridProxy.getGrid().postEvent( new MENetworkSecurityChange() );
		}
		catch (GridAccessException e)
		{
			// :P
		}
	}

	public void readPermissions(HashMap<Integer, EnumSet<SecurityPermissions>> playerPerms)
	{
		IPlayerRegistry pr = AEApi.instance().registries().players();

		// read permissions
		for (IAEItemStack ais : inventory.storedItems)
		{
			ItemStack is = ais.getItemStack();
			Item i = is.getItem();
			if ( i instanceof IBiometricCard )
			{
				IBiometricCard bc = (IBiometricCard) i;
				playerPerms.put( pr.getID( bc.getUserName( is ) ), bc.getPermissions( is ) );
			}
		}

		// make sure thea admin is Boss.
		playerPerms.put( gridProxy.getNode().getPlayerID(), EnumSet.allOf( SecurityPermissions.class ) );
	}

	@MENetworkEventSubscribe
	public void bootUpdate(MENetworkChannelsChanged changed)
	{
		markForUpdate();
	}

	@MENetworkEventSubscribe
	public void powerUpdate(MENetworkPowerStatusChange changed)
	{
		markForUpdate();
	}

	public boolean isSecurityEnabled()
	{
		return isActive && gridProxy.isActive();
	}

	public void updateNodeCount(int nodes)
	{
		gridProxy.setIdlePowerUsage( 2.0 + ((double) nodes / 0.033) );
	}

	public TileSecurity() {
		addNewHandler( new SecurityHandler() );
		gridProxy.setFlags( GridFlags.REQUIRE_CHANNEL );
		gridProxy.setIdlePowerUsage( 2.0 );
		diffrence++;
		securityKey = System.currentTimeMillis() * 10 + diffrence;
		if ( diffrence > 10 )
			diffrence = 0;
	}

	public int getOwner()
	{
		return gridProxy.getNode().getPlayerID();
	}

	@Override
	public AECableType getCableConnectionType(ForgeDirection dir)
	{
		return AECableType.SMART;
	}

	@Override
	public DimensionalCoord getLocation()
	{
		return new DimensionalCoord( this );
	}

	public boolean isActive()
	{
		return isActive;
	}

	@Override
	public IMEMonitor<IAEItemStack> getItemInventory()
	{
		return securityMonitor;
	}

	@Override
	public IMEMonitor<IAEFluidStack> getFluidInventory()
	{
		return null;
	}

}
