package appeng.api.behaviors;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

import appeng.api.stacks.AEKeyType;
import appeng.parts.automation.StackWorldBehaviors;

/**
 * Strategy to import from adjacent blocks into the grid. Used by the import bus.
 */
@ApiStatus.Experimental
public interface StackImportStrategy {
    boolean transfer(StackTransferContext context);

    @FunctionalInterface
    interface Factory {
        StackImportStrategy create(ServerLevel level, BlockPos fromPos, Direction fromSide);
    }

    static void register(AEKeyType keyType, Factory factory) {
        StackWorldBehaviors.registerImportStrategy(keyType, factory);
    }
}
