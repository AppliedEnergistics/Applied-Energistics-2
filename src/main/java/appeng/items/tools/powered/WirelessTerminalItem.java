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

package appeng.items.tools.powered;

import java.util.List;

import appeng.api.config.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.text.Text;
import net.minecraft.world.World;

import appeng.api.AEApi;
import appeng.api.features.IWirelessTermHandler;
import appeng.api.util.IConfigManager;
import appeng.core.AEConfig;
import appeng.core.localization.GuiText;
import appeng.items.tools.powered.powersink.AEBasePoweredItem;
import appeng.util.ConfigManager;

public class WirelessTerminalItem extends AEBasePoweredItem implements IWirelessTermHandler {

    public WirelessTerminalItem(Item.Settings props) {
        super(AEConfig.instance().getWirelessTerminalBattery(), props);
    }

    @Override
    public TypedActionResult<ItemStack> use(final World w, final PlayerEntity player, final Hand hand) {
        AEApi.instance().registries().wireless().openWirelessTerminalGui(player.getStackInHand(hand), w, player, hand);
        return new TypedActionResult<>(ActionResult.SUCCESS, player.getStackInHand(hand));
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void appendTooltip(final ItemStack stack, final World world, final List<Text> lines,
            final TooltipContext advancedTooltips) {
        super.appendTooltip(stack, world, lines, advancedTooltips);

        if (stack.hasTag()) {
            final CompoundTag tag = stack.getOrCreateTag();
            if (tag != null) {
                final String encKey = tag.getString("encryptionKey");

                if (encKey == null || encKey.isEmpty()) {
                    lines.add(GuiText.Unlinked.text());
                } else {
                    lines.add(GuiText.Linked.text());
                }
            }
        } else {
            lines.add(new TranslatableText("AppEng.GuiITooltip.Unlinked"));
        }
    }

    @Override
    public boolean canHandle(final ItemStack is) {
        return AEApi.instance().definitions().items().wirelessTerminal().isSameAs(is);
    }

    @Override
    public boolean usePower(final PlayerEntity player, final double amount, final ItemStack is) {
        return this.extractAEPower(is, amount, Actionable.MODULATE) >= amount - 0.5;
    }

    @Override
    public boolean hasPower(final PlayerEntity player, final double amt, final ItemStack is) {
        return this.getAECurrentPower(is) >= amt;
    }

    @Override
    public IConfigManager getConfigManager(final ItemStack target) {
        final ConfigManager out = new ConfigManager((manager, settingName, newValue) -> {
            final CompoundTag data = target.getOrCreateTag();
            manager.writeToNBT(data);
        });

        out.registerSetting(appeng.api.config.Settings.SORT_BY, SortOrder.NAME);
        out.registerSetting(appeng.api.config.Settings.VIEW_MODE, ViewItems.ALL);
        out.registerSetting(appeng.api.config.Settings.SORT_DIRECTION, SortDir.ASCENDING);

        out.readFromNBT(target.getOrCreateTag().copy());
        return out;
    }

    @Override
    public String getEncryptionKey(final ItemStack item) {
        final CompoundTag tag = item.getOrCreateTag();
        return tag.getString("encryptionKey");
    }

    @Override
    public void setEncryptionKey(final ItemStack item, final String encKey, final String name) {
        final CompoundTag tag = item.getOrCreateTag();
        tag.putString("encryptionKey", encKey);
        tag.putString("name", name);
    }

// FIXME FABRIC Needs custom mixin
// FIXME FABRIC   @Override
// FIXME FABRIC   public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
// FIXME FABRIC       return slotChanged;
// FIXME FABRIC   }
}
