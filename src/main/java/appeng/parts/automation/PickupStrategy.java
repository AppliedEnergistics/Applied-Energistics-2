package appeng.parts.automation;

import net.minecraft.world.entity.Entity;

import appeng.api.networking.energy.IEnergySource;

public interface PickupStrategy {
    void reset();

    boolean canPickUpEntity(Entity entity);

    boolean pickUpEntity(IEnergySource energySource, PickupSink sink, Entity entity);

    Result tryStartPickup(IEnergySource energySource, PickupSink sink);

    void completePickup(IEnergySource energySource, PickupSink sink);

    enum Result {
        /**
         * There is nothing this strategy can pick up.
         */
        CANT_PICKUP,
        /**
         * There is something the strategy could pick up, but the storage is full.
         */
        CANT_STORE,
        /**
         * The strategy picked something up successfully.
         */
        PICKED_UP
    }
}
