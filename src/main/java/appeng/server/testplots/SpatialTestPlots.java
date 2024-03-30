package appeng.server.testplots;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import appeng.api.implementations.items.ISpatialStorageCell;
import appeng.blockentity.spatial.SpatialIOPortBlockEntity;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.server.testworld.PlotBuilder;
import appeng.server.testworld.PlotTestHelper;
import appeng.spatial.SpatialStoragePlotManager;

@TestPlotClass
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

    /**
     * Validates that crafting CPUs inside of SCSs do not cause crashes. Regression test for
     * <a href="https://github.com/AppliedEnergistics/Applied-Energistics-2/issues/7513">issue 7513</a>.
     */
    @TestPlot("crafting_cpu_inside_scs")
    public static void craftingCpuInsideScs(PlotBuilder plot) {
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
        plot.creativeEnergyCell("3 0 3");
        plot.block("[2,4] [1,3] [2,4]", AEBlocks.CRAFTING_STORAGE_64K);

        // Woosh!
        plot.test(helper -> {
            helper.startSequence()
                    .thenIdle(5)
                    .thenExecute(() -> helper.pullLever(leverPos))
                    .thenIdle(5)
                    .thenSucceed();
        });
    }

    /**
     * Tests that entities can be stored and retrieved from spatial I/O without a player being present and loading the
     * chunks. <a href="https://github.com/AppliedEnergistics/Applied-Energistics-2/issues/6397">issue 6397</a>.
     */
    @TestPlot("spatial_entity_storage")
    public static void storeAndRetrieveEntities(PlotBuilder plot) {
        var chickenPos = new BlockPos(1, 1, 1);
        var ioPortPos = new BlockPos(-1, 0, 0);

        // Outer network
        plot.creativeEnergyCell("0 0 0");
        plot.block("[1,2] 0 0", AEBlocks.SPATIAL_PYLON);
        plot.block("0 [1,2] 0", AEBlocks.SPATIAL_PYLON);
        plot.block("0 0 [1,2]", AEBlocks.SPATIAL_PYLON);
        plot.block(chickenPos.below(), Blocks.STONE);
        plot.block(chickenPos.above(), Blocks.STONE);
        // Surround it with glass
        for (var i = 0; i < 4; i++) {
            var dir = Direction.from2DDataValue(i);
            plot.block(chickenPos.relative(dir), Blocks.GLASS);
            plot.block(chickenPos.relative(dir).above(), Blocks.GLASS);
        }
        plot.blockEntity(ioPortPos, AEBlocks.SPATIAL_IO_PORT, port -> {
            port.getInternalInventory().insertItem(0, AEItems.SPATIAL_CELL2.stack(), false);
        });
        var buttonPos = plot.buttonOn(ioPortPos, Direction.WEST);

        // Woosh!
        plot.test(helper -> {
            helper.startSequence()
                    .thenExecute(() -> {
                        // Remove all entities
                        // Spawn cow + chicken
                        helper.killAllEntities();
                        helper.spawn(EntityType.CHICKEN, chickenPos.above());
                        helper.spawnItem(Items.OBSIDIAN, chickenPos.getX() + .5f, chickenPos.getY() + .5f,
                                chickenPos.getZ() + .5f);
                    })
                    .thenIdle(5)
                    .thenExecute(() -> helper.pressButton(buttonPos))
                    .thenIdle(5)
                    .thenExecute(() -> {
                        // Validate, that the chicken and obsidian are gone
                        helper.assertItemEntityCountIs(Items.OBSIDIAN, chickenPos, 1, 0);
                        helper.assertEntitiesPresent(EntityType.CHICKEN, chickenPos, 0, 1);

                        // Swap the cell back to the input slot and trigger a transition
                        var cell = getCellFromSpatialIoPortOutput(helper, ioPortPos);
                        insertCell(helper, ioPortPos, cell);
                    })
                    // Wait for button to reset
                    .thenIdle(25)
                    // Transition back
                    .thenExecute(() -> helper.pressButton(buttonPos))
                    .thenIdle(5)
                    .thenExecute(() -> {
                        // Validate that the chicken and obsidian are back
                        helper.assertItemEntityCountIs(Items.OBSIDIAN, chickenPos, 1, 1);
                        helper.assertEntitiesPresent(EntityType.CHICKEN, chickenPos, 1, 1);
                    })
                    .thenSucceed();
        });
    }

    private static ItemStack getCellFromSpatialIoPortOutput(PlotTestHelper helper, BlockPos ioPortPos) {
        var spatialIoPort = (SpatialIOPortBlockEntity) helper.getBlockEntity(ioPortPos);
        var cell = spatialIoPort.getInternalInventory().extractItem(1, 1, false);
        helper.check(AEItems.SPATIAL_CELL2.isSameAs(cell), "no spatial cell in output slot", ioPortPos);
        return cell;
    }

    private static void insertCell(PlotTestHelper helper, BlockPos ioPortPos, ItemStack cell) {
        var spatialIoPort = (SpatialIOPortBlockEntity) helper.getBlockEntity(ioPortPos);
        spatialIoPort.getInternalInventory().insertItem(0, cell, false);
    }

    private static PlotInfo getPlotInfo(PlotTestHelper helper, BlockPos ioPortPos, ItemStack cell) {
        helper.check(cell.getItem() instanceof ISpatialStorageCell, "cell is not a spatial cell", ioPortPos);
        ISpatialStorageCell spatialCell = (ISpatialStorageCell) cell.getItem();
        var plotId = spatialCell.getAllocatedPlotId(cell);
        var plot = SpatialStoragePlotManager.INSTANCE.getPlot(plotId);
        helper.check(plot != null, "plot not found", ioPortPos);
        return new PlotInfo(
                SpatialStoragePlotManager.INSTANCE.getLevel(),
                new AABB(
                        Vec3.atLowerCornerOf(plot.getOrigin()),
                        Vec3.atLowerCornerOf(plot.getOrigin().offset(plot.getSize()))),
                plot.getOrigin());
    }

    record PlotInfo(ServerLevel level, AABB bounds, BlockPos origin) {
    }
}
