package appeng.server.testworld;

import java.util.function.Consumer;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/**
 * Calls a customization function on each placed block entity of the given type in the bounding box. Blocks that do not
 * contain such a block entity are skipped.
 */
public record BlockEntityCustomizer<T extends BlockEntity> (BoundingBox bb,
        BlockEntityType<T> type,
        Consumer<T> consumer) implements BlockPlacingBuildAction {
    @Override
    public void placeBlock(ServerLevel level, Player player, BlockPos pos, BlockPos minPos, BlockPos maxPos) {
        level.getBlockEntity(pos, type).ifPresent(consumer);
    }

    @Override
    public BoundingBox getBoundingBox() {
        return bb;
    }
}
