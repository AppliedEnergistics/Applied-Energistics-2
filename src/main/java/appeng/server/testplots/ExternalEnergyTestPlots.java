package appeng.server.testplots;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import appeng.api.stacks.AEItemKey;
import appeng.block.misc.GrowthAcceleratorBlock;
import appeng.blockentity.misc.GrowthAcceleratorBlockEntity;
import appeng.blockentity.storage.ChestBlockEntity;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.AEParts;
import appeng.server.testworld.PlotBuilder;
import appeng.server.testworld.PlotTestHelper;

@TestPlotClass
public class ExternalEnergyTestPlots {
    private static final BlockPos ORIGIN = BlockPos.ZERO;

    @TestPlot("fe_energy_acceptor_block")
    public static void testEnergyAcceptorBlock(PlotBuilder plot) {
        placeForgeEnergyGenerator(plot);
        plot.block(ORIGIN, AEBlocks.ENERGY_ACCEPTOR);
        testGridIsReceivingEnergy(plot);
    }

    @TestPlot("fe_energy_acceptor_part")
    public static void testEnergyAcceptorPart(PlotBuilder plot) {
        placeForgeEnergyGenerator(plot);
        plot.cable(ORIGIN).part(Direction.DOWN, AEParts.ENERGY_ACCEPTOR);
        testGridIsReceivingEnergy(plot);
    }

    @TestPlot("fe_controller")
    public static void testController(PlotBuilder plot) {
        placeForgeEnergyGenerator(plot);
        plot.block(ORIGIN, AEBlocks.CONTROLLER);
        testGridIsReceivingEnergy(plot);
    }

    @TestPlot("fe_inscriber")
    public static void testInscriber(PlotBuilder plot) {
        placeForgeEnergyGenerator(plot);
        plot.blockEntity(ORIGIN, AEBlocks.INSCRIBER, inscriber -> {
            inscriber.getInternalInventory().insertItem(0, AEItems.SILICON_PRESS.stack(), false);
            inscriber.getInternalInventory().insertItem(2, AEItems.SILICON.stack(), false);
        });
        plot.test(helper -> helper.startSequence()
                .thenWaitUntil(helper::checkAllInitialized)
                .thenWaitUntil(() -> {
                    var content = helper.countContainerContentAt(ORIGIN);
                    helper.assertEquals(ORIGIN, 1L, content.get(AEItemKey.of(AEItems.SILICON_PRINT)));
                })
                // Ensure that after 1 second, the grid still has no energy
                .thenExecuteAfter(20, () -> checkGridHasNoEnergy(helper))
                .thenSucceed());
    }

    @TestPlot("fe_chest")
    public static void testChest(PlotBuilder plot) {
        placeForgeEnergyGenerator(plot);
        plot.blockEntity(ORIGIN, AEBlocks.CHEST, chest -> {
            chest.getInternalInventory().addItems(AEItems.ITEM_CELL_1K.stack());
        });
        plot.test(helper -> helper.startSequence()
                .thenWaitUntil(helper::checkAllInitialized)
                .thenWaitUntil(() -> {
                    var chest = (ChestBlockEntity) helper.getBlockEntity(ORIGIN);
                    helper.check(chest.isPowered(), "should be powered", ORIGIN);
                    var linkStatus = chest.getLinkStatus();
                    helper.check(linkStatus.connected(), "link status should be connected: "
                            + linkStatus.statusDescription(), ORIGIN);
                })
                // Ensure that after 1 second, the grid still has no energy
                .thenExecuteAfter(20, () -> checkGridHasNoEnergy(helper))
                .thenSucceed());
    }

    @TestPlot("fe_charger")
    public static void testCharger(PlotBuilder plot) {
        placeForgeEnergyGenerator(plot);
        // Insert an uncharged crystal to test it gets charged
        plot.blockEntity(ORIGIN, AEBlocks.CHARGER, charger -> {
            charger.getInternalInventory().insertItem(0, AEItems.CERTUS_QUARTZ_CRYSTAL.stack(), false);
        });
        plot.test(helper -> helper.startSequence()
                .thenWaitUntil(helper::checkAllInitialized)
                .thenWaitUntil(() -> {
                    var content = helper.countContainerContentAt(ORIGIN);
                    helper.assertEquals(ORIGIN, 1L, content.get(AEItemKey.of(AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED)));
                })
                // Ensure that after 1 second, the grid still has no energy
                .thenExecuteAfter(20, () -> checkGridHasNoEnergy(helper))
                .thenSucceed())
                // due to the chargers randomness, this can take longer than the default time
                .maxTicks(15 * 20);
    }

    @TestPlot("fe_growth_accelerator")
    public static void testGrowthAccelerator(PlotBuilder plot) {
        placeForgeEnergyGenerator(plot);
        plot.blockState(ORIGIN, AEBlocks.GROWTH_ACCELERATOR.block().defaultBlockState()
                .setValue(BlockStateProperties.FACING, Direction.UP));
        plot.test(helper -> helper.startSequence()
                .thenWaitUntil(helper::checkAllInitialized)
                .thenWaitUntil(() -> {
                    var accel = (GrowthAcceleratorBlockEntity) helper.getBlockEntity(ORIGIN);
                    helper.check(accel.isPowered(), "should be powered", ORIGIN);
                })
                .thenWaitUntil(() -> helper.assertBlockProperty(ORIGIN, GrowthAcceleratorBlock.POWERED, true))
                // Ensure that after 1 second, the grid still has no energy
                .thenExecuteAfter(20, () -> checkGridHasNoEnergy(helper))
                .thenSucceed());
    }

    private static void testGridIsReceivingEnergy(PlotBuilder plot) {
        plot.test(helper -> helper.startSequence()
                .thenWaitUntil(helper::checkAllInitialized)
                .thenWaitUntil(() -> {
                    var grid = helper.getGrid(BlockPos.ZERO);
                    var power = grid.getEnergyService().getStoredPower();
                    helper.check(power > 0, "grid should contain energy", ORIGIN);
                })
                .thenSucceed());
    }

    private static void checkGridHasNoEnergy(PlotTestHelper helper) {
        var grid = helper.getGrid(BlockPos.ZERO);
        var power = grid.getEnergyService().getStoredPower();
        helper.check(power == 0, "grid should not contain energy (has " + power + ")", ORIGIN);
    }

    private static void placeForgeEnergyGenerator(PlotBuilder plot) {
        plot.blockEntity(ORIGIN.below(), AEBlocks.DEBUG_ENERGY_GEN, generator -> {
            generator.setGenerationRate(128);
        });
    }
}
