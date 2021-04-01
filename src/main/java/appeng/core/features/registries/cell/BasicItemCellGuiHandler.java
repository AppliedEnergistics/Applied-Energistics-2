package appeng.core.features.registries.cell;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import appeng.api.implementations.tiles.IChestOrDrive;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.cells.ICellGuiHandler;
import appeng.api.storage.cells.ICellHandler;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEStack;
import appeng.container.ContainerLocator;
import appeng.container.ContainerOpener;
import appeng.container.implementations.MEMonitorableContainer;
import appeng.core.Api;

public class BasicItemCellGuiHandler implements ICellGuiHandler {
    @Override
    public <T extends IAEStack<T>> boolean isHandlerFor(final IStorageChannel<T> channel) {
        return channel == Api.instance().storage().getStorageChannel(IItemStorageChannel.class);
    }

    @Override
    public void openChestGui(final PlayerEntity player, final IChestOrDrive chest, final ICellHandler cellHandler,
            final IMEInventoryHandler inv, final ItemStack is, final IStorageChannel chan) {
        ContainerOpener.openContainer(MEMonitorableContainer.TYPE, player,
                ContainerLocator.forTileEntitySide((TileEntity) chest, chest.getUp()));
    }
}
