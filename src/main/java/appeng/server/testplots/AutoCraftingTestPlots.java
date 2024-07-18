package appeng.server.testplots;

import java.util.ArrayList;
import java.util.Arrays;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;

import appeng.api.config.Actionable;
import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.blockentity.crafting.PatternProviderBlockEntity;
import appeng.blockentity.misc.InscriberBlockEntity;
import appeng.blockentity.storage.SkyChestBlockEntity;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.AEParts;
import appeng.items.storage.CreativeCellItem;
import appeng.me.helpers.BaseActionSource;
import appeng.menu.AutoCraftingMenu;
import appeng.server.testworld.PlotBuilder;
import appeng.server.testworld.TestCraftingJob;
import appeng.util.inv.AppEngInternalInventory;

public final class AutoCraftingTestPlots {
    private AutoCraftingTestPlots() {
    }

    @TestPlot("autocrafting_testplot")
    public static void create(PlotBuilder plot) {
        plot.creativeEnergyCell("4 -1 4");

        // Cable to CPU / Access / Storage
        plot.cable("4 0 [1,5]");
        plot.cable("[3,6] 0 1");
        plot.block("[4,5] [0,1] [4,5]", AEBlocks.CONTROLLER);

        // Crafting cube
        craftingCube(plot.offset(1, 0, 1));

        // Stack of pattern providers with 6 assemblers each
        plot.cable("[6,8] 0 5");
        var assemblerStack = plot.offset(8, 1, 5);
        for (var i = 0; i < 8; i++) {
            assemblerFlower(assemblerStack.offset(0, i * 3, 0));
        }

        // Storage and access
        plot.blockEntity("7 0 1", AEBlocks.DRIVE, drive -> {
            drive.getInternalInventory().addItems(AEItems.ITEM_CELL_64K.stack());
            drive.getInternalInventory().addItems(AEItems.ITEM_CELL_64K.stack());
            drive.getInternalInventory().addItems(AEItems.FLUID_CELL_64K.stack());
            drive.getInternalInventory().addItems(AEItems.FLUID_CELL_64K.stack());
            drive.getInternalInventory().addItems(CreativeCellItem.ofItems(Items.REDSTONE));
            drive.getInternalInventory().addItems(CreativeCellItem.ofFluids(Fluids.LAVA));
        });
        plot.part("6 0 1", Direction.NORTH, AEParts.PATTERN_ENCODING_TERMINAL, term -> {
            var inv = term.getLogic().getBlankPatternInv();
            inv.addItems(AEItems.BLANK_PATTERN.stack(64));
        });
        plot.part("5 0 1", Direction.NORTH, AEParts.PATTERN_ACCESS_TERMINAL);
        plot.part("4 0 1", Direction.NORTH, AEParts.TERMINAL);
        plot.part("3 0 1", Direction.NORTH, AEParts.CRAFTING_TERMINAL);

        // Subsystem to craft obsidian
        buildObsidianCrafting(plot.offset(3, 0, 5));

        // Subsystem to export crafted items
        buildChestCraftingExport(plot.offset(5, 0, 7));

        // Subsystem to allow emitting water on request
        buildWaterEmittingSource(plot.offset(5, 0, 9));

        // Connect subsystems with dense cable
        plot.cable("4 0 [6,9]", AEParts.SMART_DENSE_CABLE);

        // Add post-processing action once the grid is up and running
        plot.afterGridInitAt("4 0 4", (grid, gridNode) -> {
            var level = gridNode.getLevel();
            var patterns = new ArrayList<ItemStack>();
            // Crafting pattern table with substitutions enabled and some items that are NOT in storage
            patterns.add(encodeCraftingPattern(
                    level,
                    new Object[] {
                            Items.OAK_PLANKS, Items.OAK_PLANKS, null,
                            Items.OAK_PLANKS, Items.CRIMSON_PLANKS, null,
                            null, null, null
                    },
                    true,
                    false));
            // Crafting pattern table with substitutions enabled and some items that are NOT in storage,
            // and an actual sparse pattern
            patterns.add(encodeCraftingPattern(
                    level,
                    new Object[] {
                            Items.OAK_PLANKS, Items.OAK_PLANKS, Items.OAK_PLANKS,
                            Items.OAK_PLANKS, null, Items.OAK_PLANKS,
                            Items.CRIMSON_PLANKS, Items.OAK_PLANKS, Items.OAK_PLANKS
                    },
                    true,
                    false));

            // This isn't a real sensible pattern, but we need some pattern that results in fluid being crafted
            // to check how the terminal behaves
            patterns.add(PatternDetailsHelper.encodeProcessingPattern(
                    new GenericStack[] {
                            new GenericStack(AEFluidKey.of(Fluids.WATER), AEFluidKey.AMOUNT_BUCKET),
                            GenericStack.fromItemStack(new ItemStack(Items.REDSTONE))
                    },
                    new GenericStack[] {
                            new GenericStack(AEFluidKey.of(Fluids.WATER), AEFluidKey.AMOUNT_BUCKET)
                    }));

            // Add ingredients to network storage
            var networkInv = grid.getStorageService().getInventory();
            networkInv.insert(AEItemKey.of(Items.OAK_PLANKS), 83, Actionable.MODULATE, new BaseActionSource());

            // Distribute patterns across the pattern providers in the grid
            for (var provider : grid.getMachines(PatternProviderBlockEntity.class)) {
                while (!patterns.isEmpty()) {
                    var pattern = patterns.get(0);
                    if (provider.getLogic().getPatternInv().addItems(pattern).isEmpty()) {
                        patterns.remove(0);
                    } else {
                        break; // Try the next pattern provider
                    }
                }
            }
        });
    }

