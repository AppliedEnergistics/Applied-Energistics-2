package appeng.core.features.registries.entries;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import appeng.api.implementations.tiles.IChestOrDrive;
import appeng.api.storage.ICellHandler;
import appeng.api.storage.ICellInventory;
import appeng.api.storage.ICellInventoryHandler;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.ISaveProvider;
import appeng.api.storage.StorageChannel;
import appeng.client.texture.ExtraBlockTextures;
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
	public IMEInventoryHandler getCellInventory(ItemStack is, ISaveProvider container, StorageChannel channel)
	{
		if ( channel == StorageChannel.ITEMS )
			return CellInventory.getCell( is, container );
		return null;
	}

	@Override
	public IIcon getTopTexture_Dark()
	{
		return ExtraBlockTextures.BlockMEChestItems_Dark.getIcon();
	}

	@Override
	public IIcon getTopTexture_Light()
	{
		return ExtraBlockTextures.BlockMEChestItems_Light.getIcon();
	}

	@Override
	public IIcon getTopTexture_Medium()
	{
		return ExtraBlockTextures.BlockMEChestItems_Medium.getIcon();
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
			return ci.getStatusForCell();
		}
		return 0;
	}

	@Override
	public double cellIdleDrain(ItemStack is, IMEInventory handler)
	{
		ICellInventory inv = ((ICellInventoryHandler) handler).getCellInv();
		return inv.getIdleDrain();
	}
}
