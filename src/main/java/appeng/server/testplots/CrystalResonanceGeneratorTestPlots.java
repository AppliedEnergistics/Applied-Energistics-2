package appeng.server.testplots;

import java.util.function.Consumer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import appeng.blockentity.networking.CrystalResonanceGeneratorBlockEntity;
import appeng.blockentity.networking.EnergyCellBlockEntity;
import appeng.core.AEConfig;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEParts;
import appeng.server.testworld.PlotBuilder;
import appeng.server.testworld.PlotTestHelper;

public class CrystalResonanceGeneratorTestPlots {
    @TestPlot("crg_charges_cell")
    public static void chargesCell(PlotBuilder plot) {
        var origin = BlockPos.ZERO;
        plot.block(origin, AEBlocks.ENERGY_CELL);
        plot.blockState(origin.above(),
                AEBlocks.CRYSTAL_RESONANCE_GENERATOR.block()
                        .defaultBlockState()
                        .setValue(BlockStateProperties.FACING, Direction.UP));
        testPassiveGenerationRate(plot, origin);
    }

    @TestPlot("crg_suppression_generation")
    public static void suppression(PlotBuilder plot) {
        var origin = BlockPos.ZERO;
        setupSubnetWithCrgs(plot, origin);
        testPassiveGenerationRate(plot, origin);
    }

    @TestPlot("crg_suppression_flag")
    public static void suppressionFlag(PlotBuilder plot) {
        var origin = BlockPos.ZERO;
        setupSubnetWithCrgs(plot, origin);

        plot.test(helper -> helper.startSequence()
                .thenWaitUntil(helper::checkAllInitialized)
                .thenExecute(() -> {
                    // one of the two CRGs should be marked as suppressed
                    var crg1 = (CrystalResonanceGeneratorBlockEntity) helper.getBlockEntity(origin.above());
                    var crg2 = (CrystalResonanceGeneratorBlockEntity) helper.getBlockEntity(origin.east().east());

                    if (crg1.isSuppressed() && crg2.isSuppressed()) {
                        helper.check(false, "not both should be suppressed", helper.relativePos(crg1.getBlockPos()));
                    } else if (!crg1.isSuppressed() && !crg2.isSuppressed()) {
                        helper.check(false, "one of both should be suppressed", helper.relativePos(crg1.getBlockPos()));
                    }

                })
                .thenSucceed());
    }

    private static void setupSubnetWithCrgs(PlotBuilder plot, BlockPos origin) {
        plot.block(origin, AEBlocks.ENERGY_CELL);
        plot.blockState(origin.above(),
                AEBlocks.CRYSTAL_RESONANCE_GENERATOR.block()
                        .defaultBlockState()
                        .setValue(BlockStateProperties.FACING, Direction.UP));
        plot.cable(origin.east())
                .part(Direction.EAST, AEParts.QUARTZ_FIBER);
        plot.blockState(origin.east().east(),
                AEBlocks.CRYSTAL_RESONANCE_GENERATOR.block()
                        .defaultBlockState()
                        .setValue(BlockStateProperties.FACING, Direction.EAST));
    }

    private static void testPassiveGenerationRate(PlotBuilder plot, BlockPos origin) {
        var rate = AEConfig.instance().getCrystalResonanceGeneratorRate();
        plot.test(new Consumer<>() {
            double lastPower;
            EnergyCellBlockEntity cell;

            @Override
            public void accept(PlotTestHelper helper) {
                helper.startSequence()
                        .thenWaitUntil(helper::checkAllInitialized)
                        .thenExecute(() -> {
                            cell = (EnergyCellBlockEntity) helper.getBlockEntity(origin);
                            lastPower = cell.getAECurrentPower();
                        })
                        .thenExecuteAfter(1, () -> {
                            var now = cell.getAECurrentPower();
                            var expected = lastPower + rate;
                            helper.check(
                                    Math.abs(now - expected) < 0.01,
                                    "Expected " + expected + " AE, but has " + now,
                                    origin);
                            lastPower = now;
                        })
                        .thenExecuteAfter(1, () -> {
                            var now = cell.getAECurrentPower();
                            var expected = lastPower + rate;
                            helper.check(
                                    Math.abs(now - expected) < 0.01,
                                    "Expected " + expected + " AE, but has " + now,
                                    origin);
                        })
                        .thenSucceed();
            }
        });
    }
}
