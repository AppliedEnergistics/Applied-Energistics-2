package appeng.siteexport;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import com.mojang.math.Vector3f;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import appeng.core.definitions.AEBlockEntities;
import appeng.core.definitions.AEBlocks;

class Scene {
    private static final int PADDING = 1;

    SceneRenderSettings settings;
    String filename;
    Map<BlockPos, BlockState> blocks = new HashMap<>();
    Map<BlockPos, Item> cables = new HashMap<>();
    Consumer<ServerLevel> postSetup;
    Consumer<ClientLevel> beforeRender;
    int waitTicks = 1;
    Vector3f centerOn = Vector3f.ZERO;

    public Scene(SceneRenderSettings settings, String filename) {
        this.settings = settings;
        this.filename = filename;
    }

    public BlockPos getMin() {
        return BoundingBox.encapsulatingPositions(blocks.keySet())
                .map(bb -> new BlockPos(bb.minX(), bb.minY(), bb.minZ()))
                .orElseThrow();
    }

    public BlockPos getMax() {
        return BoundingBox.encapsulatingPositions(blocks.keySet())
                .map(bb -> new BlockPos(bb.maxX(), bb.maxY(), bb.maxZ()))
                .orElseThrow();
    }

    public void clearArea(Level level) {
        var min = getMin().offset(-PADDING, -PADDING, -PADDING);
        var max = getMax().offset(PADDING, PADDING, PADDING);

        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    public void clearLighting(ClientLevel level) {
        var min = getMin().offset(-PADDING, -PADDING, -PADDING);
        var max = getMax().offset(PADDING, PADDING, PADDING);

        var lightEngine = level.getLightEngine();
        var nibbles = new byte[DataLayer.SIZE];
        Arrays.fill(nibbles, (byte) 0xFF);
        DataLayer dataLayer = new DataLayer(nibbles);

        var secMin = SectionPos.of(min);
        var secMax = SectionPos.of(max);
        SectionPos.betweenClosedStream(
                secMin.x(), secMin.y(), secMin.z(),
                secMax.x(), secMax.y(), secMax.z()).forEach(sectionPos -> {
                    lightEngine.queueSectionData(LightLayer.SKY, sectionPos, dataLayer, true);
                    lightEngine.queueSectionData(LightLayer.BLOCK, sectionPos, dataLayer, true);
                    lightEngine.runUpdates(Integer.MAX_VALUE, true, true);
                });

    }

    public void setUp(ServerLevel level) {
        for (var entry : blocks.entrySet()) {
            var pos = entry.getKey();
            var state = entry.getValue();

            level.setBlock(pos, state, Block.UPDATE_ALL_IMMEDIATE);
        }

        for (var entry : cables.entrySet()) {
            var pos = entry.getKey();
            level.getBlockEntity(pos, AEBlockEntities.CABLE_BUS).ifPresent(cableBus -> {
                cableBus.addPart(new ItemStack(entry.getValue()), null, null);
            });
        }

        if (postSetup != null) {
            postSetup.accept(level);
        }
    }

    public void putCable(BlockPos blockPos, Item item) {
        blocks.put(blockPos, AEBlocks.CABLE_BUS.block().defaultBlockState());
        cables.put(blockPos, item);
    }
}
