package appeng.core.features.registries.entries;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import appeng.api.implementations.IChestOrDrive;
import appeng.api.storage.ICellHandler;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.StorageChannel;
import appeng.client.texture.ExtraTextures;
import appeng.core.sync.GuiBridge;
import appeng.me.storage.CellInventory;
import appeng.me.storage.CellInventoryHandler;
import appeng.tile.AEBaseTile;
import appeng.util.Platform;

public class BasicCellHandler implements ICellHandler
{

	@Override
	public boolean isCell(ItemStack is)
	{
		return CellInventory.isCell( is );
	}

	@Override
	public IMEInventoryHandler getCellInventory(ItemStack is, StorageChannel channel)
	{
		if ( channel == StorageChannel.ITEMS )
			return CellInventory.getCell( is );
		return null;
	}

	@Override
	public Icon getTopTexture()
	{
		return ExtraTextures.BlockMEChestItems.getIcon();
	}

	@Override
	public void openChestGui(EntityPlayer player, IChestOrDrive chest, ICellHandler cellHandler, IMEInventoryHandler inv, ItemStack is, StorageChannel chan)
	{
		Platform.openGUI( player, (AEBaseTile) chest, chest.getUp(), GuiBridge.GUI_ME );
	}

	@Override
	public int getStatusForCell(ItemStack is, IMEInventory handler)
	{
		if ( handler instanceof CellInventoryHandler )
		{
			CellInventoryHandler ci = (CellInventoryHandler) handler;
			return ci.getCellInv().getStatusForCell();
		}
		return 0;
	}

	@Override
	public double cellIdleDrain(ItemStack is, IMEInventory handler)
	{
		CellInventory inv = (CellInventory) handler;
		return inv.getIdleDrain();
	}
}
