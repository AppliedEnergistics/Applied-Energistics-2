/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.items.tools;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackLinkedSet;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;

import appeng.api.components.ExportedUpgrades;
import appeng.api.ids.AEComponents;
import appeng.api.implementations.items.IMemoryCard;
import appeng.api.implementations.items.MemoryCardMessages;
import appeng.api.inventories.InternalInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.util.IConfigurableObject;
import appeng.core.localization.GuiText;
import appeng.core.localization.InGameTooltip;
import appeng.core.localization.PlayerMessages;
import appeng.core.localization.Tooltips;
import appeng.datagen.providers.tags.ConventionTags;
import appeng.helpers.IConfigInvHost;
import appeng.helpers.IPriorityHost;
import appeng.items.AEBaseItem;
import appeng.util.InteractionUtil;
import appeng.util.Platform;
import appeng.util.inv.PlayerInternalInventory;

public class MemoryCardItem extends AEBaseItem implements IMemoryCard {
    private static final int DEFAULT_BASE_COLOR = 0x9cd3ff;

    public MemoryCardItem(Properties properties) {
        super(properties);
    }

    public static void exportGenericSettings(Object exportFrom, DataComponentMap.Builder builder) {
        if (exportFrom instanceof IUpgradeableObject upgradeableObject) {
            builder.set(AEComponents.EXPORTED_UPGRADES, MemoryCardItem.storeUpgrades(upgradeableObject));
        }

        if (exportFrom instanceof IConfigurableObject configurableObject) {
            builder.set(AEComponents.EXPORTED_SETTINGS, configurableObject.getConfigManager().exportSettings());
        }

        if (exportFrom instanceof IPriorityHost pHost) {
            builder.set(AEComponents.EXPORTED_PRIORITY, pHost.getPriority());
        }

        if (exportFrom instanceof IConfigInvHost configInvHost) {
            builder.set(AEComponents.EXPORTED_CONFIG_INV, configInvHost.getConfig().toList());
        }
    }

    /**
     * @return Set of {@link net.minecraft.core.component.DataComponentType} ids that were imported
     */
    public static Set<DataComponentType<?>> importGenericSettings(Object importTo,
            DataComponentMap input,
            @Nullable Player player) {
        var imported = new HashSet<DataComponentType<?>>();

        if (player != null && importTo instanceof IUpgradeableObject upgradeableObject) {
            var desiredUpgrades = input.get(AEComponents.EXPORTED_UPGRADES);
            if (desiredUpgrades != null) {
                restoreUpgrades(player, desiredUpgrades, upgradeableObject);
                imported.add(AEComponents.EXPORTED_UPGRADES);
            }
        }

        if (importTo instanceof IConfigurableObject configurableObject) {
            var exportedSettings = input.get(AEComponents.EXPORTED_SETTINGS);
            if (exportedSettings != null) {
                if (configurableObject.getConfigManager().importSettings(exportedSettings)) {
                    imported.add(AEComponents.EXPORTED_SETTINGS);
                }
            }
        }

        if (importTo instanceof IPriorityHost pHost) {
            var exportedPriority = input.get(AEComponents.EXPORTED_PRIORITY);
            if (exportedPriority != null) {
                pHost.setPriority(exportedPriority);
                imported.add(AEComponents.EXPORTED_PRIORITY);
            }
        }

        if (importTo instanceof IConfigInvHost configInvHost) {
            var exportedConfigInv = input.get(AEComponents.EXPORTED_CONFIG_INV);
            if (exportedConfigInv != null) {
                configInvHost.getConfig().readFromList(exportedConfigInv);
                imported.add(AEComponents.EXPORTED_CONFIG_INV);
            }
        }

        return imported;
    }

    public static void importGenericSettingsAndNotify(Object importTo, DataComponentMap input,
            @Nullable Player player) {
        var imported = importGenericSettings(importTo, input, player);

        if (player != null && !player.getCommandSenderWorld().isClientSide()) {
            if (imported.isEmpty()) {
                player.displayClientMessage(PlayerMessages.InvalidMachine.text(), true);
            } else {
                var restored = Tooltips
                        .conjunction(imported.stream().map(MemoryCardItem::getSettingComponent)
                                .distinct().toList());
                player.displayClientMessage(PlayerMessages.InvalidMachinePartiallyRestored.text(restored), true);
            }
        }
    }

