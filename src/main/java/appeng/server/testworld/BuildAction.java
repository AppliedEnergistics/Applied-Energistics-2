package appeng.server.testworld;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

interface BuildAction {
    BoundingBox getBoundingBox();

    default void build(ServerLevel level, Player player, BlockPos origin) {
    }

    default void spawnEntities(ServerLevel level, BlockPos origin, List<Entity> entities) {
    }
}
