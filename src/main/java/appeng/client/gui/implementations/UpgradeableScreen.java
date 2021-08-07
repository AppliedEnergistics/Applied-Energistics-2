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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntity;

import appeng.api.config.Upgrades;
import appeng.api.implementations.IUpgradeableHost;
import appeng.api.parts.IPart;
import appeng.api.parts.PartItemStack;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.ToolboxPanel;
import appeng.client.gui.widgets.UpgradesPanel;
import appeng.core.localization.GuiText;
import appeng.menu.SlotSemantic;
import appeng.menu.implementations.UpgradeableMenu;

/**
 * This screen adds the ability for {@link IUpgradeableHost} screens to show the upgrade inventory and the player's
 * toolbox to more easily install/remove upgrades.
 */
public class UpgradeableScreen<T extends UpgradeableMenu> extends AEBaseScreen<T> {

    public UpgradeableScreen(T menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);

        this.widgets.add("upgrades", new UpgradesPanel(
                menu.getSlots(SlotSemantic.UPGRADE),
                this::getCompatibleUpgrades));
        if (menu.hasToolbox()) {
            this.widgets.add("toolbox", new ToolboxPanel(style, menu.getToolboxName()));
        }
    }

    /**
     * Gets the tooltip text that is shown for empty slots of the upgrade panel to indicate which upgrades are
     * compatible.
     */
    protected List<Component> getCompatibleUpgrades() {
        IUpgradeableHost host = menu.getUpgradeable();

        Item item;
        if (host instanceof IPart) {
            item = ((IPart) host).getItemStack(PartItemStack.NETWORK).getItem();
        } else if (host instanceof BlockEntity te) {
            item = te.getBlockState().getBlock().asItem();
        } else {
            return Collections.emptyList();
        }

        return getCompatibleUpgrades(item);
    }

    protected List<Component> getCompatibleUpgrades(Item machineItem) {
        List<Component> list = new ArrayList<>();
        list.add(GuiText.CompatibleUpgrades.text());

        for (Upgrades upgrade : Upgrades.values()) {
            for (Upgrades.Supported supported : upgrade.getSupported()) {
                if (supported.isSupported(machineItem)) {
                    list.add(GuiText.CompatibleUpgrade.text(upgrade.getDisplayName(), supported.getMaxCount())
                            .withStyle(ChatFormatting.GRAY));
                    break;
                }
            }
        }

        return list;
    }

}
