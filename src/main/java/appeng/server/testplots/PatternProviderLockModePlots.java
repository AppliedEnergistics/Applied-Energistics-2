package appeng.server.testplots;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import appeng.api.config.Actionable;
import appeng.api.config.LockCraftingMode;
import appeng.api.config.Settings;
import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.blockentity.crafting.PatternProviderBlockEntity;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEParts;
import appeng.helpers.iface.PatternProviderLogicHost;
import appeng.parts.crafting.PatternProviderPart;
import appeng.server.testworld.PlotBuilder;
import appeng.server.testworld.PlotTestHelper;
import appeng.server.testworld.SavedBlockEntity;

/**
 * All plots are essentially the same: PP on Chest, powered by Creative Energy Cell. Use of the PP is via API, not
 * in-game tools.
 */
public final class PatternProviderLockModePlots {
    private static final BlockPos LEVER_POS = BlockPos.ZERO.east();
    private static final BlockPos BUTTON_POS = BlockPos.ZERO.west();
    private static final GenericStack ONE_PLANK = new GenericStack(AEItemKey.of(Blocks.OAK_PLANKS), 1);
    private static final GenericStack TWO_PLANK = new GenericStack(AEItemKey.of(Blocks.OAK_PLANKS), 2);

    private PatternProviderLockModePlots() {
    }

    @TestPlot("pp_block_lockmode_pulse")
    public static void testBlockLockModePulse(PlotBuilder plot) {
        setup(plot, false, LockCraftingMode.LOCK_UNTIL_PULSE);

        testLockModePulse(plot);
    }

    @TestPlot("pp_part_lockmode_pulse")
    public static void testPartLockModePulse(PlotBuilder plot) {
        setup(plot, true, LockCraftingMode.LOCK_UNTIL_PULSE);

        testLockModePulse(plot);
    }

    private static void testLockModePulse(PlotBuilder plot) {
        plot.test(helper -> {
            var host = getHost(helper);
            var pp = host.getLogic();

            helper.startSequence()
                    .thenExecuteAfter(1, () -> {
                        // Initially it should be unlocked
                        helper.assertEquals(BlockPos.ZERO, LockCraftingMode.NONE, pp.getCraftingLockedReason());
                        // Pushing pattern should succeed
                        helper.check(pushPattern(host), "Pushing pattern failed");
                        // Now it should be immediately locked
                        helper.assertEquals(BlockPos.ZERO, LockCraftingMode.LOCK_UNTIL_PULSE,
                                pp.getCraftingLockedReason());
                        // Pushing another pattern should fail
                        helper.check(!pushPattern(host), "Pushing pattern should fail");
                    })
                    .thenExecuteAfter(1, () -> {
                        // Turn the lever on to trigger the pulse
                        helper.pullLever(LEVER_POS);

                        // That should immediately unlock the provider
                        helper.assertEquals(BlockPos.ZERO, LockCraftingMode.NONE, pp.getCraftingLockedReason());

                        // Pushing a pattern should succeed now
                        helper.check(pushPattern(host), "Pushing pattern failed");

                        // But that should lock it again, even though the signal is still high
                        helper.assertEquals(BlockPos.ZERO, LockCraftingMode.LOCK_UNTIL_PULSE,
                                pp.getCraftingLockedReason());
                        helper.check(!pushPattern(host), "Pushing pattern should fail");

                        // Turning the lever off should not trigger the pulse
                        helper.pullLever(LEVER_POS);
                        helper.assertEquals(BlockPos.ZERO, LockCraftingMode.LOCK_UNTIL_PULSE,
                                pp.getCraftingLockedReason());
                        helper.check(!pushPattern(host), "Pushing pattern should fail");
                    })
                    .thenExecuteAfter(1, () -> {
                        // Precondition is that it's still locked
                        helper.assertEquals(BlockPos.ZERO, LockCraftingMode.LOCK_UNTIL_PULSE,
                                pp.getCraftingLockedReason());
                        helper.check(!pushPattern(host), "Pushing pattern should fail");

                        // Trigger the button
                        helper.pressButton(BUTTON_POS);

                        // This should unlock it
                        helper.assertEquals(BlockPos.ZERO, LockCraftingMode.NONE, pp.getCraftingLockedReason());
                        helper.check(pushPattern(host), "Pushing pattern should succeed");
                    })
                    .thenExecuteAfter(1, () -> {
                        // Three pushes should have succeeded
                        var counter = helper.countContainerContentAt(BlockPos.ZERO.above());
                        helper.assertEquals(BlockPos.ZERO.above(), 3L, counter.get(AEItemKey.of(Blocks.OAK_LOG)));
                    })
                    .thenSucceed();
        });
    }

