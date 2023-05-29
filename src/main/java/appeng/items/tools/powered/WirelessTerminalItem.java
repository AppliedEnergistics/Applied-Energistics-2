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
import java.util.function.DoubleSupplier;

import com.mojang.datafixers.util.Pair;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import appeng.api.config.Actionable;
import appeng.api.config.Settings;
import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.config.TypeFilter;
import appeng.api.config.ViewItems;
import appeng.api.features.IGridLinkableHandler;
import appeng.api.implementations.blockentities.IWirelessAccessPoint;
import appeng.api.implementations.menuobjects.IMenuItem;
import appeng.api.implementations.menuobjects.ItemMenuHost;
import appeng.api.networking.IGrid;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableItem;
import appeng.api.upgrades.UpgradeInventories;
import appeng.api.upgrades.Upgrades;
import appeng.api.util.IConfigManager;
import appeng.core.localization.GuiText;
import appeng.core.localization.PlayerMessages;
import appeng.core.localization.Tooltips;
import appeng.helpers.WirelessTerminalMenuHost;
import appeng.items.tools.powered.powersink.AEBasePoweredItem;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocators;
import appeng.menu.me.common.MEStorageMenu;
import appeng.util.ConfigManager;
import appeng.util.Platform;

public class WirelessTerminalItem extends AEBasePoweredItem implements IMenuItem, IUpgradeableItem {

    private static final Logger LOG = LoggerFactory.getLogger(WirelessTerminalItem.class);

    public static final IGridLinkableHandler LINKABLE_HANDLER = new LinkableHandler();

    private static final String TAG_ACCESS_POINT_POS = "accessPoint";

    public WirelessTerminalItem(DoubleSupplier powerCapacity, Item.Properties props) {
        super(powerCapacity, props);
    }

    @Override
    public double getChargeRate(ItemStack stack) {
        return 800d + 800d * Upgrades.getEnergyCardMultiplier(getUpgrades(stack));
    }

    /**
     * Open a wireless terminal from a slot in the player inventory, i.e. activated via hotkey.
     *
     * @return True if the menu was opened.
     */
    public boolean openFromInventory(Player player, int inventorySlot) {
        return openFromInventory(player, inventorySlot, false);
    }

    /**
     * Open a wireless terminal from a slot in the player inventory, i.e. activated via hotkey.
     *
     * @param returningFromSubmenu Pass true if returning from a submenu that was opened from this terminal. Will
     *                             restore previous search, scrollbar, etc.
     * @return True if the menu was opened.
     */
    protected boolean openFromInventory(Player player, int inventorySlot, boolean returningFromSubmenu) {
        var is = player.getInventory().getItem(inventorySlot);

        if (checkPreconditions(is, player)) {
            return MenuOpener.open(getMenuType(), player, MenuLocators.forInventorySlot(inventorySlot),
                    returningFromSubmenu);
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
            if (MenuOpener.open(getMenuType(), player, MenuLocators.forHand(player, hand))) {
                return new InteractionResultHolder<>(InteractionResult.sidedSuccess(level.isClientSide()), is);
            }
        }

        return new InteractionResultHolder<>(InteractionResult.FAIL, is);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void appendHoverText(ItemStack stack, Level level, List<Component> lines,
            TooltipFlag advancedTooltips) {
        super.appendHoverText(stack, level, lines, advancedTooltips);

        if (getLinkedPosition(stack) == null) {
            lines.add(Tooltips.of(GuiText.Unlinked, Tooltips.RED));
        } else {
            lines.add(Tooltips.of(GuiText.Linked, Tooltips.GREEN));
        }
    }

    /**
     * Gets the position of the wireless access point that this terminal is linked to. This can be empty to signal that
     * the terminal screen should be closed or be otherwise unavailable. To support linking your item with a wireless
     * access point, register a {@link IGridLinkableHandler}.
     */
    @Nullable
    public GlobalPos getLinkedPosition(ItemStack item) {
        CompoundTag tag = item.getTag();
        if (tag != null && tag.contains(TAG_ACCESS_POINT_POS, Tag.TAG_COMPOUND)) {
            return GlobalPos.CODEC.decode(NbtOps.INSTANCE, tag.get(TAG_ACCESS_POINT_POS))
                    .resultOrPartial(Util.prefix("Linked position", LOG::error))
                    .map(Pair::getFirst)
                    .orElse(null);
        } else {
            return null;
        }
    }

    @Nullable
    public IGrid getLinkedGrid(ItemStack item, Level level, @Nullable Player sendMessagesTo) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return null;
        }

