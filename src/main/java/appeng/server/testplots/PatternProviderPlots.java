package appeng.server.testplots;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Items;

import appeng.api.config.Actionable;
import appeng.api.stacks.AEItemKey;
import appeng.blockentity.crafting.PatternProviderBlockEntity;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEParts;
import appeng.server.testworld.PlotBuilder;

@TestPlotClass
public final class PatternProviderPlots {
    private PatternProviderPlots() {
    }

    /**
     * Tests that pattern providers pushing into each other do not cause an infinite loop on the server. Regression test
     * for <a href="https://github.com/AppliedEnergistics/Applied-Energistics-2/issues/7590">issue 7590</a>.
     */
    @TestPlot("pattern_provider_loop")
    public static void patternProviderLoop(PlotBuilder plot) {
        plot.creativeEnergyCell("0 0 0");
        plot.cable("0 1 0");
        plot.block("1 0 0", AEBlocks.PATTERN_PROVIDER);
        plot.cable("1 1 0").part(Direction.DOWN, AEParts.STORAGE_BUS);
        plot.block("-1 0 0", AEBlocks.PATTERN_PROVIDER);
        plot.cable("-1 1 0").part(Direction.DOWN, AEParts.STORAGE_BUS);

        plot.test(helper -> {
            helper.startSequence()
                    .thenIdle(5)
                    .thenExecute(() -> {
                        var pp = (PatternProviderBlockEntity) helper.getBlockEntity(new BlockPos(1, 0, 0));
                        pp.getLogic().getReturnInv().insert(0, AEItemKey.of(Items.ANDESITE), 1, Actionable.MODULATE);
                    })
                    .thenIdle(5)
                    .thenSucceed();
        });
    }
}
