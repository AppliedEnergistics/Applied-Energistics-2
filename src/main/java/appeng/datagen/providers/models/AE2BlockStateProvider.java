package appeng.datagen.providers.models;

import net.minecraft.data.DataGenerator;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;

import appeng.core.AppEng;
import appeng.core.definitions.BlockDefinition;
import appeng.datagen.providers.IAE2DataProvider;

public abstract class AE2BlockStateProvider extends BlockStateProvider implements IAE2DataProvider {
    public AE2BlockStateProvider(DataGenerator gen, String modid, ExistingFileHelper exFileHelper) {
        super(gen, modid, exFileHelper);
    }

    /**
     * Define a block model that is a simple textured cube, and uses the same model for its item. The texture path is
     * derived from the block's id.
     */
    protected void simpleBlockAndItem(BlockDefinition<?> block) {
        var model = cubeAll(block.block());
        simpleBlock(block.block(), model);
        simpleBlockItem(block.block(), model);
    }

    protected void simpleBlockAndItem(BlockDefinition<?> block, ModelFile model) {
        simpleBlock(block.block(), model);
        simpleBlockItem(block.block(), model);
    }

    /**
     * Define a block model that is a simple textured cube, and uses the same model for its item.
     */
    protected void simpleBlockAndItem(BlockDefinition<?> block, String textureName) {
        var model = models().cubeAll(block.id().getPath(), AppEng.makeId(textureName));
        simpleBlock(block.block(), model);
        simpleBlockItem(block.block(), model);
    }

    /**
     * Defines a standard wall blockstate, the necessary block models and item model.
     */
    protected void wall(BlockDefinition<WallBlock> block, String texture) {
        wallBlock(block.block(), AppEng.makeId(texture));
        itemModels().wallInventory(block.id().getPath(), AppEng.makeId(texture));
    }

    protected void slabBlock(BlockDefinition<SlabBlock> slab, BlockDefinition<?> base) {
        var texture = blockTexture(base.block()).getPath();
        slabBlock(slab, base, texture, texture, texture);
    }

    protected void slabBlock(BlockDefinition<SlabBlock> slab, BlockDefinition<?> base, String bottomTexture,
            String sideTexture, String topTexture) {
        var side = AppEng.makeId(sideTexture);
        var bottom = AppEng.makeId(bottomTexture);
        var top = AppEng.makeId(topTexture);

        var bottomModel = models().slab(slab.id().getPath(), side, bottom, top);
        simpleBlockItem(slab.block(), bottomModel);
        slabBlock(
                slab.block(),
                bottomModel,
                models().slabTop(slab.id().getPath() + "_top", side, bottom, top),
                models().getExistingFile(base.id()));
    }

    protected void stairsBlock(BlockDefinition<StairBlock> stairs, BlockDefinition<?> base) {
        var texture = "block/" + base.id().getPath();

        stairsBlock(stairs, texture, texture, texture);
    }

    protected void stairsBlock(BlockDefinition<StairBlock> stairs, String bottomTexture, String sideTexture,
            String topTexture) {
        var baseName = stairs.id().getPath();

        var side = AppEng.makeId(sideTexture);
        var bottom = AppEng.makeId(bottomTexture);
        var top = AppEng.makeId(topTexture);

        ModelFile stairsModel = models().stairs(baseName, side, bottom, top);
        ModelFile stairsInner = models().stairsInner(baseName + "_inner", side, bottom, top);
        ModelFile stairsOuter = models().stairsOuter(baseName + "_outer", side, bottom, top);
        stairsBlock(stairs.block(), stairsModel, stairsInner, stairsOuter);
        simpleBlockItem(stairs.block(), stairsModel);
    }

}