    private static void buildChestCraftingExport(PlotBuilder plot) {
        // Subsystem to export chests via crafting card export bus
        plot.cable("0 0 0").part(Direction.SOUTH, AEParts.EXPORT_BUS, eb -> {
            eb.getUpgrades().addItems(new ItemStack(AEItems.CRAFTING_CARD));
            eb.getConfig().insert(0, AEItemKey.of(Items.CHEST), 1, Actionable.MODULATE);
        });
        plot.block("0 0 1", Blocks.CHEST);
    }

    private static void buildWaterEmittingSource(PlotBuilder plot) {
        plot.cable("0 0 0")
                .part(Direction.SOUTH, AEParts.QUARTZ_FIBER);
        plot.cable("0 0 1")
                .part(Direction.NORTH, AEParts.TOGGLE_BUS);
        plot.cable("0 1 0");
        plot.cable("0 1 1")
                .craftingEmitter(Direction.DOWN, Fluids.WATER)
                .part(Direction.SOUTH, AEParts.INTERFACE);
        plot.cable("0 1 2")
                .part(Direction.NORTH, AEParts.STORAGE_BUS, storageBus -> {
                    storageBus.getConfig().insert(0, AEFluidKey.of(Fluids.WATER), 1, Actionable.MODULATE);
                });
        plot.cable("0 0 2")
                .part(Direction.DOWN, AEParts.ANNIHILATION_PLANE);
        plot.block("[-1,1] -1 2", Blocks.WATER);
    }

