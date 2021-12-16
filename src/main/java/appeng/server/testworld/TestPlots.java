package appeng.server.testworld;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;

import com.google.common.collect.ImmutableMap;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.material.Fluids;

import appeng.api.config.Actionable;
import appeng.api.config.RedstoneMode;
import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.StorageCells;
import appeng.api.util.AEColor;
import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.AEParts;
import appeng.items.storage.CreativeCellItem;
import appeng.me.helpers.BaseActionSource;

public final class TestPlots {
    private static final Map<ResourceLocation, Consumer<PlotBuilder>> PLOT_FACTORIES = ImmutableMap
            .<ResourceLocation, Consumer<PlotBuilder>>builder()
            .put(AppEng.makeId("plot/allterminals"), TestPlots::allTerminals)
            .put(AppEng.makeId("plot/itemchest"), TestPlots::itemChest)
            .put(AppEng.makeId("plot/fluidchest"), TestPlots::fluidChest)
            .put(AppEng.makeId("plot/skycompassrendering"), TestPlots::skyCompassRendering)
            .put(AppEng.makeId("plot/crystalgrowthautocrafting"), TestPlots::crystalGrowthAutoCrafting)
            .put(AppEng.makeId("plot/importexportbus"), TestPlots::importExportBus)
            .put(AppEng.makeId("plot/inscriber"), TestPlots::inscriber)
            .put(AppEng.makeId("plot/autocraftingtestplot"), AutoCraftingTestPlot::create)
            .put(AppEng.makeId("plot/importandexportinonetick"), TestPlots::importAndExportInOneTick)
            .build();

    private TestPlots() {
    }

    public static ArrayList<Plot> createPlots() {
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
        plot.creativeEnergyCell("0 -1 0");

        plot.cable("[-1,0] [0,8] 0", AEParts.COVERED_DENSE_CABLE);
        plot.part("0 [0,8] 0", Direction.WEST, AEParts.CABLE_ANCHOR);
        plot.block("[-1,0] 5 0", AEBlocks.CONTROLLER);

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
            line.cable("[1,9] 0 0", AEParts.GLASS_CABLE.stack(color));
            if (color == AEColor.TRANSPARENT) {
                line.part("[1,9] 0 0", Direction.UP, AEParts.CABLE_ANCHOR);
            }
            line.part("1 0 0", Direction.NORTH, AEParts.TERMINAL);
            line.part("2 0 0", Direction.NORTH, AEParts.CRAFTING_TERMINAL);
            line.part("3 0 0", Direction.NORTH, AEParts.PATTERN_ENCODING_TERMINAL);
            line.part("4 0 0", Direction.NORTH, AEParts.PATTERN_ACCESS_TERMINAL);
            line.part("5 0 0", Direction.NORTH, AEParts.STORAGE_MONITOR, monitor -> {
                var diamondPickaxe = new ItemStack(Items.DIAMOND_PICKAXE);
                diamondPickaxe.enchant(Enchantments.BLOCK_FORTUNE, 3);
                monitor.setConfiguredItem(AEItemKey.of(diamondPickaxe));
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

    private static ArrayList<AEColor> getColorsTransparentFirst() {
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
                var item = Registry.ITEM.getRandom(r);
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
                var fluid = Registry.FLUID.getRandom(r);
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
            provider.getDuality().getPatternInv().addItems(encodedPattern);
            // Add a pattern for fluix crystal growth
            encodedPattern = PatternDetailsHelper.encodeProcessingPattern(
                    new GenericStack[] {
                            AEItems.FLUIX_CRYSTAL_SEED.genericStack(1)
                    },
                    new GenericStack[] {
                            AEItems.FLUIX_CRYSTAL.genericStack(1)
                    });
            provider.getDuality().getPatternInv().addItems(encodedPattern);
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
            provider.getDuality().getPatternInv().addItems(encodedPattern);
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

    private static void inscriber(PlotBuilder plot) {
        processorInscriber(plot.offset(0, 1, 2), AEItems.LOGIC_PROCESSOR_PRESS, Items.GOLD_INGOT);
        processorInscriber(plot.offset(5, 1, 2), AEItems.ENGINEERING_PROCESSOR_PRESS, Items.DIAMOND);
        processorInscriber(plot.offset(10, 1, 2), AEItems.CALCULATION_PROCESSOR_PRESS, AEItems.CERTUS_QUARTZ_CRYSTAL);
    }

    private static void processorInscriber(PlotBuilder plot, ItemLike processorPress, ItemLike processorMaterial) {
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
    private static void importAndExportInOneTick(PlotBuilder plot) {
        plot.creativeEnergyCell("-1 0 0");
        plot.chest("0 0 1"); // Output Chest
        plot.cable("0 0 0")
                .part(Direction.SOUTH, AEParts.EXPORT_BUS, exportBus -> {
                    exportBus.getUpgrades().addItems(AEItems.CRAFTING_CARD.stack());
                    exportBus.getConfig().insert(0, AEItemKey.of(Items.OAK_PLANKS), 1, Actionable.MODULATE);
                });
        plot.cable("0 1 0");
        plot.cable("0 1 -1")
                .part(Direction.DOWN, AEParts.LEVEL_EMITTER, part -> {
                    part.getUpgrades().addItems(AEItems.CRAFTING_CARD.stack());
                    part.getConfig().insert(0, AEItemKey.of(Items.OAK_PLANKS), 1, Actionable.MODULATE);
                    part.getConfigManager().putSetting(Settings.CRAFT_VIA_REDSTONE, YesNo.YES);
                });
        plot.cable("0 0 -1")
                .part(Direction.NORTH, AEParts.IMPORT_BUS, part -> {
                    part.getUpgrades().addItems(AEItems.REDSTONE_CARD.stack());
                    part.getConfigManager().putSetting(Settings.REDSTONE_CONTROLLED, RedstoneMode.HIGH_SIGNAL);
                });
        plot.block("1 0 0", AEBlocks.CRAFTING_STORAGE_1K);
        plot.chest("0 0 -2", new ItemStack(Items.OAK_PLANKS, 1)); // Input Chest

        plot.addTest("itemInserted", helper -> {
            helper.succeedWhen(() -> {
                helper.assertContainerContains(new BlockPos(0, 0, 1), Items.OAK_PLANKS);
                helper.assertContainerEmpty(new BlockPos(0, 0, -2));
            });
        });
    }
}
