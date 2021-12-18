package appeng.siteexport;

import com.mojang.math.Vector3f;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import appeng.server.testworld.Plot;
import appeng.util.Platform;

public class PlotScene extends Scene {
    private final Plot plot;
    private final BlockPos minPos;
    private final BlockPos maxPos;

    public PlotScene(SceneRenderSettings settings, String filename, Plot plot) {
        super(settings, filename);
        this.plot = plot;
        this.minPos = new BlockPos(
                plot.getBounds().minX(),
                plot.getBounds().minY(),
                plot.getBounds().minZ());
        this.maxPos = new BlockPos(
                plot.getBounds().maxX(),
                plot.getBounds().maxY(),
                plot.getBounds().maxZ());

        var center = plot.getBounds().getCenter();
        centerOn = new Vector3f(
                center.getX() + 0.5f,
                center.getY(),
                center.getZ() + 0.5f);
        rotationY = 180 - 30;
    }

    @Override
    public void setUp(ServerLevel level) {
        this.plot.build(level, Platform.getPlayer(level), BlockPos.ZERO);

        if (this.postSetup != null) {
            this.postSetup.accept(level);
        }
    }

    @Override
    public BlockPos getMin() {
        return minPos;
    }

    @Override
    public BlockPos getMax() {
        return maxPos;
    }
}
