package appeng.me.energy;

import appeng.api.networking.events.GridPowerStorageStateChanged;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class StoredEnergyAmountTest {
    private final List<GridPowerStorageStateChanged.PowerEventType> emittedEvents = new ArrayList<>();
    private final StoredEnergyAmount storage = new StoredEnergyAmount(0, 100, emittedEvents::add);

    @Test
    void testNoEventsEmittedOnConstruction() {
        assertThat(emittedEvents).isEmpty();
    }

    @Test
    void testNegativeInsertionIsIgnored() {
        assertEquals(0, storage.insert(-100, false));
        assertEquals(0, storage.insert(-100, true));
        assertEquals(0, storage.getAmount());
    }

    @Test
    void testInsertionBelowMinimumAreIgnored() {
        assertEquals(0, storage.insert(StoredEnergyAmount.MIN_AMOUNT / 2, false));
        assertEquals(0, storage.insert(StoredEnergyAmount.MIN_AMOUNT / 2, true));
        assertEquals(0, storage.getAmount());
    }

    @Test
    void testAmountsBelowMinimumAreClampedToZero() {
        storage.insert(StoredEnergyAmount.MIN_AMOUNT * 2.5, true);
        assertThat(storage.getAmount()).isGreaterThan(0);
        storage.extract(StoredEnergyAmount.MIN_AMOUNT * 2, true);
        assertEquals(0, storage.getAmount());
    }

    @Test
    void testSimulatedInsertDoesNotTriggerEvent() {
        assertEquals(100, storage.insert(100, false));
        assertThat(emittedEvents).isEmpty();
    }

    @Test
    void testInsertCrossingProvideThresholdTriggersEvent() {
        assertEquals(1, storage.insert(1, true));
        // We don't emit the first time we pass the threshold, because
        // all storage is registered as a provider initially.
        assertThat(emittedEvents).isEmpty();

        // Reduce storage to zero again
        storage.setStored(0);
        assertThat(emittedEvents).isEmpty();

        // Now pass the threshold a second time
        assertEquals(1, storage.insert(1, true));
        assertThat(emittedEvents).containsOnly(GridPowerStorageStateChanged.PowerEventType.PROVIDE_POWER);
    }

    @Test
    void testExtractCrossingRequestThresholdTriggersEvent() {
        assertEquals(100, storage.insert(100, true));
        assertThat(emittedEvents).isEmpty();

        // Reduce it below the receive-threshold
        storage.extract(0.2, true);
        assertThat(emittedEvents).containsOnly(GridPowerStorageStateChanged.PowerEventType.RECEIVE_POWER);
    }

    @Test
    void testSimulatedExtractCrossingRequestThresholdDoesntTriggersEvent() {
        storage.setStored(storage.getMaximum());
        // Reduce it below the receive-threshold
        storage.extract(0.2, false);
        assertThat(emittedEvents).isEmpty();
    }

    @Test
    void testIncreasingMaximumCanTriggerEvents() {
        assertEquals(100, storage.insert(100, true));
        assertThat(emittedEvents).isEmpty();

        // Change the maximum so there's enough remaining space to receive power again
        storage.setMaximum(101);
        assertThat(emittedEvents).containsOnly(GridPowerStorageStateChanged.PowerEventType.RECEIVE_POWER);
    }

    @Test
    void testReceiveEventsAreOnlyTriggeredOnStateChanges() {
        storage.setStored(100);
        assertThat(emittedEvents).isEmpty();

        while (storage.extract(1, true) > 0) {
        }

        assertThat(emittedEvents).containsOnly(GridPowerStorageStateChanged.PowerEventType.RECEIVE_POWER);
    }

    @Test
    void testProvideEventsAreOnlyTriggeredOnStateChanges() {
        // reset the internal isProviding flag
        storage.setStored(100);
        storage.setStored(0);
        emittedEvents.clear();

        while (storage.insert(1, true) > 0) {
        }

        assertThat(emittedEvents).containsOnly(GridPowerStorageStateChanged.PowerEventType.PROVIDE_POWER);
    }

    @Test
    void testCannotExtractLessThanMinimumAmount() {
        storage.setStored(100);
        assertEquals(0, storage.extract(StoredEnergyAmount.MIN_AMOUNT / 2, false));
        assertEquals(0, storage.extract(StoredEnergyAmount.MIN_AMOUNT / 2, true));
    }
}