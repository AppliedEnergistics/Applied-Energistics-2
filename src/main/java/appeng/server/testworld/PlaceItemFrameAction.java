package appeng.server.testworld;

import java.util.List;
import java.util.function.Consumer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public record PlaceItemFrameAction(BlockPos pos, Direction facing,
        Consumer<ItemFrame> customizer) implements BuildAction {
    @Override
    public BoundingBox getBoundingBox() {
        return new BoundingBox(pos);
    }

    @Override
    public void spawnEntities(ServerLevel level, BlockPos origin, List<Entity> entities) {
        var actualPos = pos.offset(origin);

        var itemFrame = new ItemFrame(EntityType.ITEM_FRAME, level, actualPos, facing);
        if (!level.addFreshEntity(itemFrame)) {
            return;
        }
        customizer.accept(itemFrame);
        entities.add(itemFrame);
    }
}