    @TestPlot("pp_block_lockmode_high")
    public static void testBlockLockModeHigh(PlotBuilder plot) {
        setup(plot, false, LockCraftingMode.LOCK_WHILE_HIGH);

        testLockModeHigh(plot);
    }

    @TestPlot("pp_part_lockmode_high")
    public static void testPartLockModeHigh(PlotBuilder plot) {
        setup(plot, true, LockCraftingMode.LOCK_WHILE_HIGH);

        testLockModeHigh(plot);
    }

    private static void testLockModeHigh(PlotBuilder plot) {
        plot.test(helper -> {
            var host = getHost(helper);
            var pp = host.getLogic();

            helper.startSequence()
                    .thenExecuteAfter(1, () -> {
                        // Initially it should not be locked since the signal is low
                        helper.assertEquals(BlockPos.ZERO, LockCraftingMode.NONE, pp.getCraftingLockedReason());
                        // Pushing pattern should succeed, even multiple times
                        helper.check(pushPattern(host), "Pushing pattern failed (1st attempt)");
                        helper.check(pushPattern(host), "Pushing pattern failed (2nd attempt)");
                    })
                    .thenExecuteAfter(1, () -> {
                        // Turn the lever on
                        helper.pullLever(LEVER_POS);

                        helper.assertEquals(BlockPos.ZERO, LockCraftingMode.LOCK_WHILE_HIGH,
                                pp.getCraftingLockedReason());
                        helper.check(!pushPattern(host), "Pushing pattern should fail");

                        // Turning the lever again should immediately lock the provider
                        helper.pullLever(LEVER_POS);

                        // That should immediately unlock the provider, and continue to do so
                        helper.assertEquals(BlockPos.ZERO, LockCraftingMode.NONE, pp.getCraftingLockedReason());
                        helper.check(pushPattern(host), "Pushing pattern failed");
                        helper.assertEquals(BlockPos.ZERO, LockCraftingMode.NONE, pp.getCraftingLockedReason());
                        helper.check(pushPattern(host), "Pushing pattern failed (2nd attempt)");
                    })
                    .thenExecuteAfter(1, () -> {
                        // Two pushes should have succeeded
                        var counter = helper.countContainerContentAt(BlockPos.ZERO.above());
                        helper.assertEquals(BlockPos.ZERO.above(), 4L, counter.get(AEItemKey.of(Blocks.OAK_LOG)));
                    })
                    .thenSucceed();
        });
    }

    @TestPlot("pp_block_lockmode_low")
    public static void testBlockLockModeLow(PlotBuilder plot) {
        setup(plot, false, LockCraftingMode.LOCK_WHILE_LOW);

        testLockModeLow(plot);
    }

    @TestPlot("pp_part_lockmode_low")
    public static void testPartLockModeLow(PlotBuilder plot) {
        setup(plot, true, LockCraftingMode.LOCK_WHILE_LOW);

        testLockModeLow(plot);
    }

