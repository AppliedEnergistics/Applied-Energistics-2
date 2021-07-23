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

import java.util.Collections;
import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import appeng.api.config.ActionItems;
import appeng.api.config.CopyMode;
import appeng.api.config.FuzzyMode;
import appeng.api.config.Settings;
import appeng.api.config.Upgrades;
import appeng.api.implementations.items.IUpgradeModule;
import appeng.client.gui.Icon;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.ActionButton;
import appeng.client.gui.widgets.SettingToggleButton;
import appeng.client.gui.widgets.ToggleButton;
import appeng.container.implementations.CellWorkbenchContainer;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.ConfigValuePacket;

public class CellWorkbenchScreen extends UpgradeableScreen<CellWorkbenchContainer> {

    private final ToggleButton copyMode;

    private final SettingToggleButton<FuzzyMode> fuzzyMode;

    public CellWorkbenchScreen(CellWorkbenchContainer container, Inventory playerInventory,
                               Component title, ScreenStyle style) {
        super(container, playerInventory, title, style);

        this.fuzzyMode = addToLeftToolbar(
                new SettingToggleButton<>(Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL, this::toggleFuzzyMode));
        this.addToLeftToolbar(new ActionButton(ActionItems.WRENCH, act -> action("Partition")));
        this.addToLeftToolbar(new ActionButton(ActionItems.CLOSE, act -> action("Clear")));
        this.copyMode = this.addToLeftToolbar(new ToggleButton(Icon.COPY_MODE_ON, Icon.COPY_MODE_OFF,
                GuiText.CopyMode.text(), GuiText.CopyModeDesc.text(), act -> action("CopyMode")));
    }

    /**
     * For cell workbenches it is the item currently in the slot that determines which upgrades are compatible.
     */
    @Override
    protected List<Component> getCompatibleUpgrades() {
        ItemStack workbenchItem = menu.getWorkbenchItem();
        if (workbenchItem.isEmpty()) {
            return Collections.emptyList();
        }

        return super.getCompatibleUpgrades(workbenchItem.getItem());
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();

        this.copyMode.setState(this.menu.getCopyMode() == CopyMode.CLEAR_ON_REMOVE);

        boolean hasFuzzy = false;
        final IItemHandler inv = this.menu.getCellUpgradeInventory();
        for (int x = 0; x < inv.getSlots(); x++) {
            final ItemStack is = inv.getStackInSlot(x);
            if (!is.isEmpty() && is.getItem() instanceof IUpgradeModule
                    && ((IUpgradeModule) is.getItem()).getType(is) == Upgrades.FUZZY) {
                hasFuzzy = true;
            }
        }
        this.fuzzyMode.setVisibility(hasFuzzy);
    }

    private void action(String type) {
        NetworkHandler.instance().sendToServer(new ConfigValuePacket("CellWorkbench.Action", type));
    }

    private void toggleFuzzyMode(SettingToggleButton<FuzzyMode> button, boolean backwards) {
        FuzzyMode fz = button.getNextValue(backwards);
        NetworkHandler.instance().sendToServer(new ConfigValuePacket("CellWorkbench.Fuzzy", fz.name()));
    }

}
