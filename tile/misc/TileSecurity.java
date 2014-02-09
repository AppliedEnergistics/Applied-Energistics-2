package appeng.tile.misc;

import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.AEApi;
import appeng.api.config.SecurityPermissions;
import appeng.api.events.LocatableEventAnnounce;
import appeng.api.events.LocatableEventAnnounce.LocatableEvent;
import appeng.api.features.ILocatable;
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
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.IAEAppEngInventory;
import appeng.tile.inventory.InvOperation;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;

public class TileSecurity extends AENetworkTile implements IStorageMonitorable, IAEAppEngInventory, ILocatable
{

	private static int diffrence = 0;

	private SecurityInventory inventory = new SecurityInventory( this );
	private MEMonitorHandler<IAEItemStack> securityMonitor = new MEMonitorHandler<IAEItemStack>( inventory );

	private boolean isActive = false;

	public long securityKey;

	public AppEngInternalInventory configSlot = new AppEngInternalInventory( this, 1 );

	@Override
	public void onChangeInventory(IInventory inv, int slot, InvOperation mc, ItemStack removedStack, ItemStack newStack)
	{

	}

	@Override
	public void getDrops(World w, int x, int y, int z, ArrayList<ItemStack> drops)
	{
		if ( !configSlot.isEmpty() )
			drops.add( configSlot.getStackInSlot( 0 ) );

		for (IAEItemStack ais : inventory.storedItems)
			drops.add( ais.getItemStack() );
	}

	IMEInventoryHandler<IAEItemStack> getSecurityInventory()
	{
		return inventory;
	}

	@Override
	public void onReady()
	{
		super.onReady();
		if ( Platform.isServer() )
		{
			isActive = true;
			MinecraftForge.EVENT_BUS.post( new LocatableEventAnnounce( this, LocatableEvent.Register ) );
		}
	}

	@Override
	public void onChunkUnload()
	{
		super.onChunkUnload();
		MinecraftForge.EVENT_BUS.post( new LocatableEventAnnounce( this, LocatableEvent.Unregister ) );
		isActive = false;
	}

	@Override
	public void invalidate()
	{
		super.invalidate();
		MinecraftForge.EVENT_BUS.post( new LocatableEventAnnounce( this, LocatableEvent.Unregister ) );
		isActive = false;
	}

	class SecurityHandler extends AETileEventHandler
	{

		public SecurityHandler() {
			super( TileEventType.WORLD_NBT, TileEventType.NETWORK );
		}

		@Override
		public boolean readFromStream(ByteBuf data) throws IOException
		{
			boolean wasActive = isActive;
			isActive = data.readBoolean();

			return wasActive != isActive;
		}

		@Override
		public void writeToStream(ByteBuf data) throws IOException
		{
			data.writeBoolean( gridProxy.isActive() );
		}

		@Override
		public void writeToNBT(NBTTagCompound data)
		{
			data.setLong( "securityKey", securityKey );
			configSlot.writeToNBT( data, "config" );

			NBTTagCompound storedItems = new NBTTagCompound();

			int offset = 0;
			for (IAEItemStack ais : inventory.storedItems)
			{
				NBTTagCompound it = new NBTTagCompound();
				ais.getItemStack().writeToNBT( it );
				storedItems.setTag( "" + (offset++), it );
			}

			data.setTag( "storedItems", storedItems );
		}

		@Override
		public void readFromNBT(NBTTagCompound data)
		{
			securityKey = data.getLong( "securityKey" );
			configSlot.readFromNBT( data, "config" );

			NBTTagCompound storedItems = data.getCompoundTag( "storedItems" );
			for (Object key : storedItems.func_150296_c())
			{
				NBTBase obj = storedItems.getTag( (String) key );
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
				playerPerms.put( pr.getID( bc.getUsername( is ) ), bc.getPermissions( is ) );
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

	@Override
	public long getLocatableSerial()
	{
		return securityKey;
	}

	public boolean isPowered()
	{
		return gridProxy.isActive();
	}

}
