package appeng.server.testworld;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

import org.jetbrains.annotations.Nullable;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.material.Fluids;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.RedstoneMode;
import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.networking.pathing.ChannelMode;
import appeng.api.parts.PartHelper;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.StorageCells;
import appeng.api.util.AEColor;
import appeng.blockentity.crafting.MolecularAssemblerBlockEntity;
import appeng.blockentity.misc.InterfaceBlockEntity;
import appeng.blockentity.storage.DriveBlockEntity;
import appeng.blockentity.storage.SkyStoneTankBlockEntity;
import appeng.core.AELog;
import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.AEParts;
import appeng.items.storage.CreativeCellItem;
import appeng.items.tools.powered.MatterCannonItem;
import appeng.me.cells.BasicCellInventory;
import appeng.me.helpers.BaseActionSource;
import appeng.me.service.PathingService;
import appeng.menu.NullMenu;
import appeng.parts.crafting.PatternProviderPart;
import appeng.util.CraftingRecipeUtil;

public final class TestPlots {
    public static final Map<ResourceLocation, Consumer<PlotBuilder>> PLOT_FACTORIES = ImmutableMap
            .<ResourceLocation, Consumer<PlotBuilder>>builder()
            .put(AppEng.makeId("all_terminals"), TestPlots::allTerminals)
            .put(AppEng.makeId("item_chest"), TestPlots::itemChest)
            .put(AppEng.makeId("fluid_chest"), TestPlots::fluidChest)
            .put(AppEng.makeId("skycompass_rendering"), TestPlots::skyCompassRendering)
            .put(AppEng.makeId("crystal_growth_autocrafting"), TestPlots::crystalGrowthAutoCrafting)
            .put(AppEng.makeId("import_exportbus"), TestPlots::importExportBus)
            .put(AppEng.makeId("inscriber"), TestPlots::inscriber)
            .put(AppEng.makeId("autocrafting_testplot"), AutoCraftingTestPlot::create)
            .put(AppEng.makeId("import_and_export_in_one_tick"), TestPlots::importAndExportInOneTick)
            .put(AppEng.makeId("export_from_storagebus"), TestPlots::exportFromStorageBus)
            .put(AppEng.makeId("import_into_storagebus"), TestPlots::importIntoStorageBus)
            .put(AppEng.makeId("import_on_pulse"), TestPlots::importOnPulse)
            .put(AppEng.makeId("import_on_pulse_transactioncrash"), TestPlots::importOnPulseTransactionCrash)
            .put(AppEng.makeId("mattercannon_range"), TestPlots::matterCannonRange)
            .put(AppEng.makeId("insert_fluid_into_mechest"), TestPlots::testInsertFluidIntoMEChest)
            .put(AppEng.makeId("maxchannels_adhoctest"), TestPlots::maxChannelsAdHocTest)
            .put(AppEng.makeId("blockingmode_subnetwork_chesttest"), TestPlots::blockingModeSubnetworkChestTest)
            .put(AppEng.makeId("canceling_jobs_from_interfacecrash"), TestPlots::cancelingJobsFromInterfaceCrash)
            .put(AppEng.makeId("terminal_fullof_enchanteditems"), TestPlots::terminalFullOfEnchantedItems)
            .put(AppEng.makeId("p2p_me"), P2PTestPlots::me)
            .put(AppEng.makeId("p2p_items"), P2PTestPlots::item)
            .put(AppEng.makeId("p2p_fluids"), P2PTestPlots::fluid)
            .put(AppEng.makeId("p2p_energy"), P2PTestPlots::energy)
            .put(AppEng.makeId("p2p_light"), P2PTestPlots::light)
            .put(AppEng.makeId("import_from_cauldron"), TestPlots::importLavaFromCauldron)
            .put(AppEng.makeId("tool_repair_recipe"), TestPlots::toolRepairRecipe)
            .put(AppEng.makeId("double_chest_storage_bus"), TestPlots::doubleChestStorageBus)
            .build();

    private TestPlots() {
    }

    public static List<ResourceLocation> getPlotIds() {
        return new ArrayList<>(PLOT_FACTORIES.keySet());
    }

    public static List<Plot> createPlots() {
        var plots = new ArrayList<Plot>();
        for (var entry : PLOT_FACTORIES.entrySet()) {
            var plot = new Plot(entry.getKey());
            entry.getValue().accept(plot);
            plots.add(plot);
        }
        return plots;
    }

    @Nullable
    public static Plot getById(ResourceLocation name) {
        var factory = PLOT_FACTORIES.get(name);
        if (factory == null) {
            return null;
        }
        var plot = new Plot(name);
        factory.accept(plot);
        return plot;
    }

