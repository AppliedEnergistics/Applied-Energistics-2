package appeng.helpers.iface;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.*;
import appeng.api.storage.data.IAEStack;
import appeng.capabilities.Capabilities;
import appeng.me.storage.ItemHandlerAdapter;
import appeng.util.InventoryAdaptor;
import appeng.util.inv.AdaptorItemHandler;

public interface IInterfaceTarget {
    @Nullable
    static IInterfaceTarget get(Level l, BlockPos pos, @Nullable BlockEntity be, Direction side, IActionSource src) {
        if (be == null)
            return null;

        // our capability first: allows any storage channel
        var accessor = be.getCapability(Capabilities.STORAGE_MONITORABLE_ACCESSOR, side).orElse(null);
        if (accessor != null)
            return wrapStorageMonitorable(accessor, src);

        // otherwise fall back to the platform capability
        var itemHandler = be.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side).orElse(null);
        if (itemHandler != null)
            return wrapItemHandler(itemHandler, src);

        return null;
    }

    private static IInterfaceTarget wrapStorageMonitorable(IStorageMonitorableAccessor accessor, IActionSource src) {
        var monitorable = accessor.getInventory(src);
        if (monitorable == null) {
            return null;
        } else {
            return new IInterfaceTarget() {
                @Nullable
                @Override
                public IAEStack injectItems(IAEStack what, Actionable type) {
                    if (what == null)
                        return null;
                    return IInterfaceTarget.channelInjectItems(what.getChannel(), monitorable, what, type, src);
                }

                @Override
                public boolean isBusy() {
                    for (var channel : StorageChannels.getAll()) {
                        if (IInterfaceTarget.isChannelBusy(channel, monitorable, src)) {
                            return true;
                        }
                    }
                    return false;
                }
            };
        }
    }

    private static <T extends IAEStack> T channelInjectItems(IStorageChannel<T> channel,
            IStorageMonitorable monitorable, IAEStack what, Actionable type, IActionSource src) {
        var castedWhat = what.cast(channel);
        var inventory = monitorable.getInventory(channel);
        if (inventory != null) {
            return inventory.injectItems(castedWhat, type, src);
        }
        return null;
    }

    private static <T extends IAEStack> boolean isChannelBusy(IStorageChannel<T> channel,
            IStorageMonitorable monitorable, IActionSource src) {
        var inventory = monitorable.getInventory(channel);
        if (inventory != null) {
            for (var stack : inventory.getStorageList()) {
                if (inventory.extractItems(stack, Actionable.SIMULATE, src) != null) {
                    return true;
                }
            }
        }
        return false;
    }

    private static IInterfaceTarget wrapItemHandler(IItemHandler handler, IActionSource src) {
        var adapter = new ItemHandlerAdapter(handler) {
            @Override
            protected void onInjectOrExtract() {
            }
        };
        InventoryAdaptor adaptor = new AdaptorItemHandler(handler);
        return new IInterfaceTarget() {
            @Nullable
            @Override
            public IAEStack injectItems(IAEStack what, Actionable type) {
                if (what != null && what.getChannel() == StorageChannels.items()) {
                    return adapter.injectItems(what.cast(StorageChannels.items()), type, src);
                }
                return null;
            }

            @Override
            public boolean isBusy() {
                return !adaptor.simulateRemove(1, ItemStack.EMPTY, null).isEmpty();
            }
        };
    }

    @Nullable
    IAEStack injectItems(IAEStack what, Actionable type);

    boolean isBusy();
}
