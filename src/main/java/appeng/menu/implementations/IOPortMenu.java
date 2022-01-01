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

import appeng.api.config.FullnessMode;
import appeng.api.config.OperationMode;
import appeng.api.config.SecurityPermissions;
import appeng.api.config.Settings;
import appeng.api.inventories.ISegmentedInventory;
import appeng.api.util.IConfigManager;
import appeng.blockentity.storage.IOPortBlockEntity;
import appeng.menu.SlotSemantics;
import appeng.menu.guisync.GuiSync;
import appeng.menu.slot.OutputSlot;
import appeng.menu.slot.RestrictedInputSlot;

/**
 * @see appeng.client.gui.implementations.IOPortScreen
 */
public class IOPortMenu extends UpgradeableMenu<IOPortBlockEntity> {

    public static final MenuType<IOPortMenu> TYPE = MenuTypeBuilder
            .create(IOPortMenu::new, IOPortBlockEntity.class)
            .requirePermission(SecurityPermissions.BUILD)
            .build("ioport");

    @GuiSync(2)
    public FullnessMode fMode = FullnessMode.EMPTY;
    @GuiSync(3)
    public OperationMode opMode = OperationMode.EMPTY;

    public IOPortMenu(int id, Inventory ip, IOPortBlockEntity host) {
        super(TYPE, id, ip, host);
    }

    @Override
    protected void setupConfig() {
        var cells = this.getHost().getSubInventory(ISegmentedInventory.CELLS);

        for (int i = 0; i < 6; i++) {
            this.addSlot(new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.STORAGE_CELLS, cells, i),
                    SlotSemantics.MACHINE_INPUT);
        }

        for (int i = 0; i < 6; i++) {
            this.addSlot(new OutputSlot(cells, 6 + i,
                    RestrictedInputSlot.PlacableItemType.STORAGE_CELLS.icon), SlotSemantics.MACHINE_OUTPUT);
        }

        this.setupUpgrades();
    }

    @Override
    protected void loadSettingsFromHost(IConfigManager cm) {
        this.setOperationMode(cm.getSetting(Settings.OPERATION_MODE));
        this.setFullMode(cm.getSetting(Settings.FULLNESS_MODE));
        this.setRedStoneMode(cm.getSetting(Settings.REDSTONE_CONTROLLED));
    }

    public FullnessMode getFullMode() {
        return this.fMode;
    }

    private void setFullMode(FullnessMode fMode) {
        this.fMode = fMode;
    }

    public OperationMode getOperationMode() {
        return this.opMode;
    }

    private void setOperationMode(OperationMode opMode) {
        this.opMode = opMode;
    }
}