    /**
     * A wall of all terminals/monitors in all color combinations.
     */
    public static void allTerminals(PlotBuilder plot) {
        var enchantedPickaxe = new ItemStack(Items.DIAMOND_PICKAXE);
        enchantedPickaxe.enchant(Enchantments.BLOCK_FORTUNE, 3);
        var enchantedPickaxeKey = AEItemKey.of(enchantedPickaxe);

        plot.creativeEnergyCell("0 -1 0");

        plot.cable("[-1,0] [0,8] 0", AEParts.COVERED_DENSE_CABLE);
        plot.part("0 [0,8] 0", Direction.WEST, AEParts.CABLE_ANCHOR);
        plot.block("[-1,0] 5 0", AEBlocks.CONTROLLER);
        plot.storageDrive(new BlockPos(0, 5, 1));
        plot.afterGridInitAt("0 5 1", (grid, gridNode) -> {
            var storage = grid.getStorageService().getInventory();
            var src = new BaseActionSource();
            storage.insert(AEItemKey.of(Items.DIAMOND_PICKAXE), 10, Actionable.MODULATE, src);
            storage.insert(enchantedPickaxeKey, 1234, Actionable.MODULATE, src);
            storage.insert(AEItemKey.of(Items.ACACIA_LOG), Integer.MAX_VALUE, Actionable.MODULATE, src);
        });

        // Generate a "line" of cable+terminals that extends from the center
        // Only go up to 9 in height, then flip the X axis and continue on the other side
        var y = 0;
        for (var color : getColorsTransparentFirst()) {
            PlotBuilder line;
            if (y >= 9) {
                line = plot.transform(bb -> new BoundingBox(
                        -1 - bb.maxX(), bb.minY(), bb.minZ(),
                        -1 - bb.minX(), bb.maxY(), bb.maxZ())).offset(0, y - 9, 0);
            } else {
                line = plot.offset(0, y, 0);
            }
            y++;
            line.cable("[1,9] 0 0", AEParts.GLASS_CABLE, color);
            if (color == AEColor.TRANSPARENT) {
                line.part("[1,9] 0 0", Direction.UP, AEParts.CABLE_ANCHOR);
            }
            line.part("1 0 0", Direction.NORTH, AEParts.TERMINAL);
            line.part("2 0 0", Direction.NORTH, AEParts.CRAFTING_TERMINAL);
            line.part("3 0 0", Direction.NORTH, AEParts.PATTERN_ENCODING_TERMINAL);
            line.part("4 0 0", Direction.NORTH, AEParts.PATTERN_ACCESS_TERMINAL);
            line.part("5 0 0", Direction.NORTH, AEParts.STORAGE_MONITOR, monitor -> {
                monitor.setConfiguredItem(enchantedPickaxeKey);
                monitor.setLocked(true);
            });
            line.part("6 0 0", Direction.NORTH, AEParts.CONVERSION_MONITOR, monitor -> {
                monitor.setConfiguredItem(AEItemKey.of(Items.ACACIA_LOG));
                monitor.setLocked(true);
            });
            line.part("7 0 0", Direction.NORTH, AEParts.MONITOR);
            line.part("8 0 0", Direction.NORTH, AEParts.SEMI_DARK_MONITOR);
            line.part("9 0 0", Direction.NORTH, AEParts.DARK_MONITOR);
        }
    }

    public static ArrayList<AEColor> getColorsTransparentFirst() {
        var colors = new ArrayList<AEColor>();
        Collections.addAll(colors, AEColor.values());
        colors.remove(AEColor.TRANSPARENT);
        colors.add(0, AEColor.TRANSPARENT);
        return colors;
    }

    public static void itemChest(PlotBuilder plot) {
        plot.blockEntity("0 0 0", AEBlocks.CHEST, chest -> {
            var cellItem = AEItems.ITEM_CELL_1K.stack();
            var cellInv = StorageCells.getCellInventory(cellItem, null);
            var r = new Random();
            for (var i = 0; i < 100; i++) {
                var item = Registry.ITEM.getRandom(r).map(Holder::value).get();
                if (cellInv.insert(AEItemKey.of(item), 64, Actionable.MODULATE, new BaseActionSource()) == 0) {
                    break;
                }
            }
            chest.setCell(cellItem);
        });
        plot.creativeEnergyCell("0 -1 0");
    }

    public static void fluidChest(PlotBuilder plot) {
        plot.blockEntity("0 0 0", AEBlocks.CHEST, chest -> {
            var cellItem = AEItems.FLUID_CELL_1K.stack();
            var cellInv = StorageCells.getCellInventory(cellItem, null);
            var r = new Random();
            for (var i = 0; i < 100; i++) {
                var fluid = Registry.FLUID.getRandom(r).map(Holder::value).get();
                if (fluid.isSame(Fluids.EMPTY) || !fluid.isSource(fluid.defaultFluidState())) {
                    continue;
                }
                if (cellInv.insert(AEFluidKey.of(fluid), 64 * AEFluidKey.AMOUNT_BUCKET,
                        Actionable.MODULATE, new BaseActionSource()) == 0) {
                    break;
                }
            }
            chest.setCell(cellItem);
        });
        plot.creativeEnergyCell("0 -1 0");
    }

    public static void skyCompassRendering(PlotBuilder plot) {
        plot.block("1 0 1", Blocks.STONE);
        plot.blockEntity("0 0 1", AEBlocks.SKY_COMPASS, skyCompass -> {
            skyCompass.setOrientation(Direction.WEST, Direction.UP);
        });
        plot.blockEntity("1 0 0", AEBlocks.SKY_COMPASS, skyCompass -> {
            skyCompass.setOrientation(Direction.NORTH, Direction.UP);
        });
        plot.blockEntity("2 0 1", AEBlocks.SKY_COMPASS, skyCompass -> {
            skyCompass.setOrientation(Direction.EAST, Direction.UP);
        });
        plot.blockEntity("1 0 2", AEBlocks.SKY_COMPASS, skyCompass -> {
            skyCompass.setOrientation(Direction.SOUTH, Direction.UP);
        });
        plot.blockEntity("1 1 1", AEBlocks.SKY_COMPASS, skyCompass -> {
            skyCompass.setOrientation(Direction.UP, Direction.EAST);
        });
        plot.block("1 3 1", Blocks.STONE);
        plot.blockEntity("1 2 1", AEBlocks.SKY_COMPASS, skyCompass -> {
            skyCompass.setOrientation(Direction.DOWN, Direction.EAST);
        });
    }

