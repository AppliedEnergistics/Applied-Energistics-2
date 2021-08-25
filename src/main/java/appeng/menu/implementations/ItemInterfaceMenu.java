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

package appeng.menu.implementations;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

import appeng.api.config.SecurityPermissions;
import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.util.IConfigManager;
import appeng.client.gui.implementations.ItemInterfaceScreen;
import appeng.helpers.DualityItemInterface;
import appeng.helpers.IItemInterfaceHost;
import appeng.menu.SlotSemantic;
import appeng.menu.guisync.GuiSync;
import appeng.menu.slot.AppEngSlot;
import appeng.menu.slot.FakeSlot;
import appeng.menu.slot.RestrictedInputSlot;

/**
 * @see ItemInterfaceScreen
 */
public class ItemInterfaceMenu extends UpgradeableMenu<IItemInterfaceHost> {

    public static final MenuType<ItemInterfaceMenu> TYPE = MenuTypeBuilder
            .create(ItemInterfaceMenu::new, IItemInterfaceHost.class)
            .requirePermission(SecurityPermissions.BUILD)
            .build("interface");

    @GuiSync(3)
    public YesNo bMode = YesNo.NO;

    @GuiSync(4)
    public YesNo iTermMode = YesNo.YES;

    public ItemInterfaceMenu(int id, final Inventory ip, IItemInterfaceHost host) {
        super(TYPE, id, ip, host);

        DualityItemInterface duality = host.getInterfaceDuality();

        for (int x = 0; x < DualityItemInterface.NUMBER_OF_PATTERN_SLOTS; x++) {
            this.addSlot(new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.ENCODED_PATTERN,
                    duality.getPatterns(), x),
                    SlotSemantic.ENCODED_PATTERN);
        }

        for (int x = 0; x < DualityItemInterface.NUMBER_OF_CONFIG_SLOTS; x++) {
            this.addSlot(new FakeSlot(duality.getConfig(), x), SlotSemantic.CONFIG);
        }

        for (int x = 0; x < DualityItemInterface.NUMBER_OF_STORAGE_SLOTS; x++) {
            this.addSlot(new AppEngSlot(duality.getStorage(), x), SlotSemantic.STORAGE);
        }
    }

    @Override
    protected void setupConfig() {
        this.setupUpgrades();
    }

    @Override
    public int availableUpgrades() {
        return 1;
    }

    @Override
    protected void loadSettingsFromHost(IConfigManager cm) {
        this.setBlockingMode(cm.getSetting(Settings.BLOCK));
        this.setInterfaceTerminalMode(cm.getSetting(Settings.INTERFACE_TERMINAL));
    }

    public YesNo getBlockingMode() {
        return this.bMode;
    }

    private void setBlockingMode(final YesNo bMode) {
        this.bMode = bMode;
    }

    public YesNo getInterfaceTerminalMode() {
        return this.iTermMode;
    }

    private void setInterfaceTerminalMode(final YesNo iTermMode) {
        this.iTermMode = iTermMode;
    }
}
