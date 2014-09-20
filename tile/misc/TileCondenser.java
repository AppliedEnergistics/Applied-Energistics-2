package appeng.tile.misc;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import appeng.api.AEApi;
import appeng.api.config.CondenserOuput;
import appeng.api.config.Settings;
import appeng.api.implementations.items.IStorageComponent;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.tile.AEBaseInvTile;
import appeng.tile.TileEvent;
import appeng.tile.events.TileEventType;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.IAEAppEngInventory;
import appeng.tile.inventory.InvOperation;
import appeng.util.ConfigManager;
import appeng.util.IConfigManagerHost;
import appeng.util.Platform;

public class TileCondenser extends AEBaseInvTile implements IAEAppEngInventory, IFluidHandler, IConfigManagerHost, IConfigurableObject
{

	int sides[] = new int[] { 0, 1 };
	static private FluidTankInfo[] empty = new FluidTankInfo[] { new FluidTankInfo( null, 10 ) };
	AppEngInternalInventory inv = new AppEngInternalInventory( this, 3 );
	ConfigManager cm = new ConfigManager( this );

	public double storedPower = 0;

	@TileEvent(TileEventType.WORLD_NBT_WRITE)
	public void writeToNBT_TileCondenser(NBTTagCompound data)
	{
		cm.writeToNBT( data );
		data.setDouble( "storedPower", storedPower );
	}

	@TileEvent(TileEventType.WORLD_NBT_READ)
	public void readFromNBT_TileCondenser(NBTTagCompound data)
	{
		cm.readFromNBT( data );
		storedPower = data.getDouble( "storedPower" );
	}

	public TileCondenser() {
		cm.registerSetting( Settings.CONDENSER_OUTPUT, CondenserOuput.TRASH );
	}

	public double getStorage()
	{
		ItemStack is = inv.getStackInSlot( 2 );
		if ( is != null )
		{
			if ( is.getItem() instanceof IStorageComponent )
			{
				IStorageComponent sc = (IStorageComponent) is.getItem();
				if ( sc.isStorageComponent( is ) )
					return sc.getBytes( is ) * 8;
			}
		}
		return 0;
	}

	public void addPower(double rawPower)
	{
		storedPower += rawPower;
		storedPower = Math.max( 0.0, Math.min( getStorage(), storedPower ) );

		double requiredPower = getRequiredPower();
		ItemStack output = getOutput();
		while (requiredPower <= storedPower && output != null && requiredPower > 0)
		{
			if ( canAddOutput( output ) )
			{
				storedPower -= requiredPower;
				addOutput( output );
			}
			else
				break;
		}

	}

	private boolean canAddOutput(ItemStack output)
	{
		ItemStack outputStack = getStackInSlot( 1 );
		return outputStack == null || (Platform.isSameItem( outputStack, output ) && outputStack.stackSize < outputStack.getMaxStackSize());
	}

	/**
	 * make sure you validate with canAddOutput prior to this.
	 * 
	 * @param output
	 */
	private void addOutput(ItemStack output)
	{
		ItemStack outputStack = getStackInSlot( 1 );
		if ( outputStack == null )
			setInventorySlotContents( 1, output.copy() );
		else
		{
			outputStack.stackSize++;
			setInventorySlotContents( 1, outputStack );
		}
	}

	private ItemStack getOutput()
	{
		switch ((CondenserOuput) cm.getSetting( Settings.CONDENSER_OUTPUT ))
		{
		case MATTER_BALLS:
			return AEApi.instance().materials().materialMatterBall.stack( 1 );
		case SINGULARITY:
			return AEApi.instance().materials().materialSingularity.stack( 1 );
		case TRASH:
		default:
		}
		return null;
	}

	public double getRequiredPower()
	{
		return ((CondenserOuput) cm.getSetting( Settings.CONDENSER_OUTPUT )).requiredPower;
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack)
	{
		if ( i == 0 )
		{
			if ( itemstack != null )
				addPower( itemstack.stackSize );
		}
		else
		{
			inv.setInventorySlotContents( 1, itemstack );
		}
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack)
	{
		return i == 0;
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemstack, int j)
	{
		return i != 0;
	}

	@Override
	public boolean canInsertItem(int i, ItemStack itemstack, int j)
	{
		return i == 0;
	}

	@Override
	public int[] getAccessibleSlotsBySide(ForgeDirection side)
	{
		return sides;
	}

	@Override
	public IInventory getInternalInventory()
	{
		return inv;
	}

	@Override
	public void onChangeInventory(IInventory inv, int slot, InvOperation mc, ItemStack removed, ItemStack added)
	{
		if ( slot == 0 )
		{
			ItemStack is = inv.getStackInSlot( 0 );
			if ( is != null )
			{
				addPower( is.stackSize );
				inv.setInventorySlotContents( 0, null );
			}
		}
	}

	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill)
	{
		if ( doFill )
			addPower( (resource == null ? 0.0 : (double) resource.amount) / 500.0 );

		return resource == null ? 0 : resource.amount;
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain)
	{
		return null;
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain)
	{
		return null;
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid)
	{
		return true;
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid)
	{
		return false;
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from)
	{
		return empty;
	}

	@Override
	public void updateSetting(IConfigManager manager, Enum settingName, Enum newValue)
	{
		addPower( 0 );
	}

	@Override
	public IConfigManager getConfigManager()
	{
		return cm;
	}

}
