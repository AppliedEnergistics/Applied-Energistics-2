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
import net.minecraft.Util;
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
import appeng.api.features.Locatables;
import appeng.api.implementations.menuobjects.IMenuItem;
import appeng.api.implementations.menuobjects.ItemMenuHost;
import appeng.api.util.IConfigManager;
import appeng.core.localization.GuiText;
import appeng.core.localization.PlayerMessages;
import appeng.helpers.WirelessTerminalMenuHost;
import appeng.hooks.ICustomReequipAnimation;
import appeng.items.tools.powered.powersink.AEBasePoweredItem;
import appeng.menu.MenuLocator;
import appeng.menu.MenuOpener;
import appeng.menu.me.common.MEMonitorableMenu;
import appeng.util.ConfigManager;

public class WirelessTerminalItem extends AEBasePoweredItem implements ICustomReequipAnimation, IMenuItem {

    public static final IGridLinkableHandler LINKABLE_HANDLER = new LinkableHandler();

    private static final String TAG_GRID_KEY = "gridKey";

    public WirelessTerminalItem(final DoubleSupplier powerCapacity, Item.Properties props) {
        super(powerCapacity, props);
    }

    @Override
    public double getChargeRate() {
        return 800d;
    }

    /**
     * Open a wireless terminal from a slot in the player inventory, i.e. activated via hotkey.
     * 
     * @return True if the menu was opened.
     */
    public boolean openFromInventory(Player player, int inventorySlot) {
        var is = player.getInventory().getItem(inventorySlot);

        if (checkPreconditions(is, player)) {
            return MenuOpener.open(getMenuType(), player, MenuLocator.forInventorySlot(inventorySlot));
        }
        return false;
    }

    /**
     * Opens a wireless terminal when activated by the player while held in hand.
     */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var is = player.getItemInHand(hand);

        if (checkPreconditions(is, player)) {
            if (MenuOpener.open(getMenuType(), player, MenuLocator.forHand(player, hand))) {
                return new InteractionResultHolder<>(InteractionResult.sidedSuccess(level.isClientSide()), is);
            }
        }

        return new InteractionResultHolder<>(InteractionResult.FAIL, is);
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

    /**
     * Gets the key of the grid that the wireless terminal is linked to. This can be empty to signal that the terminal
     * screen should be closed or be otherwise unavailable. The grid will be looked up using
     * {@link Locatables#securityStations()}. To support setting the grid key using the standard security station slot,
     * register a {@link IGridLinkableHandler} for your item.
     */
    public OptionalLong getGridKey(ItemStack item) {
        CompoundTag tag = item.getTag();
        if (tag != null && tag.contains(TAG_GRID_KEY, Tag.TAG_LONG)) {
            return OptionalLong.of(tag.getLong(TAG_GRID_KEY));
        } else {
            return OptionalLong.empty();
        }
    }

    /**
     * Allows other wireless terminals to override which menu is shown when it is opened.
     */
    public MenuType<?> getMenuType() {
        return MEMonitorableMenu.WIRELESS_TYPE;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged;
    }

    @Nullable
    @Override
    public ItemMenuHost getMenuHost(Player player, int inventorySlot, ItemStack stack, @Nullable BlockPos pos) {
        return new WirelessTerminalMenuHost(player, inventorySlot, stack,
                (p, subMenu) -> openFromInventory(p, inventorySlot));
    }

    /**
     * Checks if a player can open a particular wireless terminal.
     * 
     * @return True if the wireless terminal can be opened (it's linked, network in range, power, etc.)
     */
    protected boolean checkPreconditions(ItemStack item, Player player) {
        if (item.isEmpty() || item.getItem() != this) {
            return false;
        }

        var level = player.getCommandSenderWorld();
        if (level.isClientSide()) {
            return false;
        }

        var key = getGridKey(item);
        if (key.isEmpty()) {
            player.sendMessage(PlayerMessages.DeviceNotLinked.get(), Util.NIL_UUID);
            return false;
        }

        var securityStation = Locatables.securityStations().get(level, key.getAsLong());
        if (securityStation == null) {
            player.sendMessage(PlayerMessages.StationCanNotBeLocated.get(), Util.NIL_UUID);
            return false;
        }

        if (!hasPower(player, 0.5, item)) {
            player.sendMessage(PlayerMessages.DeviceNotPowered.get(), Util.NIL_UUID);
            return false;
        }
        return true;
    }

    /**
     * use an amount of power, in AE units
     *
     * @param amount is in AE units ( 5 per MJ ), if you return false, the item should be dead and return false for
     *               hasPower
     * @return true if wireless terminal uses power
     */
    public boolean usePower(Player player, double amount, ItemStack is) {
        return extractAEPower(is, amount, Actionable.MODULATE) >= amount - 0.5;
    }

    /**
     * gets the power status of the item.
     *
     * @return returns true if there is any power left.
     */
    public boolean hasPower(Player player, double amt, ItemStack is) {
        return getAECurrentPower(is) >= amt;
    }

    /**
     * Return the config manager for the wireless terminal.
     *
     * @return config manager of wireless terminal
     */
    public IConfigManager getConfigManager(final ItemStack target) {
        var out = new ConfigManager((manager, settingName) -> {
            manager.writeToNBT(target.getOrCreateTag());
        });

        out.registerSetting(Settings.SORT_BY, SortOrder.NAME);
        out.registerSetting(Settings.VIEW_MODE, ViewItems.ALL);
        out.registerSetting(Settings.SORT_DIRECTION, SortDir.ASCENDING);

        out.readFromNBT(target.getOrCreateTag().copy());
        return out;
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
