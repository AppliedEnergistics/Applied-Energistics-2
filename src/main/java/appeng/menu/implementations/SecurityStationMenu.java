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

package appeng.menu.implementations;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

import appeng.api.config.SecurityPermissions;
import appeng.api.implementations.items.IBiometricCard;
import appeng.api.storage.ITerminalHost;
import appeng.blockentity.misc.SecurityStationBlockEntity;
import appeng.menu.SlotSemantics;
import appeng.menu.guisync.GuiSync;
import appeng.menu.me.common.MEStorageMenu;
import appeng.menu.slot.RestrictedInputSlot;

/**
 * @see appeng.client.gui.implementations.SecurityStationScreen
 */
public class SecurityStationMenu extends MEStorageMenu {

    private static final String ACTION_TOGGLE_PERMISSION = "togglePermission";

    public static final MenuType<SecurityStationMenu> TYPE = MenuTypeBuilder
            .create(SecurityStationMenu::new, ITerminalHost.class)
            .requirePermission(SecurityPermissions.SECURITY)
            .build("securitystation");

    private final RestrictedInputSlot configSlot;

    private final SecurityStationBlockEntity securityBox;
    @GuiSync(0)
    public int permissionMode = 0;

    public SecurityStationMenu(int id, Inventory ip, ITerminalHost monitorable) {
        super(TYPE, id, ip, monitorable, true);

        this.securityBox = (SecurityStationBlockEntity) monitorable;

        this.addSlot(this.configSlot = new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.BIOMETRIC_CARD,
                this.securityBox.getConfigSlot(), 0), SlotSemantics.BIOMETRIC_CARD);

        registerClientAction(ACTION_TOGGLE_PERMISSION, SecurityPermissions.class, this::toggleSetting);
    }

    public void toggleSetting(SecurityPermissions permission) {
        if (isClientSide()) {
            sendClientAction(ACTION_TOGGLE_PERMISSION, permission);
            return;
        }

        ItemStack a = this.configSlot.getItem();
        if (!a.isEmpty() && a.getItem() instanceof IBiometricCard bc) {
            if (bc.hasPermission(a, permission)) {
                bc.removePermission(a, permission);
            } else {
                bc.addPermission(a, permission);
            }
        }
    }

    @Override
    public void broadcastChanges() {
        this.verifyPermissions(SecurityPermissions.SECURITY, false);

        this.setPermissionMode(0);

        final ItemStack a = this.configSlot.getItem();
        if (!a.isEmpty() && a.getItem() instanceof IBiometricCard bc) {

            for (SecurityPermissions sp : bc.getPermissions(a)) {
                this.setPermissionMode(this.getPermissionMode() | 1 << sp.ordinal());
            }
        }

        this.updatePowerStatus();

        super.broadcastChanges();
    }

    public int getPermissionMode() {
        return this.permissionMode;
    }

    private void setPermissionMode(int permissionMode) {
        this.permissionMode = permissionMode;
    }

    @Override
    protected boolean showsCraftables() {
        return false;
    }
}
