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

import java.util.List;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

import appeng.api.implementations.items.IMemoryCard;
import appeng.api.implementations.items.MemoryCardMessages;
import appeng.api.util.AEColor;
import appeng.core.localization.GuiText;
import appeng.core.localization.PlayerMessages;
import appeng.hooks.AEToolItem;
import appeng.items.AEBaseItem;
import appeng.util.InteractionUtil;
import appeng.util.Platform;

public class MemoryCardItem extends AEBaseItem implements IMemoryCard, AEToolItem {

    private static final AEColor[] DEFAULT_COLOR_CODE = new AEColor[] { AEColor.TRANSPARENT, AEColor.TRANSPARENT,
            AEColor.TRANSPARENT, AEColor.TRANSPARENT, AEColor.TRANSPARENT, AEColor.TRANSPARENT, AEColor.TRANSPARENT,
            AEColor.TRANSPARENT, };

    public MemoryCardItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void appendHoverText(final ItemStack stack, final Level level, final List<Component> lines,
            final TooltipFlag advancedTooltips) {
        String firstLineKey = this.getFirstValidTranslationKey(this.getSettingsName(stack) + ".name",
                this.getSettingsName(stack));
        lines.add(new TranslatableComponent(firstLineKey));

        final CompoundTag data = this.getData(stack);
        if (data.contains("tooltip")) {
            String tooltipKey = getFirstValidTranslationKey(data.getString("tooltip") + ".name",
                    data.getString("tooltip"));
            lines.add(new TranslatableComponent(tooltipKey));
        }

        if (data.contains("freq")) {
            final short freq = data.getShort("freq");
            final String freqTooltip = ChatFormatting.BOLD + Platform.p2p().toHexString(freq);

            lines.add(new TranslatableComponent("gui.tooltips.ae2.P2PFrequency", freqTooltip));
        }
    }

    /**
     * Find the localized string...
     *
     * @param name possible names for the localized string
     * @return localized name
     */
    private String getFirstValidTranslationKey(final String... name) {
        for (final String n : name) {
            if (I18n.exists(n)) {
                return n;
            }
        }

        for (final String n : name) {
            return n;
        }

        return "";
    }

    @Override
    public void setMemoryCardContents(final ItemStack is, final String settingsName, final CompoundTag data) {
        final CompoundTag c = is.getOrCreateTag();
        c.putString("Config", settingsName);
        c.put("Data", data);
    }

    @Override
    public String getSettingsName(final ItemStack is) {
        final CompoundTag c = is.getOrCreateTag();
        final String name = c.getString("Config");
        return name.isEmpty() ? GuiText.Blank.getTranslationKey() : name;
    }

    @Override
    public CompoundTag getData(final ItemStack is) {
        final CompoundTag c = is.getOrCreateTag();
        CompoundTag o = c.getCompound("Data");
        return o.copy();
    }

    @Override
    public AEColor[] getColorCode(ItemStack is) {
        final CompoundTag tag = this.getData(is);

        if (tag.contains("colorCode")) {
            final int[] frequency = tag.getIntArray("colorCode");
            final AEColor[] colorArray = AEColor.values();

            return new AEColor[] { colorArray[frequency[0]], colorArray[frequency[1]], colorArray[frequency[2]],
                    colorArray[frequency[3]], colorArray[frequency[4]], colorArray[frequency[5]],
                    colorArray[frequency[6]], colorArray[frequency[7]], };
        }

        return DEFAULT_COLOR_CODE;
    }

    @Override
    public void notifyUser(final Player player, final MemoryCardMessages msg) {
        if (player.getCommandSenderWorld().isClientSide()) {
            return;
        }

        switch (msg) {
            case SETTINGS_CLEARED:
                player.sendMessage(PlayerMessages.SettingCleared.get(), Util.NIL_UUID);
                break;
            case INVALID_MACHINE:
                player.sendMessage(PlayerMessages.InvalidMachine.get(), Util.NIL_UUID);
                break;
            case SETTINGS_LOADED:
                player.sendMessage(PlayerMessages.LoadedSettings.get(), Util.NIL_UUID);
                break;
            case SETTINGS_SAVED:
                player.sendMessage(PlayerMessages.SavedSettings.get(), Util.NIL_UUID);
                break;
            case SETTINGS_RESET:
                player.sendMessage(PlayerMessages.ResetSettings.get(), Util.NIL_UUID);
                break;
            default:
        }
    }

    // TODO FABRIC 117 need to test this actually clears the card in all cases
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

    private void clearCard(final Player player, final Level level, final InteractionHand hand) {
        final IMemoryCard mem = (IMemoryCard) player.getItemInHand(hand).getItem();
        mem.notifyUser(player, MemoryCardMessages.SETTINGS_CLEARED);
        player.getItemInHand(hand).setTag(null);
    }
}
