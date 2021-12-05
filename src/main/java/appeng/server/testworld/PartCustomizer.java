package appeng.server.testworld;

import java.util.function.Consumer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import appeng.api.parts.IPart;
import appeng.core.definitions.ItemDefinition;
import appeng.items.parts.PartItem;

public record PartCustomizer<T extends IPart> (BoundingBox bb,
        Direction side,
        ItemDefinition<? extends PartItem<T>> part,
        Consumer<T> partCustomizer) implements BlockPlacingBuildAction {
    @Override
    public void placeBlock(ServerLevel level, ServerPlayer player, BlockPos pos, BlockPos minPos, BlockPos maxPos) {
        part.asItem().getPart(level, pos, side).ifPresent(partCustomizer);
    }

    @Override
    public BoundingBox getBoundingBox() {
        return bb;
    }
}
