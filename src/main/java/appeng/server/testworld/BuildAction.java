package appeng.server.testworld;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

interface BuildAction {
    BoundingBox getBoundingBox();

    void build(ServerLevel level, ServerPlayer player, BlockPos origin);
}