    private static void testLockModeLow(PlotBuilder plot) {
        plot.test(helper -> {
            var host = getHost(helper);
            var pp = host.getLogic();

            helper.startSequence()
                    .thenExecuteAfter(1, () -> {
                        // Initially it should be locked since the signal is low
                        helper.assertEquals(BlockPos.ZERO, LockCraftingMode.LOCK_WHILE_LOW,
                                pp.getCraftingLockedReason());
                        // Pushing pattern should fail while locked
                        helper.check(!pushPattern(host), "Pushing pattern should fail");
                    })
                    .thenExecuteAfter(1, () -> {
                        // Turn the lever on
                        helper.pullLever(LEVER_POS);

                        // That should immediately unlock the provider, and continue to do so
                        helper.assertEquals(BlockPos.ZERO, LockCraftingMode.NONE, pp.getCraftingLockedReason());
                        helper.check(pushPattern(host), "Pushing pattern failed");
                        helper.assertEquals(BlockPos.ZERO, LockCraftingMode.NONE, pp.getCraftingLockedReason());
                        helper.check(pushPattern(host), "Pushing pattern failed (2nd attempt)");

                        // Turning the lever again should immediately lock the provider
                        helper.pullLever(LEVER_POS);

                        helper.assertEquals(BlockPos.ZERO, LockCraftingMode.LOCK_WHILE_LOW,
                                pp.getCraftingLockedReason());
                        helper.check(!pushPattern(host), "Pushing pattern should fail");
                    })
                    .thenExecuteAfter(1, () -> {
                        // Two pushes should have succeeded
                        var counter = helper.countContainerContentAt(BlockPos.ZERO.above());
                        helper.assertEquals(BlockPos.ZERO.above(), 2L, counter.get(AEItemKey.of(Blocks.OAK_LOG)));
                    })
                    .thenSucceed();
        });
    }

    @TestPlot("pp_block_lockmode_result")
    public static void testBlockLockModeResult(PlotBuilder plot) {
        setup(plot, false, LockCraftingMode.LOCK_UNTIL_RESULT);

        testLockModeResult(plot);
    }

    @TestPlot("pp_part_lockmode_result")
    public static void testPartLockModeResult(PlotBuilder plot) {
        setup(plot, true, LockCraftingMode.LOCK_UNTIL_RESULT);

        testLockModeResult(plot);
    }

    private static void testLockModeResult(PlotBuilder plot) {
        // Add storage because unlocking happens only when items are actually returned to the network
        plot.storageDrive(BlockPos.ZERO.south(), Direction.SOUTH);

        plot.test(helper -> {
            var host = getHost(helper);
            var pp = host.getLogic();

            helper.startSequence()
                    .thenExecuteAfter(1, () -> {
                        // Initially it should not be waiting for a result
                        helper.assertEquals(BlockPos.ZERO, LockCraftingMode.NONE, pp.getCraftingLockedReason());
                        // Pushing pattern should work in this mode, but only once
                        helper.check(pushPattern(host), "Pushing pattern should not fail");
                        // After pushing a pattern, the wait mode should switch
                        helper.assertEquals(BlockPos.ZERO, LockCraftingMode.LOCK_UNTIL_RESULT,
                                pp.getCraftingLockedReason());
                        // And the result it is waiting for should be the primary output of the pattern
                        var expectedResult = TWO_PLANK;
                        helper.assertEquals(BlockPos.ZERO, expectedResult, pp.getUnlockStack());
                    })
                    .thenExecuteAfter(1, () -> {
                        // Return one plank to the pattern provider (simulate)
                        pp.getReturnInv().insert(AEItemKey.of(Items.OAK_PLANKS), 1, Actionable.SIMULATE, null);
                        // Simulation should not have changed the state
                        helper.assertEquals(BlockPos.ZERO, LockCraftingMode.LOCK_UNTIL_RESULT,
                                pp.getCraftingLockedReason());
                        helper.assertEquals(BlockPos.ZERO, TWO_PLANK, pp.getUnlockStack());

                        // Now insert 1 for real
                        pp.getReturnInv().insert(AEItemKey.of(Items.OAK_PLANKS), 1, Actionable.MODULATE, null);

                        // This should STILL not have reduced the expected amount, because it will happen
                        // only once the items are pushed into the network
                        helper.check(!pp.getReturnInv().isEmpty(),
                                "Items should not be returned to the network immediately");
                        helper.assertEquals(BlockPos.ZERO, LockCraftingMode.LOCK_UNTIL_RESULT,
                                pp.getCraftingLockedReason());
                        helper.assertEquals(BlockPos.ZERO, TWO_PLANK, pp.getUnlockStack());
                    })
                    .thenExecuteAfter(1, () -> {
                        // Now, one tick later, it should have reset
                        helper.assertEquals(BlockPos.ZERO, LockCraftingMode.LOCK_UNTIL_RESULT,
                                pp.getCraftingLockedReason());
                        helper.assertEquals(BlockPos.ZERO, ONE_PLANK, pp.getUnlockStack());
                    })
                    .thenExecute(() -> {
                        // Now insert another one for real
                        pp.getReturnInv().insert(AEItemKey.of(Items.OAK_PLANKS), 1, Actionable.MODULATE, null);
                    })
                    .thenExecuteAfter(1, () -> {
                        // This should have unlocked the provider
                        helper.assertEquals(BlockPos.ZERO, LockCraftingMode.NONE, pp.getCraftingLockedReason());
                        helper.assertEquals(BlockPos.ZERO, null, pp.getUnlockStack());
                    })
                    .thenExecuteAfter(1, () -> {
                        helper.check(pushPattern(host), "Pushing pattern failed");

                        // Right back to being locked
                        helper.assertEquals(BlockPos.ZERO, LockCraftingMode.LOCK_UNTIL_RESULT,
                                pp.getCraftingLockedReason());
                        helper.assertEquals(BlockPos.ZERO, TWO_PLANK, pp.getUnlockStack());
                    })
                    .thenExecuteAfter(1, () -> {
                        // Two pushes should have succeeded
                        var counter = helper.countContainerContentAt(BlockPos.ZERO.above());
                        helper.assertEquals(BlockPos.ZERO.above(), 2L, counter.get(AEItemKey.of(Blocks.OAK_LOG)));
                    })
                    .thenSucceed();
        });
    }

