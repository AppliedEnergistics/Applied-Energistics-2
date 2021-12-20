package appeng.server.testworld;

import java.util.List;
import java.util.function.Consumer;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public record SpawnEntityAction(BoundingBox bb, EntityType<?> type,
        Consumer<Entity> postProcessor) implements BuildAction {
    @Override
    public BoundingBox getBoundingBox() {
        return bb;
    }

    @Override
    public void spawnEntities(ServerLevel level, BlockPos origin, List<Entity> entities) {
        var actualBox = getBoundingBox().moved(origin.getX(), origin.getY(), origin.getZ());
        BlockPos.betweenClosedStream(actualBox).forEach(pos -> {
            var entity = type.spawn(level, null, null, null, pos, MobSpawnType.COMMAND, true, true);
            if (entity != null) {
                postProcessor.accept(entity);
                entities.add(entity);
            }
        });
    }
}
