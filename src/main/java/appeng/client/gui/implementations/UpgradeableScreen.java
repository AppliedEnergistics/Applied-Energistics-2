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

import appeng.api.implementations.IUpgradeableHost;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.Blitter;
import appeng.client.gui.widgets.ToolboxPanel;
import appeng.client.gui.widgets.UpgradesPanel;
import appeng.container.SlotSemantic;
import appeng.container.implementations.UpgradeableContainer;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

import java.util.List;

/**
 * This screen adds the ability for {@link IUpgradeableHost} screens to show the upgrade inventory and
 * the player's toolbox to more easily install/remove upgrades.
 */
public class UpgradeableScreen<T extends UpgradeableContainer> extends AEBaseScreen<T> {

    // Margin used to position additional elements to the right of the UI
    private static final int MARGIN = 2;

    private final UpgradesPanel upgradesPanel;

    private final ToolboxPanel toolboxPanel;

    public UpgradeableScreen(T container, PlayerInventory playerInventory, ITextComponent title, Blitter background) {
        super(container, playerInventory, title, background);

        upgradesPanel = new UpgradesPanel(xSize + MARGIN, 0, container.getSlots(SlotSemantic.UPGRADE));
        toolboxPanel = new ToolboxPanel(xSize + MARGIN, ySize - 90, container.getSlots(SlotSemantic.TOOLBOX));
    }

    @Override
    public void drawBG(MatrixStack matrices, final int offsetX, final int offsetY, final int mouseX, final int mouseY,
                       float partialTicks) {
        super.drawBG(matrices, offsetX, offsetY, mouseX, mouseY, partialTicks);

        upgradesPanel.draw(matrices, getBlitOffset(), offsetX, offsetY);

        if (container.hasToolbox()) {
            toolboxPanel.draw(matrices, getBlitOffset(), offsetX, offsetY);
        }
    }

    @Override
    public List<Rectangle2d> getExclusionZones() {
        List<Rectangle2d> rects = super.getExclusionZones();
        if (container.hasToolbox()) {
            toolboxPanel.addExclusionZones(guiLeft, guiTop, rects);
        }

        upgradesPanel.addExclusionZones(guiLeft, guiTop, rects);

        return rects;
    }
}
