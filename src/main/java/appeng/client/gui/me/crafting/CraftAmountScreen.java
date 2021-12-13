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

package appeng.client.gui.me.crafting;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.NumberEntryType;
import appeng.client.gui.implementations.AESubScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.NumberEntryWidget;
import appeng.core.localization.GuiText;
import appeng.menu.me.crafting.CraftAmountMenu;

/**
 * When requesting to auto-craft, this dialog allows the player to enter the desired number of items to craft.
 */
public class CraftAmountScreen extends AEBaseScreen<CraftAmountMenu> {

    private final Button next;

    private final NumberEntryWidget amountToCraft;

    private boolean amountInitialized;

    public CraftAmountScreen(CraftAmountMenu menu, Inventory playerInventory, Component title,
            ScreenStyle style) {
        super(menu, playerInventory, title, style);

        this.next = widgets.addButton("next", GuiText.Next.text(), this::confirm);

        AESubScreen.addBackButton(menu, "back", widgets);

        this.amountToCraft = new NumberEntryWidget(NumberEntryType.UNITLESS);
        this.amountToCraft.setMinValue(1);
        this.amountToCraft.setMaxValue(Integer.MAX_VALUE);
        this.amountToCraft.setLongValue(1);
        this.amountToCraft.setTextFieldStyle(style.getWidget("amountToCraftInput"));
        this.amountToCraft.setHideValidationIcon(true);
        this.amountToCraft.setOnConfirm(this::confirm);
        widgets.add("amountToCraft", this.amountToCraft);
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();

        if (!this.amountInitialized) {
            var whatToCraft = menu.getWhatToCraft();
            if (whatToCraft != null) {
                this.amountToCraft.setType(NumberEntryType.of(whatToCraft.what()));
                this.amountToCraft.setLongValue(whatToCraft.amount());
                this.amountInitialized = true;
            }
        }

        this.next.setMessage(hasShiftDown() ? GuiText.Start.text() : GuiText.Next.text());
        this.next.active = this.amountToCraft.getIntValue().orElse(0) > 0;
    }

    private void confirm() {
        int amount = this.amountToCraft.getIntValue().orElse(0);
        if (amount <= 0) {
            return;
        }
        menu.confirm(amount, hasShiftDown());
    }

}