    public static void crystalGrowthAutoCrafting(PlotBuilder plot) {
        // Lower subnet for formation plane and power for growth accelerators
        plot.cable("[4,6] 1 6", AEParts.GLASS_CABLE);
        plot.part("6 1 6", Direction.UP, AEParts.FORMATION_PLANE);
        plot.part("5 1 6", Direction.UP, AEParts.QUARTZ_FIBER);
        plot.cable("7 1 6", AEParts.GLASS_CABLE);
        plot.part("7 1 6", Direction.UP, AEParts.QUARTZ_FIBER);
        plot.cable("6 1 7", AEParts.GLASS_CABLE);
        plot.part("6 1 7", Direction.UP, AEParts.QUARTZ_FIBER);
        // Quartz fiber over to main net
        plot.part("4 1 6", Direction.WEST, AEParts.QUARTZ_FIBER);

        // Crystal growth part
        plot.block("5 2 6", AEBlocks.QUARTZ_GROWTH_ACCELERATOR);
        plot.block("7 2 6", AEBlocks.QUARTZ_GROWTH_ACCELERATOR);
        plot.block("6 2 5", Blocks.GLASS);
        plot.block("6 2 7", AEBlocks.QUARTZ_GROWTH_ACCELERATOR);
        plot.fluid("6 2 6", Fluids.WATER);

        // Interface that will receive the crafting ingredients
        plot.part("4 1 6", Direction.UP, AEParts.INTERFACE);
        plot.blockEntity("4 2 6", AEBlocks.PATTERN_PROVIDER, provider -> {
            // Make it point down (not strictly necessary, but more optimal)
            provider.setPushDirection(Direction.DOWN);
            // Add a pattern for fluix crystal growth
            var encodedPattern = PatternDetailsHelper.encodeProcessingPattern(
                    new GenericStack[] {
                            AEItems.CERTUS_CRYSTAL_SEED.genericStack(1)
                    },
                    new GenericStack[] {
                            AEItems.CERTUS_QUARTZ_CRYSTAL.genericStack(1)
                    });
            provider.getLogic().getPatternInv().addItems(encodedPattern);
            // Add a pattern for fluix crystal growth
            encodedPattern = PatternDetailsHelper.encodeProcessingPattern(
                    new GenericStack[] {
                            AEItems.FLUIX_CRYSTAL_SEED.genericStack(1)
                    },
                    new GenericStack[] {
                            AEItems.FLUIX_CRYSTAL.genericStack(1)
                    });
            provider.getLogic().getPatternInv().addItems(encodedPattern);
            // Add a pattern for fluix dust
            encodedPattern = PatternDetailsHelper.encodeProcessingPattern(
                    new GenericStack[] {
                            AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED.genericStack(1),
                            GenericStack.fromItemStack(new ItemStack(Items.QUARTZ)),
                            GenericStack.fromItemStack(new ItemStack(Items.REDSTONE))
                    },
                    new GenericStack[] {
                            AEItems.FLUIX_DUST.genericStack(2)
                    });
            provider.getLogic().getPatternInv().addItems(encodedPattern);
        });
        // Terminal to issue crafts + access results
        plot.cable("3 2 6", AEParts.GLASS_CABLE);
        plot.part("3 2 6", Direction.NORTH, AEParts.TERMINAL);
        plot.blockEntity("3 3 6", AEBlocks.DRIVE, drive -> {
            // Adds a creative cell with crafting ingredients
            drive.getInternalInventory().addItems(CreativeCellItem.ofItems(
                    AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED,
                    Items.QUARTZ,
                    Items.REDSTONE,
                    AEItems.CERTUS_CRYSTAL_SEED,
                    AEItems.FLUIX_CRYSTAL_SEED));
            // Add a cell to store the crafting results
            drive.getInternalInventory().addItems(AEItems.ITEM_CELL_64K.stack());
        });
        plot.block("3 1 6", AEBlocks.CREATIVE_ENERGY_CELL);
        plot.block("3 0 6", AEBlocks.CRAFTING_STORAGE_64K);

        // Top subnet for grabbing the crafting results
        plot.cable("[4,6] 3 6", AEParts.GLASS_CABLE);
        plot.part("4 3 6", Direction.WEST, AEParts.QUARTZ_FIBER);
        plot.part("4 3 6", Direction.DOWN, AEParts.STORAGE_BUS, storageBus -> {
            // Ensure only the completed crystals are accepted
            var config = storageBus.getConfig();
            config.setStack(0, AEItems.CERTUS_QUARTZ_CRYSTAL.genericStack(1));
            config.setStack(1, AEItems.FLUIX_CRYSTAL.genericStack(1));
            config.setStack(2, AEItems.FLUIX_DUST.genericStack(1));
        });
        plot.part("5 3 6", Direction.DOWN, AEParts.QUARTZ_FIBER);
        plot.part("6 3 6", Direction.DOWN, AEParts.ANNIHILATION_PLANE);
    }

