package appeng.server.testworld;

import java.util.Optional;
import java.util.function.BiConsumer;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestInstance;
import net.minecraft.gametest.framework.StructureUtils;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import appeng.server.testplots.TestPlots;
import appeng.util.Platform;

public class GameTestPlotAdapter extends GameTestInstance {

    private final Identifier plotId;

    public static final MapCodec<GameTestPlotAdapter> CODEC = RecordCodecBuilder.mapCodec(
            builder -> builder.group(
                    TestData.CODEC.fieldOf("testData").forGetter(i -> i.info()),
                    Identifier.CODEC.fieldOf("plotId").forGetter(GameTestPlotAdapter::plotId))
                    .apply(builder, GameTestPlotAdapter::new));

    protected GameTestPlotAdapter(TestData<Holder<TestEnvironmentDefinition>> testData,
            Identifier plotId) {
        super(testData);
        this.plotId = plotId;
    }

    public static void placeStructure(ServerLevel level, BlockPos pos, Plot plot) {

        var plotBounds = plot.getBounds();
        Vec3i size = new Vec3i(plotBounds.getXSpan(), plotBounds.getYSpan(), plotBounds.getZSpan());

        var boundingbox = StructureUtils.getStructureBoundingBox(pos, size, Rotation.NONE);
        boundingbox.intersectingChunks().forEach(cp -> {
            level.setChunkForced(cp.x, cp.z, true);
        });

        var bounds = plot.getBounds();
        var origin = pos
                .offset(getPlotTranslation(bounds));
        plot.build(
                level,
                Platform.getFakePlayer(level, null),
                origin);
    }

    public Identifier plotId() {
        return plotId;
    }

    public static void registerAll(BiConsumer<Identifier, GameTestInstance> register) {
        for (var plot : TestPlots.createPlots()) {
            var test = plot.getTest();
            if (test == null) {
                continue;
            }

            Holder<TestEnvironmentDefinition> environment = Holder.direct(new TestEnvironmentDefinition.AllOf());
            var testData = new TestData<>(
                    environment,
                    plot.getId(),
                    test.maxTicks,
                    test.setupTicks,
                    true,
                    Rotation.NONE,
                    false,
                    1,
                    1,
                    test.skyAccess);

            var instance = new GameTestPlotAdapter(testData, plot.getId());

            register.accept(plot.getId(), instance);
        }
    }

    @Override
    public void run(GameTestHelper helper) {
        var plot = TestPlots.getById(plotId);
        if (plot == null) {
            throw helper.assertionException("Plot " + plotId + " does not exist");
        }
        var test = plot.getTest();
        if (test == null) {
            throw helper.assertionException("Plot " + plotId + " has no test");
        }

        test.getTestFunction().accept(new PlotTestHelper(
                getPlotTranslation(plot.getBounds()),
                helper.testInfo));
    }

    @Override
    public MapCodec<? extends GameTestInstance> codec() {
        return CODEC;
    }

    @Override
    protected MutableComponent typeDescription() {
        return Component.literal("AE2 Plot " + plotId);
    }

    public static Optional<StructureTemplate> createStructure(ServerLevel level, Plot plot) {
        var template = new StructureTemplate();
        var tag = new CompoundTag();
        var sizeList = new ListTag();
        var bounds = plot.getBounds();
        sizeList.add(IntTag.valueOf(bounds.getXSpan()));
        sizeList.add(IntTag.valueOf(bounds.getYSpan()));
        sizeList.add(IntTag.valueOf(bounds.getZSpan()));
        tag.put(StructureTemplate.SIZE_TAG, sizeList);
        template.load(BuiltInRegistries.BLOCK, tag);
        return Optional.of(template);
    }

    private static BlockPos getPlotTranslation(BoundingBox bounds) {
        return new BlockPos(
                bounds.minX() < 0 ? -bounds.minX() : 0,
                bounds.minY() < 0 ? -bounds.minY() : 0,
                bounds.minZ() < 0 ? -bounds.minZ() : 0);
    }
}
