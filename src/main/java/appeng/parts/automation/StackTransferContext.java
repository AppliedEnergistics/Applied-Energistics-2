package appeng.parts.automation;

import java.util.HashSet;
import java.util.Set;

import appeng.api.config.Actionable;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.AEKeySpace;
import appeng.api.storage.MEStorage;
import appeng.api.storage.data.AEItemKey;
import appeng.api.storage.data.AEKey;
import appeng.util.prioritylist.IPartitionList;

/**
 * Context for stack transfer operations, regardless of whether they occur in or out of the network.
 */
class StackTransferContext {
    private final MEStorage internalStorage;
    private final IEnergySource energySource;
    private final IActionSource actionSource;
    private final IPartitionList filter;
    private final Set<AEKeySpace> keySpaces;
    private final int initialOperations;
    private int operationsRemaining;

    public StackTransferContext(MEStorage internalStorage, IEnergySource energySource, IActionSource actionSource,
            int operationsRemaining,
            IPartitionList filter) {
        this.internalStorage = internalStorage;
        this.energySource = energySource;
        this.actionSource = actionSource;
        this.filter = filter;
        this.initialOperations = operationsRemaining;
        this.operationsRemaining = operationsRemaining;
        this.keySpaces = new HashSet<>();
        for (AEKey item : filter.getItems()) {
            this.keySpaces.add(item.getChannel());
        }
    }

    public MEStorage getInternalStorage() {
        return internalStorage;
    }

    public IEnergySource getEnergySource() {
        return energySource;
    }

    public IActionSource getActionSource() {
        return actionSource;
    }

    public int getOperationsRemaining() {
        return operationsRemaining;
    }

    public void setOperationsRemaining(int operationsRemaining) {
        this.operationsRemaining = operationsRemaining;
    }

    public boolean hasOperationsLeft() {
        return operationsRemaining > 0;
    }

    public boolean hasDoneWork() {
        return initialOperations > operationsRemaining;
    }

    public boolean isKeySpaceEnabled(AEKeySpace space) {
        return keySpaces.isEmpty() || keySpaces.contains(space);
    }

    public boolean isInFilter(AEKey key) {
        return filter.isEmpty() || filter.isListed(key);
    }

    public IPartitionList getFilter() {
        return filter;
    }

    public boolean canInsert(AEItemKey what, long amount) {
        return internalStorage.insert(
                what,
                amount,
                Actionable.SIMULATE,
                actionSource) > 0;
    }

    public void reduceOperationsRemaining(long inserted) {
        operationsRemaining -= inserted;
    }
}
