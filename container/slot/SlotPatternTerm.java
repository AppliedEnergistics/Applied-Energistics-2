package appeng.container.slot;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IStorageMonitorable;

public class SlotPatternTerm extends SlotCraftingTerm
{

	int groupNum;
	IOptionalSlotHost host;

	public SlotPatternTerm(EntityPlayer player, BaseActionSource mySrc, IEnergySource energySrc, IStorageMonitorable storage, IInventory cMatrix,
			IInventory secondMatrix, IInventory output, int x, int y, IOptionalSlotHost h, int grpnum) {
		super( player, mySrc, energySrc, storage, cMatrix, secondMatrix, output, x, y );

		host = h;
		groupNum = grpnum;

	}

	@Override
	public ItemStack getStack()
	{
		if ( !isEnabled() )
		{
			if ( getDisplayStack() != null )
				clearStack();
		}

		return super.getStack();
	}

	@Override
	public boolean isEnabled()
	{
		if ( host == null )
			return false;

		return host.isSlotEnabled( groupNum );
	}

}
