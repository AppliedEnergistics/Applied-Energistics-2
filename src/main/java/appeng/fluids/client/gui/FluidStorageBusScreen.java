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

package appeng.fluids.client.gui;

import appeng.api.config.AccessRestriction;
import appeng.api.config.ActionItems;
import appeng.api.config.Settings;
import appeng.api.config.StorageFilter;
import appeng.client.gui.Blitter;
import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.widgets.ActionButton;
import appeng.client.gui.widgets.ServerSettingToggleButton;
import appeng.client.gui.widgets.SettingToggleButton;
import appeng.client.gui.widgets.TabButton;
import appeng.container.implementations.PriorityContainer;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.ConfigValuePacket;
import appeng.core.sync.packets.SwitchGuisPacket;
import appeng.fluids.client.gui.widgets.FluidSlotWidget;
import appeng.fluids.client.gui.widgets.OptionalFluidSlotWidget;
import appeng.fluids.container.FluidStorageBusContainer;
import appeng.fluids.util.IAEFluidTank;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

/**
 * @author BrockWS
 * @version rv6 - 22/05/2018
 * @since rv6 22/05/2018
 */
public class FluidStorageBusScreen extends UpgradeableScreen<FluidStorageBusContainer> {

    private static final Blitter BACKGROUND = Blitter.texture("guis/storagebus.png")
            .src(0, 0, 211, 251);

    private SettingToggleButton<AccessRestriction> rwMode;
    private SettingToggleButton<StorageFilter> storageFilter;

    public FluidStorageBusScreen(FluidStorageBusContainer container, PlayerInventory playerInventory,
                                 ITextComponent title) {
        super(container, playerInventory, title, BACKGROUND);
    }

    @Override
    public void init() {
        super.init();

        final int xo = 8;
        final int yo = 23 + 6;

        final IAEFluidTank config = this.container.getFluidConfigInventory();

        for (int y = 0; y < 7; y++) {
            for (int x = 0; x < 9; x++) {
                final int idx = y * 9 + x;
                if (y < 2) {
                    this.guiSlots.add(new FluidSlotWidget(config, idx, idx, xo + x * 18, yo + y * 18));
                } else {
                    this.guiSlots.add(new OptionalFluidSlotWidget(config, container, idx, idx, y - 2, xo, yo, x, y));
                }
            }
        }

        addButton(this.addButton(new TabButton(this.guiLeft + 154, this.guiTop, 2 + 4 * 16, GuiText.Priority.text(),
                this.itemRenderer, btn -> openPriorityGui())));

        addToLeftToolbar(new ActionButton(0, 0, ActionItems.CLOSE, btn -> clear()));
        addToLeftToolbar(new ActionButton(0, 0, ActionItems.WRENCH, btn -> partition()));
        this.rwMode = new ServerSettingToggleButton<>(0, 0, Settings.ACCESS,
                AccessRestriction.READ_WRITE);
        this.storageFilter = new ServerSettingToggleButton<>(0, 0,
                Settings.STORAGE_FILTER, StorageFilter.EXTRACTABLE_ONLY);
        addToLeftToolbar(this.storageFilter);
        addToLeftToolbar(this.rwMode);
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();

        this.storageFilter.set(this.container.getStorageFilter());
        this.rwMode.set(this.container.getReadWriteMode());
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
