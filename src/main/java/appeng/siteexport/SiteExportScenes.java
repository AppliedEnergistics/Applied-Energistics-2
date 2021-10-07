package appeng.siteexport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import appeng.block.crafting.CraftingStorageBlock;
import appeng.block.misc.QuartzFixtureBlock;
import appeng.block.misc.VibrationChamberBlock;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.BlockDefinition;

final class SiteExportScenes {
    private SiteExportScenes() {
    }

    public static List<Scene> createScenes() {
        List<Scene> scenes = new ArrayList<>();

        Function<BlockState, BlockState> craftingStorageState = (BlockState s) -> s
                .setValue(CraftingStorageBlock.FORMED, true);
        Collections.addAll(
                scenes,
                singleBlock(AEBlocks.CONTROLLER),
                singleBlock(AEBlocks.CRAFTING_STORAGE_1K, craftingStorageState),
                singleBlock(AEBlocks.CRAFTING_STORAGE_4K, craftingStorageState),
                singleBlock(AEBlocks.CRAFTING_STORAGE_16K, craftingStorageState),
                singleBlock(AEBlocks.CRAFTING_STORAGE_64K, craftingStorageState),
                singleBlock(AEBlocks.CELL_WORKBENCH),
                singleBlock(AEBlocks.QUARTZ_ORE),
                singleBlock(AEBlocks.QUARTZ_BLOCK),
                singleBlock(AEBlocks.QUARTZ_FIXTURE, b -> b.setValue(QuartzFixtureBlock.FACING, Direction.UP)),
                singleBlock(AEBlocks.QUARTZ_FIXTURE, b -> b.setValue(QuartzFixtureBlock.FACING, Direction.EAST)),
                singleBlock(AEBlocks.CHISELED_QUARTZ_BLOCK),
                singleBlock(AEBlocks.FLUIX_BLOCK),
                singleBlock(AEBlocks.INSCRIBER),
                singleBlock(AEBlocks.ITEM_INTERFACE),
                singleBlock(AEBlocks.FLUID_INTERFACE),
                singleBlock(AEBlocks.IO_PORT),
                singleBlock(AEBlocks.CONDENSER),
                singleBlock(AEBlocks.CHEST),
                singleBlock(AEBlocks.DRIVE),
                singleBlock(AEBlocks.QUANTUM_LINK),
                singleBlock(AEBlocks.QUANTUM_RING),
                singleBlock(AEBlocks.QUARTZ_GLASS),
                singleBlock(AEBlocks.SECURITY_STATION),
                singleBlock(AEBlocks.SKY_STONE_BLOCK),
                singleBlock(AEBlocks.SMOOTH_SKY_STONE_CHEST),
                singleBlock(AEBlocks.SKY_STONE_BRICK),
                singleBlock(AEBlocks.SKY_STONE_SMALL_BRICK),
                singleBlock(AEBlocks.SPATIAL_IO_PORT),
                singleBlock(AEBlocks.VIBRATION_CHAMBER, b -> b.setValue(VibrationChamberBlock.ACTIVE, true)));

        return scenes;
    }

    private static Scene singleBlock(BlockDefinition<?> block) {
        return singleBlock(block.id().getPath(), block.block(), Function.identity());
    }

    private static Scene singleBlock(BlockDefinition<?> block, Function<BlockState, BlockState> stateCustomizer) {
        return singleBlock(block.id().getPath(), block.block(), stateCustomizer);
    }

    private static Scene singleBlock(String filename, Block block, Function<BlockState, BlockState> stateCustomizer) {
        String fullPath = "large/" + filename + ".png";
        var scene = new Scene(bigBlockSettings(), fullPath);
        var state = block.defaultBlockState();
        state = stateCustomizer.apply(state);
        scene.world.blocks.put(BlockPos.ZERO, state);
        return scene;
    }

    private static SceneRenderSettings bigBlockSettings() {
        var settings = new SceneRenderSettings();
        settings.ortographic = false;
        settings.width = 512;
        settings.height = 512;
        return settings;
    }

}
