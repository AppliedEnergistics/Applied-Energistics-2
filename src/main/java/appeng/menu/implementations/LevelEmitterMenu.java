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
import appeng.api.storage.data.AEFluidKey;
import appeng.api.util.IConfigManager;
import appeng.menu.SlotSemantic;
import appeng.menu.slot.FakeSlot;
import appeng.parts.automation.AbstractStorageLevelEmitterPart;
import appeng.parts.automation.FluidLevelEmitterPart;
import appeng.parts.automation.ItemLevelEmitterPart;

public class LevelEmitterMenu extends UpgradeableMenu<AbstractStorageLevelEmitterPart> {

    private static final String ACTION_SET_REPORTING_VALUE = "setReportingValue";

    public static final MenuType<LevelEmitterMenu> ITEM_TYPE = MenuTypeBuilder
            .create(LevelEmitterMenu::new, ItemLevelEmitterPart.class)
            .requirePermission(SecurityPermissions.BUILD)
            .withInitialData((host, buffer) -> {
                buffer.writeVarLong(host.getReportingValue());
            }, (host, menu, buffer) -> {
                menu.reportingValue = buffer.readVarLong();
            })
            .build("item_level_emitter");

    public static final MenuType<LevelEmitterMenu> FLUID_TYPE = MenuTypeBuilder
            .create(LevelEmitterMenu::new, FluidLevelEmitterPart.class)
            .requirePermission(SecurityPermissions.BUILD)
            .withInitialData((host, buffer) -> {
                // Convert stack size to millibuckets
                buffer.writeVarLong(host.getReportingValue() * 1000 / AEFluidKey.AMOUNT_BUCKET);
            }, (host, menu, buffer) -> {
                menu.reportingValue = buffer.readVarLong();
            })
            .build("fluid_level_emitter");

    // Only synced once on menu-open, and only used on client
    private long reportingValue;

    public LevelEmitterMenu(MenuType<LevelEmitterMenu> menuType, int id, Inventory ip,
            AbstractStorageLevelEmitterPart te) {
        super(menuType, id, ip, te);

        registerClientAction(ACTION_SET_REPORTING_VALUE, Long.class, this::setReportingValue);
    }

    public long getReportingValue() {
        return reportingValue;
    }

    public void setReportingValue(long reportingValue) {
        if (isClient()) {
            if (reportingValue != this.reportingValue) {
                this.reportingValue = reportingValue;
                sendClientAction(ACTION_SET_REPORTING_VALUE, reportingValue);
            }
        } else {
            if (getType() == FLUID_TYPE) {
                getHost().setReportingValue(reportingValue * AEFluidKey.AMOUNT_BUCKET / 1000);
            } else {
                getHost().setReportingValue(reportingValue);
            }
        }
    }

    @Override
    protected void setupConfig() {
        this.setupUpgrades();

        var inv = getHost().getConfig().createMenuWrapper();
        this.addSlot(new FakeSlot(inv, 0), SlotSemantic.CONFIG);
    }

    @Override
    protected boolean supportCapacity() {
        return false;
    }

    @Override
    protected void loadSettingsFromHost(IConfigManager cm) {
        this.setCraftingMode(cm.getSetting(Settings.CRAFT_VIA_REDSTONE));
        if (cm.hasSetting(Settings.FUZZY_MODE)) {
            this.setFuzzyMode(cm.getSetting(Settings.FUZZY_MODE));
        }
        this.setRedStoneMode(cm.getSetting(Settings.REDSTONE_EMITTER));
    }

    public boolean supportsFuzzySearch() {
        return getHost().getConfigManager().hasSetting(Settings.FUZZY_MODE) && hasUpgrade(Upgrades.FUZZY);
    }
}
