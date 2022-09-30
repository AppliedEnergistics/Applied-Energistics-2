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
import java.util.IdentityHashMap;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.ResourceLocationException;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

import appeng.api.implementations.items.IMemoryCard;
import appeng.api.implementations.items.MemoryCardMessages;
import appeng.api.inventories.InternalInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.util.AEColor;
import appeng.api.util.IConfigurableObject;
import appeng.core.AELog;
import appeng.core.localization.GuiText;
import appeng.core.localization.PlayerMessages;
import appeng.core.localization.Tooltips;
import appeng.helpers.IConfigInvHost;
import appeng.helpers.IPriorityHost;
import appeng.hooks.AEToolItem;
import appeng.items.AEBaseItem;
import appeng.util.InteractionUtil;
import appeng.util.Platform;
import appeng.util.inv.PlayerInternalInventory;

public class MemoryCardItem extends AEBaseItem implements IMemoryCard, AEToolItem {

    private static final AEColor[] DEFAULT_COLOR_CODE = new AEColor[] { AEColor.TRANSPARENT, AEColor.TRANSPARENT,
            AEColor.TRANSPARENT, AEColor.TRANSPARENT, AEColor.TRANSPARENT, AEColor.TRANSPARENT, AEColor.TRANSPARENT,
            AEColor.TRANSPARENT, };

    public MemoryCardItem(Item.Properties properties) {
        super(properties);
    }

    public static void exportGenericSettings(Object exportFrom, CompoundTag output) {
        if (exportFrom instanceof IUpgradeableObject upgradeableObject) {
            MemoryCardItem.storeUpgrades(upgradeableObject, output);
        }

        if (exportFrom instanceof IConfigurableObject configurableObject) {
            configurableObject.getConfigManager().writeToNBT(output);
        }

        if (exportFrom instanceof IPriorityHost pHost) {
            output.putInt("priority", pHost.getPriority());
        }

        if (exportFrom instanceof IConfigInvHost configInvHost) {
            configInvHost.getConfig().writeToChildTag(output, "config");
        }
    }

    public static void importGenericSettings(Object importTo, CompoundTag input, @Nullable Player player) {
        if (player != null && importTo instanceof IUpgradeableObject upgradeableObject) {
            restoreUpgrades(player, input, upgradeableObject);
        }

        if (importTo instanceof IConfigurableObject configurableObject) {
            configurableObject.getConfigManager().readFromNBT(input);
        }

        if (importTo instanceof IPriorityHost pHost) {
            pHost.setPriority(input.getInt("priority"));
        }

        if (importTo instanceof IConfigInvHost configInvHost) {
            configInvHost.getConfig().readFromChildTag(input, "config");
        }
    }

    private static void storeUpgrades(IUpgradeableObject upgradeableObject, CompoundTag output) {
        // Accumulate upgrades as itemId->count NBT
        var desiredUpgradesTag = new CompoundTag();
        for (var upgrade : upgradeableObject.getUpgrades()) {
            var itemId = Registry.ITEM.getKey(upgrade.getItem());
            if (itemId.equals(Registry.ITEM.getDefaultKey())) {
                AELog.warn("Cannot save unregistered upgrade to memory card %s", upgrade.getItem());
                continue;
            }
            var key = itemId.toString();
            desiredUpgradesTag.putInt(key, desiredUpgradesTag.getInt(key) + upgrade.getCount());
        }

        if (!desiredUpgradesTag.isEmpty()) {
            output.put("upgrades", desiredUpgradesTag);
        }
    }

