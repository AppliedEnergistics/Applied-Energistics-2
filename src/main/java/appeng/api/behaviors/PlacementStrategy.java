package appeng.api.behaviors;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;

import appeng.api.config.Actionable;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.parts.automation.StackWorldBehaviors;

@ApiStatus.Experimental
public interface PlacementStrategy {
    void clearBlocked();

    long placeInWorld(AEKey what, long amount, Actionable type, boolean placeAsEntity);

    @FunctionalInterface
    interface Factory {
        PlacementStrategy create(ServerLevel level, BlockPos fromPos, Direction fromSide, BlockEntity host);
    }

    static void register(AEKeyType type, Factory factory) {
        StackWorldBehaviors.registerPlacementStrategy(type, factory);
    }
}
