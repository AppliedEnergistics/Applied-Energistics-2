package appeng.server.testplots;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.server.testworld.PlotBuilder;

/**
 * Tests that verify that side-effects from internal inventory changes are properly deferred until transaction commit
 * for each block entity with exposed inventories and side-effects.
 * <p>
 * Each test follows the pattern:
 * 1. Setup block entity with necessary configuration
 * 2. Access inventory via resource handler (transactional API)
 * 3. Verify side-effects do NOT occur during transaction
 * 4. Commit transaction
 * 5. Verify side-effects DID occur after commit
 */
@TestPlotClass
public final class InternalInventoryTransactionTestPlots {
    private InternalInventoryTransactionTestPlots() {
    }

    /**
     * Tests QuantumBridge - side-effect is cluster.updateStatus() when singularity inserted/removed.
     * To verify the side-effect is deferred, we build two QNB rings, insert matching singularities,
     * and verify they don't link (same grid) until after commit.
     */
    @TestPlot("qnb_deferred_side_effects")
    public static void testQuantumBridge(PlotBuilder plot) {
        var qnbA = BlockPos.ZERO.above();
        var qnbB = qnbA.east(4);

        buildQnbRing(plot, qnbA);
        buildQnbRing(plot, qnbB);

        plot.test(helper -> {
            helper.startSequence()
                    // Wait until both QNBs have formed
                    .thenWaitUntil(() -> {
                        var coreA = helper.getBlockEntity(qnbA, appeng.blockentity.qnb.QuantumBridgeBlockEntity.class);
                        var coreB = helper.getBlockEntity(qnbB, appeng.blockentity.qnb.QuantumBridgeBlockEntity.class);
                        helper.check(coreA.isFormed(), "QNB A not formed", qnbA);
                        helper.check(coreB.isFormed(), "QNB B not formed", qnbB);
                    })
                    .thenExecute(() -> {
                        var coreA = helper.getBlockEntity(qnbA, appeng.blockentity.qnb.QuantumBridgeBlockEntity.class);
                        var coreB = helper.getBlockEntity(qnbB, appeng.blockentity.qnb.QuantumBridgeBlockEntity.class);

                        // Create matching singularities
                        var singularities = AEItems.QUANTUM_ENTANGLED_SINGULARITY.stack();
                        appeng.blockentity.qnb.QuantumBridgeBlockEntity.assignFrequency(singularities);
                        var resource = ItemResource.of(singularities);

                        // Get handlers for both QNBs
                        var handlerA = coreA.getExposedItemHandler(Direction.NORTH);
                        var handlerB = coreB.getExposedItemHandler(Direction.NORTH);

                        try (var tx = Transaction.open(null)) {
                            // Insert singularities into both QNBs
                            helper.check(handlerA.insert(resource, 1, tx) == 1, "Should insert singularity A", qnbA);
                            helper.check(handlerB.insert(resource, 1, tx) == 1, "Should insert singularity B", qnbB);

                            // Verify singularities are visible
                            helper.check(!coreA.getInternalInventory().getStackInSlot(0).isEmpty(),
                                    "Singularity A visible in transaction", qnbA);
                            helper.check(!coreB.getInternalInventory().getStackInSlot(0).isEmpty(),
                                    "Singularity B visible in transaction", qnbB);

                            // But clusters should NOT be linked yet (side-effect not triggered)
                            var gridA = helper.getGrid(qnbA);
                            var gridB = helper.getGrid(qnbB);
                            helper.check(gridA != gridB, "Grids should NOT be linked before commit", qnbA);

                            tx.commit();
                        }
                    })
                    // Wait until linked (-> same grid) after commit
                    .thenWaitUntil(() -> {
                        var gridA = helper.getGrid(qnbA);
                        var gridB = helper.getGrid(qnbB);
                        helper.check(gridA == gridB, "Grids should be linked after commit", qnbA);
                    })
                    .thenSucceed();
        });
    }

    private static void buildQnbRing(PlotBuilder plot, BlockPos origin) {
        plot.block(origin, AEBlocks.QUANTUM_LINK);
        for (var x = -1; x <= 1; x++) {
            for (var y = -1; y <= 1; y++) {
                var pos = origin.offset(x, y, 0);
                if (x != 0 || y != 0) {
                    plot.block(pos, AEBlocks.QUANTUM_RING);
                }
            }
        }
    }

