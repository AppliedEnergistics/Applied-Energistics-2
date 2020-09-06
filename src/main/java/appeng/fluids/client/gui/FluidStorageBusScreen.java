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

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

import appeng.api.config.AccessRestriction;
import appeng.api.config.ActionItems;
import appeng.api.config.FuzzyMode;
import appeng.api.config.Settings;
import appeng.api.config.StorageFilter;
import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.widgets.ActionButton;
import appeng.client.gui.widgets.ServerSettingToggleButton;
import appeng.client.gui.widgets.SettingToggleButton;
import appeng.client.gui.widgets.TabButton;
import appeng.client.theme.ThemeColor;
import appeng.container.implementations.PriorityContainer;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.ConfigValuePacket;
import appeng.core.sync.packets.SwitchGuisPacket;
import appeng.fluids.client.gui.widgets.FluidSlotWidget;
import appeng.fluids.client.gui.widgets.OptionalFluidSlotWidget;
import appeng.fluids.container.FluidStorageBusContainer;
import appeng.fluids.util.IAEFluidTank;

/**
 * @author BrockWS
 * @version rv6 - 22/05/2018
 * @since rv6 22/05/2018
 */
public class FluidStorageBusScreen extends UpgradeableScreen<FluidStorageBusContainer> {
    private SettingToggleButton<AccessRestriction> rwMode;
    private SettingToggleButton<StorageFilter> storageFilter;

    public FluidStorageBusScreen(FluidStorageBusContainer container, PlayerInventory playerInventory,
            ITextComponent title) {
        super(container, playerInventory, title);
        this.ySize = 251;
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
    }

    @Override
    protected void addButtons() {
        addButton(new ActionButton(this.guiLeft - 18, this.guiTop + 8, ActionItems.CLOSE, btn -> clear()));
        addButton(new ActionButton(this.guiLeft - 18, this.guiTop + 28, ActionItems.WRENCH, btn -> partition()));
        this.rwMode = new ServerSettingToggleButton<>(this.guiLeft - 18, this.guiTop + 48, Settings.ACCESS,
                AccessRestriction.READ_WRITE);
        this.storageFilter = new ServerSettingToggleButton<>(this.guiLeft - 18, this.guiTop + 68,
                Settings.STORAGE_FILTER, StorageFilter.EXTRACTABLE_ONLY);
        this.fuzzyMode = new ServerSettingToggleButton<>(this.guiLeft - 18, this.guiTop + 88, Settings.FUZZY_MODE,
                FuzzyMode.IGNORE_ALL);

        addButton(this.addButton(new TabButton(this.guiLeft + 154, this.guiTop, 2 + 4 * 16, GuiText.Priority.text(),
                this.itemRenderer, btn -> openPriorityGui())));

        this.addButton(this.storageFilter);
        this.addButton(this.fuzzyMode);
        this.addButton(this.rwMode);
    }

    @Override
    public void drawFG(MatrixStack matrixStack, final int offsetX, final int offsetY, final int mouseX,
            final int mouseY) {
        this.font.drawString(matrixStack, this.getGuiDisplayName(this.getName().text()).getString(), 8, 6,
                ThemeColor.TEXT_TITLE.argb());
        this.font.drawString(matrixStack, GuiText.inventory.text().getString(), 8, this.ySize - 96 + 3,
                ThemeColor.TEXT_TITLE.argb());

        if (this.fuzzyMode != null) {
            this.fuzzyMode.set(this.cvb.getFuzzyMode());
        }

        if (this.storageFilter != null) {
            this.storageFilter.set(((FluidStorageBusContainer) this.cvb).getStorageFilter());
        }

        if (this.rwMode != null) {
            this.rwMode.set(((FluidStorageBusContainer) this.cvb).getReadWriteMode());
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

    @Override
    protected GuiText getName() {
        return GuiText.StorageBusFluids;
    }
}
