package appeng.datagen.providers.models;

import net.minecraft.block.WallBlock;
import net.minecraft.data.DataGenerator;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import appeng.api.definitions.IBlockDefinition;
import appeng.core.AppEng;
import appeng.datagen.providers.IAE2DataProvider;

public class DecorationModelProvider extends BlockStateProvider implements IAE2DataProvider {

    public DecorationModelProvider(DataGenerator gen, ExistingFileHelper exFileHelper) {
        super(gen, AppEng.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        wall(BLOCKS.skyStoneWall(), modLoc("block/sky_stone_block"));
        wall(BLOCKS.smoothSkyStoneWall(), modLoc("block/smooth_sky_stone_block"));
        wall(BLOCKS.skyStoneBrickWall(), modLoc("block/sky_stone_brick"));
        wall(BLOCKS.skyStoneSmallBrickWall(), modLoc("block/sky_stone_small_brick"));
        wall(BLOCKS.fluixWall(), modLoc("block/fluix_block"));
        wall(BLOCKS.quartzWall(), modLoc("block/quartz_block"));
        wall(BLOCKS.chiseledQuartzWall(), modLoc("block/chiseled_quartz_block_side"));
        wall(BLOCKS.quartzPillarWall(), modLoc("block/quartz_pillar_side"));
    }

    /**
     * Defines a standard wall blockstate, the necessary block models and item model.
     */
    private void wall(IBlockDefinition block, ResourceLocation texture) {
        wallBlock((WallBlock) block.block(), texture);
        itemModels().wallInventory(block.identifier(), texture);
    }

}
