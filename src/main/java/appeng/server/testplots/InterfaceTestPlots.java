package appeng.server.testplots;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;

import appeng.api.config.Actionable;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.blockentity.misc.InterfaceBlockEntity;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.AEParts;
import appeng.me.helpers.BaseActionSource;
import appeng.parts.misc.InterfacePart;
import appeng.server.testworld.PlotBuilder;

public class InterfaceTestPlots {

    /**
     * Test that configured slots of interfaces are filtered and prevent insertion.
     */
    @TestPlot("interface_slot_filtering")
    public static void interfaceSlotFiltering(PlotBuilder builder) {
        var o = BlockPos.ZERO;
        builder.blockEntity(o, AEBlocks.INTERFACE, iface -> {
            // Set slot 0 to sticks
            iface.getInterfaceLogic().getConfig().setStack(0, new GenericStack(AEItemKey.of(Items.STICK), 1));
        });
        builder.hopper(o.above(), Direction.DOWN, Items.BRICK);
        builder.test(helper -> {
            helper.startSequence()
                    .thenExecute(() -> {
                        var itemCap = helper.getCapability(o, Capabilities.ItemHandler.BLOCK, Direction.UP);
                        helper.check(itemCap.isItemValid(0, ItemStack.EMPTY), "empty stack should be valid in slot 0");
                        helper.check(itemCap.isItemValid(0, Items.STICK.getDefaultInstance()),
                                "stick should be valid in slot 0");
                        helper.check(itemCap.isItemValid(1, Items.STICK.getDefaultInstance()),
                                "stick should be valid in slot 1");
                        helper.check(!itemCap.isItemValid(0, Blocks.BRICKS.asItem().getDefaultInstance()),
                                "bricks should not be valid in slot 0");
                        helper.check(itemCap.isItemValid(1, Blocks.BRICKS.asItem().getDefaultInstance()),
                                "bricks should be valid in slot 1");

                        var fluidCap = helper.getCapability(o, Capabilities.FluidHandler.BLOCK, Direction.UP);
                        helper.check(fluidCap.isFluidValid(0, FluidStack.EMPTY),
                                "empty fluid stack should be valid in slot 0");
                        helper.check(!fluidCap.isFluidValid(0, new FluidStack(Fluids.WATER, 1)),
                                "fluid should not be valid in slot 0");
                        helper.check(fluidCap.isFluidValid(1, new FluidStack(Fluids.WATER, 1)),
                                "fluid should be valid in slot 1");
                    })
                    .thenWaitUntil(() -> {
                        var iface = (InterfaceBlockEntity) helper.getBlockEntity(o);
                        helper.assertEquals(o, null, iface.getStorage().getKey(0));
                        helper.assertEquals(o, AEItemKey.of(Items.BRICK), iface.getStorage().getKey(1));
                    })
                    .thenExecute(() -> {
                        var hopper = (HopperBlockEntity) helper.getBlockEntity(o.above());
                        hopper.setItem(0, Items.STICK.getDefaultInstance());
                    })
                    .thenWaitUntil(() -> {
                        var iface = (InterfaceBlockEntity) helper.getBlockEntity(o);
                        helper.assertEquals(o, AEItemKey.of(Items.STICK), iface.getStorage().getKey(0));
                        helper.assertEquals(o, AEItemKey.of(Items.BRICK), iface.getStorage().getKey(1));
                    })
                    .thenSucceed();
        });
    }

    /**
     * Similar to {@link TestPlots#exportBusDupeRegression(PlotBuilder)}, but tests that interface restocking will not
     * make the same mistake.
     */
    @TestPlot("interface_restock_dupe_test")
    public static void interfaceRestockDupeTest(PlotBuilder plot) {
        var o = BlockPos.ZERO;
        // Set up a double chest with 64 sticks which will report as 128 sticks
        // and allow simulated extractions of 128 sticks to succeed.
        plot.chest(o.north(), new ItemStack(Items.STICK, 64));
        plot.blockState(o.north(), Blocks.CHEST.defaultBlockState().setValue(ChestBlock.TYPE, ChestType.RIGHT));
        plot.cable(o).part(Direction.NORTH, AEParts.STORAGE_BUS);
        plot.blockState(o.north().west(), Blocks.CHEST.defaultBlockState().setValue(ChestBlock.TYPE, ChestType.LEFT));
        plot.cable(o.west()).part(Direction.NORTH, AEParts.STORAGE_BUS);

        // Set up an interface that tries to stock 128 sticks
        plot.blockEntity(o.above(), AEBlocks.INTERFACE, iface -> {
            iface.getConfig().setStack(0, GenericStack.fromItemStack(new ItemStack(Items.STICK, 64)));
            iface.getConfig().setStack(1, GenericStack.fromItemStack(new ItemStack(Items.STICK, 64)));
        });
        plot.creativeEnergyCell(o.below());

        plot.test(helper -> {
            helper.succeedWhen(() -> {
                // Both double chests should be empty
                helper.assertContainerEmpty(o.north());
                helper.assertContainerEmpty(o.north().west());

                // The output interface should have 64 sticks
                var iface = (InterfaceBlockEntity) helper.getBlockEntity(o.above());
                var counter = new KeyCounter();
                iface.getInterfaceLogic().getStorage().getAvailableStacks(counter);
                var stickCount = counter.get(AEItemKey.of(Items.STICK));
                helper.check(stickCount == 64,
                        "Expected 64 sticks total, but found: " + stickCount);
            });
        });
    }

