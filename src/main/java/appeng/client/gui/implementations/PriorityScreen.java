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

import java.util.OptionalInt;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.NumberEntryType;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.NumberEntryWidget;
import appeng.menu.implementations.PriorityMenu;

public class PriorityScreen extends AEBaseScreen<PriorityMenu> {

    private final NumberEntryWidget priority;

    public PriorityScreen(PriorityMenu menu, Inventory playerInventory, Component title,
            ScreenStyle style) {
        super(menu, playerInventory, title, style);
        AESubScreen.addBackButton(menu, "back", widgets);

        this.priority = new NumberEntryWidget(NumberEntryType.UNITLESS);
        this.priority.setTextFieldStyle(style.getWidget("priorityInput"));
        this.priority.setMinValue(Integer.MIN_VALUE);
        this.priority.setLongValue(this.menu.getPriorityValue());
        this.priority.setOnChange(this::savePriority);
        this.priority.setOnConfirm(() -> {
            savePriority();
            AESubScreen.goBack();
        });
        widgets.add("priority", priority);
    }

    private void savePriority() {
        OptionalInt priority = this.priority.getIntValue();
        if (priority.isPresent()) {
            menu.setPriority(priority.getAsInt());
        }
    }
}
