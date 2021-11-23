/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2018, AlgorithmX2, All rights reserved.
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

package appeng.menu.me.fluids;

import javax.annotation.Nullable;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

import appeng.api.config.Actionable;
import appeng.api.config.SecurityPermissions;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.StorageChannels;
import appeng.api.storage.StorageHelper;
import appeng.api.storage.data.AEFluidKey;
import appeng.api.storage.data.AEKey;
import appeng.core.AELog;
import appeng.helpers.InventoryAction;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.me.common.MEMonitorableMenu;
import appeng.util.fluid.FluidSoundHelper;

/**
 * @see appeng.client.gui.me.fluids.FluidTerminalScreen
 * @since rv6 12/05/2018
 */
public class FluidTerminalMenu extends MEMonitorableMenu {

    public static final MenuType<FluidTerminalMenu> TYPE = MenuTypeBuilder
            .create(FluidTerminalMenu::new, ITerminalHost.class)
            .requirePermission(SecurityPermissions.BUILD)
            .build("fluid_terminal");

    public FluidTerminalMenu(int id, Inventory ip, ITerminalHost monitorable) {
        this(TYPE, id, ip, monitorable, true);
    }

    public FluidTerminalMenu(MenuType<?> menuType, int id, Inventory ip, ITerminalHost host,
            boolean bindInventory) {
        super(menuType, id, ip, host, bindInventory, StorageChannels.fluids());
    }

    @Override
    protected boolean isKeyVisible(AEKey key) {
        return key instanceof AEFluidKey;
    }

    @Override
    protected boolean hideViewCells() {
        return true;
    }

}