    public static void importExportBus(PlotBuilder plot) {
        plot.chest("1 0 1", new ItemStack(Items.ACACIA_LOG, 16), new ItemStack(Items.ENDER_PEARL, 6));
        plot.block("1 1 1", Blocks.HOPPER);
        plot.creativeEnergyCell("3 -1 1");
        plot.cable("3 0 1")
                .part(Direction.NORTH, AEParts.TERMINAL);
        plot.cable("2 0 1")
                .part(Direction.WEST, AEParts.IMPORT_BUS);
        plot.cable("2 1 1")
                .part(Direction.WEST, AEParts.EXPORT_BUS, bus -> {
                    bus.getConfig().setStack(0, new GenericStack(AEItemKey.of(Items.ENDER_PEARL), 1));
                });
        plot.blockEntity("3 -1 0", AEBlocks.DRIVE, drive -> {
            drive.getInternalInventory().addItems(AEItems.ITEM_CELL_64K.stack());
        });
    }

    public static void inscriber(PlotBuilder plot) {
        processorInscriber(plot.offset(0, 1, 2), AEItems.LOGIC_PROCESSOR_PRESS, Items.GOLD_INGOT);
        processorInscriber(plot.offset(5, 1, 2), AEItems.ENGINEERING_PROCESSOR_PRESS, Items.DIAMOND);
        processorInscriber(plot.offset(10, 1, 2), AEItems.CALCULATION_PROCESSOR_PRESS, AEItems.CERTUS_QUARTZ_CRYSTAL);
    }

    public static void processorInscriber(PlotBuilder plot, ItemLike processorPress, ItemLike processorMaterial) {
        // Set up the inscriber for the processor print
        plot.filledHopper("-1 3 0", Direction.DOWN, processorMaterial);
        plot.creativeEnergyCell("-1 2 1");
        plot.blockEntity("-1 2 0", AEBlocks.INSCRIBER, inscriber -> {
            inscriber.getInternalInventory().setItemDirect(0, new ItemStack(processorPress));
            inscriber.setOrientation(Direction.NORTH, Direction.WEST);
        });

        // Set up the inscriber for the silicon print
        plot.filledHopper("1 3 0", Direction.DOWN, AEItems.SILICON);
        plot.creativeEnergyCell("1 2 1");
        plot.blockEntity("1 2 0", AEBlocks.INSCRIBER, inscriber -> {
            inscriber.getInternalInventory().setItemDirect(0, AEItems.SILICON_PRESS.stack());
            inscriber.setOrientation(Direction.NORTH, Direction.WEST);
        });

        // Set up the inscriber for assembly
        plot.hopper("1 1 0", Direction.WEST);
        plot.hopper("-1 1 0", Direction.EAST);
        plot.filledHopper("0 2 0", Direction.DOWN, Items.REDSTONE);
        plot.creativeEnergyCell("0 1 1");
        plot.blockEntity("0 1 0", AEBlocks.INSCRIBER, inscriber -> {
            inscriber.setOrientation(Direction.NORTH, Direction.WEST);
        });
        plot.hopper("0 0 0", Direction.DOWN);
    }

    /**
     * Reproduces an issue with Fabric Transactions found in
     * https://github.com/AppliedEnergistics/Applied-Energistics-2/issues/5798
     */
    public static void importAndExportInOneTick(PlotBuilder plot) {
        plot.creativeEnergyCell("-1 0 0");
        plot.chest("0 0 1"); // Output Chest
        plot.cable("0 0 0")
                .part(Direction.SOUTH, AEParts.EXPORT_BUS, exportBus -> {
                    exportBus.getUpgrades().addItems(AEItems.CRAFTING_CARD.stack());
                    exportBus.getConfig().addFilter(Items.OAK_PLANKS);
                });
        plot.cable("0 1 0");
        plot.cable("0 1 -1")
                .craftingEmitter(Direction.DOWN, Items.OAK_PLANKS);
        plot.cable("0 0 -1")
                .part(Direction.NORTH, AEParts.IMPORT_BUS, part -> {
                    part.getUpgrades().addItems(AEItems.REDSTONE_CARD.stack());
                    part.getConfigManager().putSetting(Settings.REDSTONE_CONTROLLED, RedstoneMode.HIGH_SIGNAL);
                });
        plot.block("1 0 0", AEBlocks.CRAFTING_STORAGE_1K);
        plot.chest("0 0 -2", new ItemStack(Items.OAK_PLANKS, 1)); // Input Chest

        plot.test(helper -> {
            helper.succeedWhen(() -> {
                helper.assertContainerContains(new BlockPos(0, 0, 1), Items.OAK_PLANKS);
                helper.assertContainerEmpty(new BlockPos(0, 0, -2));
            });
        });
    }

