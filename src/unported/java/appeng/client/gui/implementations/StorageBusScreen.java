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
import net.minecraft.text.Text;

import appeng.api.config.AccessRestriction;
import appeng.api.config.ActionItems;
import appeng.api.config.FuzzyMode;
import appeng.api.config.Settings;
import appeng.api.config.StorageFilter;
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

public class StorageBusScreen extends UpgradeableScreen<StorageBusContainer> {

    private SettingToggleButton<AccessRestriction> rwMode;
    private SettingToggleButton<StorageFilter> storageFilter;

    public StorageBusScreen(StorageBusContainer container, PlayerInventory playerInventory, Text title) {
        super(container, playerInventory, title);
        this.backgroundHeight = 251;
    }

    @Override
    protected void addButtons() {
        addButton(new ActionButton(this.x - 18, this.y + 8, ActionItems.CLOSE, btn -> clear()));
        addButton(new ActionButton(this.x - 18, this.y + 28, ActionItems.WRENCH, btn -> partition()));
        this.rwMode = new ServerSettingToggleButton<>(this.x - 18, this.y + 48, Settings.ACCESS,
                AccessRestriction.READ_WRITE);
        this.storageFilter = new ServerSettingToggleButton<>(this.x - 18, this.y + 68,
                Settings.STORAGE_FILTER, StorageFilter.EXTRACTABLE_ONLY);
        this.fuzzyMode = new ServerSettingToggleButton<>(this.x - 18, this.y + 88, Settings.FUZZY_MODE,
                FuzzyMode.IGNORE_ALL);

        this.addButton(new TabButton(this.x + 154, this.y, 2 + 4 * 16, GuiText.Priority.getLocal(),
                this.itemRenderer, btn -> openPriorityGui()));

        this.addButton(this.storageFilter);
        this.addButton(this.fuzzyMode);
        this.addButton(this.rwMode);
    }

    @Override
    public void drawFG(MatrixStack matrices, final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        this.textRenderer.draw(matrices, this.getGuiDisplayName(GuiText.StorageBus.getLocal()), 8, 6, 4210752);
        this.textRenderer.draw(matrices, GuiText.inventory.getLocal(), 8, this.backgroundHeight - 96 + 3, 4210752);

        if (this.fuzzyMode != null) {
            this.fuzzyMode.set(this.cvb.getFuzzyMode());
        }

        if (this.storageFilter != null) {
            this.storageFilter.set(((StorageBusContainer) this.cvb).getStorageFilter());
        }

        if (this.rwMode != null) {
            this.rwMode.set(((StorageBusContainer) this.cvb).getReadWriteMode());
        }
    }

    @Override
    protected String getBackground() {
        return "guis/storagebus.png";
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
