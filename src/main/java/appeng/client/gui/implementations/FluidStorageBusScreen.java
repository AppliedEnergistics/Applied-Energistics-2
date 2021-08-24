/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2018, AlgorithmX2, All rights reserved.
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

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import appeng.api.config.AccessRestriction;
import appeng.api.config.ActionItems;
import appeng.api.config.Settings;
import appeng.api.config.StorageFilter;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.ActionButton;
import appeng.client.gui.widgets.FluidSlotWidget;
import appeng.client.gui.widgets.OptionalFluidSlotWidget;
import appeng.client.gui.widgets.ServerSettingToggleButton;
import appeng.client.gui.widgets.SettingToggleButton;
import appeng.menu.SlotSemantic;
import appeng.menu.implementations.FluidStorageBusMenu;
import appeng.util.fluid.IAEFluidTank;

/**
 * @author BrockWS
 * @version rv6 - 22/05/2018
 * @since rv6 22/05/2018
 */
public class FluidStorageBusScreen extends UpgradeableScreen<FluidStorageBusMenu> {

    private final SettingToggleButton<AccessRestriction> rwMode;
    private final SettingToggleButton<StorageFilter> storageFilter;

    public FluidStorageBusScreen(FluidStorageBusMenu menu, Inventory playerInventory,
            Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);

        final IAEFluidTank config = this.menu.getFluidConfigInventory();

        for (int y = 0; y < 7; y++) {
            for (int x = 0; x < 9; x++) {
                final int idx = y * 9 + x;
                if (y < 2) {
                    addSlot(new FluidSlotWidget(config, idx), SlotSemantic.CONFIG);
                } else {
                    addSlot(new OptionalFluidSlotWidget(config, menu, idx, y - 2), SlotSemantic.CONFIG);
                }
            }
        }

        widgets.addOpenPriorityButton();

        addToLeftToolbar(new ActionButton(ActionItems.CLOSE, btn -> menu.clear()));
        addToLeftToolbar(new ActionButton(ActionItems.WRENCH, btn -> menu.partition()));
        this.rwMode = new ServerSettingToggleButton<>(Settings.ACCESS,
                AccessRestriction.READ_WRITE);
        this.storageFilter = new ServerSettingToggleButton<>(
                Settings.STORAGE_FILTER, StorageFilter.EXTRACTABLE_ONLY);
        addToLeftToolbar(this.storageFilter);
        addToLeftToolbar(this.rwMode);
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();

        this.storageFilter.set(this.menu.getStorageFilter());
        this.rwMode.set(this.menu.getReadWriteMode());
    }

}