    /**
     * Tests that the priority checks for interface -> interface restocking don't apply when the source interface is on
     * another network. Regression test for https://github.com/AppliedEnergistics/Applied-Energistics-2/issues/6847.
     */
    @TestPlot("interface_to_interface_different_networks")
    public static void interfaceToInterfaceDifferentNetworks(PlotBuilder plot) {
        var o = BlockPos.ZERO;
        plot.cable(o)
                .part(Direction.NORTH, AEParts.STORAGE_BUS);
        plot.blockEntity(o.north(), AEBlocks.INTERFACE, iface -> {
            // Need something in the config to not expose the full network...
            iface.getConfig().setStack(0, GenericStack.fromItemStack(new ItemStack(Items.APPLE)));
            iface.getStorage().setStack(0, GenericStack.fromItemStack(new ItemStack(Items.APPLE, 64)));
        });
        plot.block(o.north().north(), AEBlocks.CREATIVE_ENERGY_CELL);
        plot.block(o.east(), AEBlocks.CREATIVE_ENERGY_CELL);
        plot.blockEntity(o.south(), AEBlocks.INTERFACE, iface -> {
            iface.getConfig().setStack(0, GenericStack.fromItemStack(new ItemStack(Items.APPLE)));
        });

        plot.test(helper -> helper.startSequence()
                // Test interface restock
                .thenWaitUntil(() -> {
                    var iface = (InterfaceBlockEntity) helper.getBlockEntity(o.south());
                    var apples = iface.getStorage().getStack(0);
                    helper.check(apples != null && apples.amount() == 1, "Expected 1 apple", o.south());
                })
                // Test interface pushing items away to subnet
                .thenExecute(() -> {
                    var iface = (InterfaceBlockEntity) helper.getBlockEntity(o.south());
                    iface.getStorage().setStack(1, GenericStack.fromItemStack(new ItemStack(Items.DIAMOND)));
                })
                .thenWaitUntil(() -> {
                    var iface = (InterfaceBlockEntity) helper.getBlockEntity(o.north());
                    var diamonds = iface.getStorage().getStack(1);
                    helper.check(diamonds != null && diamonds.amount() == 1, "Expected 1 diamond", o.north());
                })
                .thenSucceed());
    }

    @TestPlot("interface_part_caps")
    public static void interfacePartCaps(PlotBuilder plot) {
        plot.cable(BlockPos.ZERO).part(Direction.UP, AEParts.INTERFACE);
        plot.hopper(BlockPos.ZERO.above(), Direction.DOWN, Items.DIRT);

        plot.test(helper -> {
            helper.startSequence()
                    .thenWaitUntil(() -> {
                        var interfacePart = helper.getPart(BlockPos.ZERO, Direction.UP, InterfacePart.class);
                        var storage = interfacePart.getInterfaceLogic().getStorage();
                        helper.assertEquals(BlockPos.ZERO, AEItemKey.of(Items.DIRT), storage.getKey(0));
                    })
                    .thenSucceed();
        });
    }

    /**
     * Regression test for https://github.com/AppliedEnergistics/Applied-Energistics-2/issues/5919
     */
    @TestPlot("canceling_jobs_from_interfacecrash")
    public static void cancelingJobsFromInterfaceCrash(PlotBuilder plot) {
        var origin = BlockPos.ZERO;

        plot.creativeEnergyCell(origin);
        // Stock 1 oak_plank via crafting
        plot.blockEntity(origin.above(), AEBlocks.INTERFACE, iface -> {
            iface.getUpgrades().addItems(AEItems.CRAFTING_CARD.stack());
            iface.getConfig().setStack(0, new GenericStack(AEItemKey.of(Items.OAK_PLANKS), 1));
        });
        plot.block(origin.east(), AEBlocks.CRAFTING_STORAGE_1K);
        // Set up a level emitter for oak_planks
        plot.cable(origin.west()).craftingEmitter(Direction.WEST, Items.OAK_PLANKS);

        plot.test(helper -> {
            helper.startSequence()
                    .thenWaitUntil(() -> {
                        var grid = helper.getGrid(origin);
                        helper.check(
                                grid.getCraftingService().isRequesting(AEItemKey.of(Items.OAK_PLANKS)),
                                "Interface is not crafting oak planks");
                    })
                    .thenExecute(() -> {
                        // Cancel the job by removing the upgrade card
                        var iface = (InterfaceBlockEntity) helper.getBlockEntity(origin.above());
                        iface.getUpgrades().removeItems(1, ItemStack.EMPTY, null);

                        // and immediately insert a craft result into the network storage
                        // this would crash because the crafting job was not cleaned up properly before
                        // the crafting service ticks
                        var grid = helper.getGrid(origin);
                        var inserted = grid.getStorageService().getInventory().insert(
                                AEItemKey.of(Items.OAK_PLANKS), 1, Actionable.MODULATE, new BaseActionSource());
                        helper.check(inserted == 0,
                                "Nothing should have been inserted into the network");
                        helper.check(iface.getInterfaceLogic().getStorage().isEmpty(),
                                "Nothing should have been inserted into the interface");
                    })
                    .thenSucceed();
        }).maxTicks(300 /* interface takes a while to request */);
    }

}
