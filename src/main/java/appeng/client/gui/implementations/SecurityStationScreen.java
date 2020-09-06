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

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

import appeng.api.config.SecurityPermissions;
import appeng.api.config.SortOrder;
import appeng.client.gui.widgets.ToggleButton;
import appeng.client.theme.ThemeColor;
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

    public SecurityStationScreen(SecurityStationContainer container, PlayerInventory playerInventory,
            ITextComponent title) {
        super(container, playerInventory, title);
        this.setCustomSortOrder(false);
        this.setReservedSpace(33);

        // increase size so that the slot is over the gui.
        this.xSize += 56;
        this.setStandardSize(this.xSize);
    }

    private void toggleOption(SecurityPermissions permission) {
        NetworkHandler.instance()
                .sendToServer(new ConfigValuePacket("TileSecurityStation.ToggleOption", permission.name()));
    }

    @Override
    public void init() {
        super.init();

        final int top = this.guiTop + this.ySize - 116;
        this.inject = this.addButton(new ToggleButton(this.guiLeft + 56, top, 11 * 16, 12 * 16,
                SecurityPermissions.INJECT.getTranslatedName(), SecurityPermissions.INJECT.getTranslatedTip(),
                btn -> toggleOption(SecurityPermissions.INJECT)));

        this.extract = this.addButton(new ToggleButton(this.guiLeft + 56 + 18, top, 11 * 16 + 1, 12 * 16 + 1,
                SecurityPermissions.EXTRACT.getTranslatedName(), SecurityPermissions.EXTRACT.getTranslatedTip(),
                btn -> toggleOption(SecurityPermissions.EXTRACT)));

        this.craft = this.addButton(new ToggleButton(this.guiLeft + 56 + 18 * 2, top, 11 * 16 + 2, 12 * 16 + 2,
                SecurityPermissions.CRAFT.getTranslatedName(), SecurityPermissions.CRAFT.getTranslatedTip(),
                btn -> toggleOption(SecurityPermissions.CRAFT)));

        this.build = this.addButton(new ToggleButton(this.guiLeft + 56 + 18 * 3, top, 11 * 16 + 3, 12 * 16 + 3,
                SecurityPermissions.BUILD.getTranslatedName(), SecurityPermissions.BUILD.getTranslatedTip(),
                btn -> toggleOption(SecurityPermissions.BUILD)));

        this.security = this.addButton(new ToggleButton(this.guiLeft + 56 + 18 * 4, top, 11 * 16 + 4, 12 * 16 + 4,
                SecurityPermissions.SECURITY.getTranslatedName(), SecurityPermissions.SECURITY.getTranslatedTip(),
                btn -> toggleOption(SecurityPermissions.SECURITY)));
    }

    @Override
    public void drawFG(MatrixStack matrixStack, final int offsetX, final int offsetY, final int mouseX,
            final int mouseY) {
        super.drawFG(matrixStack, offsetX, offsetY, mouseX, mouseY);
        this.font.drawString(matrixStack, GuiText.SecurityCardEditor.getLocal(), 8,
                this.ySize - 96 + 1 - this.getReservedSpace(), ThemeColor.TEXT_HEADING.argb());
    }

    @Override
    protected String getBackground() {
        this.inject.setState((container.getPermissionMode() & (1 << SecurityPermissions.INJECT.ordinal())) > 0);
        this.extract.setState((container.getPermissionMode() & (1 << SecurityPermissions.EXTRACT.ordinal())) > 0);
        this.craft.setState((container.getPermissionMode() & (1 << SecurityPermissions.CRAFT.ordinal())) > 0);
        this.build.setState((container.getPermissionMode() & (1 << SecurityPermissions.BUILD.ordinal())) > 0);
        this.security.setState((container.getPermissionMode() & (1 << SecurityPermissions.SECURITY.ordinal())) > 0);

        return "guis/security_station.png";
    }

    @Override
    public SortOrder getSortBy() {
        return SortOrder.NAME;
    }
}
