package appeng.datagen.providers.models;

import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.BlockDefinition;
import appeng.datagen.providers.IAE2DataProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.common.data.ExistingFileHelper;

public class BlockModelProvider extends AE2BlockStateProvider {
    public BlockModelProvider(DataGenerator gen, ExistingFileHelper exFileHelper) {
        super(gen, AppEng.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        generateOreBlock(AEBlocks.QUARTZ_ORE);
        generateOreBlock(AEBlocks.DEEPSLATE_QUARTZ_ORE);

        simpleBlockAndItem(AEBlocks.CONDENSER);
        simpleBlockAndItem(AEBlocks.ENERGY_ACCEPTOR);
        simpleBlockAndItem(AEBlocks.FLUID_INTERFACE);
        simpleBlockAndItem(AEBlocks.ITEM_INTERFACE);
        simpleBlockAndItem(AEBlocks.PATTERN_PROVIDER);

        simpleBlockAndItem(AEBlocks.DEBUG_ITEM_GEN, "block/debug/item_gen");
        simpleBlockAndItem(AEBlocks.DEBUG_CHUNK_LOADER, "block/debug/chunk_loader");
        simpleBlockAndItem(AEBlocks.DEBUG_PHANTOM_NODE, "block/debug/phantom_node");
        simpleBlockAndItem(AEBlocks.DEBUG_CUBE_GEN, "block/debug/cube_gen");
        simpleBlockAndItem(AEBlocks.DEBUG_ENERGY_GEN, "block/debug/energy_gen");
    }

    /**
     * Generate an ore block with 4 variants (0 to 3, inclusive).
     */
    private void generateOreBlock(BlockDefinition<?> block) {
        String name = block.id().getPath();
        BlockModelBuilder primaryModel = models().cubeAll(
                name + "_0",
                AppEng.makeId("block/" + name + "_0"));

        simpleBlock(
                block.block(),
                ConfiguredModel.builder()
                        .modelFile(primaryModel)
                        .nextModel()
                        .modelFile(models().cubeAll(
                                name + "_1",
                                AppEng.makeId("block/" + name + "_1")))
                        .nextModel()
                        .modelFile(models().cubeAll(
                                name + "_2",
                                AppEng.makeId("block/" + name + "_2")))
                        .nextModel()
                        .modelFile(models().cubeAll(
                                name + "_3",
                                AppEng.makeId("block/" + name + "_3")))
                        .build());
        simpleBlockItem(block.block(), primaryModel);
    }
}
