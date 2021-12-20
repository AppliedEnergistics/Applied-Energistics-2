package appeng.server.testworld;

import java.util.function.Consumer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import appeng.api.parts.IPart;
import appeng.api.parts.PartHelper;
import appeng.core.definitions.ItemDefinition;
import appeng.items.parts.PartItem;

public record PartCustomizer<T extends IPart> (BoundingBox bb,
        Direction side,
        ItemDefinition<? extends PartItem<T>> part,
        Consumer<T> partCustomizer) implements BlockPlacingBuildAction {
    @Override
    public void placeBlock(ServerLevel level, Player player, BlockPos pos, BlockPos minPos, BlockPos maxPos) {
        var placedPart = PartHelper.getPart(part.asItem(), level, pos, side);
        if (placedPart != null) {
            partCustomizer.accept(placedPart);
        }
    }

    @Override
    public BoundingBox getBoundingBox() {
        return bb;
    }
}
