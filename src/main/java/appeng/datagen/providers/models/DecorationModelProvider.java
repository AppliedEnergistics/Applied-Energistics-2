package appeng.datagen.providers.models;

import net.minecraft.block.WallBlock;
import net.minecraft.data.DataGenerator;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import appeng.core.AppEng;
import appeng.core.api.definitions.ApiBlocks;
import appeng.core.features.BlockDefinition;
import appeng.datagen.providers.IAE2DataProvider;

public class DecorationModelProvider extends BlockStateProvider implements IAE2DataProvider {

    public DecorationModelProvider(DataGenerator gen, ExistingFileHelper exFileHelper) {
        super(gen, AppEng.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        wall(ApiBlocks.SKY_STONE_WALL, modLoc("block/sky_stone_block"));
        wall(ApiBlocks.SMOOTH_SKY_STONE_WALL, modLoc("block/smooth_sky_stone_block"));
        wall(ApiBlocks.SKY_STONE_BRICK_WALL, modLoc("block/sky_stone_brick"));
        wall(ApiBlocks.SKY_STONE_SMALL_BRICK_WALL, modLoc("block/sky_stone_small_brick"));
        wall(ApiBlocks.FLUIX_WALL, modLoc("block/fluix_block"));
        wall(ApiBlocks.QUARTZ_WALL, modLoc("block/quartz_block"));
        wall(ApiBlocks.CHISELED_QUARTZ_WALL, modLoc("block/chiseled_quartz_block_side"));
        wall(ApiBlocks.QUARTZ_PILLAR_WALL, modLoc("block/quartz_pillar_side"));
    }

    /**
     * Defines a standard wall blockstate, the necessary block models and item model.
     */
    private void wall(BlockDefinition block, ResourceLocation texture) {
        wallBlock((WallBlock) block.block(), texture);
        itemModels().wallInventory(block.id().getPath(), texture);
    }

}