    private static void restoreUpgrades(Player player, CompoundTag input, IUpgradeableObject upgradeableObject) {
        var desiredUpgradesTag = input.getCompound("upgrades");
        var desiredUpgrades = new IdentityHashMap<Item, Integer>();
        for (String itemIdStr : desiredUpgradesTag.getAllKeys()) {
            ResourceLocation itemId;
            try {
                itemId = new ResourceLocation(itemIdStr);
            } catch (ResourceLocationException e) {
                AELog.warn("Memory card contains invalid item id %s", itemIdStr);
                continue;
            }

            var item = Registry.ITEM.getOptional(itemId).orElse(null);
            if (item == null) {
                AELog.warn("Memory card contains unknown item id %s", itemId);
                continue;
            }

            int desiredCount = desiredUpgradesTag.getInt(itemIdStr);
            if (desiredCount > 0) {
                desiredUpgrades.put(item, desiredCount);
            }
        }

        var upgrades = upgradeableObject.getUpgrades();

        // In creative mode, just set it exactly as the memory card says
        if (player.getAbilities().instabuild) {
            // Clear it out first
            for (int i = 0; i < upgrades.size(); i++) {
                upgrades.setItemDirect(i, ItemStack.EMPTY);
            }
            for (var entry : desiredUpgrades.entrySet()) {
                upgrades.addItems(new ItemStack(entry.getKey(), entry.getValue()));
            }
            return;
        }

        var upgradeSources = new ArrayList<InternalInventory>();
        upgradeSources.add(new PlayerInternalInventory(player.getInventory()));

        // Search the player for a network tool
        var networkTool = NetworkToolItem.findNetworkToolInv(player);
        if (networkTool != null) {
            upgradeSources.add(networkTool.getInventory());
        }

        // Move out excess
        for (int i = 0; i < upgrades.size(); i++) {
            var current = upgrades.getStackInSlot(i);
            if (current.isEmpty()) {
                continue;
            }

            var desiredCount = desiredUpgrades.getOrDefault(current.getItem(), 0);
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
        for (var entry : desiredUpgrades.entrySet()) {
            var missingAmount = entry.getValue() - upgradeableObject.getInstalledUpgrades(entry.getKey());
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

                if (missingAmount > 0 && !player.level.isClientSide()) {
                    player.sendSystemMessage(
                            PlayerMessages.MissingUpgrades.text(entry.getKey().getDescription(), missingAmount));
                }
            }
        }
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void appendHoverText(ItemStack stack, Level level, List<Component> lines,
            TooltipFlag advancedTooltips) {

        String firstLineKey = this.getFirstValidTranslationKey(this.getSettingsName(stack) + ".name",
                this.getSettingsName(stack));
        lines.add(Tooltips.of(Component.translatable(firstLineKey)));

        final CompoundTag data = this.getData(stack);
        if (data.contains("tooltip")) {
            String tooltipKey = getFirstValidTranslationKey(data.getString("tooltip") + ".name",
                    data.getString("tooltip"));
            lines.add(Tooltips.of(Component.translatable(tooltipKey)));
        }

        if (data.contains("freq")) {
            final short freq = data.getShort("freq");
            final String freqTooltip = ChatFormatting.BOLD + Platform.p2p().toHexString(freq);

            lines.add(Tooltips.of(Component.translatable("gui.tooltips.ae2.P2PFrequency", freqTooltip)));
        }
    }

    /**
     * Find the localized string...
     *
     * @param name possible names for the localized string
     * @return localized name
     */
    private String getFirstValidTranslationKey(String... name) {
        for (String n : name) {
            if (I18n.exists(n)) {
                return n;
            }
        }

        for (String n : name) {
            return n;
        }

        return "";
    }

    @Override
    public void setMemoryCardContents(ItemStack is, String settingsName, CompoundTag data) {
        final CompoundTag c = is.getOrCreateTag();
        c.putString("Config", settingsName);
        c.put("Data", data);
    }

    @Override
    public String getSettingsName(ItemStack is) {
        final CompoundTag c = is.getOrCreateTag();
        final String name = c.getString("Config");
        return name.isEmpty() ? GuiText.Blank.getTranslationKey() : name;
    }

    @Override
    public CompoundTag getData(ItemStack is) {
        final CompoundTag c = is.getOrCreateTag();
        CompoundTag o = c.getCompound("Data");
        return o.copy();
    }

    @Override
    public AEColor[] getColorCode(ItemStack is) {
        final CompoundTag tag = this.getData(is);

        if (tag.contains(IMemoryCard.NBT_COLOR_CODE, Tag.TAG_INT_ARRAY)) {
            var frequency = tag.getIntArray(IMemoryCard.NBT_COLOR_CODE);
            var colorArray = AEColor.values();

            if (frequency.length == 8) {
                return new AEColor[] { colorArray[frequency[0]], colorArray[frequency[1]], colorArray[frequency[2]],
                        colorArray[frequency[3]], colorArray[frequency[4]], colorArray[frequency[5]],
                        colorArray[frequency[6]], colorArray[frequency[7]], };
            }
        }

        return DEFAULT_COLOR_CODE;
    }

    @Override
    public void notifyUser(Player player, MemoryCardMessages msg) {
        if (player.getCommandSenderWorld().isClientSide()) {
            return;
        }

        switch (msg) {
            case SETTINGS_CLEARED -> player.sendSystemMessage(PlayerMessages.SettingCleared.text());
            case INVALID_MACHINE -> player.sendSystemMessage(PlayerMessages.InvalidMachine.text());
            case SETTINGS_LOADED -> player.sendSystemMessage(PlayerMessages.LoadedSettings.text());
            case SETTINGS_SAVED -> player.sendSystemMessage(PlayerMessages.SavedSettings.text());
            case SETTINGS_RESET -> player.sendSystemMessage(PlayerMessages.ResetSettings.text());
            default -> {
            }
        }
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        var player = context.getPlayer();
        if (player != null && InteractionUtil.isInAlternateUseMode(player)) {
            var level = context.getLevel();
            if (!level.isClientSide()) {
                var state = context.getLevel().getBlockState(context.getClickedPos());
                var useResult = state.use(context.getLevel(), context.getPlayer(),
                        context.getHand(),
                        new BlockHitResult(context.getClickLocation(), context.getClickedFace(),
                                context.getClickedPos(),
                                context.isInside()));
                if (!useResult.consumesAction()) {
                    clearCard(context.getPlayer(), context.getLevel(), context.getHand());
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        return InteractionResult.PASS;
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
        player.getItemInHand(hand).setTag(null);
    }
}
