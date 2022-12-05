package appeng.server.testworld;

import java.util.Objects;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import appeng.api.parts.PartHelper;
import appeng.core.definitions.AEItems;

record PlaceFacade(BoundingBox bb, ItemStack visual, @Nullable Direction side) implements BlockPlacingBuildAction {
    @Override
    public BoundingBox getBoundingBox() {
        return bb;
    }

    @Override
    public void placeBlock(ServerLevel level, Player player, BlockPos pos, BlockPos minPos, BlockPos maxPos) {
        var actualSide = Objects.requireNonNullElse(side, Direction.UP);
        var partHost = PartHelper.getPartHost(level, pos);
        var facadeItem = AEItems.FACADE.asItem().createFacadeForItemUnchecked(visual);
        var facadePart = AEItems.FACADE.asItem().createPartFromItemStack(facadeItem, actualSide);
        partHost.getFacadeContainer().addFacade(facadePart);
    }
}
