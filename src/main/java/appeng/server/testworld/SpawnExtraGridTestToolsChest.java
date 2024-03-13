package appeng.server.testworld;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.neoforged.neoforge.common.NeoForge;

import appeng.core.definitions.AEBlockEntities;
import appeng.core.definitions.AEBlocks;
import appeng.server.testplots.SpawnExtraGridTestTools;

/**
 * Spawns a sky stone chest at the given position and once the grid at another position is initialized, posts
 * {@link SpawnExtraGridTestTools} to allow the chest to be populated.
 */
public record SpawnExtraGridTestToolsChest(BlockPos chestPos, BlockPos gridPos,
        ResourceLocation plotId) implements BuildAction {
    @Override
    public BoundingBox getBoundingBox() {
        return new BoundingBox(chestPos);
    }

    @Override
    public void build(ServerLevel level, Player player, BlockPos origin) {
        var absChestPod = chestPos.offset(origin);
        var absGridPos = gridPos.offset(origin);
        level.setBlock(absChestPod, AEBlocks.SMOOTH_SKY_STONE_CHEST.block().defaultBlockState(), Block.UPDATE_ALL);

        GridInitHelper.doAfterGridInit(level, absGridPos, false, (grid, gridNode) -> {
            var chestOpt = level.getBlockEntity(absChestPod, AEBlockEntities.SKY_CHEST);
            chestOpt.ifPresent(chest -> {
                var inventory = chest.getInternalInventory();
                NeoForge.EVENT_BUS.post(new SpawnExtraGridTestTools(plotId, inventory, grid));
            });
        });
    }
}
