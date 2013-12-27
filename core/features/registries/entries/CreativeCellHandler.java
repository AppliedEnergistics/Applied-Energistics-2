package appeng.core.features.registries.entries;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.Icon;
import appeng.api.implementations.IChestOrDrive;
import appeng.api.storage.ICellHandler;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.StorageChannel;
import appeng.client.texture.ExtraTextures;
import appeng.items.storage.ItemCreativeStorageCell;
import appeng.me.storage.CreativeCellInventory;

public class CreativeCellHandler implements ICellHandler
{

	@Override
	public boolean isCell(ItemStack is)
	{
		return is != null && is.getItem() instanceof ItemCreativeStorageCell;
	}

	@Override
	public IMEInventoryHandler getCellInventory(ItemStack is, StorageChannel channel)
	{
		if ( channel == StorageChannel.ITEMS && is != null && is.getItem() instanceof ItemCreativeStorageCell )
			return CreativeCellInventory.getCell( is );
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
		player.sendChatToPlayer( ChatMessageComponent.createFromText( "Not ready yet..." ) );
	}

	@Override
	public int getStatusForCell(ItemStack is, IMEInventory handler)
	{
		return 2;
	}

	@Override
	public double cellIdleDrain(ItemStack is, IMEInventory handler)
	{
		return 0;
	}

}
