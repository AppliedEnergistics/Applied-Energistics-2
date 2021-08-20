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
import java.util.OptionalLong;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.api.config.Actionable;
import appeng.api.config.Settings;
import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.config.ViewItems;
import appeng.api.features.IGridLinkableHandler;
import appeng.api.features.IWirelessTerminalHandler;
import appeng.api.features.WirelessTerminals;
import appeng.api.util.IConfigManager;
import appeng.core.AEConfig;
import appeng.core.localization.GuiText;
import appeng.items.tools.powered.powersink.AEBasePoweredItem;
import appeng.util.ConfigManager;

public class WirelessTerminalItem extends AEBasePoweredItem {

    public static final IWirelessTerminalHandler TERMINAL_HANDLER = new TerminalHandler();

    public static final IGridLinkableHandler LINKABLE_HANDLER = new LinkableHandler();

    private static final String TAG_GRID_KEY = "gridKey";

    public WirelessTerminalItem(Item.Properties props) {
        super(AEConfig.instance().getWirelessTerminalBattery(), props);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(final Level level, final Player player, final InteractionHand hand) {
        WirelessTerminals.openTerminal(player.getItemInHand(hand), player, hand);
        return new InteractionResultHolder<>(InteractionResult.sidedSuccess(level.isClientSide()),
                player.getItemInHand(hand));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(final ItemStack stack, final Level level, final List<Component> lines,
            final TooltipFlag advancedTooltips) {
        super.appendHoverText(stack, level, lines, advancedTooltips);

        if (getGridKey(stack).isEmpty()) {
            lines.add(GuiText.Unlinked.text());
        } else {
            lines.add(GuiText.Linked.text());
        }
    }

    private OptionalLong getGridKey(ItemStack item) {
        CompoundTag tag = item.getTag();
        if (tag != null && tag.contains(TAG_GRID_KEY, Tag.TAG_LONG)) {
            return OptionalLong.of(tag.getLong(TAG_GRID_KEY));
        } else {
            return OptionalLong.empty();
        }
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged;
    }

    private static class TerminalHandler implements IWirelessTerminalHandler {
        private static WirelessTerminalItem getItem(ItemStack stack) {
            return (WirelessTerminalItem) stack.getItem();
        }

        @Override
        public OptionalLong getGridKey(ItemStack is) {
            return getItem(is).getGridKey(is);
        }

        @Override
        public boolean usePower(final Player player, final double amount, final ItemStack is) {
            return getItem(is).extractAEPower(is, amount, Actionable.MODULATE) >= amount - 0.5;
        }

        @Override
        public boolean hasPower(final Player player, final double amt, final ItemStack is) {
            return getItem(is).getAECurrentPower(is) >= amt;
        }

        @Override
        public IConfigManager getConfigManager(final ItemStack target) {
            final ConfigManager out = new ConfigManager((manager, settingName, newValue) -> {
                final CompoundTag data = target.getOrCreateTag();
                manager.writeToNBT(data);
            });

            out.registerSetting(Settings.SORT_BY, SortOrder.NAME);
            out.registerSetting(Settings.VIEW_MODE, ViewItems.ALL);
            out.registerSetting(Settings.SORT_DIRECTION, SortDir.ASCENDING);

            out.readFromNBT(target.getOrCreateTag().copy());
            return out;
        }
    }

    private static class LinkableHandler implements IGridLinkableHandler {
        @Override
        public boolean canLink(ItemStack stack) {
            return stack.getItem() instanceof WirelessTerminalItem;
        }

        @Override
        public void link(ItemStack itemStack, long securityKey) {
            itemStack.getOrCreateTag().putLong(TAG_GRID_KEY, securityKey);
        }

        @Override
        public void unlink(ItemStack itemStack) {
            itemStack.removeTagKey(TAG_GRID_KEY);
        }
    }
}
