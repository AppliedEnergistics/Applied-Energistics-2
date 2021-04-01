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
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.LanguageMap;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import appeng.api.implementations.items.IMemoryCard;
import appeng.api.implementations.items.MemoryCardMessages;
import appeng.api.util.AEColor;
import appeng.core.localization.GuiText;
import appeng.core.localization.PlayerMessages;
import appeng.hooks.AEToolItem;
import appeng.items.AEBaseItem;
import appeng.util.Platform;

public class MemoryCardItem extends AEBaseItem implements AEToolItem, IMemoryCard {

    private static final AEColor[] DEFAULT_COLOR_CODE = new AEColor[] { AEColor.TRANSPARENT, AEColor.TRANSPARENT,
            AEColor.TRANSPARENT, AEColor.TRANSPARENT, AEColor.TRANSPARENT, AEColor.TRANSPARENT, AEColor.TRANSPARENT,
            AEColor.TRANSPARENT, };

    public MemoryCardItem(Properties properties) {
        super(properties);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void addInformation(final ItemStack stack, final World world, final List<ITextComponent> lines,
            final ITooltipFlag advancedTooltips) {
        String firstLineKey = this.getFirstValidTranslationKey(this.getSettingsName(stack) + ".name",
                this.getSettingsName(stack));
        lines.add(new TranslationTextComponent(firstLineKey));

        final CompoundNBT data = this.getData(stack);
        if (data.contains("tooltip")) {
            String tooltipKey = getFirstValidTranslationKey(data.getString("tooltip") + ".name",
                    data.getString("tooltip"));
            lines.add(new TranslationTextComponent(tooltipKey));
        }

        if (data.contains("freq")) {
            final short freq = data.getShort("freq");
            final String freqTooltip = TextFormatting.field_1067 + Platform.p2p().toHexString(freq);

            lines.add(new TranslationTextComponent("gui.tooltips.appliedenergistics2.P2PFrequency", freqTooltip));
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
            if (LanguageMap.getInstance().method_4678(n)) {
                return n;
            }
        }

        for (final String n : name) {
            return n;
        }

        return "";
    }

    @Override
    public void setMemoryCardContents(final ItemStack is, final String settingsName, final CompoundNBT data) {
        final CompoundNBT c = is.getOrCreateTag();
        c.putString("Config", settingsName);
        c.put("Data", data);
    }

    @Override
    public String getSettingsName(final ItemStack is) {
        final CompoundNBT c = is.getOrCreateTag();
        final String name = c.getString("Config");
        return name.isEmpty() ? GuiText.Blank.getTranslationKey() : name;
    }

    @Override
    public CompoundNBT getData(final ItemStack is) {
        final CompoundNBT c = is.getOrCreateTag();
        CompoundNBT o = c.getCompound("Data");
        return o.copy();
    }

    @Override
    public AEColor[] getColorCode(ItemStack is) {
        final CompoundNBT tag = this.getData(is);

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
    public void notifyUser(final PlayerEntity player, final MemoryCardMessages msg) {
        if (Platform.isClient()) {
            return;
        }

        switch (msg) {
            case SETTINGS_CLEARED:
                player.sendMessage(PlayerMessages.SettingCleared.get(), Util.DUMMY_UUID);
                break;
            case INVALID_MACHINE:
                player.sendMessage(PlayerMessages.InvalidMachine.get(), Util.DUMMY_UUID);
                break;
            case SETTINGS_LOADED:
                player.sendMessage(PlayerMessages.LoadedSettings.get(), Util.DUMMY_UUID);
                break;
            case SETTINGS_SAVED:
                player.sendMessage(PlayerMessages.SavedSettings.get(), Util.DUMMY_UUID);
                break;
            case SETTINGS_RESET:
                player.sendMessage(PlayerMessages.ResetSettings.get(), Util.DUMMY_UUID);
                break;
            default:
        }
    }

    @Override
    public ActionResultType onItemUseFirst(ItemStack stack, ItemUseContext context) {
        if (context.getPlayer().isCrouching()) {
            // Bypass the memory card's own use handler and go straight to the block
            if (!context.getPlayer().world.isRemote) {
                BlockState state = context.getWorld().getBlockState(context.getPos());
                ActionResultType useResult = state.onBlockActivated(context.getWorld(), context.getPlayer(), context.getHand(),
                        new BlockRayTraceResult(context.getHitVec(), context.getFace(), context.getPos(),
                                context.isInside()));
                if (!useResult.isSuccessOrConsume()) {
                    clearCard(context.getPlayer(), context.getWorld(), context.getHand());
                }
            }
            return ActionResultType.field_5812;
        }

        return ActionResultType.PASS;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World w, PlayerEntity player, Hand hand) {
        if (player.isCrouching()) {
            if (!w.isRemote) {
                this.clearCard(player, w, hand);
            }
        }

        return super.onItemRightClick(w, player, hand);
    }

    private void clearCard(final PlayerEntity player, final World w, final Hand hand) {
        final IMemoryCard mem = (IMemoryCard) player.getHeldItem(hand).getItem();
        mem.notifyUser(player, MemoryCardMessages.SETTINGS_CLEARED);
        player.getHeldItem(hand).setTag(null);
    }

}
