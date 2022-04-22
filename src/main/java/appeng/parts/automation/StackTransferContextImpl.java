package appeng.parts.automation;

import java.util.HashSet;
import java.util.Set;

import appeng.api.behaviors.StackTransferContext;
import appeng.api.config.Actionable;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageService;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.util.prioritylist.IPartitionList;

/**
 * Context for stack transfer operations, regardless of whether they occur in or out of the network.
 */
class StackTransferContextImpl implements StackTransferContext {
    private final IStorageService internalStorage;
    private final IEnergySource energySource;
    private final IActionSource actionSource;
    private final IPartitionList filter;
    private final Set<AEKeyType> keyTypes;
    private final int initialOperations;
    private int operationsRemaining;
    private boolean isInverted;

    public StackTransferContextImpl(IStorageService internalStorage, IEnergySource energySource,
            IActionSource actionSource,
            int operationsRemaining,
            IPartitionList filter) {
        this.internalStorage = internalStorage;
        this.energySource = energySource;
        this.actionSource = actionSource;
        this.filter = filter;
        this.initialOperations = operationsRemaining;
        this.operationsRemaining = operationsRemaining;
        this.keyTypes = new HashSet<>();
        for (AEKey item : filter.getItems()) {
            this.keyTypes.add(item.getType());
        }
    }

    @Override
    public IStorageService getInternalStorage() {
        return internalStorage;
    }

    @Override
    public IEnergySource getEnergySource() {
        return energySource;
    }

    @Override
    public IActionSource getActionSource() {
        return actionSource;
    }

    @Override
    public int getOperationsRemaining() {
        return operationsRemaining;
    }

    @Override
    public void setOperationsRemaining(int operationsRemaining) {
        this.operationsRemaining = operationsRemaining;
    }

    @Override
    public boolean hasOperationsLeft() {
        return operationsRemaining > 0;
    }

    @Override
    public boolean hasDoneWork() {
        return initialOperations > operationsRemaining;
    }

    @Override
    public boolean isKeyTypeEnabled(AEKeyType space) {
        return keyTypes.isEmpty() || keyTypes.contains(space);
    }

    @Override
    public boolean isInFilter(AEKey key) {
        return filter.isEmpty() || filter.isListed(key);
    }

    @Override
    public IPartitionList getFilter() {
        return filter;
    }

    @Override
    public void invert(boolean inverted) {
        isInverted = inverted;
    }

    @Override
    public boolean isInverted() {
        return isInverted;
    }

    @Override
    public boolean canInsert(AEItemKey what, long amount) {
        return internalStorage.getInventory().insert(
                what,
                amount,
                Actionable.SIMULATE,
                actionSource) > 0;
    }

    @Override
    public void reduceOperationsRemaining(long inserted) {
        operationsRemaining -= inserted;
    }
}
