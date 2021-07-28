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

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import appeng.api.config.SecurityPermissions;
import appeng.api.config.SortOrder;
import appeng.client.gui.Icon;
import appeng.client.gui.me.items.ItemTerminalScreen;
import appeng.client.gui.style.Blitter;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.ToggleButton;
import appeng.container.implementations.SecurityStationContainer;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.ConfigValuePacket;

public class SecurityStationScreen extends ItemTerminalScreen<SecurityStationContainer> {

    private final ToggleButton inject;
    private final ToggleButton extract;
    private final ToggleButton craft;
    private final ToggleButton build;
    private final ToggleButton security;

    private final Blitter encodingBg;

    public SecurityStationScreen(SecurityStationContainer container,
            Inventory playerInventory, Component title, ScreenStyle style) {
        super(container, playerInventory, title, style);

        encodingBg = style.getImage("encoding");

        this.inject = new ToggleButton(Icon.PERMISSION_INJECT, Icon.PERMISSION_INJECT_DISABLED,
                SecurityPermissions.INJECT.getDisplayName(), SecurityPermissions.INJECT.getDisplayHint(),
                btn -> toggleOption(SecurityPermissions.INJECT));
        this.extract = new ToggleButton(Icon.PERMISSION_EXTRACT, Icon.PERMISSION_EXTRACT_DISABLED,
                SecurityPermissions.EXTRACT.getDisplayName(), SecurityPermissions.EXTRACT.getDisplayHint(),
                btn -> toggleOption(SecurityPermissions.EXTRACT));
        this.craft = new ToggleButton(Icon.PERMISSION_CRAFT, Icon.PERMISSION_CRAFT_DISABLED,
                SecurityPermissions.CRAFT.getDisplayName(), SecurityPermissions.CRAFT.getDisplayHint(),
                btn -> toggleOption(SecurityPermissions.CRAFT));
        this.build = new ToggleButton(Icon.PERMISSION_BUILD, Icon.PERMISSION_BUILD_DISABLED,
                SecurityPermissions.BUILD.getDisplayName(), SecurityPermissions.BUILD.getDisplayHint(),
                btn -> toggleOption(SecurityPermissions.BUILD));
        this.security = new ToggleButton(Icon.PERMISSION_SECURITY, Icon.PERMISSION_SECURITY_DISABLED,
                SecurityPermissions.SECURITY.getDisplayName(), SecurityPermissions.SECURITY.getDisplayHint(),
                btn -> toggleOption(SecurityPermissions.SECURITY));

        widgets.add("permissionInject", this.inject);
        widgets.add("permissionExtract", this.extract);
        widgets.add("permissionCraft", this.craft);
        widgets.add("permissionBuild", this.build);
        widgets.add("permissionSecurity", this.security);
    }

    private void toggleOption(SecurityPermissions permission) {
        NetworkHandler.instance()
                .sendToServer(new ConfigValuePacket("TileSecurityStation.ToggleOption", permission.name()));
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
    public void drawBG(PoseStack matrixStack, int offsetX, int offsetY, int mouseX, int mouseY, float partialTicks) {
        super.drawBG(matrixStack, offsetX, offsetY, mouseX, mouseY, partialTicks);

        // Draw the encoding-box on the right
        encodingBg.dest(offsetX + imageWidth + 3, offsetY).blit(matrixStack, getBlitOffset());
    }

    @Override
    public SortOrder getSortBy() {
        return SortOrder.NAME;
    }

    @Override
    public List<Rect2i> getExclusionZones() {
        List<Rect2i> result = super.getExclusionZones();
        result.add(new Rect2i(
                leftPos + imageWidth + 3,
                topPos,
                encodingBg.getSrcWidth(),
                encodingBg.getSrcHeight()));
        return result;
    }
}