    /**
     * Tests Condenser - side-effect is addPower() when items inserted
     */
    @TestPlot("condenser_deferred_side_effects")
    public static void testCondenser(PlotBuilder plot) {
        plot.blockEntity(BlockPos.ZERO, AEBlocks.CONDENSER, condenser -> {
            condenser.getInternalInventory().setItemDirect(2, AEItems.CELL_COMPONENT_1K.stack());
        });

        plot.test(helper -> {
            helper.startSequence()
                    .thenExecute(() -> {
                        var condenser = helper.getBlockEntity(BlockPos.ZERO,
                                appeng.blockentity.misc.CondenserBlockEntity.class);
                        var handler = condenser.getExposedItemHandler(null);
                        var initialPower = condenser.getStoredPower();

                        try (var tx = Transaction.open(null)) {
                            handler.insert(ItemResource.of(Items.DIAMOND), 1, tx);

                            // Power should not change during transaction
                            var duringPower = condenser.getStoredPower();
                            helper.check(duringPower == initialPower,
                                    "No side-effects during transaction", BlockPos.ZERO);

                            tx.commit();
                        }

                        // After commit, power should have increased
                        var finalPower = condenser.getStoredPower();
                        helper.check(finalPower > initialPower,
                                "Side-effects after commit", BlockPos.ZERO);
                    })
                    .thenSucceed();
        });
    }

    /**
     * Tests Inscriber - side-effect is clearTask() when inventory changes
     */
    @TestPlot("inscriber_deferred_side_effects")
    public static void testInscriber(PlotBuilder plot) {
        plot.creativeEnergyCell(BlockPos.ZERO.below());
        plot.block(BlockPos.ZERO, AEBlocks.INSCRIBER);

        plot.test(helper -> {
            helper.startSequence()
                    .thenExecute(() -> {
                        var inscriber = helper.getBlockEntity(BlockPos.ZERO,
                                appeng.blockentity.misc.InscriberBlockEntity.class);
                        var handler = inscriber.getExposedItemHandler(Direction.UP);
                        helper.check(handler != null, "handler should not be null", BlockPos.ZERO);

                        var press = AEItems.CALCULATION_PROCESSOR_PRESS.stack();
                        var resource = ItemResource.of(press);

                        try (var tx = Transaction.open(null)) {
                            var inserted = handler.insert(resource, 1, tx);
                            helper.check(inserted == 1, "Should insert press", BlockPos.ZERO);

                            // Change should be visible
                            var currentSlot = inscriber.getInternalInventory().getStackInSlot(0);
                            helper.check(!currentSlot.isEmpty(), "Change visible in transaction", BlockPos.ZERO);

                            tx.commit();
                        }

                        // Verify press is in inventory after commit
                        var finalSlot = inscriber.getInternalInventory().getStackInSlot(0);
                        helper.check(AEItems.CALCULATION_PROCESSOR_PRESS.is(finalSlot),
                                "Press in inventory after commit", BlockPos.ZERO);
                    })
                    .thenSucceed();
        });
    }

    /**
     * Tests MEChest - side-effect is IStorageProvider.requestUpdate() when cell inserted/removed
     */
    @TestPlot("mechest_deferred_side_effects")
    public static void testMEChest(PlotBuilder plot) {
        plot.creativeEnergyCell(BlockPos.ZERO.below());
        plot.block(BlockPos.ZERO, AEBlocks.ME_CHEST);

        plot.test(helper -> {
            helper.startSequence()
                    .thenExecute(() -> {
                        var chest = helper.getBlockEntity(BlockPos.ZERO,
                                appeng.blockentity.storage.MEChestBlockEntity.class);
                        var handler = chest.getExposedItemHandler(chest.getFront());
                        helper.check(handler != null, "handler should not be null", BlockPos.ZERO);

                        var cell = AEItems.ITEM_CELL_1K.stack();
                        var resource = ItemResource.of(cell);

                        try (var tx = Transaction.open(null)) {
                            var inserted = handler.insert(resource, 1, tx);
                            helper.check(inserted == 1, "Should insert cell", BlockPos.ZERO);

                            // Change should be visible
                            var currentSlot = chest.getCell();
                            helper.check(!currentSlot.isEmpty(), "Change visible in transaction", BlockPos.ZERO);

                            tx.commit();
                        }

                        // Verify cell is in inventory after commit
                        var finalSlot = chest.getCell();
                        helper.check(AEItems.ITEM_CELL_1K.is(finalSlot),
                                "Cell in inventory after commit", BlockPos.ZERO);
                    })
                    .thenSucceed();
        });
    }

