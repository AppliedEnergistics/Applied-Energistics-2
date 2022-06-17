package appeng.api.behaviors;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

import appeng.api.config.Actionable;
import appeng.api.ids.AEConstants;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.GenericStack;

/**
 * Abstraction layer over the "generic" (meaning that it can accept any AEKey in each slot) inventory used by various
 * AE2 devices. This is exposed to allow addons to adapt them for foreign capabilities/API lookups.
 */
@ApiStatus.Experimental
public interface GenericInternalInventory {
    /**
     * Use this lookup to access instances of generic inventories. Use a fallback to wrap them on-the-fly.
     */
    BlockApiLookup<GenericInternalInventory, Direction> SIDED = BlockApiLookup.get(
            new ResourceLocation(AEConstants.MOD_ID, "genericinternalinventory"), GenericInternalInventory.class,
            Direction.class);

    /**
     * @return The number of slots in this inventory. Never changes.
     */
    int size();

    @Nullable
    GenericStack getStack(int slot);

    @Nullable
    AEKey getKey(int slot);

    long getAmount(int slot);

    /**
     * @return The key-specific max amount (for items this takes the max stack size into account).
     */
    long getMaxAmount(AEKey key);

    /**
     * @return The type-specific max amount (or an estimate if it depends on the stored item).
     */
    long getCapacity(AEKeyType keyType);

    boolean canInsert();

    boolean canExtract();

    void setStack(int slot, @Nullable GenericStack newStack);

    /**
     * Return true if the key would generally be allowed, ignoring the current state of the inventory.
     */
    boolean isAllowed(AEKey what);

    /**
     * Try to insert something into a given slot.
     */
    long insert(int slot, AEKey what, long amount, Actionable mode);

    /**
     * Try to extract something from a given slot.
     */
    long extract(int slot, AEKey what, long amount, Actionable mode);

    /**
     * Start a new change notifications batch, deferring change notifications.
     */
    void beginBatch();

    /**
     * Finish the current batch and send any pending notification.
     */
    void endBatch();

    /**
     * Finish the current batch and suppress any pending notification.
     */
    void endBatchSuppressed();

    /**
     * Send a change notification manually, for example because the automatic notification was suppressed.
     */
    void onChange();

    /**
     * Fabric only: call this before modifying a slot as part of a transaction.
     */
    void updateSnapshots(int slot, TransactionContext transaction);
}
