package appeng.container.slot;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import appeng.api.AEApi;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IStorageMonitorable;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.packets.PacketPatternSlot;
import appeng.helpers.IContainerCraftingPacket;

public class SlotPatternTerm extends SlotCraftingTerm
{

	int groupNum;
	IOptionalSlotHost host;

	public SlotPatternTerm(EntityPlayer player, BaseActionSource mySrc, IEnergySource energySrc, IStorageMonitorable storage, IInventory cMatrix,
			IInventory secondMatrix, IInventory output, int x, int y, IOptionalSlotHost h, int groupNumber, IContainerCraftingPacket c)
	{
		super( player, mySrc, energySrc, storage, cMatrix, secondMatrix, output, x, y, c );

		host = h;
		groupNum = groupNumber;

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

	public AppEngPacket getRequest(boolean shift) throws IOException
	{
		return new PacketPatternSlot( this.pattern, AEApi.instance().storage().createItemStack( getStack() ), shift );
	}

}
