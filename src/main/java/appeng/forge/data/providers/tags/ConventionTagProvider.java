package appeng.forge.data.providers.tags;

import appeng.core.AppEng;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;

import java.io.IOException;
import java.nio.file.Path;

public class ConventionTagProvider extends TagProvider {

    public ConventionTagProvider(Path outputPath) {
        super(outputPath);
    }

    @Override
    protected void generate() throws IOException {
        // Dyes
        addItemTag("white_dyes", Items.WHITE_DYE);
        addItemTag("orange_dyes", Items.ORANGE_DYE);
        addItemTag("magenta_dyes", Items.MAGENTA_DYE);
        addItemTag("light_blue_dyes", Items.LIGHT_BLUE_DYE);
        addItemTag("yellow_dyes", Items.YELLOW_DYE);
        addItemTag("lime_dyes", Items.LIME_DYE);
        addItemTag("pink_dyes", Items.PINK_DYE);
        addItemTag("gray_dyes", Items.GRAY_DYE);
        addItemTag("light_gray_dyes", Items.LIGHT_GRAY_DYE);
        addItemTag("cyan_dyes", Items.CYAN_DYE);
        addItemTag("purple_dyes", Items.PURPLE_DYE);
        addItemTag("blue_dyes", Items.BLUE_DYE);
        addItemTag("brown_dyes", Items.BROWN_DYE);
        addItemTag("green_dyes", Items.GREEN_DYE);
        addItemTag("red_dyes", Items.RED_DYE);
        addItemTag("black_dyes", Items.BLACK_DYE);

        addItemTag("iron_ingots", Items.IRON_INGOT);
        addItemTag("iron_ores", Items.IRON_ORE);
        addItemTag("gold_ingots", Items.GOLD_INGOT);
        addItemTag("gold_ores", Items.GOLD_ORE);
        addItemTag("glowstone_dusts", Items.GLOWSTONE_DUST);
        addItemTag("wooden_rods", Items.STICK);
        addItemTag("nether_quartz_ores", Items.NETHER_QUARTZ_ORE);
        addItemTag("nether_quartz_crystals", Items.QUARTZ);
        addItemTag("sand_blocks", Items.SAND, Items.RED_SAND);
        addItemTag("diamonds", Items.DIAMOND);
        addItemTag("wooden_chests", Items.CHEST, Items.TRAPPED_CHEST);
        addItemTag("wheat_crops", Items.WHEAT);
        addItemTag("redstone_dusts", Items.REDSTONE);
        addItemTag("ender_pearls", Items.ENDER_PEARL);
        addItemTag("terracotta_blocks", Items.TERRACOTTA,
                Items.WHITE_TERRACOTTA,
                Items.ORANGE_TERRACOTTA,
                Items.MAGENTA_TERRACOTTA,
                Items.LIGHT_BLUE_TERRACOTTA,
                Items.YELLOW_TERRACOTTA,
                Items.LIME_TERRACOTTA,
                Items.PINK_TERRACOTTA,
                Items.GRAY_TERRACOTTA,
                Items.LIGHT_GRAY_TERRACOTTA,
                Items.CYAN_TERRACOTTA,
                Items.PURPLE_TERRACOTTA,
                Items.BLUE_TERRACOTTA,
                Items.BROWN_TERRACOTTA,
                Items.GREEN_TERRACOTTA,
                Items.RED_TERRACOTTA,
                Items.BLACK_TERRACOTTA
        );
        addItemTag("glass_blocks", Items.GLASS,
                Items.WHITE_STAINED_GLASS,
                Items.ORANGE_STAINED_GLASS,
                Items.MAGENTA_STAINED_GLASS,
                Items.LIGHT_BLUE_STAINED_GLASS,
                Items.YELLOW_STAINED_GLASS,
                Items.LIME_STAINED_GLASS,
                Items.PINK_STAINED_GLASS,
                Items.GRAY_STAINED_GLASS,
                Items.LIGHT_GRAY_STAINED_GLASS,
                Items.CYAN_STAINED_GLASS,
                Items.PURPLE_STAINED_GLASS,
                Items.BLUE_STAINED_GLASS,
                Items.BROWN_STAINED_GLASS,
                Items.GREEN_STAINED_GLASS,
                Items.RED_STAINED_GLASS,
                Items.BLACK_STAINED_GLASS
        );

        addBlockTag("glass_blocks", Blocks.GLASS,
                Blocks.WHITE_STAINED_GLASS,
                Blocks.ORANGE_STAINED_GLASS,
                Blocks.MAGENTA_STAINED_GLASS,
                Blocks.LIGHT_BLUE_STAINED_GLASS,
                Blocks.YELLOW_STAINED_GLASS,
                Blocks.LIME_STAINED_GLASS,
                Blocks.PINK_STAINED_GLASS,
                Blocks.GRAY_STAINED_GLASS,
                Blocks.LIGHT_GRAY_STAINED_GLASS,
                Blocks.CYAN_STAINED_GLASS,
                Blocks.PURPLE_STAINED_GLASS,
                Blocks.BLUE_STAINED_GLASS,
                Blocks.BROWN_STAINED_GLASS,
                Blocks.GREEN_STAINED_GLASS,
                Blocks.RED_STAINED_GLASS,
                Blocks.BLACK_STAINED_GLASS
        );
    }

    @Override
    public String getName() {
        return AppEng.MOD_NAME + " Convention Tags";
    }

}
