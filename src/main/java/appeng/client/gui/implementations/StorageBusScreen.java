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

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

import appeng.api.config.AccessRestriction;
import appeng.api.config.ActionItems;
import appeng.api.config.FuzzyMode;
import appeng.api.config.Settings;
import appeng.api.config.StorageFilter;
import appeng.api.config.Upgrades;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.ActionButton;
import appeng.client.gui.widgets.ServerSettingToggleButton;
import appeng.client.gui.widgets.SettingToggleButton;
import appeng.container.implementations.StorageBusContainer;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.ConfigValuePacket;

public class StorageBusScreen extends UpgradeableScreen<StorageBusContainer> {

    private final SettingToggleButton<AccessRestriction> rwMode;
    private final SettingToggleButton<StorageFilter> storageFilter;
    private final SettingToggleButton<FuzzyMode> fuzzyMode;

    public StorageBusScreen(StorageBusContainer container, PlayerInventory playerInventory, ITextComponent title,
            ScreenStyle style) {
        super(container, playerInventory, title, style);

        widgets.addOpenPriorityButton();

        addToLeftToolbar(new ActionButton(ActionItems.CLOSE, btn -> clear()));
        addToLeftToolbar(new ActionButton(ActionItems.WRENCH, btn -> partition()));
        this.rwMode = new ServerSettingToggleButton<>(Settings.ACCESS,
                AccessRestriction.READ_WRITE);
        this.storageFilter = new ServerSettingToggleButton<>(
                Settings.STORAGE_FILTER, StorageFilter.EXTRACTABLE_ONLY);
        this.fuzzyMode = new ServerSettingToggleButton<>(Settings.FUZZY_MODE,
                FuzzyMode.IGNORE_ALL);

        this.addToLeftToolbar(this.storageFilter);
        this.addToLeftToolbar(this.fuzzyMode);
        this.addToLeftToolbar(this.rwMode);
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();

        this.storageFilter.set(this.menu.getStorageFilter());
        this.rwMode.set(this.menu.getReadWriteMode());
        this.fuzzyMode.set(this.menu.getFuzzyMode());
        this.fuzzyMode.setVisibility(menu.hasUpgrade(Upgrades.FUZZY));
    }

    private void partition() {
        NetworkHandler.instance().sendToServer(new ConfigValuePacket("StorageBus.Action", "Partition"));
    }

    private void clear() {
        NetworkHandler.instance().sendToServer(new ConfigValuePacket("StorageBus.Action", "Clear"));
    }

}
