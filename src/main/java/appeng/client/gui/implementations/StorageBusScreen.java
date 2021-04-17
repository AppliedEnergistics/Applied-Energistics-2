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

import appeng.api.config.AccessRestriction;
import appeng.api.config.ActionItems;
import appeng.api.config.FuzzyMode;
import appeng.api.config.Settings;
import appeng.api.config.StorageFilter;
import appeng.api.config.Upgrades;
import appeng.client.gui.Blitter;
import appeng.client.gui.widgets.ActionButton;
import appeng.client.gui.widgets.ServerSettingToggleButton;
import appeng.client.gui.widgets.SettingToggleButton;
import appeng.client.gui.widgets.TabButton;
import appeng.container.implementations.PriorityContainer;
import appeng.container.implementations.StorageBusContainer;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.ConfigValuePacket;
import appeng.core.sync.packets.SwitchGuisPacket;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class StorageBusScreen extends UpgradeableScreen<StorageBusContainer> {

    private static final Blitter BACKGROUND = Blitter.texture("guis/storagebus.png")
            .src(0, 0, 176, 251);

    private SettingToggleButton<AccessRestriction> rwMode;
    private SettingToggleButton<StorageFilter> storageFilter;
    private SettingToggleButton<FuzzyMode> fuzzyMode;

    public StorageBusScreen(StorageBusContainer container, PlayerInventory playerInventory, ITextComponent title) {
        super(container, playerInventory, title, BACKGROUND);
    }

    @Override
    public void init() {
        super.init();

        this.addButton(new TabButton(this.guiLeft + 154, this.guiTop, 2 + 4 * 16, GuiText.Priority.text(),
                this.itemRenderer, btn -> openPriorityGui()));

        addToLeftToolbar(new ActionButton(0, 0, ActionItems.CLOSE, btn -> clear()));
        addToLeftToolbar(new ActionButton(0, 0, ActionItems.WRENCH, btn -> partition()));
        this.rwMode = new ServerSettingToggleButton<>(0, 0, Settings.ACCESS,
                AccessRestriction.READ_WRITE);
        this.storageFilter = new ServerSettingToggleButton<>(0, 0,
                Settings.STORAGE_FILTER, StorageFilter.EXTRACTABLE_ONLY);
        this.fuzzyMode = new ServerSettingToggleButton<>(0, 0, Settings.FUZZY_MODE,
                FuzzyMode.IGNORE_ALL);

        this.addToLeftToolbar(this.storageFilter);
        this.addToLeftToolbar(this.fuzzyMode);
        this.addToLeftToolbar(this.rwMode);
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();

        this.storageFilter.set(this.container.getStorageFilter());
        this.rwMode.set(this.container.getReadWriteMode());
        this.fuzzyMode.set(this.container.getFuzzyMode());
        this.fuzzyMode.setVisibility(container.hasUpgrade(Upgrades.FUZZY));
    }

    private void partition() {
        NetworkHandler.instance().sendToServer(new ConfigValuePacket("StorageBus.Action", "Partition"));
    }

    private void clear() {
        NetworkHandler.instance().sendToServer(new ConfigValuePacket("StorageBus.Action", "Clear"));
    }

    private void openPriorityGui() {
        NetworkHandler.instance().sendToServer(new SwitchGuisPacket(PriorityContainer.TYPE));
    }

}
