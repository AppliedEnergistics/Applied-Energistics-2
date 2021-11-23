/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
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

package appeng.menu.me.items;

import javax.annotation.Nullable;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

import appeng.api.config.Actionable;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.StorageChannels;
import appeng.api.storage.StorageHelper;
import appeng.api.storage.data.AEItemKey;
import appeng.api.storage.data.AEKey;
import appeng.core.AELog;
import appeng.helpers.InventoryAction;
import appeng.menu.MenuLocator;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.me.common.MEMonitorableMenu;
import appeng.menu.me.crafting.CraftAmountMenu;
import appeng.util.Platform;

/**
 * @see appeng.client.gui.me.items.ItemTerminalScreen
 */
public class ItemTerminalMenu extends MEMonitorableMenu {

    public static final MenuType<ItemTerminalMenu> TYPE = MenuTypeBuilder
            .create(ItemTerminalMenu::new, ITerminalHost.class)
            .build("item_terminal");

    public ItemTerminalMenu(int id, Inventory ip, ITerminalHost monitorable) {
        this(TYPE, id, ip, monitorable, true);
    }

    public ItemTerminalMenu(MenuType<?> menuType, int id, Inventory ip, ITerminalHost host,
            boolean bindInventory) {
        super(menuType, id, ip, host, bindInventory,
                StorageChannels.items());
    }

    @Override
    protected boolean isKeyVisible(AEKey key) {
        return true;
    }

}
