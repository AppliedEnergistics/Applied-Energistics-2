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

package appeng.fluids.client.gui;


import appeng.api.implementations.guiobjects.IPortableCell;
import appeng.fluids.container.ContainerWirelessFluidTerminal;
import appeng.helpers.WirelessTerminalGuiObject;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.player.InventoryPlayer;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;


public class GuiWirelessFluidTerminal extends GuiMEPortableFluidCell {

    public GuiWirelessFluidTerminal(final InventoryPlayer inventoryPlayer, final WirelessTerminalGuiObject te) {
        super(inventoryPlayer, te, new ContainerWirelessFluidTerminal(inventoryPlayer, te));
    }

    @Override
    public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY) {
        this.bindTexture("guis/wirelessupgrades.png");
        Gui.drawModalRectWithCustomSizedTexture(offsetX + 175, offsetY + 131, 0, 0, 32, 32, 32, 32);
        super.drawBG(offsetX, offsetY, mouseX, mouseY);
    }

    @Override
    public List<Rectangle> getJEIExclusionArea() {
        List<Rectangle> ea = new ArrayList<>();
        ea.add(new Rectangle(this.guiLeft + 174,
                this.guiTop + 131,
                32,
                32));
        return ea;
    }
}
