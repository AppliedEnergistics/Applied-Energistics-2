package appeng.api.behaviors;

import org.jetbrains.annotations.ApiStatus;

import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageService;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.util.prioritylist.IPartitionList;

/**
 * Context for import and export bus transfer operations.
 */
@ApiStatus.Experimental
@ApiStatus.NonExtendable
public interface StackTransferContext {

    IStorageService getInternalStorage();

    IEnergySource getEnergySource();

    IActionSource getActionSource();

    int getOperationsRemaining();

    void setOperationsRemaining(int operationsRemaining);

    boolean hasOperationsLeft();

    boolean hasDoneWork();

    boolean isKeyTypeEnabled(AEKeyType space);

    boolean isInFilter(AEKey key);

    IPartitionList getFilter();

    void invert(boolean inverted);

    boolean isInverted();

    boolean canInsert(AEItemKey what, long amount);

    void reduceOperationsRemaining(long inserted);
}
