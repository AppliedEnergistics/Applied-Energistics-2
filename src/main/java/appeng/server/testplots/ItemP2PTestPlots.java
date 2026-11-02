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
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEParts;
import appeng.parts.AEBasePart;
import appeng.parts.p2p.ItemP2PTunnelPart;
import appeng.server.testworld.PlotBuilder;

@TestPlotClass
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

        plot.test(helper -> helper
                .startSequence()
                .thenWaitUntil(() -> helper.assertContainerContains(chestPos, Items.BEDROCK))
                .thenSucceed());
    }

    /**
     * Inserting fewer items than there are outputs squared used to leave part of the remainder undistributed, so the
     * tunnel accepted less than it could have (e.g. only 6 of 8 items with 3 outputs).
     */
    @TestPlot("p2p_items_distributes_remainder")
    public static void itemDistributesRemainder(PlotBuilder plot) {
        var origin = BlockPos.ZERO;
        plot.creativeEnergyCell(origin.below());
        plot.cable(origin).part(Direction.WEST, AEParts.ITEM_P2P_TUNNEL);
        List<BlockPos> outputPositions = new ArrayList<>();
        for (var i = 1; i <= 3; i++) {
            var p = origin.east(i);
            plot.cable(p).part(Direction.DOWN, AEParts.ITEM_P2P_TUNNEL);
            plot.chest(p.below());
            outputPositions.add(p);
        }

        plot.afterGridInitAt(origin, (grid, gridNode) -> {
            var absOrigin = ((AEBasePart) gridNode.getOwner()).getBlockEntity().getBlockPos();
            linkTunnels(grid,
                    PosAndSide.west(absOrigin),
                    outputPositions.stream().map(p -> PosAndSide.down(p.offset(absOrigin))).toList());
        });

        plot.test(helper -> helper
                .startSequence()
                .thenWaitUntil(() -> {
                    var input = helper.getPart(origin, Direction.WEST, ItemP2PTunnelPart.class);
                    helper.check(input.isActive() && input.getOutputs().size() == 3, "tunnels are not linked yet");
                })
                .thenExecute(() -> {
                    var handler = helper.getCapability(origin, Capabilities.Item.BLOCK, Direction.WEST);
                    try (var tx = Transaction.open(null)) {
                        var inserted = handler.insert(ItemResource.of(Items.DIAMOND), 8, tx);
                        helper.check(inserted == 8, "expected all 8 diamonds to be inserted, but got " + inserted);
                        tx.commit();
                    }

                    var total = 0;
                    for (var p : outputPositions) {
                        total += helper.getBlockEntity(p.below(), ChestBlockEntity.class).countItem(Items.DIAMOND);
                    }
                    helper.check(total == 8, "expected 8 diamonds across the output chests, but got " + total);
                })
                .thenSucceed());
    }

    @TestPlot("p2p_recursive_item")
    public static void recursiveItemP2P(PlotBuilder plot) {
        var origin = BlockPos.ZERO;

        plot.block(origin, AEBlocks.DEBUG_ITEM_GEN);
        plot.creativeEnergyCell(origin.south().above().above());
        var curPos = origin.south();
        for (var i = 0; i < 5; i++) {
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