    private static void buildObsidianCrafting(PlotBuilder plot) {
        // Builds towards west (negative X)
        plot.blockEntity("0 0 0", AEBlocks.PATTERN_PROVIDER, provider -> {
            // A pattern to create obsidian by combining lava with water
            var pattern = PatternDetailsHelper.encodeProcessingPattern(
                    new GenericStack[] {
                            new GenericStack(AEFluidKey.of(Fluids.LAVA), AEFluidKey.AMOUNT_BUCKET)
                    },
                    new GenericStack[] {
                            new GenericStack(AEItemKey.of(Items.OBSIDIAN), 1)
                    });
            provider.getLogic().getPatternInv().addItems(pattern);
        });
        plot.cable("-1 0 0")
                // Insertion inventory for the pattern provider
                .part(Direction.EAST, AEParts.INTERFACE)
                // Plane to place the lava into the world
                .part(Direction.WEST, AEParts.FORMATION_PLANE, plane -> {
                    plane.getConfig().insert(0, AEFluidKey.of(Fluids.LAVA), 1, Actionable.MODULATE);
                });

        // Annihilation plane for picking up the obsidian
        // This is on a third subnet to prevent it from picking up the lava before it turns into obsidian
        plot.cable("-2 1 0").part(Direction.DOWN, AEParts.ANNIHILATION_PLANE);
        // Give power to the net below, but don't connect grids
        plot.cable("-1 1 0").part(Direction.DOWN, AEParts.QUARTZ_FIBER);
        plot.cable("0 1 0")
                // Storage channel for return of obsidian
                .part(Direction.DOWN, AEParts.STORAGE_BUS, part -> {
                    part.getConfig().insert(0, AEItemKey.of(Items.OBSIDIAN), 1, Actionable.MODULATE);
                })
                // Take power from controller, but dont connect grids
                .part(Direction.EAST, AEParts.QUARTZ_FIBER);

        // Non-interactive blocks
        plot.block("-3 0 [-2,0]", Blocks.COBBLESTONE);
        plot.block("-1 0 [-2,-1]", Blocks.COBBLESTONE);
        plot.block("-2 0 1", Blocks.COBBLESTONE);
        plot.block("-2 0 -2", Blocks.WATER);
    }

    private static ItemStack encodeCraftingPattern(ServerLevel level,
            Object[] ingredients,
            boolean allowSubstitutions,
            boolean allowFluidSubstitutions) {

        // Allow a mixed input of items or item stacks as ingredients
        var stacks = Arrays.stream(ingredients)
                .map(in -> {
                    if (in instanceof ItemLike itemLike) {
                        return new ItemStack(itemLike);
                    } else if (in instanceof ItemStack itemStack) {
                        return itemStack;
                    } else if (in == null) {
                        return ItemStack.EMPTY;
                    } else {
                        throw new IllegalArgumentException("Unsupported argument: " + in);
                    }
                })
                .toArray(ItemStack[]::new);

        var c = new TransientCraftingContainer(new AutoCraftingMenu(), 3, 3);
        for (int i = 0; i < stacks.length; i++) {
            c.setItem(i, stacks[i]);
        }

        var recipe = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, c, level).orElseThrow();

        var result = recipe.assemble(c, level.registryAccess());

