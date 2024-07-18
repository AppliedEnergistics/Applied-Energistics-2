package appeng.server.testplots;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Items;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.networking.storage.IStorageService;
import appeng.api.stacks.AEItemKey;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEParts;
import appeng.me.service.EnergyService;
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

    /**
     * Creates three grids of different energy storage capacity. Test that all are discoveable in the overlay energy
     * grid.
     */
    @TestPlot("energy_overlay")
    public static void energy_overlay(PlotBuilder plot) {
        var origin = BlockPos.ZERO;

        plot.cable(origin).part(Direction.EAST, AEParts.QUARTZ_FIBER);
        plot.block(origin.east(), AEBlocks.DENSE_ENERGY_CELL);
        plot.block(origin.west(), AEBlocks.ENERGY_CELL);
        plot.cable(origin.west().west()).part(Direction.EAST, AEParts.QUARTZ_FIBER);

        plot.test(helper -> {
            helper.startSequence()
                    .thenWaitUntil(() -> helper.getGrid(origin))
                    .thenExecute(() -> {
                        var denseCellGrid = helper.getGrid(origin.east());
                        var cellGrid = helper.getGrid(origin.west());
                        var noCellGrid = helper.getGrid(origin.west().west());

                        var denseCellService = (EnergyService) denseCellGrid.getService(IEnergyService.class);
                        var cellService = (EnergyService) cellGrid.getService(IEnergyService.class);
                        var noCellService = (EnergyService) noCellGrid.getService(IEnergyService.class);

                        // Inject power into each of the three grids. It should always end up in the dense
                        // cell grid, due to prioritizing grids with high storage.
                        denseCellService.injectPower(1, Actionable.MODULATE);
                        helper.check(getLocalStoredPower(denseCellService) == 1, "expect power = 1", origin.east());
                        cellService.injectPower(1, Actionable.MODULATE);
                        helper.check(getLocalStoredPower(denseCellService) == 2, "expect power = 2", origin.east());
                        noCellService.injectPower(1, Actionable.MODULATE);
                        helper.check(getLocalStoredPower(denseCellService) == 3, "expect power = 3", origin.east());

                        // Extract power from each of the three grids. It should always end up in the dense
                        // cell grid, due to prioritizing grids with high storage.
                        denseCellService.extractAEPower(1, Actionable.MODULATE, PowerMultiplier.ONE);
                        helper.check(getLocalStoredPower(denseCellService) == 2, "expect power = 2", origin.east());
                        cellService.extractAEPower(1, Actionable.MODULATE, PowerMultiplier.ONE);
                        helper.check(getLocalStoredPower(denseCellService) == 1, "expect power = 1", origin.east());
                        noCellService.extractAEPower(1, Actionable.MODULATE, PowerMultiplier.ONE);
                        helper.check(getLocalStoredPower(denseCellService) == 0, "expect power = 0", origin.east());
                    })
                    .thenSucceed();
        });
    }

    private static double getLocalStoredPower(EnergyService service) {
        service.refreshPower();
        return service.getStoredPower();
    }

}
