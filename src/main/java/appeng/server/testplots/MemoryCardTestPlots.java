package appeng.server.testplots;

import java.util.Objects;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;

import appeng.api.config.FuzzyMode;
import appeng.api.config.RedstoneMode;
import appeng.api.config.SchedulingMode;
import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.inventories.InternalInventory;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.util.IConfigurableObject;
import appeng.blockentity.crafting.PatternProviderBlockEntity;
import appeng.blockentity.misc.InterfaceBlockEntity;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.AEParts;
import appeng.items.tools.NetworkToolItem;
import appeng.parts.automation.ExportBusPart;
import appeng.parts.crafting.PatternProviderPart;
import appeng.parts.misc.InterfacePart;
import appeng.server.testworld.PlotBuilder;
import appeng.server.testworld.PlotTestHelper;
import appeng.util.SettingsFrom;

public final class MemoryCardTestPlots {
    private MemoryCardTestPlots() {
    }

    @TestPlot("memcard_export_bus")
    public static void testExportBus(PlotBuilder plot) {
        var origin = BlockPos.ZERO;
        plot.creativeEnergyCell(origin.below());
        plot.cable(origin)
                .part(Direction.WEST, AEParts.EXPORT_BUS)
                .part(Direction.EAST, AEParts.EXPORT_BUS);

        plot.test(helper -> {
            var fromPart = helper.getPart(BlockPos.ZERO, Direction.WEST, ExportBusPart.class);
            var toPart = helper.getPart(BlockPos.ZERO, Direction.EAST, ExportBusPart.class);

            var player = helper.makeMockPlayer();
            var networkToolInv = addNetworkToolToPlayer(player);

            // Run part-specific setup code
            fromPart.getUpgrades().addItems(AEItems.REDSTONE_CARD.stack());
            fromPart.getUpgrades().addItems(AEItems.FUZZY_CARD.stack());
            fromPart.getUpgrades().addItems(AEItems.CRAFTING_CARD.stack());
            fromPart.getUpgrades().addItems(AEItems.CAPACITY_CARD.stack());
            fromPart.getConfig().addFilter(Items.STICK);
            fromPart.getConfig().addFilter(Fluids.WATER);

            fromPart.getConfigManager().putSetting(Settings.REDSTONE_CONTROLLED, RedstoneMode.SIGNAL_PULSE);
            fromPart.getConfigManager().putSetting(Settings.FUZZY_MODE, FuzzyMode.PERCENT_25);
            fromPart.getConfigManager().putSetting(Settings.CRAFT_ONLY, YesNo.YES);
            fromPart.getConfigManager().putSetting(Settings.SCHEDULING_MODE, SchedulingMode.RANDOM);

            copyUpgradesToNetworkInv(fromPart, networkToolInv);

            // Export&Import settings
            var settings = new CompoundTag();
            fromPart.exportSettings(SettingsFrom.MEMORY_CARD, settings);
            toPart.importSettings(SettingsFrom.MEMORY_CARD, settings, player);

            assertUpgradeEquals(origin, helper, fromPart, toPart);

            assertConfigEquals(origin, helper, fromPart, toPart);

            // Check configured config inventory of part we copied to
            if (!toPart.getConfig().keySet().equals(Set.of(
                    AEItemKey.of(Items.STICK),
                    AEFluidKey.of(Fluids.WATER)))) {
                helper.fail("wrong filter", origin);
            }

            helper.succeed();
        });
    }

    @TestPlot("memcard_interface")
    public static void testInterface(PlotBuilder plot) {
        var origin = BlockPos.ZERO;
        plot.cable(origin).part(Direction.WEST, AEParts.INTERFACE);
        plot.block(origin.east(), AEBlocks.INTERFACE);

        plot.test(helper -> {
            var from = (InterfaceBlockEntity) helper.getBlockEntity(BlockPos.ZERO.east());
            var to = helper.getPart(BlockPos.ZERO, Direction.WEST, InterfacePart.class);

            var player = helper.makeMockPlayer();
            var networkToolInv = addNetworkToolToPlayer(player);

            // Run part-specific setup code
            from.getUpgrades().addItems(AEItems.FUZZY_CARD.stack());
            // This should be moved out
            to.getUpgrades().addItems(AEItems.REDSTONE_CARD.stack());
            from.getConfig().setStack(0, new GenericStack(AEItemKey.of(Items.STICK), 1));
            from.getConfig().setStack(1, new GenericStack(AEFluidKey.of(Fluids.WATER), 1));

            from.getConfigManager().putSetting(Settings.FUZZY_MODE, FuzzyMode.PERCENT_25);

            copyUpgradesToNetworkInv(from, networkToolInv);

            // Export&Import settings
            var settings = new CompoundTag();
            from.exportSettings(SettingsFrom.MEMORY_CARD, settings, null);
            to.importSettings(SettingsFrom.MEMORY_CARD, settings, player);

            assertUpgradeEquals(origin, helper, from, to);

            assertConfigEquals(origin, helper, from, to);

            // Check configured config inventory of part we copied to
            if (!Objects.equals(to.getConfig().getKey(0), AEItemKey.of(Items.STICK))) {
                helper.fail("missing stick in filter", origin);
            }
            if (!Objects.equals(to.getConfig().getKey(1), AEFluidKey.of(Fluids.WATER))) {
                helper.fail("missing water in filter", origin);
            }

            helper.succeed();
        });
    }