        var linkedPos = getLinkedPosition(item);
        if (linkedPos == null) {
            if (sendMessagesTo != null) {
                sendMessagesTo.displayClientMessage(PlayerMessages.DeviceNotLinked.text(), true);
            }
            return null;
        }

        var linkedLevel = serverLevel.getServer().getLevel(linkedPos.dimension());
        if (linkedLevel == null) {
            if (sendMessagesTo != null) {
                sendMessagesTo.displayClientMessage(PlayerMessages.LinkedNetworkNotFound.text(), true);
            }
            return null;
        }

        var be = Platform.getTickingBlockEntity(linkedLevel, linkedPos.pos());
        if (!(be instanceof IWirelessAccessPoint accessPoint)) {
            if (sendMessagesTo != null) {
                sendMessagesTo.displayClientMessage(PlayerMessages.LinkedNetworkNotFound.text(), true);
            }
            return null;
        }

        var grid = accessPoint.getGrid();
        if (grid == null) {
            if (sendMessagesTo != null) {
                sendMessagesTo.displayClientMessage(PlayerMessages.LinkedNetworkNotFound.text(), true);
            }
        }
        return grid;
    }

    /**
     * Allows other wireless terminals to override which menu is shown when it is opened.
     */
    public MenuType<?> getMenuType() {
        return MEStorageMenu.WIRELESS_TYPE;
    }

    @Override
    public boolean allowNbtUpdateAnimation(Player player, InteractionHand hand, ItemStack oldStack,
            ItemStack newStack) {
        return false;
    }

    @Nullable
    @Override
    public ItemMenuHost getMenuHost(Player player, int inventorySlot, ItemStack stack, @Nullable BlockPos pos) {
        return new WirelessTerminalMenuHost(player, inventorySlot, stack,
                (p, subMenu) -> openFromInventory(p, inventorySlot, true));
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
        if (getLinkedGrid(item, player.level(), player) == null) {
            return false;
        }

        if (!hasPower(player, 0.5, item)) {
            player.displayClientMessage(PlayerMessages.DeviceNotPowered.text(), true);
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
    public IConfigManager getConfigManager(ItemStack target) {
        var out = new ConfigManager((manager, settingName) -> {
            manager.writeToNBT(target.getOrCreateTag());
        });

        out.registerSetting(Settings.SORT_BY, SortOrder.NAME);
        out.registerSetting(Settings.VIEW_MODE, ViewItems.ALL);
        out.registerSetting(Settings.TYPE_FILTER, TypeFilter.ALL);
        out.registerSetting(Settings.SORT_DIRECTION, SortDir.ASCENDING);

        out.readFromNBT(target.getOrCreateTag().copy());
        return out;
    }

    @Override
    public IUpgradeInventory getUpgrades(ItemStack stack) {
        return UpgradeInventories.forItem(stack, 2, this::onUpgradesChanged);
    }

    private void onUpgradesChanged(ItemStack stack, IUpgradeInventory upgrades) {
        setAEMaxPowerMultiplier(stack, 1 + Upgrades.getEnergyCardMultiplier(upgrades));
    }

    private static class LinkableHandler implements IGridLinkableHandler {
        @Override
        public boolean canLink(ItemStack stack) {
            return stack.getItem() instanceof WirelessTerminalItem;
        }

        @Override
        public void link(ItemStack itemStack, GlobalPos pos) {
            GlobalPos.CODEC.encodeStart(NbtOps.INSTANCE, pos)
                    .result()
                    .ifPresent(tag -> itemStack.getOrCreateTag().put(TAG_ACCESS_POINT_POS, tag));
        }

        @Override
        public void unlink(ItemStack itemStack) {
            itemStack.removeTagKey(TAG_ACCESS_POINT_POS);
        }
    }
}
