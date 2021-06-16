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
        wall(ApiBlocks.skyStoneWall, modLoc("block/sky_stone_block"));
        wall(ApiBlocks.smoothSkyStoneWall, modLoc("block/smooth_sky_stone_block"));
        wall(ApiBlocks.skyStoneBrickWall, modLoc("block/sky_stone_brick"));
        wall(ApiBlocks.skyStoneSmallBrickWall, modLoc("block/sky_stone_small_brick"));
        wall(ApiBlocks.fluixWall, modLoc("block/fluix_block"));
        wall(ApiBlocks.quartzWall, modLoc("block/quartz_block"));
        wall(ApiBlocks.chiseledQuartzWall, modLoc("block/chiseled_quartz_block_side"));
        wall(ApiBlocks.quartzPillarWall, modLoc("block/quartz_pillar_side"));
    }

    /**
     * Defines a standard wall blockstate, the necessary block models and item model.
     */
    private void wall(BlockDefinition block, ResourceLocation texture) {
        wallBlock((WallBlock) block.block(), texture);
        itemModels().wallInventory(block.id().getPath(), texture);
    }

}
