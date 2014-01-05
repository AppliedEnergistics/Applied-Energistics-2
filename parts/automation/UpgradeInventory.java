package appeng.parts.automation;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import appeng.api.config.Upgrades;
import appeng.api.implementations.IUpgradeModule;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.IAEAppEngInventory;
import appeng.tile.inventory.InvOperation;
import appeng.util.Platform;

public class UpgradeInventory extends AppEngInternalInventory implements IAEAppEngInventory
{

	private boolean cached = false;
	private int FuzzyUpgrades = 0;
	private int SpeedUpgrades = 0;
	private int RedstoneUpgrades = 0;
	private int CapacityUpgrades = 0;

	IAEAppEngInventory parent;

	public UpgradeInventory(IAEAppEngInventory _te, int s) {
		super( null, s );
		te = this;
		parent = _te;
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
				return getInstalledUpgrades( u ) < u.maxInstalled;
			}
		}
		return false;
	}

	private void updateUpgradeInfo()
	{
		cached = true;
		CapacityUpgrades = RedstoneUpgrades = SpeedUpgrades = FuzzyUpgrades = 0;

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
			default:
				break;
			}
		}

		CapacityUpgrades = Math.min( CapacityUpgrades, Upgrades.CAPACITY.maxInstalled );
		FuzzyUpgrades = Math.min( FuzzyUpgrades, Upgrades.FUZZY.maxInstalled );
		RedstoneUpgrades = Math.min( RedstoneUpgrades, Upgrades.REDSTONE.maxInstalled );
		SpeedUpgrades = Math.min( SpeedUpgrades, Upgrades.SPEED.maxInstalled );
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