    @TestPlot("pp_part_wait_for_pulse_saved")
    public static void testPartWaitForPulseSaved(PlotBuilder plot) {
        setup(plot, true, LockCraftingMode.LOCK_UNTIL_PULSE);

        testWaitForPulseSaved(plot);
    }

    @TestPlot("pp_block_wait_for_pulse_saved")
    public static void testBlockWaitForPulseSaved(PlotBuilder plot) {
        setup(plot, false, LockCraftingMode.LOCK_UNTIL_PULSE);

        testWaitForPulseSaved(plot);
    }

    /**
     * Tests that the pattern provider saves the fact that it is waiting for a redstone pulse to NBT. It might
     * accidentally unlock when the chunk reloads, otherwise.
     */
    private static void testWaitForPulseSaved(PlotBuilder plot) {
        plot.test(helper -> {
            var host = getHost(helper);
            var pp = host.getLogic();
            var savedBe = new SavedBlockEntity(helper);

            helper.startSequence()
                    .thenExecute(() -> {
                        // Push a pattern once to get the pattern provider to lock
                        helper.check(pushPattern(host), "push pattern should succeed");
                        helper.assertEquals(BlockPos.ZERO, LockCraftingMode.LOCK_UNTIL_PULSE,
                                pp.getCraftingLockedReason());

                        // Save the block entity and remove it
                        savedBe.saveAndRemove(BlockPos.ZERO);
                    })
                    .thenExecuteAfter(1, () -> {
                        // Restore the BE from NBT
                        savedBe.restore();
                    })
                    .thenExecuteAfter(1, () -> {
                        // Re-Query the PP from the level after its grid has been set-up
                        var newHost = getHost(helper);
                        var newPp = newHost.getLogic();
                        helper.check(newPp != pp, "New pattern provider should not be the same as the old one");

                        // Now check that the lock-state is still the same
                        helper.assertEquals(BlockPos.ZERO, LockCraftingMode.LOCK_UNTIL_PULSE,
                                newPp.getCraftingLockedReason());
                        helper.check(!pushPattern(newHost), "push pattern should fail");
                    })
                    .thenSucceed();
        });
    }

    @TestPlot("pp_part_wait_for_result_saved")
    public static void testPartWaitForResultSaved(PlotBuilder plot) {
        setup(plot, true, LockCraftingMode.LOCK_UNTIL_RESULT);

        testWaitForResultSaved(plot);
    }

    @TestPlot("pp_block_wait_for_result_saved")
    public static void testBlockWaitForResultSaved(PlotBuilder plot) {
        setup(plot, false, LockCraftingMode.LOCK_UNTIL_RESULT);

        testWaitForResultSaved(plot);
    }

