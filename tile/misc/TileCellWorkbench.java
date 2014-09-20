package appeng.tile.misc;

import java.util.ArrayList;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import appeng.api.config.CopyMode;
import appeng.api.config.Settings;
import appeng.api.config.Upgrades;
import appeng.api.implementations.IUpgradeableHost;
import appeng.api.storage.ICellWorkbenchItem;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.tile.AEBaseTile;
import appeng.tile.TileEvent;
import appeng.tile.events.TileEventType;
import appeng.tile.inventory.AppEngInternalAEInventory;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.IAEAppEngInventory;
import appeng.tile.inventory.InvOperation;
import appeng.util.ConfigManager;
import appeng.util.IConfigManagerHost;

public class TileCellWorkbench extends AEBaseTile implements IUpgradeableHost, IAEAppEngInventory, IConfigurableObject, IConfigManagerHost
{

	AppEngInternalInventory cell = new AppEngInternalInventory( this, 1 );
	AppEngInternalAEInventory config = new AppEngInternalAEInventory( this, 63 );
	ConfigManager cm = new ConfigManager( this );

	IInventory cacheUpgrades = null;
	IInventory cacheConfig = null;

	public IInventory getCellUpgradeInventory()
	{
		if ( cacheUpgrades == null )
		{
			ICellWorkbenchItem cwbi = getCell();
			if ( cwbi == null )
				return null;

			ItemStack is = cell.getStackInSlot( 0 );
			if ( is == null )
				return null;

			IInventory inv = cwbi.getUpgradesInventory( is );
			if ( inv == null )
				return null;

			return cacheUpgrades = inv;
		}
		return cacheUpgrades;
	}

	public IInventory getCellConfigInventory()
	{
		if ( cacheConfig == null )
		{
			ICellWorkbenchItem cwbi = getCell();
			if ( cwbi == null )
				return null;

			ItemStack is = cell.getStackInSlot( 0 );
			if ( is == null )
				return null;

			IInventory inv = cwbi.getConfigInventory( is );
			if ( inv == null )
				return null;

			return cacheConfig = inv;
		}
		return cacheConfig;
	}

	@TileEvent(TileEventType.WORLD_NBT_WRITE)
	public void writeToNBT_TileCellWorkbench(NBTTagCompound data)
	{
		cell.writeToNBT( data, "cell" );
		config.writeToNBT( data, "config" );
		cm.writeToNBT( data );
	}

	@TileEvent(TileEventType.WORLD_NBT_READ)
	public void readFromNBT_TileCellWorkbench(NBTTagCompound data)
	{
		cell.readFromNBT( data, "cell" );
		config.readFromNBT( data, "config" );
		cm.readFromNBT( data );
	}

	public TileCellWorkbench() {
		cm.registerSetting( Settings.COPY_MODE, CopyMode.CLEAR_ON_REMOVE );
		cell.enableClientEvents = true;
	}

	@Override
	public IInventory getInventoryByName(String name)
	{
		if ( name.equals( "config" ) )
			return config;

		if ( name.equals( "cell" ) )
			return cell;

		return null;
	}

	@Override
	public int getInstalledUpgrades(Upgrades u)
	{
		return 0;
	}

	private boolean locked = false;

	@Override
	public void onChangeInventory(IInventory inv, int slot, InvOperation mc, ItemStack removedStack, ItemStack newStack)
	{
		if ( inv == cell && locked == false )
		{
			locked = true;

			cacheUpgrades = null;
			cacheConfig = null;

			IInventory c = getCellConfigInventory();
			if ( c != null )
			{
				boolean cellHasConfig = false;
				for (int x = 0; x < c.getSizeInventory(); x++)
				{
					if ( c.getStackInSlot( x ) != null )
					{
						cellHasConfig = true;
						break;
					}
				}

				if ( cellHasConfig )
				{
					for (int x = 0; x < config.getSizeInventory(); x++)
						config.setInventorySlotContents( x, c.getStackInSlot( x ) );
				}
				else
				{
					for (int x = 0; x < config.getSizeInventory(); x++)
						c.setInventorySlotContents( x, config.getStackInSlot( x ) );

					c.markDirty();
				}
			}
			else if ( cm.getSetting( Settings.COPY_MODE ) == CopyMode.CLEAR_ON_REMOVE )
			{
				for (int x = 0; x < config.getSizeInventory(); x++)
					config.setInventorySlotContents( x, null );

				this.markDirty();
			}

			locked = false;
		}
		else if ( inv == config && locked == false )
		{
			IInventory c = getCellConfigInventory();
			if ( c != null )
			{
				for (int x = 0; x < config.getSizeInventory(); x++)
					c.setInventorySlotContents( x, config.getStackInSlot( x ) );

				c.markDirty();
			}
		}
	}

	@Override
	public void getDrops(World w, int x, int y, int z, ArrayList<ItemStack> drops)
	{
		super.getDrops( w, x, y, z, drops );

		if ( cell.getStackInSlot( 0 ) != null )
			drops.add( cell.getStackInSlot( 0 ) );
	}

	public ICellWorkbenchItem getCell()
	{
		if ( cell.getStackInSlot( 0 ) == null )
			return null;

		if ( cell.getStackInSlot( 0 ).getItem() instanceof ICellWorkbenchItem )
			return ((ICellWorkbenchItem) cell.getStackInSlot( 0 ).getItem());

		return null;
	}

	@Override
	public IConfigManager getConfigManager()
	{
		return cm;
	}

	@Override
	public void updateSetting(IConfigManager manager, Enum settingName, Enum newValue)
	{
		// nothing here..
	}

}
