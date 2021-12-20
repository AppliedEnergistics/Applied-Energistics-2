package appeng.server.testworld;

import java.util.Objects;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import appeng.api.parts.IPartItem;
import appeng.api.parts.PartHelper;

record PlacePart(BoundingBox bb, IPartItem<?> what, @Nullable Direction side) implements BlockPlacingBuildAction {
    @Override
    public BoundingBox getBoundingBox() {
        return bb;
    }

    @Override
    public void placeBlock(ServerLevel level, Player player, BlockPos pos, BlockPos minPos, BlockPos maxPos) {
        var actualSide = Objects.requireNonNullElse(side, Direction.UP);
        PartHelper.setPart(level, pos, actualSide, player, what);
    }
}
