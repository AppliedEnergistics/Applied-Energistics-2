package appeng.server.testworld;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

interface BuildAction {
    BoundingBox getBoundingBox();

    void build(ServerLevel level, Player player, BlockPos origin);
}
