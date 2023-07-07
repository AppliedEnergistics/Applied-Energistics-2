package appeng.server.testplots;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Items;

import appeng.api.config.Actionable;
import appeng.api.networking.storage.IStorageService;
import appeng.api.stacks.AEItemKey;
import appeng.core.definitions.AEParts;
import appeng.server.testworld.PlotBuilder;

/**
 * Test plot that sets up a working area for working on Guidebook structures.
 */
public final class SubnetPlots {

    private static final AEItemKey STICK = AEItemKey.of(Items.STICK);

    private SubnetPlots() {
    }

    @TestPlot("subnet")
    public static void subnet(PlotBuilder plot) {
        var origin = BlockPos.ZERO;
        plot.creativeEnergyCell(origin.below());
        ;
        plot.cable(origin)
                .part(Direction.NORTH, AEParts.TERMINAL)
                .part(Direction.SOUTH, AEParts.STORAGE_BUS)
                .part(Direction.EAST, AEParts.QUARTZ_FIBER);
        plot.cable(origin.east());
        plot.cable(origin.east().south());
        plot.cable(origin.south())
                .part(Direction.NORTH, AEParts.INTERFACE);
        plot.storageDrive(origin.south().above());

        var subNetPos = origin.south();
        var mainNetPos = origin;

        plot.test(helper -> {
            helper.startSequence()
                    .thenWaitUntil(() -> helper.getGrid(subNetPos))
                    .thenWaitUntil(() -> helper.getGrid(mainNetPos))
                    .thenExecute(() -> {
                        var mainGrid = helper.getGrid(mainNetPos);
                        var storageService = mainGrid.getService(IStorageService.class);
                        var inserted = storageService.getInventory().insert(
                                STICK,
                                1,
                                Actionable.MODULATE,
                                null);
                        helper.check(inserted == 1, "inserted != 1: " + inserted, mainNetPos);

                        // Check if it's retrievable
                        var inventory = storageService.getInventory().getAvailableStacks();
                        helper.check(inventory.get(STICK) == 1, "stick not present", mainNetPos);
                    })
                    .thenIdle(10)
                    .thenExecute(() -> {
                        // Check again if it's retrievable
                        var mainGrid = helper.getGrid(mainNetPos);
                        var storageService = mainGrid.getService(IStorageService.class);
                        var inventory = storageService.getInventory().getAvailableStacks();
                        helper.check(inventory.get(STICK) == 1, "stick not present in tick #10", mainNetPos);

                        // try to extract it
                        var extracted = storageService.getInventory().extract(STICK, 1, Actionable.MODULATE, null);
                        helper.check(extracted == 1, "unable to extract", mainNetPos);
                    })
                    .thenSucceed();

        });
    }
}