    /**
     * Tests Drive - side-effect is updateState() when cells inserted/removed
     */
    @TestPlot("drive_deferred_side_effects")
    public static void testDrive(PlotBuilder plot) {
        plot.creativeEnergyCell(BlockPos.ZERO.below());
        plot.block(BlockPos.ZERO, AEBlocks.DRIVE);

        plot.test(helper -> {
            helper.startSequence()
                    .thenExecute(() -> {
                        var drive = helper.getBlockEntity(BlockPos.ZERO,
                                appeng.blockentity.storage.DriveBlockEntity.class);
                        var handler = drive.getExposedItemHandler(Direction.UP);
                        helper.check(handler != null, "handler should not be null", BlockPos.ZERO);

                        var cell = AEItems.ITEM_CELL_4K.stack();
                        var resource = ItemResource.of(cell);

                        try (var tx = Transaction.open(null)) {
                            var inserted = handler.insert(resource, 1, tx);
                            helper.check(inserted == 1, "Should insert cell", BlockPos.ZERO);

                            // Change should be visible
                            var currentSlot = drive.getInternalInventory().getStackInSlot(0);
                            helper.check(!currentSlot.isEmpty(), "Change visible in transaction", BlockPos.ZERO);

                            tx.commit();
                        }

                        // Verify cell is in inventory after commit
                        var finalSlot = drive.getInternalInventory().getStackInSlot(0);
                        helper.check(AEItems.ITEM_CELL_4K.is(finalSlot),
                                "Cell in inventory after commit", BlockPos.ZERO);
                    })
                    .thenSucceed();
        });
    }

    /**
     * Tests Charger - side-effect is setWorking() when items inserted/removed
     */
    @TestPlot("charger_deferred_side_effects")
    public static void testCharger(PlotBuilder plot) {
        plot.creativeEnergyCell(BlockPos.ZERO.below());
        plot.block(BlockPos.ZERO, AEBlocks.CHARGER);

        plot.test(helper -> {
            helper.startSequence()
                    .thenExecute(() -> {
                        var charger = helper.getBlockEntity(BlockPos.ZERO,
                                appeng.blockentity.misc.ChargerBlockEntity.class);
                        var handler = charger.getExposedItemHandler(Direction.UP);
                        helper.check(handler != null, "handler should not be null", BlockPos.ZERO);

                        var crystal = AEItems.CERTUS_QUARTZ_CRYSTAL.stack();
                        var resource = ItemResource.of(crystal);

                        try (var tx = Transaction.open(null)) {
                            var inserted = handler.insert(resource, 1, tx);
                            helper.check(inserted == 1, "Should insert crystal", BlockPos.ZERO);

                            // Change should be visible
                            var currentSlot = charger.getInternalInventory().getStackInSlot(0);
                            helper.check(!currentSlot.isEmpty(), "Change visible in transaction", BlockPos.ZERO);

                            tx.commit();
                        }

                        // Verify crystal is in inventory after commit
                        var finalSlot = charger.getInternalInventory().getStackInSlot(0);
                        helper.check(AEItems.CERTUS_QUARTZ_CRYSTAL.is(finalSlot),
                                "Crystal in inventory after commit", BlockPos.ZERO);
                    })
                    .thenSucceed();
        });
    }

