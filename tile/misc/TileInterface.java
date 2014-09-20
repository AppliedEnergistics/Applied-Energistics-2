package appeng.tile.misc;

import java.util.ArrayList;
import java.util.EnumSet;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.config.Actionable;
import appeng.api.config.Upgrades;
import appeng.api.implementations.tiles.ISegmentedInventory;
import appeng.api.implementations.tiles.ITileStorageMonitorable;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingProviderHelper;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.security.BaseActionSource;
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
import appeng.api.util.IConfigurableObject;
import appeng.helpers.DualityInterface;
import appeng.helpers.IInterfaceHost;
import appeng.helpers.IPriorityHost;
import appeng.tile.TileEvent;
import appeng.tile.events.TileEventType;
import appeng.tile.grid.AENetworkInvTile;
import appeng.tile.inventory.InvOperation;
import appeng.util.Platform;
import appeng.util.inv.IInventoryDestination;

import com.google.common.collect.ImmutableSet;

public class TileInterface extends AENetworkInvTile implements IGridTickable, ISegmentedInventory, ITileStorageMonitorable, IStorageMonitorable,
		IInventoryDestination, IInterfaceHost, IConfigurableObject, IPriorityHost
{

	ForgeDirection pointAt = ForgeDirection.UNKNOWN;
	DualityInterface duality = new DualityInterface( gridProxy, this );

	@MENetworkEventSubscribe
	public void stateChange(MENetworkChannelsChanged c)
	{
		duality.notifyNeightbors();
	}

	@MENetworkEventSubscribe
	public void stateChange(MENetworkPowerStatusChange c)
	{
		duality.notifyNeightbors();
	}

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
	public void getDrops(World w, int x, int y, int z, ArrayList<ItemStack> drops)
	{
		duality.addDrops( drops );
	}

	@Override
	public void gridChanged()
	{
		duality.gridChanged();
	}

	@TileEvent(TileEventType.WORLD_NBT_WRITE)
	public void writeToNBT_TileInterface(NBTTagCompound data)
	{
		data.setInteger( "pointAt", pointAt.ordinal() );
		duality.writeToNBT( data );
	}

	@TileEvent(TileEventType.WORLD_NBT_READ)
	public void readFromNBT_TileInterface(NBTTagCompound data)
	{
		int val = data.getInteger( "pointAt" );

		if ( val >= 0 && val < ForgeDirection.values().length )
			pointAt = ForgeDirection.values()[val];
		else
			pointAt = ForgeDirection.UNKNOWN;

		duality.readFromNBT( data );
	}

	@Override
	public void onReady()
	{
		gridProxy.setValidSides( EnumSet.complementOf( EnumSet.of( pointAt ) ) );
		super.onReady();
		duality.initialize();
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
	public IStorageMonitorable getMonitorable(ForgeDirection side, BaseActionSource src)
	{
		return duality.getMonitorable( side, src, this );
	}

	@Override
	public IConfigManager getConfigManager()
	{
		return duality.getConfigManager();
	}

	@Override
	public boolean pushPattern(ICraftingPatternDetails patternDetails, InventoryCrafting table)
	{
		return duality.pushPattern( patternDetails, table );
	}

	@Override
	public void provideCrafting(ICraftingProviderHelper craftingTracker)
	{
		duality.provideCrafting( craftingTracker );
	}

	@Override
	public EnumSet<ForgeDirection> getTargets()
	{
		if ( pointAt == null || pointAt == ForgeDirection.UNKNOWN )
			return EnumSet.complementOf( EnumSet.of( ForgeDirection.UNKNOWN ) );
		return EnumSet.of( pointAt );
	}

	@Override
	public boolean isBusy()
	{
		return duality.isBusy();
	}

	@Override
	public int getInstalledUpgrades(Upgrades u)
	{
		return duality.getInstalledUpgrades( u );
	}

	@Override
	public ImmutableSet<ICraftingLink> getRequestedJobs()
	{
		return duality.getRequestedJobs();
	}

	@Override
	public IAEItemStack injectCraftedItems(ICraftingLink link, IAEItemStack items, Actionable mode)
	{
		return duality.injectCraftedItems( link, items, mode );
	}

	@Override
	public void jobStateChange(ICraftingLink link)
	{
		duality.jobStateChange( link );
	}

	@Override
	public int getPriority()
	{
		return duality.getPriority();
	}

	@Override
	public void setPriority(int newValue)
	{
		duality.setPriority( newValue );
	}
}
