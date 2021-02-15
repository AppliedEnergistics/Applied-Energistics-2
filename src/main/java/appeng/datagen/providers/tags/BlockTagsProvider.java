package appeng.datagen.providers.tags;

import java.nio.file.Path;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.tags.ITag;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.Tags;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;

import appeng.api.definitions.IBlockDefinition;
import appeng.core.AppEng;
import appeng.datagen.providers.IAE2DataProvider;

public class BlockTagsProvider extends net.minecraft.data.BlockTagsProvider implements IAE2DataProvider {
    public BlockTagsProvider(GatherDataEvent dataEvent) {
        super(dataEvent.getGenerator(), AppEng.MOD_ID, dataEvent.getExistingFileHelper());
    }

    @Override
    protected void registerTags() {
        addForge("ores/certus_quartz", BLOCKS.quartzOre(), BLOCKS.quartzOreCharged());
        addForge("ores", "#forge:ores/certus_quartz");

        addForge("storage_blocks/certus_quartz", BLOCKS.quartzBlock());
        addForge("storage_blocks", "#forge:storage_blocks/certus_quartz");

        addForge("terracotta", Blocks.TERRACOTTA,
                Blocks.WHITE_TERRACOTTA,
                Blocks.ORANGE_TERRACOTTA,
                Blocks.MAGENTA_TERRACOTTA,
                Blocks.LIGHT_BLUE_TERRACOTTA,
                Blocks.YELLOW_TERRACOTTA,
                Blocks.LIME_TERRACOTTA,
                Blocks.PINK_TERRACOTTA,
                Blocks.GRAY_TERRACOTTA,
                Blocks.LIGHT_GRAY_TERRACOTTA,
                Blocks.CYAN_TERRACOTTA,
                Blocks.PURPLE_TERRACOTTA,
                Blocks.BLUE_TERRACOTTA,
                Blocks.BROWN_TERRACOTTA,
                Blocks.GREEN_TERRACOTTA,
                Blocks.RED_TERRACOTTA,
                Blocks.BLACK_TERRACOTTA);

        addAe2("blacklisted/annihilation_plane",
                Blocks.BEDROCK,
                Blocks.END_PORTAL,
                Blocks.END_PORTAL_FRAME,
                Blocks.COMMAND_BLOCK);

        addAe2("spatial/blacklist");
        addAe2("spatial/whitelist");

        addAe2("whitelisted/facades",
                Blocks.GLASS,
                Tags.Blocks.STAINED_GLASS,
                BLOCKS.quartzGlass(),
                BLOCKS.quartzVibrantGlass());
    }

    private void addForge(String tagName, Object... blockSources) {
        add(new ResourceLocation("forge", tagName), blockSources);
    }

    private void addAe2(String tagName, Object... blockSources) {
        add(AppEng.makeId(tagName), blockSources);
    }

    @SuppressWarnings("unchecked")
    private void add(ResourceLocation tagName, Object... blockSources) {
        Builder<Block> builder = getOrCreateBuilder(net.minecraft.tags.BlockTags.createOptional(tagName));

        for (Object blockSource : blockSources) {
            if (blockSource instanceof Block) {
                builder.add((Block) blockSource);
            } else if (blockSource instanceof IBlockDefinition) {
                builder.add(((IBlockDefinition) blockSource).block());
            } else if (blockSource instanceof ITag.INamedTag) {
                builder.addTag(
                        (ITag.INamedTag<Block>) blockSource);
            } else if (blockSource instanceof String) {
                String blockSourceString = (String) blockSource;
                if (blockSourceString.startsWith("#")) {
                    builder.add(new ITag.TagEntry(new ResourceLocation(blockSourceString.substring(1))));
                } else {
                    builder.add(new ITag.ItemEntry(new ResourceLocation(blockSourceString)));
                }
            } else {
                throw new IllegalArgumentException("Unknown block source: " + blockSource);
            }
        }
    }

    @Override
    protected Path makePath(ResourceLocation id) {
        return this.generator.getOutputFolder()
                .resolve("data/" + id.getNamespace() + "/tags/blocks/" + id.getPath() + ".json");
    }

    @Override
    public String getName() {
        return AppEng.MOD_NAME + " Block Tags";
    }
}
