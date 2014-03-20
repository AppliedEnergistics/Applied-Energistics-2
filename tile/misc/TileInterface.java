package appeng.tile.misc;

import java.util.EnumSet;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.implementations.tiles.ISegmentedInventory;
import appeng.api.implementations.tiles.ITileStorageMonitorable;
import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IStorageMonitorable;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalCoord;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigureableObject;
import appeng.helpers.DualityInterface;
import appeng.helpers.IInterfaceHost;
import appeng.tile.events.AETileEventHandler;
import appeng.tile.events.TileEventType;
import appeng.tile.grid.AENetworkInvTile;
import appeng.tile.inventory.InvOperation;
import appeng.util.Platform;
import appeng.util.inv.IInventoryDestination;

public class TileInterface extends AENetworkInvTile implements IGridTickable, ISegmentedInventory, ITileStorageMonitorable, IStorageMonitorable,
		IInventoryDestination, IInterfaceHost, IConfigureableObject
{

	ForgeDirection pointAt = ForgeDirection.UNKNOWN;
	DualityInterface duality = new DualityInterface( gridProxy, this );

	public void setSide(ForgeDirection axis)
	{
		if ( Platform.isClient() )
			return;

		if ( pointAt == axis.getOpposite() )
			pointAt = axis;
		else if ( pointAt == axis || pointAt == axis.getOpposite() )
			pointAt = ForgeDirection.UNKNOWN;
		else if ( pointAt == ForgeDirection.UNKNOWN )
			pointAt = axis.getOpposite();
		else
			pointAt = Platform.rotateAround( pointAt, axis );

		if ( ForgeDirection.UNKNOWN == pointAt )
			setOrientation( pointAt, pointAt );
		else
			setOrientation( pointAt.offsetY != 0 ? ForgeDirection.SOUTH : ForgeDirection.UP, pointAt.getOpposite() );

		gridProxy.setValidSides( EnumSet.complementOf( EnumSet.of( pointAt ) ) );
		markForUpdate();
		markDirty();
	}

	@Override
	public void gridChanged()
	{
		duality.gridChanged();
	}

	class TileInterfaceHandler extends AETileEventHandler
	{

		public TileInterfaceHandler() {
			super( TileEventType.WORLD_NBT );
		}

		@Override
		public void writeToNBT(NBTTagCompound data)
		{
			data.setInteger( "pointAt", pointAt.ordinal() );
			duality.writeToNBT( data );
		}

		@Override
		public void readFromNBT(NBTTagCompound data)
		{
			int val = data.getInteger( "pointAt" );

			if ( val >= 0 && val < ForgeDirection.values().length )
				pointAt = ForgeDirection.values()[val];
			else
				pointAt = ForgeDirection.UNKNOWN;

			duality.readFromNBT( data );
		}
	};

	public TileInterface() {
		addNewHandler( new TileInterfaceHandler() );
	}

	@Override
	public void onReady()
	{
		gridProxy.setValidSides( EnumSet.complementOf( EnumSet.of( pointAt ) ) );
		super.onReady();
	}

	@Override
	public AECableType getCableConnectionType(ForgeDirection dir)
	{
		return duality.getCableConnectionType( dir );
	}

	@Override
	public DimensionalCoord getLocation()
	{
		return duality.getLocation();
	}

	@Override
	public TileEntity getTileEntity()
	{
		return this;
	}

	@Override
	public boolean canInsert(ItemStack stack)
	{
		return duality.canInsert( stack );
	}

	@Override
	public IMEMonitor<IAEItemStack> getItemInventory()
	{
		return duality.getItemInventory();
	}

	@Override
	public IMEMonitor<IAEFluidStack> getFluidInventory()
	{
		return duality.getFluidInventory();
	}

	@Override
	public IInventory getInventoryByName(String name)
	{
		return duality.getInventoryByName( name );
	}

	@Override
	public TickingRequest getTickingRequest(IGridNode node)
	{
		return duality.getTickingRequest( node );
	}

	@Override
	public TickRateModulation tickingRequest(IGridNode node, int TicksSinceLastCall)
	{
		return duality.tickingRequest( node, TicksSinceLastCall );
	}

	@Override
	public IInventory getInternalInventory()
	{
		return duality.getInternalInventory();
	}

	@Override
	public void markDirty()
	{
		duality.markDirty();
	}

	@Override
	public void onChangeInventory(IInventory inv, int slot, InvOperation mc, ItemStack removed, ItemStack added)
	{
		duality.onChangeInventory( inv, slot, mc, removed, added );
	}

	@Override
	public int[] getAccessibleSlotsBySide(ForgeDirection side)
	{
		return duality.getAccessibleSlotsFromSide( side.ordinal() );
	}

	@Override
	public DualityInterface getInterfaceDuality()
	{
		return duality;
	}

	@Override
	public IStorageMonitorable getMonitorable(ForgeDirection side)
	{
		return this;
	}

	@Override
	public IConfigManager getConfigManager()
	{
		return duality.getConfigManager();
	}

}
