package appeng.tile.misc;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
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
import appeng.util.inv.IInventoryDestination;

public class TileInterface extends AENetworkInvTile implements IGridTickable, ISegmentedInventory, ITileStorageMonitorable, IStorageMonitorable,
		IInventoryDestination, IInterfaceHost, IConfigureableObject
{

	DualityInterface duality = new DualityInterface( gridProxy, this );

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
			duality.writeToNBT( data );
		}

		@Override
		public void readFromNBT(NBTTagCompound data)
		{
			duality.readFromNBT( data );
		}

	};

	public TileInterface() {
		addNewHandler( new TileInterfaceHandler() );
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
	public void onInventoryChanged()
	{
		duality.onInventoryChanged();
	}

	@Override
	public void onChangeInventory(IInventory inv, int slot, InvOperation mc, ItemStack removed, ItemStack added)
	{
		duality.onChangeInventory( inv, slot, mc, removed, added );
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int side)
	{
		return duality.getAccessibleSlotsFromSide( side );
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
		// TODO Auto-generated method stub
		return null;
	}

}
