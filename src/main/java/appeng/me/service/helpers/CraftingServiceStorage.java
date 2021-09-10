package appeng.me.service.helpers;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.StorageChannels;
import appeng.api.storage.cells.ICellProvider;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.me.service.CraftingService;

/**
 * The storage exposed by the crafting service. It does two things:
 * <ul>
 * <li>Report craftable items as craftable to the network, and thus the terminals.</li>
 * <li>Intercept crafted item injections and forward them to the CPUs.</li>
 * </ul>
 */
public class CraftingServiceStorage implements ICellProvider {
    private final CraftingService craftingService;
    private final Map<IStorageChannel<?>, IMEInventoryHandler<?>> inventories = new HashMap<>();

    private final IMEInventoryHandler getOrComputeInventory(IStorageChannel<?> channel) {
        return inventories.computeIfAbsent(channel, chan -> new IMEInventoryHandler() {
            @Override
            public AccessRestriction getAccess() {
                return AccessRestriction.WRITE;
            }

            @Override
            public boolean isPrioritized(IAEStack input) {
                return true;
            }

            @Override
            public boolean canAccept(IAEStack input) {
                return craftingService.isRequesting(input);
            }

            @Override
            public int getPriority() {
                return Integer.MAX_VALUE;
            }

            @Override
            public boolean validForPass(int pass) {
                return pass == 1;
            }

            @Override
            public IAEStack injectItems(IAEStack input, Actionable type, IActionSource src) {
                // Item interception logic
                return craftingService.injectItemsIntoCpus(input, type);
            }

            @Override
            public IAEStack extractItems(IAEStack request, Actionable mode, IActionSource src) {
                return null;
            }

            @Override
            public IItemList getAvailableItems(IItemList out) {
                // Add craftable items so the terminals can see them.
                return craftingService.addCrafting(chan, out);
            }

            @Override
            public IStorageChannel<IAEItemStack> getChannel() {
                return StorageChannels.items();
            }
        });
    }

    public CraftingServiceStorage(CraftingService craftingService) {
        this.craftingService = craftingService;
    }

    @Nonnull
    @Override
    public List<IMEInventoryHandler> getCellArray(IStorageChannel<?> channel) {
        if (channel == StorageChannels.items()) {
            return List.of(getOrComputeInventory(channel));
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public int getPriority() {
        return Integer.MAX_VALUE;
    }
}