    private static Set<DataComponentType<?>> getExportedSettings(DataComponentMap input) {
        var result = new HashSet<DataComponentType<?>>();
        for (var type : input.keySet()) {
            if (BuiltInRegistries.DATA_COMPONENT_TYPE.wrapAsHolder(type).is(ConventionTags.EXPORTED_SETTINGS)) {
                result.add(type);
            }
        }
        return result;
    }

    public static String getSettingTranslationKey(DataComponentType<?> settingsType) {
        var id = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(settingsType);
        return Util.makeDescriptionId("exported_setting", id);
    }

    private static Component getSettingComponent(DataComponentType<?> settingsType) {
        return Component.translatable(getSettingTranslationKey(settingsType));
    }

    private static ExportedUpgrades storeUpgrades(IUpgradeableObject upgradeableObject) {
        // Accumulate upgrades as itemId->count NBT
        Object2IntMap<ItemStack> upgradeCount = new Object2IntOpenCustomHashMap<>(ItemStackLinkedSet.TYPE_AND_TAG);
        for (var upgrade : upgradeableObject.getUpgrades()) {
            upgradeCount.mergeInt(upgrade, upgrade.getCount(), Integer::sum);
        }

        var result = new ArrayList<ItemStack>(upgradeCount.size());
        for (var entry : upgradeCount.object2IntEntrySet()) {
            result.add(entry.getKey().copyWithCount(entry.getIntValue()));
        }
        return new ExportedUpgrades(result);
    }

