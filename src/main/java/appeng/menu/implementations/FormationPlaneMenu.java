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
import appeng.api.config.Upgrades;
import appeng.api.config.YesNo;
import appeng.api.storage.data.AEKey;
import appeng.api.util.IConfigManager;
import appeng.client.gui.implementations.FormationPlaneScreen;
import appeng.menu.SlotSemantic;
import appeng.menu.guisync.GuiSync;
import appeng.menu.slot.FakeSlot;
import appeng.menu.slot.OptionalFakeSlot;
import appeng.parts.automation.FormationPlanePart;

/**
 * This is used by both fluid and item formation planes.
 *
 * @see FormationPlaneScreen
 */
public class FormationPlaneMenu extends UpgradeableMenu<FormationPlanePart> {

    public static final MenuType<FormationPlaneMenu> ITEM_TYPE = MenuTypeBuilder
            .create(FormationPlaneMenu::new, FormationPlanePart.class)
            .requirePermission(SecurityPermissions.BUILD)
            .build("item_formationplane");

    public static final MenuType<FormationPlaneMenu> FLUID_TYPE = MenuTypeBuilder
            .create(FormationPlaneMenu::new, FormationPlanePart.class)
            .requirePermission(SecurityPermissions.BUILD)
            .build("fluid_formationplane");

    @GuiSync(7)
    public YesNo placeMode;

    public FormationPlaneMenu(MenuType<FormationPlaneMenu> type, int id, Inventory ip,
            FormationPlanePart host) {
        super(type, id, ip, host);
    }

    @Override
    protected void setupConfig() {
        var config = this.getHost().getConfig().createMenuWrapper();
        for (int y = 0; y < 7; y++) {
            for (int x = 0; x < 9; x++) {
                int invIdx = y * 9 + x;
                if (y < 2) {
                    this.addSlot(new FakeSlot(config, invIdx), SlotSemantic.CONFIG);
                } else {
                    this.addSlot(new OptionalFakeSlot(config, this, invIdx, y - 2), SlotSemantic.CONFIG);
                }
            }
        }

        this.setupUpgrades();
    }

    @Override
    protected boolean supportCapacity() {
        return true;
    }

    @Override
    protected void loadSettingsFromHost(IConfigManager cm) {
        if (supportsFuzzyRangeSearch()) {
            this.setFuzzyMode(cm.getSetting(Settings.FUZZY_MODE));
        }
        if (getHost().supportsEntityPlacement()) {
            this.setPlaceMode(cm.getSetting(Settings.PLACE_BLOCK));
        }
    }

    @Override
    public boolean isSlotEnabled(final int idx) {
        final int upgrades = getUpgrades().getInstalledUpgrades(Upgrades.CAPACITY);
        return upgrades > idx;
    }

    public YesNo getPlaceMode() {
        return this.placeMode;
    }

    private void setPlaceMode(final YesNo placeMode) {
        this.placeMode = placeMode;
    }

    public boolean supportsPlaceMode() {
        return getHost().supportsEntityPlacement();
    }

    public boolean supportsFuzzyMode() {
        return hasUpgrade(Upgrades.FUZZY) && supportsFuzzyRangeSearch();
    }

    private boolean supportsFuzzyRangeSearch() {
        for (AEKey key : this.getHost().getConfig().keySet()) {
            if (key.supportsFuzzyRangeSearch()) {
                return true;
            }
        }
        return false;
    }
}
