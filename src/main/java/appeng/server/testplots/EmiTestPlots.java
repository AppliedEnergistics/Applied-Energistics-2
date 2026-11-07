package appeng.server.testplots;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import appeng.api.networking.pathing.ChannelMode;
import appeng.api.stacks.AEItemKey;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.AEParts;
import appeng.me.service.PathingService;
import appeng.server.testworld.PlotBuilder;

/**
 * Plots with a network holding a large number of distinct item types, for profiling how the recipe viewers handle those
 * contents. Profile by hand: {@code /ae2 setuptestworld ae2:emi_network_inventory_all_items}, then switch EMI's sidebar
 * to the craftables page. {@code _churn} keeps the amounts changing; EMI merges items that differ only in components,
 * so the sized plots show it fewer stacks than AE2 keys.
 */
@TestPlotClass
public final class EmiTestPlots {
    private static final int TYPES_PER_CELL = 63;
    private static final int CELLS_PER_DRIVE = 10;

    private static final long AMOUNT_PER_TYPE = 64;

    private EmiTestPlots() {
    }

    @TestPlotGenerator
    public static void generateNetworkInventoryPlots(TestPlotCollection plots) {
        plots.add("emi_network_inventory_all_items",
                plot -> buildNetworkInventoryPlot(plot, createAllItemKeys(), false));

        plots.add("emi_network_inventory_50000",
                plot -> buildNetworkInventoryPlot(plot, createKeys(50000), false));

        plots.add("emi_network_inventory_50000_churn",
                plot -> buildNetworkInventoryPlot(plot, createKeys(50000), true));
    }

    private static void buildNetworkInventoryPlot(PlotBuilder plot, List<AEItemKey> keys, boolean churn) {
        var typeCount = keys.size();
        var drives = (typeCount + TYPES_PER_CELL * CELLS_PER_DRIVE - 1) / (TYPES_PER_CELL * CELLS_PER_DRIVE);

        plot.creativeEnergyCell("0 -1 0");
        plot.afterGridExistsAt(new BlockPos(-1, 0, 0),
                (grid, node) -> ((PathingService) grid.getPathingService()).setForcedChannelMode(ChannelMode.INFINITE));
        plot.cable("-1 0 0")
                .part(Direction.NORTH, AEParts.CRAFTING_TERMINAL)
                .part(Direction.SOUTH, AEParts.PATTERN_ENCODING_TERMINAL);

        if (churn) {
            plot.chest("0 0 2");
            plot.cable("0 0 1")
                    .part(Direction.SOUTH, AEParts.EXPORT_BUS, bus -> {
                        bus.getConfig().addFilter(Items.STONE);
                        addSpeedCards(bus);
                    });
            plot.cable("1 0 1");
            plot.cable("1 0 2")
                    .part(Direction.WEST, AEParts.IMPORT_BUS, EmiTestPlots::addSpeedCards);
        }

        plot.denseCable("[0," + (drives - 1) + "] 0 0");

        var keyIndex = 0;
        for (var driveIndex = 0; driveIndex < drives; driveIndex++) {
            var drive = plot.drive(new BlockPos(driveIndex, 1, 0));
            for (var cellIndex = 0; cellIndex < CELLS_PER_DRIVE && keyIndex < typeCount; cellIndex++) {
                var cell = drive.addItemCell64k();
                for (var typeInCell = 0; typeInCell < TYPES_PER_CELL && keyIndex < typeCount; typeInCell++) {
                    cell.add(keys.get(keyIndex++), AMOUNT_PER_TYPE);
                }
            }
        }
    }

    private static void addSpeedCards(IUpgradeableObject part) {
        for (var i = 0; i < 3; i++) {
            part.getUpgrades().addItems(AEItems.SPEED_CARD.stack());
        }
    }

    static List<AEItemKey> createAllItemKeys() {
        var keys = new ArrayList<AEItemKey>();
        var seen = new HashSet<AEItemKey>();
        for (var item : BuiltInRegistries.ITEM) {
            var key = AEItemKey.of(item.getDefaultInstance());
            if (key != null && seen.add(key)) {
                keys.add(key);
            }
        }
        return keys;
    }

    /**
     * The given number of distinct keys: registered items first, then custom-named tools.
     */
    static List<AEItemKey> createKeys(int count) {
        var keys = new ArrayList<AEItemKey>(count);
        var seen = new HashSet<AEItemKey>();

        for (var key : createAllItemKeys()) {
            if (keys.size() >= count) {
                break;
            }
            if (seen.add(key)) {
                keys.add(key);
            }
        }

        var template = new ItemStack(Items.DIAMOND_PICKAXE);
        for (var i = 0; keys.size() < count; i++) {
            var stack = template.copy();
            stack.set(DataComponents.CUSTOM_NAME, Component.literal("Perf Item " + i));
            var key = AEItemKey.of(stack);
            if (key != null && seen.add(key)) {
                keys.add(key);
            }
        }

        return keys;
    }
}
