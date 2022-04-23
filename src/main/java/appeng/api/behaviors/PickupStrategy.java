package appeng.api.behaviors;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;

import appeng.api.networking.energy.IEnergySource;
import appeng.api.stacks.AEKeyType;
import appeng.parts.automation.StackWorldBehaviors;

@ApiStatus.Experimental
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

    @FunctionalInterface
    interface Factory {
        PickupStrategy create(ServerLevel level, BlockPos fromPos, Direction fromSide, BlockEntity host,
                boolean allowSilkTouch, int fortuneLevel);
    }

    static void register(AEKeyType type, Factory factory) {
        StackWorldBehaviors.registerPickupStrategy(type, factory);
    }
}
