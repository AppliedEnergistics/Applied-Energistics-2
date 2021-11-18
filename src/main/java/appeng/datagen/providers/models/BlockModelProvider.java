package appeng.datagen.providers.models;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.data.models.blockstates.PropertyDispatch;
import net.minecraft.data.models.blockstates.Variant;
import net.minecraft.data.models.blockstates.VariantProperties;
import net.minecraft.data.models.model.TextureMapping;
import net.minecraft.data.models.model.TextureSlot;
import net.minecraft.data.models.model.TexturedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.WallSide;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.MultiPartBlockStateBuilder;
import net.minecraftforge.client.model.generators.VariantBlockStateBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;

import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.BlockDefinition;
import appeng.datagen.providers.IAE2DataProvider;
import appeng.decorative.solid.QuartzOreBlock;

public class BlockModelProvider extends BlockStateProvider implements IAE2DataProvider {
    public BlockModelProvider(DataGenerator gen, ExistingFileHelper exFileHelper) {
        super(gen, AppEng.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        generateOreBlock(AEBlocks.QUARTZ_ORE);
        generateOreBlock(AEBlocks.DEEPSLATE_QUARTZ_ORE);
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
