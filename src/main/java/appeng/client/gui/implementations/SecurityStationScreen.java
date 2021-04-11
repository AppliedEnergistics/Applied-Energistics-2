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

import appeng.api.config.SecurityPermissions;
import appeng.api.config.SortOrder;
import appeng.client.gui.Blitter;
import appeng.client.gui.me.common.TerminalStyle;
import appeng.client.gui.me.items.ItemTerminalScreen;
import appeng.client.gui.widgets.ToggleButton;
import appeng.container.implementations.SecurityStationContainer;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.ConfigValuePacket;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

import java.util.List;

public class SecurityStationScreen extends ItemTerminalScreen<SecurityStationContainer> {

    private static final String TEXTURE = "guis/security_station.png";

    private static final Blitter ENCODING_BG = Blitter.texture(TEXTURE).src(198, 0, 44, 98);

    private ToggleButton inject;
    private ToggleButton extract;
    private ToggleButton craft;
    private ToggleButton build;
    private ToggleButton security;

    public SecurityStationScreen(TerminalStyle style, SecurityStationContainer container, PlayerInventory playerInventory, ITextComponent title) {
        super(style, container, playerInventory, title);
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
    public void drawBG(MatrixStack matrixStack, int offsetX, int offsetY, int mouseX, int mouseY, float partialTicks) {
        super.drawBG(matrixStack, offsetX, offsetY, mouseX, mouseY, partialTicks);

        // Draw the encoding-box on the right
        ENCODING_BG.dest(offsetX + xSize + 3, offsetY).blit(matrixStack, getBlitOffset());
    }

    @Override
    public void drawFG(MatrixStack matrixStack, final int offsetX, final int offsetY, final int mouseX,
                       final int mouseY) {
        super.drawFG(matrixStack, offsetX, offsetY, mouseX, mouseY);
        this.font.drawString(matrixStack, GuiText.SecurityCardEditor.getLocal(), 8,
                this.ySize - 96 + 1 - 33, COLOR_DARK_GRAY);
    }

    @Override
    public void tick() {
        super.tick();

        this.inject.setState((container.getPermissionMode() & (1 << SecurityPermissions.INJECT.ordinal())) > 0);
        this.extract.setState((container.getPermissionMode() & (1 << SecurityPermissions.EXTRACT.ordinal())) > 0);
        this.craft.setState((container.getPermissionMode() & (1 << SecurityPermissions.CRAFT.ordinal())) > 0);
        this.build.setState((container.getPermissionMode() & (1 << SecurityPermissions.BUILD.ordinal())) > 0);
        this.security.setState((container.getPermissionMode() & (1 << SecurityPermissions.SECURITY.ordinal())) > 0);
    }

    @Override
    public SortOrder getSortBy() {
        return SortOrder.NAME;
    }

    @Override
    public List<Rectangle2d> getExclusionZones() {
        List<Rectangle2d> result = super.getExclusionZones();
        result.add(new Rectangle2d(
                guiLeft + xSize + 3,
                guiTop,
                ENCODING_BG.getSrcWidth(),
                ENCODING_BG.getSrcHeight()
        ));
        return result;
    }
}
