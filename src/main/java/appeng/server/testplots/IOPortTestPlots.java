package appeng.server.testplots;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.HopperBlockEntity;

import appeng.blockentity.storage.IOPortBlockEntity;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.server.testworld.PlotBuilder;

@TestPlotClass
public class IOPortTestPlots {

    /**
     * Tests that the input faces of the ME IO Port only accept storage cells. Regression test for
     * https://github.com/AppliedEnergistics/Applied-Energistics-2/issues/8879
     */
    @TestPlot("io_port_only_accepts_cells_as_input")
    public static void ioPortOnlyAcceptsCellsAsInput(PlotBuilder plot) {
        var o = BlockPos.ZERO;
        plot.block(o, AEBlocks.IO_PORT);
        // The hopper tries to push the cobblestone first, then the cell
        plot.hopper(o.above(), Direction.DOWN, Items.COBBLESTONE, AEItems.ITEM_CELL_1K);
        plot.test(helper -> {
            helper.startSequence()
                    // The IO port is not on a grid, so the cell will remain in its input slots
                    .thenWaitUntil(() -> {
                        var ioPort = helper.getBlockEntity(o, IOPortBlockEntity.class);
                        helper.check(countItem(ioPort, AEItems.ITEM_CELL_1K.asItem()) == 1,
                                "cell should have been inserted into the IO port");
                    })
                    .thenExecute(() -> {
                        var ioPort = helper.getBlockEntity(o, IOPortBlockEntity.class);
                        helper.check(countItem(ioPort, Items.COBBLESTONE) == 0,
                                "cobblestone should not have been inserted into the IO port");
                        var hopper = helper.getBlockEntity(o.above(), HopperBlockEntity.class);
                        helper.check(hopper.countItem(Items.COBBLESTONE) == 1,
                                "cobblestone should have remained in the hopper");
                    })
                    .thenSucceed();
        });
    }

    private static int countItem(IOPortBlockEntity ioPort, Item item) {
        var count = 0;
        for (var stack : ioPort.getInternalInventory()) {
            if (stack.is(item)) {
                count += stack.getCount();
            }
        }
        return count;
    }
}
