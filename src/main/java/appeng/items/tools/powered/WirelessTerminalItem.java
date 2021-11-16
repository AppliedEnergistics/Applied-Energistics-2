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
import java.util.function.DoubleSupplier;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import appeng.api.config.*;
import appeng.api.features.IGridLinkableHandler;
import appeng.api.features.IWirelessTerminalHandler;
import appeng.api.features.WirelessTerminals;
import appeng.api.implementations.guiobjects.IGuiItem;
import appeng.api.implementations.guiobjects.IGuiItemObject;
import appeng.api.util.IConfigManager;
import appeng.core.localization.GuiText;
import appeng.helpers.WirelessTerminalGuiObject;
import appeng.hooks.ICustomReequipAnimation;
import appeng.items.tools.powered.powersink.AEBasePoweredItem;
import appeng.menu.me.items.WirelessTermMenu;
import appeng.util.ConfigManager;

public class WirelessTerminalItem extends AEBasePoweredItem implements ICustomReequipAnimation, IGuiItem {

    public static final IWirelessTerminalHandler TERMINAL_HANDLER = new TerminalHandler();

    public static final IGridLinkableHandler LINKABLE_HANDLER = new LinkableHandler();

    private static final String TAG_GRID_KEY = "gridKey";

    public WirelessTerminalItem(final DoubleSupplier powerCapacity, Item.Properties props) {
        super(powerCapacity, props);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(final Level level, final Player player, final InteractionHand hand) {
        WirelessTerminals.openTerminal(player.getItemInHand(hand), player, hand);
        return new InteractionResultHolder<>(InteractionResult.sidedSuccess(level.isClientSide()),
                player.getItemInHand(hand));
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void appendHoverText(final ItemStack stack, final Level level, final List<Component> lines,
            final TooltipFlag advancedTooltips) {
        super.appendHoverText(stack, level, lines, advancedTooltips);

        if (getGridKey(stack).isEmpty()) {
            lines.add(GuiText.Unlinked.text());
        } else {
            lines.add(GuiText.Linked.text());
        }
    }

    public OptionalLong getGridKey(ItemStack item) {
        CompoundTag tag = item.getTag();
        if (tag != null && tag.contains(TAG_GRID_KEY, Tag.TAG_LONG)) {
            return OptionalLong.of(tag.getLong(TAG_GRID_KEY));
        } else {
            return OptionalLong.empty();
        }
    }

    public MenuType<?> getMenuType() {
        return WirelessTermMenu.TYPE;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged;
    }

    @Nullable
    @Override
    public IGuiItemObject getGuiObject(ItemStack is, int playerInventorySlot, Level level, @Nullable BlockPos pos) {
        return null;
    }

    @Nullable
    @Override
    public IGuiItemObject getGuiObject(ItemStack is, int playerInventorySlot, Player player, @Nullable BlockPos pos) {
        return new WirelessTerminalGuiObject(TERMINAL_HANDLER, is, player, playerInventorySlot);
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
        public IConfigManager getConfigManager(final ItemStack target) {// TODO maybe provide an easy way for other
                                                                        // Terminals to overwrite this without making
                                                                        // their own IWirelessTerminalHandler that
                                                                        // mostly copies this one
            var out = new ConfigManager((manager, settingName) -> {
                manager.writeToNBT(target.getOrCreateTag());
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
