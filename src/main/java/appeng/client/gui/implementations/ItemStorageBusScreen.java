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

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import appeng.api.config.*;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.ActionButton;
import appeng.client.gui.widgets.ServerSettingToggleButton;
import appeng.client.gui.widgets.SettingToggleButton;
import appeng.menu.implementations.ItemStorageBusMenu;

public class ItemStorageBusScreen extends UpgradeableScreen<ItemStorageBusMenu> {

    private final SettingToggleButton<AccessRestriction> rwMode;
    private final SettingToggleButton<StorageFilter> storageFilter;
    private final SettingToggleButton<YesNo> filterOnExtract;
    private final SettingToggleButton<FuzzyMode> fuzzyMode;

    public ItemStorageBusScreen(ItemStorageBusMenu menu, Inventory playerInventory, Component title,
            ScreenStyle style) {
        super(menu, playerInventory, title, style);

        widgets.addOpenPriorityButton();

        addToLeftToolbar(new ActionButton(ActionItems.CLOSE, btn -> menu.clear()));
        addToLeftToolbar(new ActionButton(ActionItems.WRENCH, btn -> menu.partition()));
        this.rwMode = new ServerSettingToggleButton<>(Settings.ACCESS, AccessRestriction.READ_WRITE);
        this.storageFilter = new ServerSettingToggleButton<>(Settings.STORAGE_FILTER, StorageFilter.EXTRACTABLE_ONLY);
        this.filterOnExtract = new ServerSettingToggleButton<>(Settings.FILTER_ON_EXTRACT, YesNo.YES);
        this.fuzzyMode = new ServerSettingToggleButton<>(Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL);

        this.addToLeftToolbar(this.storageFilter);
        this.addToLeftToolbar(this.filterOnExtract);
        this.addToLeftToolbar(this.fuzzyMode);
        this.addToLeftToolbar(this.rwMode);
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();

        this.storageFilter.set(this.menu.getStorageFilter());
        this.rwMode.set(this.menu.getReadWriteMode());
        this.filterOnExtract.set(this.menu.getFilterOnExtract());
        this.fuzzyMode.set(this.menu.getFuzzyMode());
        this.fuzzyMode.setVisibility(menu.hasUpgrade(Upgrades.FUZZY));
    }

}
