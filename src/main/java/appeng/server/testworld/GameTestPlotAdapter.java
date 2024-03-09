package appeng.server.testworld;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.gametest.framework.StructureUtils;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.properties.StructureMode;
import net.minecraft.world.level.entity.Visibility;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import appeng.server.testplots.TestPlots;
import appeng.util.Platform;

public class GameTestPlotAdapter {

    @GameTestGenerator
    public List<TestFunction> gameTestAdapter() {
        var result = new ArrayList<TestFunction>();

        for (var plot : TestPlots.createPlots()) {
            var test = plot.getTest();
            if (test == null) {
                continue;
            }

            result.add(new TestFunction(
                    "ae2",
                    "ae2." + plot.getId().getPath(),
                    plot.getId().toString(),
                    Rotation.NONE,
                    test.maxTicks,
                    test.setupTicks,
                    true,
                    1,
                    1,
                    gameTestHelper -> {
                        test.getTestFunction().accept(new PlotTestHelper(
                                getPlotTranslation(plot.getBounds()),
                                gameTestHelper.testInfo));
                    }));
        }

        return result;
    }

    /**
     * Create a fake structure template that has the right bounding box size for our test setup.
     */
    public static StructureTemplate getStructureTemplate(String structureName) {
        var id = ResourceLocation.tryParse(structureName);
        if (id == null) {
            return null;
        }

        var plot = TestPlots.getById(id);
        if (plot != null) {
            var template = new StructureTemplate();
            var tag = new CompoundTag();
            var sizeList = new ListTag();
            var bounds = plot.getBounds();
            sizeList.add(IntTag.valueOf(bounds.getXSpan()));
            sizeList.add(IntTag.valueOf(bounds.getYSpan()));
            sizeList.add(IntTag.valueOf(bounds.getZSpan()));
            tag.put(StructureTemplate.SIZE_TAG, sizeList);
            template.load(BuiltInRegistries.BLOCK.asLookup(), tag);
            return template;
        }

        return null;
    }

    /**
     * Place our test plot when a structure block using a fake plot ID is being spawned.
     */
    public static StructureBlockEntity createStructure(Plot plot, GameTestInfo info, BlockPos pos, ServerLevel level) {

        var plotBounds = plot.getBounds();
        Vec3i size = new Vec3i(plotBounds.getXSpan(), plotBounds.getYSpan(), plotBounds.getZSpan());

        var boundingbox = StructureUtils.getStructureBoundingBox(pos, size, Rotation.NONE);
        var entityManager = level.entityManager;
        // TODO: Re-Evaluate in 1.20.5
        if (net.minecraft.DetectedVersion.tryDetectVersion().getId().equals("1.20.4")) {
            boundingbox.intersectingChunks().forEach(cp -> {
                level.setChunkForced(cp.x, cp.z, true);
                var status = entityManager.chunkVisibility.get(cp.toLong());
                if (!status.isAccessible()) {
                    entityManager.updateChunkStatus(cp, Visibility.TRACKED);
                }
            });
        } else {
            System.err.println("FIX CODE IN GameTestPlotAdapter");
            throw new RuntimeException("FIX CODE IN GameTestPlotAdapter");
        }

        StructureUtils.clearSpaceForStructure(boundingbox, level);

        level.setBlockAndUpdate(pos, Blocks.STRUCTURE_BLOCK.defaultBlockState());
        var structureBlock = (StructureBlockEntity) level.getBlockEntity(pos);
        structureBlock.setMode(StructureMode.LOAD);
        structureBlock.setIgnoreEntities(false);
        structureBlock.setStructureName(new ResourceLocation(info.getStructureName()));
        structureBlock.setMetaData(info.getTestName());
        structureBlock.setStructureSize(size);

        var bounds = plot.getBounds();
        var origin = pos
                .offset(structureBlock.getStructurePos())
                .offset(getPlotTranslation(bounds));
        plot.build(
                level,
                Platform.getFakePlayer(level, null),
                origin);

        return structureBlock;
    }

    private static BlockPos getPlotTranslation(BoundingBox bounds) {
        return new BlockPos(
                bounds.minX() < 0 ? -bounds.minX() : 0,
                bounds.minY() < 0 ? -bounds.minY() : 0,
                bounds.minZ() < 0 ? -bounds.minZ() : 0);
    }
}