    /**
     * Tests that the pattern provider saves the fact that it is waiting for a crafting result to NBT. It might
     * accidentally unlock when the chunk reloads, otherwise.
     */
    private static void testWaitForResultSaved(PlotBuilder plot) {
        plot.test(helper -> {
            var host = getHost(helper);
            var pp = host.getLogic();

            var savedBe = new SavedBlockEntity(helper);

            helper.startSequence()
                    .thenExecute(() -> {
                        // Push a pattern once to get the pattern provider to lock
                        helper.check(pushPattern(host), "push pattern should succeed");
                        helper.assertEquals(BlockPos.ZERO, LockCraftingMode.LOCK_UNTIL_RESULT,
                                pp.getCraftingLockedReason());
                        helper.assertEquals(BlockPos.ZERO, TWO_PLANK, pp.getUnlockStack());

                        // Save the block entity and remove it
                        savedBe.save(BlockPos.ZERO);
                        helper.destroyBlock(BlockPos.ZERO);
                    })
                    .thenExecuteAfter(1, () -> {
                        // Restore the BE from NBT
                        savedBe.restore();
                    })
                    .thenExecuteAfter(1, () -> {
                        // Re-Query the PP from the level after its grid has been set-up
                        var newHost = getHost(helper);
                        var newPp = newHost.getLogic();
                        helper.check(newPp != pp, "New pattern provider should not be the same as the old one");

                        // Now check that the lock-state is still the same
                        helper.assertEquals(BlockPos.ZERO, LockCraftingMode.LOCK_UNTIL_RESULT,
                                newPp.getCraftingLockedReason());
                        helper.assertEquals(BlockPos.ZERO, TWO_PLANK, pp.getUnlockStack());
                        helper.check(!pushPattern(newHost), "push pattern should fail");
                    })
                    .thenSucceed();
        });
    }

    private static boolean pushPattern(PatternProviderLogicHost host) {
        var details = createPatternDetails(host);
        var inputs = new KeyCounter[1];
        inputs[0] = new KeyCounter();
        inputs[0].add(AEItemKey.of(Blocks.OAK_LOG), 1);

        return host.getLogic().pushPattern(details, inputs);
    }

    private static PatternProviderLogicHost getHost(PlotTestHelper plotTestHelper) {
        var be = plotTestHelper.getBlockEntity(BlockPos.ZERO);
        if (be instanceof PatternProviderBlockEntity host) {
            return host;
        }
        return plotTestHelper.getPart(BlockPos.ZERO, Direction.UP, PatternProviderPart.class);
    }

    private static void setup(PlotBuilder plot, boolean usePart, LockCraftingMode mode) {

        var origin = BlockPos.ZERO;
        plot.creativeEnergyCell(origin.below());
        if (!usePart) {
            plot.blockEntity(origin, AEBlocks.PATTERN_PROVIDER, host -> setupPatternProvider(host, mode));
        } else {
            // Place a pattern provider facing up and a facade WEST, so we can place a button/lever on it
            plot
                    .cable(origin)
                    .part(Direction.UP, AEParts.PATTERN_PROVIDER, host -> setupPatternProvider(host, mode))
                    .facade(Direction.WEST, Blocks.STONE)
                    .facade(Direction.EAST, Blocks.STONE);
        }
        plot.buttonOn(BlockPos.ZERO, Direction.WEST);
        plot.leverOn(BlockPos.ZERO, Direction.EAST);
        plot.chest(origin.above());
    }

    private static void setupPatternProvider(PatternProviderLogicHost host, LockCraftingMode mode) {
        var pp = host.getLogic();
        pp.getConfigManager().putSetting(Settings.LOCK_CRAFTING_MODE, mode);
        var pattern = createPattern();
        pp.getPatternInv().addItems(pattern);
    }

    private static ItemStack createPattern() {
        return PatternDetailsHelper.encodeProcessingPattern(
                new GenericStack[] { new GenericStack(AEItemKey.of(Blocks.OAK_LOG), 1) },
                new GenericStack[] { TWO_PLANK });
    }

    private static IPatternDetails createPatternDetails(PatternProviderLogicHost host) {
        return PatternDetailsHelper.decodePattern(
                createPattern(),
                host.getBlockEntity().getLevel());
    }
}