    private static void restoreUpgrades(Player player, ExportedUpgrades desiredUpgrades,
            IUpgradeableObject upgradeableObject) {
        var upgrades = upgradeableObject.getUpgrades();

        // In creative mode, just set it exactly as the memory card says
        if (player.getAbilities().instabuild) {
            // Clear it out first
            for (int i = 0; i < upgrades.size(); i++) {
                upgrades.setItemDirect(i, ItemStack.EMPTY);
            }
            for (var upgrade : desiredUpgrades.upgrades()) {
                upgrades.addItems(upgrade);
            }
        }

        var upgradeSources = new ArrayList<InternalInventory>();
        upgradeSources.add(new PlayerInternalInventory(player.getInventory()));

        // Search the player for a network tool
        var networkTool = NetworkToolItem.findNetworkToolInv(player);
        if (networkTool != null) {
            upgradeSources.add(networkTool.getInventory());
        }

        // Map of desired upgrade (ignoring the count in the key) to the desired count
        var desiredUpgradeCounts = new Reference2IntOpenHashMap<Item>(desiredUpgrades.upgrades().size());
        for (var desiredUpgrade : desiredUpgrades.upgrades()) {
            desiredUpgradeCounts.put(desiredUpgrade.getItem(), desiredUpgrade.getCount());
        }

        // Move out excess
        for (int i = 0; i < upgrades.size(); i++) {
            var current = upgrades.getStackInSlot(i);
            if (current.isEmpty()) {
                continue;
            }

            var desiredCount = desiredUpgradeCounts.getOrDefault(current, 0);
            var totalInstalled = upgradeableObject.getInstalledUpgrades(current.getItem());
            var toRemove = totalInstalled - desiredCount;
            if (toRemove > 0) {
                var removed = upgrades.extractItem(i, toRemove, false);

                for (var upgradeSource : upgradeSources) {
                    if (!removed.isEmpty()) {
                        removed = upgradeSource.addItems(removed);
                    }
                }
                if (!removed.isEmpty()) {
                    player.drop(removed, false);
                }
            }
        }

        // Move in what's still missing
        for (var entry : desiredUpgradeCounts.reference2IntEntrySet()) {
            var missingAmount = entry.getIntValue() - upgradeableObject.getInstalledUpgrades(entry.getKey());
            if (missingAmount > 0) {
                var potential = new ItemStack(entry.getKey(), missingAmount);
                // Determine how many can we *actually* insert
                var overflow = upgrades.addItems(potential, true);
                if (!overflow.isEmpty()) {
                    missingAmount -= overflow.getCount();
                }

                // Try getting them from the network tool or player inventory
                for (var upgradeSource : upgradeSources) {
                    var cards = upgradeSource.removeItems(missingAmount, potential, null);
                    if (!cards.isEmpty()) {
                        overflow = upgrades.addItems(cards);
                        if (!overflow.isEmpty()) {
                            player.getInventory().placeItemBackInInventory(overflow);
                        }
                        missingAmount -= cards.getCount();
                    }
                    if (missingAmount <= 0) {
                        break;
                    }
                }

                if (missingAmount > 0 && !player.level().isClientSide()) {
                    player.displayClientMessage(
                            PlayerMessages.MissingUpgrades.text(entry.getKey().getDescription(), missingAmount), true);
                }
            }
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines,
            TooltipFlag advancedTooltips) {

        var settingsSource = stack.get(AEComponents.EXPORTED_SETTINGS_SOURCE);
        if (settingsSource != null) {
            lines.add(Tooltips.of(settingsSource));
        } else {
            lines.add(Tooltips.of(GuiText.Blank.text()));
        }

        var p2pFreq = stack.get(AEComponents.EXPORTED_P2P_FREQUENCY);
        if (p2pFreq != null) {
            var freqTooltip = Platform.p2p().toColoredHexString(p2pFreq).withStyle(ChatFormatting.BOLD);
            lines.add(Tooltips.of(Component.translatable(InGameTooltip.P2PFrequency.getTranslationKey(), freqTooltip)));
        }
    }

    @Override
    public void notifyUser(Player player, MemoryCardMessages msg) {
        if (player.getCommandSenderWorld().isClientSide()) {
            return;
        }

        switch (msg) {
            case SETTINGS_CLEARED -> player.displayClientMessage(PlayerMessages.SettingCleared.text(), true);
            case INVALID_MACHINE -> player.displayClientMessage(PlayerMessages.InvalidMachine.text(), true);
            case SETTINGS_LOADED -> player.displayClientMessage(PlayerMessages.LoadedSettings.text(), true);
            case SETTINGS_SAVED -> player.displayClientMessage(PlayerMessages.SavedSettings.text(), true);
            case SETTINGS_RESET -> player.displayClientMessage(PlayerMessages.ResetSettings.text(), true);
            default -> {
            }
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (InteractionUtil.isInAlternateUseMode(context.getPlayer())) {
            Level level = context.getLevel();
            if (!level.isClientSide()) {
                this.clearCard(context.getPlayer(), context.getLevel(), context.getHand());
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        } else {
            return super.useOn(context);
        }
    }

    @Override
    public boolean doesSneakBypassUse(ItemStack stack, LevelReader level, BlockPos pos, Player player) {
        return true;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (InteractionUtil.isInAlternateUseMode(player) && !level.isClientSide) {
            this.clearCard(player, level, hand);
        }

        return super.use(level, player, hand);
    }

    private void clearCard(Player player, Level level, InteractionHand hand) {
        final IMemoryCard mem = (IMemoryCard) player.getItemInHand(hand).getItem();
        mem.notifyUser(player, MemoryCardMessages.SETTINGS_CLEARED);
        clearCard(player.getItemInHand(hand));
    }

    public static void clearCard(ItemStack card) {
        for (var holder : BuiltInRegistries.DATA_COMPONENT_TYPE.getTagOrEmpty(ConventionTags.EXPORTED_SETTINGS)) {
            card.remove(holder.value());
        }
        card.remove(AEComponents.MEMORY_CARD_COLORS);
    }

    // Override to change the default color
    public int getColor(ItemStack stack) {
        return DyedItemColor.getOrDefault(stack, DEFAULT_BASE_COLOR);
    }

    public static int getTintColor(ItemStack stack, int tintIndex) {
        if (tintIndex == 1 && stack.getItem() instanceof MemoryCardItem memoryCard) {
            return memoryCard.getColor(stack);
        } else {
            // White
            return 0xFFFFFF;
        }
    }
}