    /**
     * Export from a chest->storagebus->exportbus->chest to test that it interacts correctly with Fabric transactions.
     */
    public static void exportFromStorageBus(PlotBuilder plot) {
        plot.creativeEnergyCell("1 0 0");
        plot.cable("0 0 0")
                .part(Direction.SOUTH, AEParts.EXPORT_BUS, part -> {
                    part.getConfig().addFilter(Items.OAK_PLANKS);
                })
                .part(Direction.NORTH, AEParts.STORAGE_BUS);
        plot.chest("0 0 1"); // Output Chest
        plot.chest("0 0 -1", new ItemStack(Items.OAK_PLANKS)); // Import Chest

        plot.test(helper -> {
            helper.succeedWhen(() -> {
                helper.assertContainerContains(new BlockPos(0, 0, 1), Items.OAK_PLANKS);
                helper.assertContainerEmpty(new BlockPos(0, 0, -1));
            });
        });
    }

    /**
     * Import into a storage bus, which tests that the external interaction is correct w.r.t. Fabric transactions.
     */
    public static void importIntoStorageBus(PlotBuilder plot) {
        plot.creativeEnergyCell("1 0 0");
        plot.cable("0 0 0")
                .part(Direction.NORTH, AEParts.IMPORT_BUS)
                .part(Direction.SOUTH, AEParts.STORAGE_BUS);
        plot.chest("0 0 1"); // Output Chest
        plot.chest("0 0 -1", new ItemStack(Items.OAK_PLANKS)); // Import Chest

        plot.test(helper -> {
            helper.succeedWhen(() -> {
                helper.assertContainerContains(new BlockPos(0, 0, 1), Items.OAK_PLANKS);
                helper.assertContainerEmpty(new BlockPos(0, 0, -1));
            });
            helper.startSequence()
                    .thenIdle(10)
                    .thenSucceed();
        });
    }

    /**
     * Import on Pulse (transition low->high)
     */
    public static void importOnPulse(PlotBuilder plot) {
        var origin = BlockPos.ZERO;
        var inputPos = origin.south();

        plot.creativeEnergyCell(origin.west().west());
        plot.storageDrive(origin.west());
        plot.cable(origin)
                .part(Direction.SOUTH, AEParts.IMPORT_BUS, bus -> {
                    bus.getUpgrades().addItems(AEItems.REDSTONE_CARD.stack());
                    bus.getConfigManager().putSetting(Settings.REDSTONE_CONTROLLED, RedstoneMode.SIGNAL_PULSE);
                })
                .part(Direction.NORTH, AEParts.TERMINAL);
        plot.chest(inputPos, new ItemStack(Items.OAK_PLANKS)); // Import Chest
        plot.block(origin.east(), Blocks.STONE);
        var leverPos = plot.leverOn(origin.east(), Direction.NORTH);

        plot.test(helper -> {
            // Import bus should import nothing on its own
            var inputChest = (ChestBlockEntity) helper.getBlockEntity(inputPos);
            var grid = helper.getGrid(origin);
            Runnable assertNothingMoved = () -> {
                helper.assertContainerContains(inputPos, Items.OAK_PLANKS);
            };
            Runnable assertMoved = () -> {
                helper.assertContainerEmpty(inputPos);
                helper.assertContains(grid, Items.OAK_PLANKS);
            };
            Runnable reset = () -> {
                inputChest.clearContent();
                helper.clearStorage(grid);
                inputChest.setItem(0, new ItemStack(Items.OAK_PLANKS));
            };
            Runnable toggleSignal = () -> {
                helper.pullLever(leverPos);
            };

            helper.startSequence()
                    .thenExecuteAfter(1, assertNothingMoved)
                    .thenExecute(toggleSignal)
                    // The items should only be moved on the subsequent tick
                    .thenExecute(assertNothingMoved)
                    .thenExecuteAfter(1, assertMoved)
                    .thenExecute(reset)
                    // The transition from on->off should NOT count as a pulse,
                    // and it should not move anything on its own afterwards
                    .thenExecute(toggleSignal)
                    .thenExecuteFor(30, assertNothingMoved)
                    .thenSucceed();
        });
    }

    /**
     * Import on Pulse (transition low->high), combined with the storage bus attached to the storage we are importing
     * from. This is a regression test for Fabric, where the Storage Bus has to open a Transaction for
     * getAvailableStacks, and the simulated extraction causes a neighbor update, triggering the import bus.
     */
    public static void importOnPulseTransactionCrash(PlotBuilder plot) {
        plot.creativeEnergyCell("1 0 0");
        plot.chest("0 0 -1", new ItemStack(Items.OAK_PLANKS)); // Import Chest
        plot.chest("0 0 1"); // Output Chest
        plot.block("0 1 0", Blocks.REDSTONE_BLOCK);
        plot.cable("-1 0 0");
        // This storage bus triggers a neighbor update on the input chest when it scans its inventory
        plot.cable("-1 0 -1")
                .part(Direction.EAST, AEParts.STORAGE_BUS, storageBus -> {
                    storageBus.getConfigManager().putSetting(Settings.ACCESS, AccessRestriction.READ);
                });
        plot.cable("0 0 0")
                .part(Direction.SOUTH, AEParts.STORAGE_BUS);

        // The planks should never move over to the output chest since there's never an actual pulse
        plot.test(helper -> {
            helper.startSequence()
                    .thenExecuteAfter(1, () -> {
                        var pos = helper.absolutePos(BlockPos.ZERO);
                        var importBus = PartHelper.setPart(helper.getLevel(), pos, Direction.NORTH,
                                null, AEParts.IMPORT_BUS.asItem());
                        importBus.getUpgrades().addItems(AEItems.REDSTONE_CARD.stack());
                        importBus.getConfigManager().putSetting(Settings.REDSTONE_CONTROLLED,
                                RedstoneMode.SIGNAL_PULSE);
                    })
                    .thenExecuteFor(100, () -> {
                        // Force an inventory rescan
                        helper.assertContainerEmpty(new BlockPos(0, 0, 1));
                    })
                    .thenSucceed();
        }).setupTicks(20).maxTicks(150);
    }