        return PatternDetailsHelper.encodeCraftingPattern(
                recipe,
                stacks,
                result,
                allowSubstitutions,
                allowFluidSubstitutions);
    }

    private static void craftingCube(PlotBuilder plot) {
        plot.block("[-1,1] [0,2] [-1,1]", AEBlocks.CRAFTING_STORAGE_64K);
        plot.block("-1 2 -1", AEBlocks.CRAFTING_STORAGE_16K);
        plot.block("1 2 -1", AEBlocks.CRAFTING_STORAGE_4K);
        plot.block("-1 2 1", AEBlocks.CRAFTING_STORAGE_1K);
        plot.block("[-1,1] 0 [-1,1]", AEBlocks.CRAFTING_ACCELERATOR);
        plot.block("0 1 -1", AEBlocks.CRAFTING_MONITOR);
    }

    private static void assemblerFlower(PlotBuilder plot) {
        plot.block("0 0 0", AEBlocks.PATTERN_PROVIDER);
        plot.block("-1 0 0", AEBlocks.MOLECULAR_ASSEMBLER);
        plot.block("1 0 0", AEBlocks.MOLECULAR_ASSEMBLER);
        plot.block("0 0 1", AEBlocks.MOLECULAR_ASSEMBLER);
        plot.block("0 0 -1", AEBlocks.MOLECULAR_ASSEMBLER);
        plot.block("0 -1 0", AEBlocks.MOLECULAR_ASSEMBLER);
        plot.block("0 1 0", AEBlocks.MOLECULAR_ASSEMBLER);
    }

    /**
     * Tests that PPs correctly round-robin crafts across their sides.
     */
    @TestPlot("pattern_provider_faces_round_robin")
    public static void patternProviderFacesRoundRobin(PlotBuilder plot) {
        var inscriberPos = new BlockPos[] {
                new BlockPos(-1, 0, -3),
                new BlockPos(1, 0, -3),
        };

        craftingCube(plot);

        plot.cable("0 0 -2");
        plot.blockEntity("0 0 -3", AEBlocks.PATTERN_PROVIDER, provider -> {
            var pattern = PatternDetailsHelper.encodeProcessingPattern(
                    new GenericStack[] {
                            GenericStack.fromItemStack(AEItems.CERTUS_QUARTZ_CRYSTAL.stack())
                    },
                    new GenericStack[] {
                            GenericStack.fromItemStack(AEItems.CERTUS_QUARTZ_DUST.stack())
                    });
            provider.getLogic().getPatternInv().addItems(pattern);
        });
        for (var pos : inscriberPos) {
            plot.blockEntity(pos, AEBlocks.INSCRIBER, inscriber -> {
                inscriber.getConfigManager().putSetting(Settings.AUTO_EXPORT, YesNo.YES);
            });
        }
        plot.cable("0 0 -4");
        {
            var db = plot.drive(new BlockPos(0, 0, -5));
            db.addCreativeCell().add(AEItems.CERTUS_QUARTZ_CRYSTAL);
            db.addItemCell64k();
        }
        plot.cable("0 1 -5").part(Direction.NORTH, AEParts.CRAFTING_TERMINAL);
        plot.creativeEnergyCell("0 -1 -5");

        // Check item distribution in PPs
        plot.test(helper -> {
            var craftingJob = new TestCraftingJob(helper, BlockPos.ZERO, AEItemKey.of(AEItems.CERTUS_QUARTZ_DUST), 10);
            helper.startSequence()
                    .thenWaitUntil(craftingJob::tickUntilStarted)
                    .thenIdle(1) // give time to push out job
                    .thenExecute(() -> {
                        for (var pos : inscriberPos) {
                            var inscriber = (InscriberBlockEntity) helper.getBlockEntity(pos);
                            helper.check(inscriber.getInternalInventory().getStackInSlot(2).getCount() == 5,
                                    "Inscriber should have 5 = 10/2 items", pos);
                        }
                    })
                    .thenSucceed();
        });
    }

    /**
     * Tests that identical processing pattern inputs get "unstacked" when they are pushed. This is validated using a
     * sky stone chest with a changed slot limit of 1.
     */
    @TestPlot("processing_pattern_inputs_unstacking")
    public static void processingPatternInputsUnstacking(PlotBuilder plot) {
        var chestPos = new BlockPos(1, 0, -3);

        craftingCube(plot);

        plot.cable("0 0 -2");
        plot.blockEntity("0 0 -3", AEBlocks.PATTERN_PROVIDER, provider -> {
            var pattern = PatternDetailsHelper.encodeProcessingPattern(
                    new GenericStack[] {
                            GenericStack.fromItemStack(new ItemStack(Items.STONE)),
                            GenericStack.fromItemStack(new ItemStack(Items.STONE)),
                            GenericStack.fromItemStack(new ItemStack(Items.DIAMOND)),
                            GenericStack.fromItemStack(new ItemStack(Items.STONE)),
                    },
                    new GenericStack[] {
                            GenericStack.fromItemStack(AEItems.CERTUS_QUARTZ_DUST.stack())
                    });
            provider.getLogic().getPatternInv().addItems(pattern);
        });
        plot.blockEntity(chestPos, AEBlocks.SKY_STONE_CHEST, skyChest -> {
            var inv = (AppEngInternalInventory) skyChest.getInternalInventory();
            for (int i = 0; i < inv.size(); i++) {
                inv.setMaxStackSize(i, 1);
            }
        });
        plot.cable("0 0 -4");
        {
            var db = plot.drive(new BlockPos(0, 0, -5));
            db.addCreativeCell().add(Items.STONE).add(Items.DIAMOND);
            db.addItemCell64k();
        }
        plot.cable("0 1 -5").part(Direction.NORTH, AEParts.CRAFTING_TERMINAL);
        plot.creativeEnergyCell("0 -1 -5");

        // Check item distribution in chest
        plot.test(helper -> {
            var craftingJob = new TestCraftingJob(helper, BlockPos.ZERO, AEItemKey.of(AEItems.CERTUS_QUARTZ_DUST), 1);
            helper.startSequence()
                    .thenWaitUntil(craftingJob::tickUntilStarted)
                    .thenIdle(1) // give time to push out job
                    .thenExecute(() -> {
                        var chest = (SkyChestBlockEntity) helper.getBlockEntity(chestPos);
                        var inv = chest.getInternalInventory();
                        for (int i = 0; i < 4; ++i) {
                            helper.check(inv.getStackInSlot(i).getCount() == 1,
                                    "Chest should have 1 item in slot " + i, chestPos);
                        }

                        helper.check(inv.getStackInSlot(0).is(Items.STONE), "Chest should have stone in slot 0",
                                chestPos);
                        helper.check(inv.getStackInSlot(1).is(Items.STONE), "Chest should have stone in slot 1",
                                chestPos);
                        helper.check(inv.getStackInSlot(2).is(Items.DIAMOND), "Chest should have diamond in slot 2",
                                chestPos);
                        helper.check(inv.getStackInSlot(3).is(Items.STONE), "Chest should have stone in slot 3",
                                chestPos);
                    })
                    .thenSucceed();
        });
    }

    /**
     * The crafting CPU inventory drops from every block of the multiblock when the multiblock is broken, as long as the
     * multiblock is currently offline. https://github.com/AppliedEnergistics/Applied-Energistics-2/issues/7288
     */
    @TestPlot("regression_7288")
    public static void testCraftingCpuDupe(PlotBuilder plot) {
        craftingCube(plot);

        plot.cable("0 0 -2");
        plot.blockEntity("0 0 -3", AEBlocks.PATTERN_PROVIDER, provider -> {
            var pattern = PatternDetailsHelper.encodeProcessingPattern(
                    new GenericStack[] {
                            GenericStack.fromItemStack(new ItemStack(Items.DIAMOND))
                    },
                    new GenericStack[] {
                            GenericStack.fromItemStack(new ItemStack(Items.STICK))
                    });
            provider.getLogic().getPatternInv().addItems(pattern);
        });
        plot.cable("0 0 -4");
        {
            var db = plot.drive(new BlockPos(0, 0, -5));
            db.addItemCell64k().add(Items.DIAMOND, 1);
        }
        plot.cable("0 1 -5").part(Direction.NORTH, AEParts.CRAFTING_TERMINAL);
        plot.creativeEnergyCell("0 -1 -5");

        plot.test(helper -> {
            var craftingJob = new TestCraftingJob(helper, BlockPos.ZERO, AEItemKey.of(Items.STICK), 1);
            helper.startSequence()
                    .thenWaitUntil(craftingJob::tickUntilStarted)
                    .thenIdle(1) // give time to push out job
                    .thenExecute(() -> {
                        // break cable
                        helper.destroyBlock(new BlockPos(0, 0, -2));
                        // break part of the crafting CPU
                        helper.destroyBlock(new BlockPos(0, 0, -1));
                        // Fail if not exactly 1 diamond was spawned as item entities
                        helper.assertItemEntityCountIs(Items.DIAMOND, new BlockPos(0, 0, 0), 3, 1);
                    })
                    .thenSucceed();
        });
    }
}
