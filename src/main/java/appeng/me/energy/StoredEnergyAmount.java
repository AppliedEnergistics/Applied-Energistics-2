package appeng.me.energy;

import net.minecraft.util.Mth;

import appeng.api.networking.events.GridPowerStorageStateChanged;

/**
 * Wraps a stored energy amount with callbacks when it passes a low / high threshold.
 */
public final class StoredEnergyAmount {
    /**
     * To avoid numerical precision issues, we reject operations that insert or extract less than this.
     */
    public static final double MIN_AMOUNT = 0.000001;
    /**
     * This is the amount stored by the creative energy cell.
     */
    public static final double MAX_MAXIMUM = (double) (Long.MAX_VALUE / 10000);

    /**
     * If we store more energy than this, we are capable of providing energy to the grid. If we change from being below
     * to being above the threshold, we emit a {@link GridPowerStorageStateChanged.PowerEventType#PROVIDE_POWER} event.
     */
    private final double provideThreshold;
    /**
     * If we have more unfilled storage than this, we are capable of receiving energy. If we change from being below to
     * being above this threshold, we emit a {@link GridPowerStorageStateChanged.PowerEventType#RECEIVE_POWER} event.
     */
    private final double receiveThreshold;
    private double maximum;
    private final EventEmitter eventEmitter;
    private double stored;
    // The energy service will by default add all nodes as providers&receivers initially,
    // only depending on their WRITE/READ settings.
    private boolean isReceiving = true;
    private boolean isProviding = true;

    public StoredEnergyAmount(double stored, double maximum, EventEmitter eventEmitter) {
        this(stored, 0.1, 0.1, maximum, eventEmitter);
    }

    public StoredEnergyAmount(double stored, double provideThreshold, double receiveThreshold, double maximum,
            EventEmitter eventEmitter) {
        this.provideThreshold = provideThreshold;
        this.receiveThreshold = receiveThreshold;
        this.maximum = maximum;
        this.eventEmitter = eventEmitter;
        this.stored = stored;
    }

    public double getAmount() {
        return stored;
    }

    public double getMaximum() {
        return this.maximum;
    }

    /**
     * Increase the amount, and return the amount that could actually be inserted.
     */
    public double insert(double amount, boolean commit) {
        if (amount < MIN_AMOUNT) {
            return 0; // This also prevents negative insertion
        }

        double inserted = Math.min(amount, maximum - stored);
        if (commit) {
            setStored(stored + inserted);
        }
        return inserted;
    }

    /**
     * Decrease the amount, and return how much was actually extracted.
     */
    public double extract(double amount, boolean commit) {
        if (amount < MIN_AMOUNT) {
            return 0; // This also prevents negative extraction
        }

        double extracted = Math.min(amount, this.stored);
        if (commit) {
            setStored(stored - extracted);
        }
        return extracted;
    }

    public void setStored(double amount) {
        if (amount < MIN_AMOUNT) {
            amount = 0;
        }
        this.stored = Mth.clamp(amount, 0, maximum);

        sendEvents();
    }

    public void setMaximum(double maximum) {
        this.maximum = Math.min(MAX_MAXIMUM, maximum);
        this.stored = Mth.clamp(this.stored, 0, maximum);

        sendEvents();
    }

    public double remainingCapacity() {
        return this.maximum - this.stored;
    }

    private boolean canProvide() {
        return this.stored >= provideThreshold;
    }

    private boolean canReceive() {
        return remainingCapacity() >= receiveThreshold;
    }

    private void sendEvents() {
        var wasProviding = isProviding;
        var wasReceiving = isReceiving;
        this.isProviding = canProvide();
        this.isReceiving = canReceive();

        if (!wasProviding && this.isProviding) {
            eventEmitter.emitEvent(GridPowerStorageStateChanged.PowerEventType.PROVIDE_POWER);
        }
        if (!wasReceiving && this.isReceiving) {
            eventEmitter.emitEvent(GridPowerStorageStateChanged.PowerEventType.RECEIVE_POWER);
        }
    }

    @FunctionalInterface
    public interface EventEmitter {
        void emitEvent(GridPowerStorageStateChanged.PowerEventType type);
    }
}
