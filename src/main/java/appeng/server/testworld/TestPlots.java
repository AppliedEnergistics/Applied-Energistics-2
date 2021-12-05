package appeng.server.testworld;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import appeng.api.config.Actionable;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.storage.StorageCells;
import appeng.api.util.AEColor;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.AEParts;
import appeng.me.helpers.BaseActionSource;

final class TestPlots {
    private TestPlots() {
    }

    public static ArrayList<Plot> createPlots() {
        var plots = new ArrayList<Plot>();
        Collections.addAll(plots,
                allTerminals(),
                itemChest(),
                fluidChest());
        return plots;
    }

    /**
     * A wall of all terminals/monitors in all color combinations.
     */
    public static Plot allTerminals() {
        var plot = new Plot();
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
        return plot;
    }

    private static ArrayList<AEColor> getColorsTransparentFirst() {
        var colors = new ArrayList<AEColor>();
        Collections.addAll(colors, AEColor.values());
        colors.remove(AEColor.TRANSPARENT);
        colors.add(0, AEColor.TRANSPARENT);
        return colors;
    }

    public static Plot itemChest() {
        var plot = new Plot();
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
        return plot;
    }

    public static Plot fluidChest() {
        var plot = new Plot();
        plot.blockEntity("0 0 0", AEBlocks.CHEST, chest -> {
            var cellItem = AEItems.FLUID_CELL_1K.stack();
            var cellInv = StorageCells.getCellInventory(cellItem, null);
            var r = new Random();
            for (var i = 0; i < 100; i++) {
                var fluid = Registry.FLUID.getRandom(r);
                if (cellInv.insert(AEFluidKey.of(fluid), 64 * AEFluidKey.AMOUNT_BUCKET,
                        Actionable.MODULATE, new BaseActionSource()) == 0) {
                    break;
                }
            }
            chest.setCell(cellItem);
        });
        plot.creativeEnergyCell("0 -1 0");
        return plot;
    }
}
