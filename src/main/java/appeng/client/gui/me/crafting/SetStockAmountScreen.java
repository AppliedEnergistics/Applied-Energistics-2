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

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.NumberEntryType;
import appeng.client.gui.implementations.AESubScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.NumberEntryWidget;
import appeng.core.localization.GuiText;
import appeng.menu.implementations.SetStockAmountMenu;

/**
 * Allows precisely setting the amount to stock for an interface slot.
 */
public class SetStockAmountScreen extends AEBaseScreen<SetStockAmountMenu> {

    private final NumberEntryWidget amount;

    private boolean initialAmountInitialized;

    public SetStockAmountScreen(SetStockAmountMenu menu, Inventory playerInventory, Component title,
            ScreenStyle style) {
        super(menu, playerInventory, title, style);

        widgets.addButton("save", GuiText.Set.text(), this::confirm);

        AESubScreen.addBackButton(menu, "back", widgets);

        this.amount = new NumberEntryWidget(NumberEntryType.CRAFT_ITEM_COUNT);
        this.amount.setValue(1);
        this.amount.setTextFieldBounds(62, 57, 50);
        this.amount.setMinValue(1);
        this.amount.setHideValidationIcon(true);
        this.amount.setOnConfirm(this::confirm);
        widgets.add("amountToCraft", this.amount);
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();

        this.amount.setMaxValue(menu.getMaxAmount());

        if (this.menu.getInitialAmount() != -1 && !this.initialAmountInitialized) {
            this.amount.setValue(this.menu.getInitialAmount());
            this.initialAmountInitialized = true;
        }
    }

    private void confirm() {
        menu.confirm(this.amount.getIntValue().orElse(0));
    }
}