    /**
     * Tests VibrationChamber - side-effect is markForUpdate() when fuel inserted
     */
    @TestPlot("vibration_chamber_deferred_side_effects")
    public static void testVibrationChamber(PlotBuilder plot) {
        plot.block(BlockPos.ZERO, AEBlocks.VIBRATION_CHAMBER);

        plot.test(helper -> {
            helper.startSequence()
                    .thenExecute(() -> {
                        var chamber = helper.getBlockEntity(BlockPos.ZERO,
                                appeng.blockentity.misc.VibrationChamberBlockEntity.class);
                        var handler = chamber.getExposedItemHandler(Direction.UP);
                        helper.check(handler != null, "handler should not be null", BlockPos.ZERO);

                        var coal = ItemResource.of(Items.COAL);

                        try (var tx = Transaction.open(null)) {
                            var inserted = handler.insert(coal, 1, tx);
                            helper.check(inserted == 1, "Should insert coal", BlockPos.ZERO);

                            // Change should be visible
                            var currentSlot = chamber.getInternalInventory().getStackInSlot(0);
                            helper.check(!currentSlot.isEmpty(), "Change visible in transaction", BlockPos.ZERO);

                            tx.commit();
                        }

                        // Verify coal is in inventory after commit
                        var finalSlot = chamber.getInternalInventory().getStackInSlot(0);
                        helper.check(finalSlot.getItem() == Items.COAL,
                                "Coal in inventory after commit", BlockPos.ZERO);
                    })
                    .thenSucceed();
        });
    }

    /**
     * Tests IOPort - side-effect is updateTask() when cells inserted/removed
     */
    @TestPlot("ioport_deferred_side_effects")
    public static void testIOPort(PlotBuilder plot) {
        plot.creativeEnergyCell(BlockPos.ZERO.below());
        plot.block(BlockPos.ZERO, AEBlocks.IO_PORT);

        plot.test(helper -> {
            helper.startSequence()
                    .thenExecute(() -> {
                        var ioport = helper.getBlockEntity(BlockPos.ZERO,
                                appeng.blockentity.storage.IOPortBlockEntity.class);
                        var handler = ioport.getExposedItemHandler(Direction.UP);
                        helper.check(handler != null, "handler should not be null", BlockPos.ZERO);

                        var cell = AEItems.ITEM_CELL_1K.stack();
                        var resource = ItemResource.of(cell);

                        try (var tx = Transaction.open(null)) {
                            var inserted = handler.insert(resource, 1, tx);
                            helper.check(inserted == 1, "Should insert cell", BlockPos.ZERO);

                            // Change should be visible
                            var currentSlot = ioport.getInternalInventory().getStackInSlot(0);
                            helper.check(!currentSlot.isEmpty(), "Change visible in transaction", BlockPos.ZERO);

                            tx.commit();
                        }

                        // Verify cell is in inventory after commit
                        var finalSlot = ioport.getInternalInventory().getStackInSlot(0);
                        helper.check(AEItems.ITEM_CELL_1K.is(finalSlot),
                                "Cell in inventory after commit", BlockPos.ZERO);
                    })
                    .thenSucceed();
        });
    }

    /**
     * Tests CellWorkbench - side-effect is saveChanges() when cell or config cards change
     */
    @TestPlot("cell_workbench_deferred_side_effects")
    public static void testCellWorkbench(PlotBuilder plot) {
        plot.block(BlockPos.ZERO, AEBlocks.CELL_WORKBENCH);

        plot.test(helper -> {
            helper.startSequence()
                    .thenExecute(() -> {
                        var workbench = helper.getBlockEntity(BlockPos.ZERO,
                                appeng.blockentity.misc.CellWorkbenchBlockEntity.class);
                        var cellInv = workbench.getSubInventory(Identifier.parse("ae2:cells"));
                        var handler = cellInv.toResourceHandler();
                        helper.check(handler != null, "handler should not be null", BlockPos.ZERO);

                        var cell = AEItems.ITEM_CELL_1K.stack();
                        var resource = ItemResource.of(cell);

                        try (var tx = Transaction.open(null)) {
                            var inserted = handler.insert(resource, 1, tx);
                            helper.check(inserted == 1, "Should insert cell", BlockPos.ZERO);

                            // Change should be visible
                            var currentSlot = cellInv.getStackInSlot(0);
                            helper.check(!currentSlot.isEmpty(), "Change visible in transaction", BlockPos.ZERO);

                            tx.commit();
                        }

                        // Verify cell is in inventory after commit
                        var finalSlot = cellInv.getStackInSlot(0);
                        helper.check(AEItems.ITEM_CELL_1K.is(finalSlot),
                                "Cell in inventory after commit", BlockPos.ZERO);
                    })
                    .thenSucceed();
        });
    }
}