    public static void matterCannonRange(PlotBuilder plot) {
        var origin = BlockPos.ZERO;

        plot.fencedEntity(origin.offset(0, 0, 5), EntityType.COW, entity -> {
            entity.setSilent(true);
        });
        plot.creativeEnergyCell(origin.below());
        plot.blockEntity(
                origin,
                AEBlocks.CHEST,
                chest -> chest.setCell(createMatterCannon(Items.IRON_NUGGET)));

        plot.block("-2 [0,1] 5", Blocks.STONE);
        plot.block("2 [0,1] 5", Blocks.STONE);

        plot.creativeEnergyCell(origin.west().below());
        plot.block(origin.west(), AEBlocks.CHARGER);

        matterCannonDispenser(plot.offset(-2, 1, 1), AEItems.COLORED_LUMEN_PAINT_BALL.item(AEColor.PURPLE));
        matterCannonDispenser(plot.offset(0, 1, 1), Items.IRON_NUGGET);
        matterCannonDispenser(plot.offset(2, 1, 1));
    }

    private static void matterCannonDispenser(PlotBuilder plot, Item... ammos) {
        plot.blockState(BlockPos.ZERO, Blocks.DISPENSER.defaultBlockState()
                .setValue(DispenserBlock.FACING, Direction.SOUTH));
        plot.customizeBlockEntity(BlockPos.ZERO, BlockEntityType.DISPENSER, dispenser -> {
            dispenser.addItem(createMatterCannon(ammos));
        });
        plot.buttonOn(BlockPos.ZERO, Direction.NORTH);
    }

    private static ItemStack createMatterCannon(Item... ammo) {
        var cannon = AEItems.MATTER_CANNON.stack();
        ((MatterCannonItem) cannon.getItem()).injectAEPower(cannon, Double.MAX_VALUE, Actionable.MODULATE);
        var cannonInv = BasicCellInventory.createInventory(cannon, null);
        for (var item : ammo) {
            cannonInv.insert(
                    AEItemKey.of(item), item.getMaxStackSize(), Actionable.MODULATE, new BaseActionSource());
        }
        return cannon;
    }

    /**
     * Regression test for https://github.com/AppliedEnergistics/Applied-Energistics-2/issues/5821
     */
    public static void testInsertFluidIntoMEChest(PlotBuilder plot) {
        var origin = BlockPos.ZERO;
        plot.creativeEnergyCell(origin.below());
        plot.blockEntity(origin, AEBlocks.CHEST, chest -> {
            chest.setCell(AEItems.FLUID_CELL_4K.stack());
        });
        plot.cable(origin.east())
                .part(Direction.WEST, AEParts.EXPORT_BUS, bus -> {
                    bus.getConfig().addFilter(Fluids.WATER);
                });
        plot.blockEntity(origin.east().north(), AEBlocks.DRIVE, drive -> {
            drive.getInternalInventory().addItems(CreativeCellItem.ofFluids(Fluids.WATER));
        });
        plot.creativeEnergyCell(origin.east().north().below());

        plot.test(helper -> helper.succeedWhen(() -> {
            var meChest = (appeng.blockentity.storage.ChestBlockEntity) helper.getBlockEntity(origin);
            helper.assertContains(meChest.getInventory(), AEFluidKey.of(Fluids.WATER));
        }));
    }

