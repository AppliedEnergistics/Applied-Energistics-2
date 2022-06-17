package appeng.api.behaviors;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

import appeng.api.config.Actionable;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.parts.automation.StackWorldBehaviors;

/**
 * Strategy to export stacks into adjacent blocks from the grid. Used by the export bus.
 */
@ApiStatus.Experimental
public interface StackExportStrategy {
    /**
     * Transfer from the network inventory in {@param context} to the external inventory this strategy was created for.
     */
    long transfer(StackTransferContext context, AEKey what, long maxAmount);

    /**
     * Tries inserting into the adjacent inventory and returns the amount that was pushed.
     */
    long push(AEKey what, long maxAmount, Actionable mode);

    @FunctionalInterface
    interface Factory {
        StackExportStrategy create(ServerLevel level, BlockPos fromPos, Direction fromSide);
    }

    static void register(AEKeyType type, Factory factory) {
        StackWorldBehaviors.registerExportStrategy(type, factory);
    }
}
