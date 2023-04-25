package appeng.server.testplots;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.server.testworld.PlotBuilder;

public final class SpatialTestPlots {
    private SpatialTestPlots() {
    }

    /**
     * Validates that controllers inside of SCSs do not cause crashes. Regression test for
     * <a href="https://github.com/AppliedEnergistics/Applied-Energistics-2/issues/6990">issue 6990</a>.
     */
    @TestPlot("controller_inside_scs")
    public static void controllerInsideScs(PlotBuilder plot) {
        // Outer network
        plot.creativeEnergyCell("0 0 0");
        plot.block("[1,10] 0 0", AEBlocks.SPATIAL_PYLON);
        plot.block("0 [1,10] 0", AEBlocks.SPATIAL_PYLON);
        plot.block("0 0 [1,10]", AEBlocks.SPATIAL_PYLON);
        plot.blockEntity("-1 0 0", AEBlocks.SPATIAL_IO_PORT, port -> {
            port.getInternalInventory().insertItem(0, AEItems.SPATIAL_CELL128.stack(), false);
        });
        var leverPos = plot.leverOn(new BlockPos(-1, 0, 0), Direction.WEST);

        // Inner network
        plot.creativeEnergyCell("[4,6] 3 [4,6]");
        for (int x = -1; x <= 1; ++x) {
            for (int y = -1; y <= 1; ++y) {
                for (int z = -1; z <= 1; ++z) {
                    boolean edge = Math.abs(x) + Math.abs(y) + Math.abs(z) >= 2;
                    if (edge) {
                        plot.block(new BlockPos(5 + x, 5 + y, 5 + z), AEBlocks.CONTROLLER);
                    }
                }
            }
        }

        // Woosh!
        plot.test(helper -> {
            helper.startSequence()
                    .thenIdle(5)
                    .thenExecute(() -> helper.pullLever(leverPos))
                    .thenIdle(5)
                    .thenSucceed();
        });
    }
}
