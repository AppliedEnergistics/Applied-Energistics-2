package appeng.parts.automation;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;

import appeng.api.behaviors.StackImportStrategy;
import appeng.api.behaviors.StackTransferContext;
import appeng.api.config.Actionable;
import appeng.core.AELog;
import appeng.util.BlockApiCache;

/**
 * Strategy for efficiently importing stacks from external storage into an internal
 * {@link appeng.api.storage.MEStorage}.
 */
class StorageImportStrategy<C, S> implements StackImportStrategy {
    private final BlockApiCache<C> apiCache;
    private final Direction fromSide;
    private final HandlerStrategy<C, S> conversion;

    public StorageImportStrategy(Capability<C> capability,
            HandlerStrategy<C, S> conversion,
            ServerLevel level,
            BlockPos fromPos,
            Direction fromSide) {
        this.apiCache = BlockApiCache.create(capability, level, fromPos);
        this.fromSide = fromSide;
        this.conversion = conversion;
    }

    @Override
    public boolean transfer(StackTransferContext context) {
        if (!context.isKeyTypeEnabled(conversion.getKeyType())) {
            return false;
        }

        var adjacentHandler = apiCache.find(fromSide);
        if (adjacentHandler == null) {
            return false;
        }

        var adjacentStorage = conversion.getFacade(adjacentHandler);

        long remainingTransferAmount = context.getOperationsRemaining()
                * (long) conversion.getKeyType().getAmountPerOperation();

        var inv = context.getInternalStorage();

        // Try to find an extractable resource that fits our filter
        for (int i = 0; i < adjacentStorage.getSlots(); i++) {
            var resource = adjacentStorage.getStackInSlot(i);
            if (resource == null
                    // Regard a filter that is set on the bus
                    || context.isInFilter(resource.what()) == context.isInverted()) {
                continue;
            }

            // Check how much of *this* resource we can actually insert into the network, it might be 0
            // if the cells are partitioned or there's not enough types left, etc.
            var amountForThisResource = inv.getInventory().insert(resource.what(), remainingTransferAmount,
                    Actionable.SIMULATE,
                    context.getActionSource());

            // Try to simulate-extract it
            var amount = adjacentStorage.extract(resource.what(), amountForThisResource, Actionable.MODULATE,
                    context.getActionSource());
            if (amount > 0) {
                var inserted = inv.getInventory().insert(resource.what(), amount, Actionable.MODULATE,
                        context.getActionSource());

                if (inserted < amount) {
                    // Be nice and try to give the overflow back
                    long leftover = amount - inserted;
                    leftover -= adjacentStorage.insert(resource.what(), leftover, Actionable.MODULATE,
                            context.getActionSource());
                    if (leftover > 0) {
                        AELog.warn("Extracted %dx%s from adjacent storage and voided it because network refused insert",
                                leftover, resource.what());
                    }
                }

                var opsUsed = Math.max(1, inserted / conversion.getKeyType().getAmountPerOperation());
                context.reduceOperationsRemaining(opsUsed);
            }
        }

        return false;
    }

    public static StackImportStrategy createItem(ServerLevel level, BlockPos fromPos, Direction fromSide) {
        return new StorageImportStrategy<>(
                CapabilityItemHandler.ITEM_HANDLER_CAPABILITY,
                HandlerStrategy.ITEMS,
                level,
                fromPos,
                fromSide);
    }

    public static StackImportStrategy createFluid(ServerLevel level, BlockPos fromPos, Direction fromSide) {
        return new StorageImportStrategy<>(
                CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY,
                HandlerStrategy.FLUIDS,
                level,
                fromPos,
                fromSide);
    }
}
