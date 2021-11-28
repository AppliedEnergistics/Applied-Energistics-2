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

import javax.annotation.Nullable;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;

import appeng.api.config.SecurityPermissions;
import appeng.api.config.Settings;
import appeng.api.config.Upgrades;
import appeng.api.storage.GenericStack;
import appeng.api.storage.data.AEKey;
import appeng.api.util.IConfigManager;
import appeng.client.gui.implementations.StorageLevelEmitterScreen;
import appeng.menu.SlotSemantic;
import appeng.menu.slot.FakeSlot;
import appeng.parts.automation.StorageLevelEmitterPart;

/**
 * @see StorageLevelEmitterScreen
 */
public class StorageLevelEmitterMenu extends UpgradeableMenu<StorageLevelEmitterPart> {

    private static final String ACTION_SET_REPORTING_VALUE = "setReportingValue";

    public static final MenuType<StorageLevelEmitterMenu> TYPE = MenuTypeBuilder
            .create(StorageLevelEmitterMenu::new, StorageLevelEmitterPart.class)
            .requirePermission(SecurityPermissions.BUILD)
            .withInitialData((host, buffer) -> {
                buffer.writeVarLong(host.getReportingValue());
            }, (host, menu, buffer) -> {
                menu.reportingValue = buffer.readVarLong();
            })
            .build("storage_level_emitter");

    // Only synced once on menu-open, and only used on client
    private long reportingValue;

    private FakeSlot configSlot;

    public StorageLevelEmitterMenu(MenuType<StorageLevelEmitterMenu> menuType, int id, Inventory ip,
            StorageLevelEmitterPart te) {
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
            getHost().setReportingValue(reportingValue);
        }
    }

    @Override
    protected void setupConfig() {
        this.setupUpgrades();

        var inv = getHost().getConfig().createMenuWrapper();
        configSlot = new FakeSlot(inv, 0);
        this.addSlot(configSlot, SlotSemantic.CONFIG);
    }

    @Override
    public void onSlotChange(Slot s) {
        super.onSlotChange(s);
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

    @Nullable
    public AEKey getConfiguredFilter() {
        var stack = GenericStack.unwrapItemStack(configSlot.getItem());
        return stack != null ? stack.what() : null;
    }
}
