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

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;

import appeng.api.config.SecurityPermissions;
import appeng.api.config.SortOrder;
import appeng.client.gui.widgets.ToggleButton;
import appeng.container.implementations.SecurityStationContainer;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.ConfigValuePacket;

public class SecurityStationScreen extends MEMonitorableScreen<SecurityStationContainer> {

    private ToggleButton inject;
    private ToggleButton extract;
    private ToggleButton craft;
    private ToggleButton build;
    private ToggleButton security;

    public SecurityStationScreen(SecurityStationContainer container, PlayerInventory playerInventory, Text title) {
        super(container, playerInventory, title);
        this.setCustomSortOrder(false);
        this.setReservedSpace(33);

        // increase size so that the slot is over the gui.
        this.backgroundWidth += 56;
        this.setStandardSize(this.backgroundWidth);
    }

    private void toggleOption(SecurityPermissions permission) {
        NetworkHandler.instance()
                .sendToServer(new ConfigValuePacket("TileSecurityStation.ToggleOption", permission.name()));
    }

    @Override
    public void init() {
        super.init();

        final int top = this.y + this.backgroundHeight - 116;
        this.inject = this
                .addButton(new ToggleButton(this.x + 56, top, 11 * 16, 12 * 16, SecurityPermissions.INJECT.nameText(),
                        SecurityPermissions.INJECT.tooltipText(), btn -> toggleOption(SecurityPermissions.INJECT)));

        this.extract = this.addButton(new ToggleButton(this.x + 56 + 18, top, 11 * 16 + 1, 12 * 16 + 1,
                SecurityPermissions.EXTRACT.nameText(), SecurityPermissions.EXTRACT.tooltipText(),
                btn -> toggleOption(SecurityPermissions.EXTRACT)));

        this.craft = this.addButton(new ToggleButton(this.x + 56 + 18 * 2, top, 11 * 16 + 2, 12 * 16 + 2,
                SecurityPermissions.CRAFT.nameText(), SecurityPermissions.CRAFT.tooltipText(),
                btn -> toggleOption(SecurityPermissions.CRAFT)));

        this.build = this.addButton(new ToggleButton(this.x + 56 + 18 * 3, top, 11 * 16 + 3, 12 * 16 + 3,
                SecurityPermissions.BUILD.nameText(), SecurityPermissions.BUILD.tooltipText(),
                btn -> toggleOption(SecurityPermissions.BUILD)));

        this.security = this.addButton(new ToggleButton(this.x + 56 + 18 * 4, top, 11 * 16 + 4, 12 * 16 + 4,
                SecurityPermissions.SECURITY.nameText(), SecurityPermissions.SECURITY.tooltipText(),
                btn -> toggleOption(SecurityPermissions.SECURITY)));
    }

    @Override
    public void drawFG(MatrixStack matrices, final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        super.drawFG(matrices, offsetX, offsetY, mouseX, mouseY);
        this.textRenderer.method_30883(matrices, GuiText.SecurityCardEditor.text(), 8,
                this.backgroundHeight - 96 + 1 - this.getReservedSpace(), 4210752);
    }

    @Override
    protected String getBackground() {
        this.inject.setState((handler.getPermissionMode() & (1 << SecurityPermissions.INJECT.ordinal())) > 0);
        this.extract.setState((handler.getPermissionMode() & (1 << SecurityPermissions.EXTRACT.ordinal())) > 0);
        this.craft.setState((handler.getPermissionMode() & (1 << SecurityPermissions.CRAFT.ordinal())) > 0);
        this.build.setState((handler.getPermissionMode() & (1 << SecurityPermissions.BUILD.ordinal())) > 0);
        this.security.setState((handler.getPermissionMode() & (1 << SecurityPermissions.SECURITY.ordinal())) > 0);

        return "guis/security_station.png";
    }

    @Override
    public SortOrder getSortBy() {
        return SortOrder.NAME;
    }
}
