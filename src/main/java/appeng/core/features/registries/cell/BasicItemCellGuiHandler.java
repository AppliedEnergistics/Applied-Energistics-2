
package appeng.core.features.registries.cell;


import appeng.container.ContainerLocator;
import appeng.container.ContainerOpener;
import appeng.container.implementations.ContainerMEMonitorable;
import appeng.container.implementations.ContainerWireless;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import appeng.api.AEApi;
import appeng.api.implementations.tiles.IChestOrDrive;
import appeng.api.storage.ICellGuiHandler;
import appeng.api.storage.ICellHandler;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEStack;
import appeng.api.util.AEPartLocation;

import appeng.util.Platform;


public class BasicItemCellGuiHandler implements ICellGuiHandler
{
	@Override
	public <T extends IAEStack<T>> boolean isHandlerFor( final IStorageChannel<T> channel )
	{
		return channel == AEApi.instance().storage().getStorageChannel( IItemStorageChannel.class );
	}

	@Override
	public void openChestGui( final PlayerEntity player, final IChestOrDrive chest, final ICellHandler cellHandler, final IMEInventoryHandler inv, final ItemStack is, final IStorageChannel chan )
	{
		ContainerOpener.openContainer(ContainerMEMonitorable.TYPE, player, ContainerLocator.forTileEntitySide((TileEntity) chest, chest.getUp()));
	}
}