    @TestPlot("memcard_pattern_provider")
    public static void testPatternProvider(PlotBuilder plot) {
        var origin = BlockPos.ZERO;
        plot.cable(origin).part(Direction.WEST, AEParts.PATTERN_PROVIDER);
        plot.block(origin.east(), AEBlocks.PATTERN_PROVIDER);

        plot.test(helper -> {

            // Create arbitrary processing+crafting patterns
            var processingPattern = PatternDetailsHelper.encodeProcessingPattern(
                    new GenericStack[] { new GenericStack(AEFluidKey.of(Fluids.WATER), 1) },
                    new GenericStack[] { new GenericStack(AEFluidKey.of(Fluids.LAVA), 1) });
            var craftingPattern = CraftingPatternHelper.encodeShapelessCraftingRecipe(
                    helper.getLevel(), Items.OAK_LOG.getDefaultInstance());
            var differentCraftingPattern = CraftingPatternHelper.encodeShapelessCraftingRecipe(
                    helper.getLevel(), Items.SPRUCE_LOG.getDefaultInstance());

            var from = (PatternProviderBlockEntity) helper.getBlockEntity(BlockPos.ZERO.east());
            var to = helper.getPart(BlockPos.ZERO, Direction.WEST, PatternProviderPart.class);

            var player = helper.makeMockPlayer();
            player.getInventory().placeItemBackInInventory(AEItems.BLANK_PATTERN.stack(64));

            // This should be copied to the other pattern provider
            var fromPatternInv = from.getLogic().getPatternInv();
            fromPatternInv.addItems(processingPattern);
            fromPatternInv.addItems(craftingPattern);
            // This should be cleared into a blank pattern and moved to the player
            var toPatternInv = to.getLogic().getPatternInv();
            toPatternInv.addItems(differentCraftingPattern.copy());
            toPatternInv.addItems(differentCraftingPattern.copy());
            toPatternInv.addItems(differentCraftingPattern.copy());

            var blankPatternsBefore = player.getInventory().countItem(AEItems.BLANK_PATTERN.asItem());

            // Export&Import settings
            var settings = new CompoundTag();
            from.exportSettings(SettingsFrom.MEMORY_CARD, settings, null);
            to.importSettings(SettingsFrom.MEMORY_CARD, settings, player);

            var blankPatternsAfter = player.getInventory().countItem(AEItems.BLANK_PATTERN.asItem());

            // There was one more pattern in "to" than requested, so the player should be given one blank pattern back
            helper.check(blankPatternsAfter == blankPatternsBefore + 1,
                    "Expected player to be given back one blank pattern");

            // Compare the pattern inventories
            for (int i = 0; i < fromPatternInv.size(); i++) {
                var fromItem = fromPatternInv.getStackInSlot(i);
                var toItem = toPatternInv.getStackInSlot(i);
                if (!ItemStack.isSameItemSameTags(fromItem, toItem)) {
                    helper.fail("Mismatch in slot " + i, origin.east());
                }
            }

            helper.succeed();
        });
    }

    private static InternalInventory addNetworkToolToPlayer(Player player) {
        // Add all upgrades to the player inventory in a network tool, so restoring settings can install them
        player.addItem(AEItems.NETWORK_TOOL.stack());
        return NetworkToolItem.findNetworkToolInv(player).getInventory();
    }

    private static void assertUpgradeEquals(BlockPos origin, PlotTestHelper helper, Object fromPart, Object toPart) {
        // We can generically check for upgrades
        if (fromPart instanceof IUpgradeableObject upgradableFrom) {
            IUpgradeableObject upgradeableTo = (IUpgradeableObject) toPart;
            for (var upgrade : upgradableFrom.getUpgrades()) {
                if (upgradableFrom.getInstalledUpgrades(upgrade.getItem()) != upgradeableTo
                        .getInstalledUpgrades(upgrade.getItem())) {
                    helper.fail(upgrade.getHoverName().getString() + " mismatch", origin);
                }
            }
        }
    }

    private static void assertConfigEquals(BlockPos origin, PlotTestHelper helper, Object fromPart, Object toPart) {
        // We can generically check for config manager changes
        if (fromPart instanceof IConfigurableObject fromConfigurable) {
            var toConfigurable = (IConfigurableObject) toPart;
            var fromConfig = fromConfigurable.getConfigManager();
            var toConfig = toConfigurable.getConfigManager();

            // Any setting that from supports should be on to and be equal
            for (var setting : fromConfig.getSettings()) {
                if (!fromConfig.getSetting(setting).equals(toConfig.getSetting(setting))) {
                    helper.fail("Setting " + setting + " mismatch", origin);
                }
            }
        }
    }

    private static void copyUpgradesToNetworkInv(Object fromPart, InternalInventory networkToolInv) {
        // Copy over all upgrades into the network tool so the memory card can add the upgrades
        if (fromPart instanceof IUpgradeableObject upgradeable) {
            for (ItemStack upgrade : upgradeable.getUpgrades()) {
                networkToolInv.addItems(upgrade.copy());
            }
        }
    }
}