    public static void maxChannelsAdHocTest(PlotBuilder plot) {
        plot.creativeEnergyCell("0 -1 0");
        plot.block("[-3,3] -2 [-3,3]", AEBlocks.DRIVE);
        plot.cable("[-3,3] 0 [-3,3]", AEParts.SMART_DENSE_CABLE);
        plot.cable("[-3,3] [1,64] [-3,2]")
                .part(Direction.EAST, AEParts.TERMINAL)
                .part(Direction.NORTH, AEParts.TERMINAL)
                .part(Direction.WEST, AEParts.TERMINAL)
                .part(Direction.WEST, AEParts.TERMINAL);
        plot.cable("[-3,3] [1,64] 3")
                .part(Direction.NORTH, AEParts.PATTERN_PROVIDER)
                .part(Direction.SOUTH, AEParts.PATTERN_PROVIDER)
                .part(Direction.EAST, AEParts.PATTERN_PROVIDER)
                .part(Direction.WEST, AEParts.PATTERN_PROVIDER);

        plot.afterGridExistsAt("0 0 0", (grid, node) -> {
            // This has so many nodes it needs infinite mode
            ((PathingService) grid.getPathingService()).setForcedChannelMode(ChannelMode.INFINITE);

            var patternProviders = grid.getMachines(PatternProviderPart.class).iterator();
            PatternProviderPart current = patternProviders.next();
            var craftingRecipes = node.getLevel().getRecipeManager().getAllRecipesFor(RecipeType.CRAFTING);

            Set<AEItemKey> neededIngredients = new HashSet<>();
            Set<AEItemKey> providedResults = new HashSet<>();

            for (var recipe : craftingRecipes) {
                if (recipe.isSpecial()) {
                    continue;
                }

                ItemStack craftingPattern;
                try {
                    var ingredients = CraftingRecipeUtil.ensure3by3CraftingMatrix(recipe).stream()
                            .map(i -> {
                                if (i.isEmpty()) {
                                    return ItemStack.EMPTY;
                                } else {
                                    return i.getItems()[0];
                                }
                            }).toArray(ItemStack[]::new);
                    craftingPattern = PatternDetailsHelper.encodeCraftingPattern(
                            recipe,
                            ingredients,
                            recipe.getResultItem(),
                            false,
                            false);

                    for (ItemStack ingredient : ingredients) {
                        var key = AEItemKey.of(ingredient);
                        if (key != null) {
                            neededIngredients.add(key);
                        }
                    }
                    if (!recipe.getResultItem().isEmpty()) {
                        providedResults.add(AEItemKey.of(recipe.getResultItem()));
                    }
                } catch (Exception e) {
                    AELog.warn(e);
                    continue;
                }

                if (!current.getLogic().getPatternInv().addItems(craftingPattern).isEmpty()) {
                    if (!patternProviders.hasNext()) {
                        break;
                    }
                    current = patternProviders.next();
                    current.getLogic().getPatternInv().addItems(craftingPattern);
                }
            }

            // Add creative cells for anything that's not provided as a recipe result
            var keysToAdd = Sets.difference(neededIngredients, providedResults).iterator();
            drives: for (var drive : grid.getMachines(DriveBlockEntity.class)) {

                var cellInv = drive.getInternalInventory();
                for (int i = 0; i < cellInv.size(); i++) {
                    var creativeCell = AEItems.ITEM_CELL_CREATIVE.stack();
                    var configInv = AEItems.ITEM_CELL_CREATIVE.asItem().getConfigInventory(creativeCell);

                    for (int j = 0; j < configInv.size(); j++) {
                        if (!keysToAdd.hasNext()) {
                            cellInv.addItems(creativeCell);
                            break drives;
                        }

                        var keyToAdd = keysToAdd.next();
                        configInv.setStack(j, new GenericStack(keyToAdd, 1));
                    }
                    cellInv.addItems(creativeCell);

                }
            }
        });
    }

    /**
     * Regression test for https://github.com/AppliedEnergistics/Applied-Energistics-2/issues/5860.
     */
    public static void blockingModeSubnetworkChestTest(PlotBuilder plot) {
        // Network itself
        plot.creativeEnergyCell("0 -1 0");
        plot.block("[0,1] [0,1] [0,1]", AEBlocks.CRAFTING_ACCELERATOR);
        plot.block("0 0 0", AEBlocks.CRAFTING_STORAGE_64K);
        var input = GenericStack.fromItemStack(new ItemStack(Items.GOLD_INGOT));
        var output = GenericStack.fromItemStack(new ItemStack(Items.DIAMOND));
        plot.cable("2 0 0")
                .part(Direction.EAST, AEParts.PATTERN_PROVIDER, pp -> {
                    pp.getLogic().getPatternInv().addItems(
                            PatternDetailsHelper.encodeProcessingPattern(
                                    new GenericStack[] { input },
                                    new GenericStack[] { output }));
                    pp.getLogic().getConfigManager().putSetting(Settings.BLOCKING_MODE, YesNo.YES);
                });
        plot.drive(new BlockPos(2, 0, -1))
                .addCreativeCell()
                .add(input);
        // Subnetwork
        plot.creativeEnergyCell("3 -1 0");
        plot.cable("3 0 0")
                .part(Direction.WEST, AEParts.INTERFACE)
                .part(Direction.EAST, AEParts.STORAGE_BUS);
        plot.block("4 0 0", Blocks.CHEST);
        // Crafting operation
        plot.test(helper -> {
            var craftingJob = new TestCraftingJob(helper, BlockPos.ZERO, output.what(), 64);
            helper.startSequence()
                    .thenWaitUntil(craftingJob::tickUntilStarted)
                    .thenWaitUntil(() -> {
                        var grid = helper.getGrid(BlockPos.ZERO);
                        var requesting = grid.getCraftingService().getRequestedAmount(output.what());
                        helper.check(requesting > 0, "not yet requesting items");
                        if (requesting != 1) {
                            helper.fail("blocking mode failed, requesting: " + requesting);
                        }
                    })
                    .thenSucceed();
        });
    }

    /**
     * Regression test for https://github.com/AppliedEnergistics/Applied-Energistics-2/issues/5919
     */
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

    /**
     * Simple terminal full of enchanted items to test rendering performance.
     */
    public static void terminalFullOfEnchantedItems(PlotBuilder plot) {
        var origin = BlockPos.ZERO;
        plot.creativeEnergyCell(origin.below());
        plot.cable(origin).part(Direction.NORTH, AEParts.TERMINAL);
        var drive = plot.drive(origin.east());

        var pickaxe = new ItemStack(Items.DIAMOND_PICKAXE);
        pickaxe.enchant(Enchantments.BLOCK_FORTUNE, 1);
        for (var i = 0; i < 10; i++) {
            var cell = drive.addItemCell64k();
            for (var j = 0; j < 63; j++) {
                pickaxe.setDamageValue(pickaxe.getDamageValue() + 1);
                cell.add(AEItemKey.of(pickaxe), 2);
            }
        }
    }

