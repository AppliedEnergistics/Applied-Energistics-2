package appeng.server.testworld;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

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
                    plot.getId().toString(),
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
            template.load(tag);
            return template;
        }

        return null;
    }

    /**
     * Place our test plot when a structure block using a fake plot ID is being spawned.
     */
    public static void createStructure(StructureBlockEntity structureBlock) {
        var id = ResourceLocation.tryParse(structureBlock.getStructureName());
        if (id == null) {
            return;
        }

        var plot = TestPlots.getById(id);

        if (plot != null) {
            var bounds = plot.getBounds();
            var origin = structureBlock.getBlockPos()
                    .offset(structureBlock.getStructurePos())
                    .offset(getPlotTranslation(bounds));
            var level = (ServerLevel) structureBlock.getLevel();
            plot.build(
                    level,
                    Platform.getPlayer(level),
                    origin);
        }
    }

    private static BlockPos getPlotTranslation(BoundingBox bounds) {
        return new BlockPos(
                bounds.minX() < 0 ? -bounds.minX() : 0,
                bounds.minY() < 0 ? -bounds.minY() : 0,
                bounds.minZ() < 0 ? -bounds.minZ() : 0);
    }
}
