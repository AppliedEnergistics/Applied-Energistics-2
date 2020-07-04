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
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.*;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import net.fabricmc.api.Environment;

import appeng.api.implementations.items.IMemoryCard;
import appeng.api.implementations.items.MemoryCardMessages;
import appeng.api.util.AEColor;
import appeng.core.localization.GuiText;
import appeng.core.localization.PlayerMessages;
import appeng.items.AEBaseItem;
import appeng.util.Platform;

public class MemoryCardItem extends AEBaseItem implements IMemoryCard {

    private static final AEColor[] DEFAULT_COLOR_CODE = new AEColor[] { AEColor.TRANSPARENT, AEColor.TRANSPARENT,
            AEColor.TRANSPARENT, AEColor.TRANSPARENT, AEColor.TRANSPARENT, AEColor.TRANSPARENT, AEColor.TRANSPARENT,
            AEColor.TRANSPARENT, };

    public MemoryCardItem(Settings properties) {
        super(properties);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void appendTooltip(final ItemStack stack, final World world, final List<Text> lines,
            final TooltipContext advancedTooltips) {
        String firstLineKey = this.getFirstValidTranslationKey(this.getSettingsName(stack) + ".name",
                this.getSettingsName(stack));
        lines.add(new TranslatableText(firstLineKey));

        final CompoundTag data = this.getData(stack);
        if (data.contains("tooltip")) {
            String tooltipKey = getFirstValidTranslationKey(data.getString("tooltip") + ".name",
                    data.getString("tooltip"));
            lines.add(new TranslatableText(tooltipKey));
        }

        if (data.contains("freq")) {
            final short freq = data.getShort("freq");
            final String freqTooltip = Formatting.BOLD + Platform.p2p().toHexString(freq);

            lines.add(new TranslatableText("gui.tooltips.appliedenergistics2.P2PFrequency", freqTooltip));
        }
    }

    /**
     * Find the localized string...
     *
     * @param name possible names for the localized string
     *
     * @return localized name
     */
    private String getFirstValidTranslationKey(final String... name) {
        for (final String n : name) {
            if (Language.getInstance().hasTranslation(n)) {
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
    public void notifyUser(final PlayerEntity player, final MemoryCardMessages msg) {
        if (Platform.isClient()) {
            return;
        }

        switch (msg) {
            case SETTINGS_CLEARED:
                player.sendSystemMessage(PlayerMessages.SettingCleared.get(), Util.NIL_UUID);
                break;
            case INVALID_MACHINE:
                player.sendSystemMessage(PlayerMessages.InvalidMachine.get(), Util.NIL_UUID);
                break;
            case SETTINGS_LOADED:
                player.sendSystemMessage(PlayerMessages.LoadedSettings.get(), Util.NIL_UUID);
                break;
            case SETTINGS_SAVED:
                player.sendSystemMessage(PlayerMessages.SavedSettings.get(), Util.NIL_UUID);
                break;
            case SETTINGS_RESET:
                player.sendSystemMessage(PlayerMessages.ResetSettings.get(), Util.NIL_UUID);
                break;
            default:
        }
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (context.getPlayer().isInSneakingPose()) {
            if (!context.getPlayer().world.isClient) {
                this.clearCard(context.getPlayer(), context.getWorld(), context.getHand());
            }
            return ActionResult.SUCCESS;
        } else {
            return super.useOnBlock(context);
        }
    }

    @Override
    public TypedActionResult<ItemStack> use(World w, PlayerEntity player, Hand hand) {
        if (player.isInSneakingPose()) {
            if (!w.isClient) {
                this.clearCard(player, w, hand);
            }
        }

        return super.use(w, player, hand);
    }

// FIXME FABRIC probably needs a custom mixin
// FIXME FABRIC    @Override
// FIXME FABRIC    public boolean doesSneakBypassUse(ItemStack stack, WorldView world, BlockPos pos, PlayerEntity player) {
// FIXME FABRIC        return true;
// FIXME FABRIC    }

    private void clearCard(final PlayerEntity player, final World w, final Hand hand) {
        final IMemoryCard mem = (IMemoryCard) player.getStackInHand(hand).getItem();
        mem.notifyUser(player, MemoryCardMessages.SETTINGS_CLEARED);
        player.getStackInHand(hand).setTag(null);
    }
}
