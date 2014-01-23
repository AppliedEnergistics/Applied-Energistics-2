package appeng.parts.automation;

import net.minecraft.block.Block;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import appeng.api.config.Upgrades;
import appeng.api.implementations.items.IUpgradeModule;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.IAEAppEngInventory;
import appeng.tile.inventory.InvOperation;
import appeng.util.Platform;

public class UpgradeInventory extends AppEngInternalInventory implements IAEAppEngInventory
{

	private final Object itemorblock;
	private final IAEAppEngInventory parent;

	private boolean cached = false;
	private int FuzzyUpgrades = 0;
	private int SpeedUpgrades = 0;
	private int RedstoneUpgrades = 0;
	private int CapacityUpgrades = 0;
	private int InverterUpgrades = 0;

	public UpgradeInventory(Object itemOrBlock, IAEAppEngInventory _te, int s) {
		super( null, s );
		te = this;
		parent = _te;
		itemorblock = itemOrBlock;
	}

	@Override
	protected boolean eventsEnabled()
	{
		return true;
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack newItemStack)
	{
		super.setInventorySlotContents( slot, newItemStack );
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack)
	{
		if ( itemstack == null )
			return false;
		Item it = itemstack.getItem();
		if ( it instanceof IUpgradeModule )
		{
			Upgrades u = ((IUpgradeModule) it).getType( itemstack );
			if ( u != null )
			{
				return getInstalledUpgrades( u ) < getMaxInstalled( u );
			}
		}
		return false;
	}

	private int getMaxInstalled(Upgrades u)
	{
		Integer max = null;

		for (ItemStack is : u.getSupported().keySet())
		{
			if ( is.getItem() == itemorblock )
			{
				max = u.getSupported().get( is );
				break;
			}
			else if ( is.getItem() instanceof ItemBlock && Block.blocksList[((ItemBlock) is.getItem()).getBlockID()] == itemorblock )
			{
				max = u.getSupported().get( is );
				break;
			}
		}

		if ( max == null )
			return 0;
		return max;
	}

	@Override
	public int getInventoryStackLimit()
	{
		return 1;
	}

	private void updateUpgradeInfo()
	{
		cached = true;
		InverterUpgrades = CapacityUpgrades = RedstoneUpgrades = SpeedUpgrades = FuzzyUpgrades = 0;

		for (ItemStack is : this)
		{
			if ( is == null || is.getItem() == null || !(is.getItem() instanceof IUpgradeModule) )
				continue;

			Upgrades myUpgrade = ((IUpgradeModule) is.getItem()).getType( is );
			switch (myUpgrade)
			{
			case CAPACITY:
				CapacityUpgrades++;
				break;
			case FUZZY:
				FuzzyUpgrades++;
				break;
			case REDSTONE:
				RedstoneUpgrades++;
				break;
			case SPEED:
				SpeedUpgrades++;
				break;
			case INVERTER:
				InverterUpgrades++;
				break;
			default:
				break;
			}
		}

		CapacityUpgrades = Math.min( CapacityUpgrades, getMaxInstalled( Upgrades.CAPACITY ) );
		FuzzyUpgrades = Math.min( FuzzyUpgrades, getMaxInstalled( Upgrades.FUZZY ) );
		RedstoneUpgrades = Math.min( RedstoneUpgrades, getMaxInstalled( Upgrades.REDSTONE ) );
		SpeedUpgrades = Math.min( SpeedUpgrades, getMaxInstalled( Upgrades.SPEED ) );
		InverterUpgrades = Math.min( InverterUpgrades, getMaxInstalled( Upgrades.INVERTER ) );
	}

	public int getInstalledUpgrades(Upgrades u)
	{
		if ( !cached )
			updateUpgradeInfo();

		switch (u)
		{
		case CAPACITY:
			return CapacityUpgrades;
		case FUZZY:
			return FuzzyUpgrades;
		case REDSTONE:
			return RedstoneUpgrades;
		case SPEED:
			return SpeedUpgrades;
		case INVERTER:
			return InverterUpgrades;
		default:
			return 0;
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound target)
	{
		super.readFromNBT( target );
		updateUpgradeInfo();
	}

	@Override
	public void onChangeInventory(IInventory inv, int slot, InvOperation mc, ItemStack removedStack, ItemStack newStack)
	{
		cached = false;
		if ( parent != null && Platform.isServer() )
			parent.onChangeInventory( inv, slot, mc, removedStack, newStack );
	}

}
