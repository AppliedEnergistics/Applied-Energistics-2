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

import appeng.client.gui.Blitter;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.items.IItemHandler;

import appeng.api.config.ActionItems;
import appeng.api.config.CopyMode;
import appeng.api.config.FuzzyMode;
import appeng.api.config.Settings;
import appeng.api.config.Upgrades;
import appeng.api.implementations.items.IUpgradeModule;
import appeng.client.gui.widgets.ActionButton;
import appeng.client.gui.widgets.SettingToggleButton;
import appeng.client.gui.widgets.ToggleButton;
import appeng.container.implementations.CellWorkbenchContainer;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.ConfigValuePacket;

public class CellWorkbenchScreen extends UpgradeableScreen<CellWorkbenchContainer> {

    private static final Blitter BACKGROUND = Blitter.texture("guis/cellworkbench.png").src(0, 0, 176, 251);

    private ToggleButton copyMode;

    public CellWorkbenchScreen(CellWorkbenchContainer container, PlayerInventory playerInventory,
            ITextComponent title) {
        super(container, playerInventory, title, BACKGROUND);
    }

    @Override
    protected void addButtons() {
        this.fuzzyMode = this.addToLeftToolbar(new SettingToggleButton<>(0, 0,
                Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL, this::toggleFuzzyMode));
        this.addToLeftToolbar(
                new ActionButton(0, 0, ActionItems.WRENCH, act -> action("Partition")));
        this.addToLeftToolbar(new ActionButton(0, 0, ActionItems.CLOSE, act -> action("Clear")));
        this.copyMode = this.addToLeftToolbar(new ToggleButton(0, 0, 11 * 16 + 5, 12 * 16 + 5,
                GuiText.CopyMode.getLocal(), GuiText.CopyModeDesc.getLocal(), act -> action("CopyMode")));
    }

    @Override
    protected void handleButtonVisibility() {
        this.copyMode.setState(this.container.getCopyMode() == CopyMode.CLEAR_ON_REMOVE);

        boolean hasFuzzy = false;
        final IItemHandler inv = this.container.getCellUpgradeInventory();
        for (int x = 0; x < inv.getSlots(); x++) {
            final ItemStack is = inv.getStackInSlot(x);
            if (!is.isEmpty() && is.getItem() instanceof IUpgradeModule) {
                if (((IUpgradeModule) is.getItem()).getType(is) == Upgrades.FUZZY) {
                    hasFuzzy = true;
                }
            }
        }
        this.fuzzyMode.setVisibility(hasFuzzy);
    }

    @Override
    protected GuiText getName() {
        return GuiText.CellWorkbench;
    }

    private void action(String type) {
        NetworkHandler.instance().sendToServer(new ConfigValuePacket("CellWorkbench.Action", type));
    }

    private void toggleFuzzyMode(SettingToggleButton<FuzzyMode> button, boolean backwards) {
        FuzzyMode fz = button.getNextValue(backwards);
        NetworkHandler.instance().sendToServer(new ConfigValuePacket("CellWorkbench.Fuzzy", fz.name()));
    }

}
