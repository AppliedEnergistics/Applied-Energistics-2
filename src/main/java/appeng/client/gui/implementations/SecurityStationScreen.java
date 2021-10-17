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

package appeng.client.gui.implementations;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import appeng.api.config.SecurityPermissions;
import appeng.api.config.SortOrder;
import appeng.client.gui.Icon;
import appeng.client.gui.me.items.ItemTerminalScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.ToggleButton;
import appeng.menu.implementations.SecurityStationMenu;

public class SecurityStationScreen extends ItemTerminalScreen<SecurityStationMenu> {

    private final ToggleButton inject;
    private final ToggleButton extract;
    private final ToggleButton craft;
    private final ToggleButton build;
    private final ToggleButton security;

    public SecurityStationScreen(SecurityStationMenu menu,
            Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);

        widgets.addBackgroundPanel("encodingPanel");

        this.inject = new ToggleButton(Icon.PERMISSION_INJECT, Icon.PERMISSION_INJECT_DISABLED,
                SecurityPermissions.INJECT.getDisplayName(), SecurityPermissions.INJECT.getDisplayHint(),
                btn -> menu.toggleSetting(SecurityPermissions.INJECT));
        this.extract = new ToggleButton(Icon.PERMISSION_EXTRACT, Icon.PERMISSION_EXTRACT_DISABLED,
                SecurityPermissions.EXTRACT.getDisplayName(), SecurityPermissions.EXTRACT.getDisplayHint(),
                btn -> menu.toggleSetting(SecurityPermissions.EXTRACT));
        this.craft = new ToggleButton(Icon.PERMISSION_CRAFT, Icon.PERMISSION_CRAFT_DISABLED,
                SecurityPermissions.CRAFT.getDisplayName(), SecurityPermissions.CRAFT.getDisplayHint(),
                btn -> menu.toggleSetting(SecurityPermissions.CRAFT));
        this.build = new ToggleButton(Icon.PERMISSION_BUILD, Icon.PERMISSION_BUILD_DISABLED,
                SecurityPermissions.BUILD.getDisplayName(), SecurityPermissions.BUILD.getDisplayHint(),
                btn -> menu.toggleSetting(SecurityPermissions.BUILD));
        this.security = new ToggleButton(Icon.PERMISSION_SECURITY, Icon.PERMISSION_SECURITY_DISABLED,
                SecurityPermissions.SECURITY.getDisplayName(), SecurityPermissions.SECURITY.getDisplayHint(),
                btn -> menu.toggleSetting(SecurityPermissions.SECURITY));

        widgets.add("permissionInject", this.inject);
        widgets.add("permissionExtract", this.extract);
        widgets.add("permissionCraft", this.craft);
        widgets.add("permissionBuild", this.build);
        widgets.add("permissionSecurity", this.security);
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();

        this.inject.setState((menu.getPermissionMode() & 1 << SecurityPermissions.INJECT.ordinal()) > 0);
        this.extract.setState((menu.getPermissionMode() & 1 << SecurityPermissions.EXTRACT.ordinal()) > 0);
        this.craft.setState((menu.getPermissionMode() & 1 << SecurityPermissions.CRAFT.ordinal()) > 0);
        this.build.setState((menu.getPermissionMode() & 1 << SecurityPermissions.BUILD.ordinal()) > 0);
        this.security.setState((menu.getPermissionMode() & 1 << SecurityPermissions.SECURITY.ordinal()) > 0);
    }

    @Override
    public SortOrder getSortBy() {
        return SortOrder.NAME;
    }
}
