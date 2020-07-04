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
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;

import appeng.api.config.ActionItems;
import appeng.client.gui.widgets.ActionButton;
import appeng.container.implementations.CraftingTermContainer;
import appeng.container.slot.CraftingMatrixSlot;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.InventoryActionPacket;
import appeng.helpers.InventoryAction;

public class CraftingTermScreen extends MEMonitorableScreen<CraftingTermContainer> {

    public CraftingTermScreen(CraftingTermContainer container, PlayerInventory playerInventory, Text title) {
        super(container, playerInventory, title);
        this.setReservedSpace(73);
    }

    private void clear() {
        Slot s = null;
        for (final Object j : this.handler.slots) {
            if (j instanceof CraftingMatrixSlot) {
                s = (Slot) j;
            }
        }

        if (s != null) {
            final InventoryActionPacket p = new InventoryActionPacket(InventoryAction.MOVE_REGION, s.id, 0);
            NetworkHandler.instance().sendToServer(p);
        }
    }

    @Override
    public void init() {
        super.init();
        ActionButton clearBtn = this.addButton(
                new ActionButton(this.x + 92, this.y + this.backgroundHeight - 156, ActionItems.STASH, btn -> clear()));
        clearBtn.setHalfSize(true);
    }

    @Override
    public void drawFG(MatrixStack matrices, final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        super.drawFG(matrices, offsetX, offsetY, mouseX, mouseY);
        this.textRenderer.draw(matrices, GuiText.CraftingTerminal.getLocal(), 8, this.backgroundHeight - 96 + 1 - this.getReservedSpace(),
                4210752);
    }

    @Override
    protected String getBackground() {
        return "guis/crafting.png";
    }
}
