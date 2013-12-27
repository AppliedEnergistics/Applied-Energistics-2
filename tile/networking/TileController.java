package appeng.tile.networking;

import java.util.EnumSet;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ForgeDirection;
import appeng.api.config.Actionable;
import appeng.api.networking.GridFlags;
import appeng.api.networking.energy.IAEPowerStorage;
import appeng.api.networking.events.MENetworkControllerChange;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.events.MENetworkPowerStorage;
import appeng.api.networking.events.MENetworkPowerStorage.PowerEventType;
import appeng.api.networking.pathing.ControllerState;
import appeng.me.GridAccessException;
import appeng.tile.grid.AENetworkPowerTile;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.InvOperation;

public class TileController extends AENetworkPowerTile implements IAEPowerStorage
{

	boolean isValid = false;

	public TileController() {
		internalMaxPower = 8000;
		internalPublicPowerStorage = true;
		gridProxy.setIdlePowerUsage( 6 );
		gridProxy.setFlags( GridFlags.CANNOT_CARRY );
	}

	@Override
	protected double getFunnelPowerDemand()
	{
		try
		{
			return gridProxy.getEnergy().getEnergyDemand( 8000 );
		}
		catch (GridAccessException e)
		{
			// no grid? use local...
			return super.getFunnelPowerDemand();
		}
	}

	@Override
	protected double funnelPowerIntoStorage(double AEUnits, Actionable mode)
	{
		try
		{
			double ret = gridProxy.getEnergy().injectPower( AEUnits, mode );
			if ( mode == Actionable.SIMULATE )
				return ret;
			return 0;
		}
		catch (GridAccessException e)
		{
			// no grid? use local...
			return super.funnelPowerIntoStorage( AEUnits, mode );
		}
	}

	@Override
	protected void PowerEvent(PowerEventType x)
	{
		try
		{
			gridProxy.getGrid().postEvent( new MENetworkPowerStorage( this, x ) );
		}
		catch (GridAccessException e)
		{
			// not ready!
		}
	}

	@MENetworkEventSubscribe
	public void onPowerChange(MENetworkControllerChange status)
	{
		updateMeta();
	}

	@MENetworkEventSubscribe
	public void onPowerChange(MENetworkPowerStatusChange status)
	{
		updateMeta();
	}

	@Override
	public void onReady()
	{
		onNeighborChange( true );
		super.onReady();
	}

	public void onNeighborChange(boolean force)
	{
		boolean xx = worldObj.getBlockTileEntity( xCoord - 1, yCoord, zCoord ) instanceof TileController
				&& worldObj.getBlockTileEntity( xCoord + 1, yCoord, zCoord ) instanceof TileController;
		boolean yy = worldObj.getBlockTileEntity( xCoord, yCoord - 1, zCoord ) instanceof TileController
				&& worldObj.getBlockTileEntity( xCoord, yCoord + 1, zCoord ) instanceof TileController;
		boolean zz = worldObj.getBlockTileEntity( xCoord, yCoord, zCoord - 1 ) instanceof TileController
				&& worldObj.getBlockTileEntity( xCoord, yCoord, zCoord + 1 ) instanceof TileController;

		// int meta = world.getBlockMetadata( xCoord, yCoord, zCoord );
		// boolean hasPower = meta > 0;
		// boolean isConflict = meta == 2;

		boolean oldValid = isValid;

		isValid = (xx && !yy && !zz) || (!xx && yy && !zz) || (!xx && !yy && zz) || ((xx ? 1 : 0) + (yy ? 1 : 0) + (zz ? 1 : 0) <= 1);

		if ( oldValid != isValid || force )
		{
			if ( isValid )
				gridProxy.setValidSides( EnumSet.allOf( ForgeDirection.class ) );
			else
				gridProxy.setValidSides( EnumSet.noneOf( ForgeDirection.class ) );
		}

		updateMeta();
	}

	private void updateMeta()
	{
		if ( !gridProxy.isReady() )
			return;

		int meta = 0;

		try
		{
			if ( gridProxy.getEnergy().isNetworkPowered() )
			{
				meta = 1;

				if ( gridProxy.getPath().getControllerState() == ControllerState.CONTROLLER_CONFLICT )
					meta = 2;
			}
		}
		catch (GridAccessException e)
		{
			meta = 0;
		}

		worldObj.setBlockMetadataWithNotify( xCoord, yCoord, zCoord, meta, 2 );
	}

	final int sides[] = new int[] { 0 };
	AppEngInternalInventory inv = new AppEngInternalInventory( this, 1 );

	@Override
	public IInventory getInternalInventory()
	{
		return inv;
	}

	@Override
	public void onChangeInventory(IInventory inv, int slot, InvOperation mc, ItemStack removed, ItemStack added)
	{

	}

	@Override
	public int[] getAccessibleSlotsFromSide(int side)
	{
		return sides;
	}

}
