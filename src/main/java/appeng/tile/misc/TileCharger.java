package appeng.tile.misc;

import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.PowerUnits;
import appeng.api.implementations.items.IAEItemPowerStorage;
import appeng.api.implementations.tiles.ICrankable;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalCoord;
import appeng.me.GridAccessException;
import appeng.tile.TileEvent;
import appeng.tile.events.TileEventType;
import appeng.tile.grid.AENetworkPowerTile;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.InvOperation;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;

public class TileCharger extends AENetworkPowerTile implements ICrankable
{

	final int sides[] = new int[] { 0 };
	final AppEngInternalInventory inv = new AppEngInternalInventory( this, 1 );
	int tickTickTimer = 0;

	int lastUpdate = 0;
	boolean requiresUpdate = false;

	@Override
	public AECableType getCableConnectionType(ForgeDirection dir)
	{
		return AECableType.COVERED;
	}

	@TileEvent(TileEventType.NETWORK_READ)
	public boolean readFromStream_TileCharger(ByteBuf data) throws IOException
	{
		try
		{
			IAEItemStack item = AEItemStack.loadItemStackFromPacket( data );
			ItemStack is = item.getItemStack();
			inv.setInventorySlotContents( 0, is );
		}
		catch (Throwable t)
		{
			inv.setInventorySlotContents( 0, null );
		}
		return false; // TESR doesn't need updates!
	}

	@TileEvent(TileEventType.NETWORK_WRITE)
	public void writeToStream_TileCharger(ByteBuf data) throws IOException
	{
		AEItemStack is = AEItemStack.create( getStackInSlot( 0 ) );
		if ( is != null )
			is.writeToPacket( data );
	}

	@TileEvent(TileEventType.TICK)
	public void Tick_TileCharger()
	{
		if ( lastUpdate > 60 && requiresUpdate )
		{
			requiresUpdate = false;
			markForUpdate();
			lastUpdate = 0;
		}
		lastUpdate++;

		tickTickTimer++;
		if ( tickTickTimer < 20 )
			return;
		tickTickTimer = 0;

		ItemStack myItem = getStackInSlot( 0 );

		// charge from the network!
		if ( internalCurrentPower < 1499 )
		{
			try
			{
				injectExternalPower( PowerUnits.AE,
						gridProxy.getEnergy().extractAEPower( Math.min( 150.0, 1500.0 - internalCurrentPower ), Actionable.MODULATE, PowerMultiplier.ONE ) );
				tickTickTimer = 20; // keep ticking...
			}
			catch (GridAccessException e)
			{
				// continue!
			}
		}

		if ( myItem == null )
			return;

		if ( internalCurrentPower > 149 && Platform.isChargeable( myItem ) )
		{
			IAEItemPowerStorage ps = (IAEItemPowerStorage) myItem.getItem();
			if ( ps.getAEMaxPower( myItem ) > ps.getAECurrentPower( myItem ) )
			{
				double oldPower = internalCurrentPower;

				double adjustment = ps.injectAEPower( myItem, extractAEPower( 150.0, Actionable.MODULATE, PowerMultiplier.CONFIG ) );
				internalCurrentPower += adjustment;
				if ( oldPower > internalCurrentPower )
					requiresUpdate = true;
				tickTickTimer = 20; // keep ticking...
			}
		}
		else if ( internalCurrentPower > 1499 && AEApi.instance().materials().materialCertusQuartzCrystal.sameAsStack( myItem ) )
		{
			if ( Platform.getRandomFloat() > 0.8f ) // simulate wait
			{
				extractAEPower( internalMaxPower, Actionable.MODULATE, PowerMultiplier.CONFIG );// 1500
				setInventorySlotContents( 0, AEApi.instance().materials().materialCertusQuartzCrystalCharged.stack( myItem.stackSize ) );
			}
		}
	}

	public TileCharger() {
		gridProxy.setValidSides( EnumSet.noneOf( ForgeDirection.class ) );
		gridProxy.setFlags();
		internalMaxPower = 1500;
		gridProxy.setIdlePowerUsage( 0 );
	}

	@Override
	public void setOrientation(ForgeDirection inForward, ForgeDirection inUp)
	{
		super.setOrientation( inForward, inUp );
		gridProxy.setValidSides( EnumSet.of( getUp(), getUp().getOpposite() ) );
		setPowerSides( EnumSet.of( getUp(), getUp().getOpposite() ) );
	}

	@Override
	public boolean canTurn()
	{
		return internalCurrentPower < internalMaxPower;
	}

	@Override
	public void applyTurn()
	{
		injectExternalPower( PowerUnits.AE, 150 );

		ItemStack myItem = getStackInSlot( 0 );
		if ( internalCurrentPower > 1499 && AEApi.instance().materials().materialCertusQuartzCrystal.sameAsStack( myItem ) )
		{
			extractAEPower( internalMaxPower, Actionable.MODULATE, PowerMultiplier.CONFIG );// 1500
			setInventorySlotContents( 0, AEApi.instance().materials().materialCertusQuartzCrystalCharged.stack( myItem.stackSize ) );
		}
	}

	@Override
	public boolean canCrankAttach(ForgeDirection directionToCrank)
	{
		return getUp().equals( directionToCrank ) || getUp().getOpposite().equals( directionToCrank );
	}

	@Override
	public IInventory getInternalInventory()
	{
		return inv;
	}

	@Override
	public void onChangeInventory(IInventory inv, int slot, InvOperation mc, ItemStack removed, ItemStack added)
	{
		markForUpdate();
	}

	@Override
	public int[] getAccessibleSlotsBySide(ForgeDirection whichSide)
	{
		return sides;
	}

	@Override
	public int getInventoryStackLimit()
	{
		return 1;
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack)
	{
		return Platform.isChargeable( itemstack ) || AEApi.instance().materials().materialCertusQuartzCrystal.sameAsStack( itemstack );
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemstack, int j)
	{
		if ( Platform.isChargeable( itemstack ) )
		{
			IAEItemPowerStorage ips = (IAEItemPowerStorage) itemstack.getItem();
			if ( ips.getAECurrentPower( itemstack ) >= ips.getAEMaxPower( itemstack ) )
				return true;
		}

		return AEApi.instance().materials().materialCertusQuartzCrystalCharged.sameAsStack( itemstack );
	}

	public void activate(EntityPlayer player)
	{
		if ( !Platform.hasPermissions( new DimensionalCoord( this ), player ) )
			return;

		ItemStack myItem = getStackInSlot( 0 );
		if ( myItem == null )
		{
			ItemStack held = player.inventory.getCurrentItem();
			if ( AEApi.instance().materials().materialCertusQuartzCrystal.sameAsStack( held ) || Platform.isChargeable( held ) )
			{
				held = player.inventory.decrStackSize( player.inventory.currentItem, 1 );
				setInventorySlotContents( 0, held );
			}
		}
		else
		{
			List<ItemStack> drops = new ArrayList<ItemStack>();
			drops.add( myItem );
			setInventorySlotContents( 0, null );
			Platform.spawnDrops( worldObj, xCoord + getForward().offsetX, yCoord + getForward().offsetY, zCoord + getForward().offsetZ, drops );
		}
	}

	@Override
	public boolean requiresTESR()
	{
		return true;
	}

}