    public static void importLavaFromCauldron(PlotBuilder plot) {
        var origin = BlockPos.ZERO;
        plot.creativeEnergyCell(origin.below());
        plot.cable(origin)
                .part(Direction.EAST, AEParts.IMPORT_BUS, importBus -> {
                    importBus.getUpgrades().addItems(AEItems.SPEED_CARD.stack());
                })
                .part(Direction.WEST, AEParts.STORAGE_BUS);
        plot.block(origin.west(), AEBlocks.SKY_STONE_TANK);
        plot.block(origin.east(), Blocks.LAVA_CAULDRON);
        plot.test(helper -> {
            helper.succeedWhen(() -> {
                helper.assertBlockPresent(Blocks.CAULDRON, origin.east());
                var tank = (SkyStoneTankBlockEntity) helper.getBlockEntity(origin.west());
                helper.check(tank.getStorage().getFluidAmount() == AEFluidKey.AMOUNT_BUCKET,
                        "Less than a bucket stored");
                helper.check(tank.getStorage().getFluid().getFluid() == Fluids.LAVA,
                        "Something other than lava stored");
            });
        });
    }

    /**
     * Regression test for https://github.com/AppliedEnergistics/Applied-Energistics-2/issues/6104. Ensures that
     * repairing tools properly checks for damage values.
     */
    public static void toolRepairRecipe(PlotBuilder plot) {
        var undamaged = AEItemKey.of(Items.DIAMOND_PICKAXE);
        var maxDamage = undamaged.getFuzzySearchMaxValue();
        var damaged = Util.make(() -> {
            var is = undamaged.toStack();
            is.setDamageValue(maxDamage - 1);
            return AEItemKey.of(is);
        });
        var correctResult = Util.make(() -> {
            var is = undamaged.toStack();
            var usesLeft = 2 + maxDamage * 5 / 100;
            is.setDamageValue(maxDamage - usesLeft);
            return AEItemKey.of(is);
        });

        plot.creativeEnergyCell("0 0 0");
        var molecularAssemblerPos = new BlockPos(0, 1, 0);
        plot.blockEntity(molecularAssemblerPos, AEBlocks.MOLECULAR_ASSEMBLER, molecularAssembler -> {
            // Get repair recipe
            var craftingContainer = new CraftingContainer(new NullMenu(), 3, 3);
            craftingContainer.setItem(0, undamaged.toStack());
            craftingContainer.setItem(1, undamaged.toStack());
            var level = molecularAssembler.getLevel();
            var recipe = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, craftingContainer, level).get();

            // Encode pattern
            var sparseInputs = new ItemStack[9];
            sparseInputs[0] = undamaged.toStack();
            sparseInputs[1] = undamaged.toStack();
            for (int i = 2; i < 9; ++i)
                sparseInputs[i] = ItemStack.EMPTY;
            var encodedPattern = PatternDetailsHelper.encodeCraftingPattern(recipe, sparseInputs, undamaged.toStack(),
                    true, false);
            var patternDetails = PatternDetailsHelper.decodePattern(encodedPattern, level);

            // Push it to the assembler
            var table = new KeyCounter[] { new KeyCounter() };
            table[0].add(damaged, 2);
            molecularAssembler.pushPattern(patternDetails, table, Direction.UP);
        });

        plot.test(helper -> {
            helper.runAfterDelay(40, () -> {
                var molecularAssembler = (MolecularAssemblerBlockEntity) helper.getBlockEntity(molecularAssemblerPos);
                var outputItem = molecularAssembler.getInternalInventory().getStackInSlot(9);
                if (correctResult.matches(outputItem)) {
                    helper.succeed();
                } else if (undamaged.matches(outputItem)) {
                    helper.fail("created undamaged item");
                }
            });
        });
    }

    /**
     * Placing a storage bus on a double chest should report the content of both chests.
     */
    private static void doubleChestStorageBus(PlotBuilder plot) {
        var o = BlockPos.ZERO;
        plot.chest(o.north(), new ItemStack(Items.STICK));
        plot.chest(o.north().west(), new ItemStack(Items.STICK));
        plot.blockState(o.north(), Blocks.CHEST.defaultBlockState().setValue(ChestBlock.TYPE, ChestType.RIGHT));
        plot.blockState(o.north().west(), Blocks.CHEST.defaultBlockState().setValue(ChestBlock.TYPE, ChestType.LEFT));
        plot.cable(o).part(Direction.NORTH, AEParts.STORAGE_BUS);
        plot.creativeEnergyCell(o.below());

        plot.test(helper -> {
            helper.succeedWhen(() -> {
                var grid = helper.getGrid(o);
                var stacks = grid.getStorageService().getInventory().getAvailableStacks();
                var stickCount = stacks.get(AEItemKey.of(Items.STICK));
                // The contents of both chest halves should be reported
                helper.check(2 == stickCount, "Stick count wasn't 2: " + stickCount);
            });
        });
    }

}
