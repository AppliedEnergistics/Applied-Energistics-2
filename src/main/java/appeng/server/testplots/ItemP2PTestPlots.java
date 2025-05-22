package appeng.server.testplots;

import static appeng.server.testplots.P2PPlotHelper.linkTunnels;
import static appeng.server.testplots.P2PPlotHelper.placeTunnel;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEParts;
import appeng.parts.AEBasePart;
import appeng.server.testworld.PlotBuilder;

public class ItemP2PTestPlots {

    @TestPlot("p2p_items")
    public static void item(PlotBuilder plot) {
        var origin = BlockPos.ZERO;
        placeTunnel(plot, AEParts.ITEM_P2P_TUNNEL);

        // Hopper pointing into the input P2P
        plot.hopper(origin.west().west(), Direction.EAST, new ItemStack(Items.BEDROCK));
        // Chest adjacent to output
        var chestPos = origin.east().east();
        plot.chest(chestPos);

        plot.test(helper -> {
            helper.succeedWhen(() -> {
                helper.assertContainerContains(chestPos, Items.BEDROCK);
            });
        });
    }

    @TestPlot("p2p_recursive_item")
    public static void recursiveItemP2P(PlotBuilder plot) {
        var origin = BlockPos.ZERO;

        plot.block(origin, AEBlocks.DEBUG_ITEM_GEN);
        plot.creativeEnergyCell(origin.south().above().above());
        var curPos = origin.south();
        for (var i = 0; i < 10; i++) {
            placeSubnet(plot, curPos);
            curPos = curPos.south(6);
        }

        plot.test(GameTestHelper::succeed);
    }

    private static void placeSubnet(PlotBuilder plot, BlockPos origin) {
        // Subnet consists of:
        // - 1 input item P2P
        // - 7 output item P2P to machines
        // - 1 output item P2P to next subnet
        // - "loop" to power it via energy subnetting
        List<PosAndSide> outputTunnels = new ArrayList<>();
        for (var i = 0; i < 6; i++) {
            var p = origin.relative(Direction.SOUTH, i);
            var cb = plot.cable(p);
            cb.part(Direction.DOWN, AEParts.ITEM_P2P_TUNNEL);
            outputTunnels.add(PosAndSide.down(p));
            boolean first = i == 0;
            boolean last = i + 1 >= 6;
            if (first) {
                cb.part(Direction.NORTH, AEParts.ITEM_P2P_TUNNEL);
            } else if (last) {
                cb.part(Direction.SOUTH, AEParts.ITEM_P2P_TUNNEL);
                outputTunnels.add(PosAndSide.south(p));
            }
            if (first || last) {
                cb.part(Direction.UP, AEParts.QUARTZ_FIBER);
            }

            plot.hopper(p.below(), Direction.DOWN);
            plot.block(p.below().below(), AEBlocks.CONDENSER); // Just void the hopper output
        }

        // Cables for connecting adjacent subnets for energy
        plot.cable(origin.above());
        plot.cable(origin.south(5).above());

        plot.afterGridInitAt(origin, (grid, gridNode) -> {
            var absOrigin = ((AEBasePart) gridNode.getOwner()).getBlockEntity().getBlockPos();
            var relativeOffset = absOrigin.offset(-origin.getX(), -origin.getY(), -origin.getZ());
            linkTunnels(grid,
                    PosAndSide.north(origin.offset(relativeOffset)),
                    outputTunnels.stream().map(p -> p.offset(relativeOffset)).toList());
        });
    }

}
