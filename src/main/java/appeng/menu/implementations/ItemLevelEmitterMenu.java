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
import appeng.api.implementations.blockentities.ISegmentedInventory;
import appeng.api.util.IConfigManager;
import appeng.menu.SlotSemantic;
import appeng.menu.guisync.GuiSync;
import appeng.menu.slot.FakeTypeOnlySlot;
import appeng.parts.automation.ItemLevelEmitterPart;

public class ItemLevelEmitterMenu extends UpgradeableMenu<ItemLevelEmitterPart> {

    private static final String ACTION_SET_REPORTING_VALUE = "setReportingValue";

    public static final MenuType<ItemLevelEmitterMenu> TYPE = MenuTypeBuilder
            .create(ItemLevelEmitterMenu::new, ItemLevelEmitterPart.class)
            .requirePermission(SecurityPermissions.BUILD)
            .withInitialData((host, buffer) -> {
                buffer.writeVarLong(host.getReportingValue());
            }, (host, menu, buffer) -> {
                menu.reportingValue = buffer.readVarLong();
            })
            .build("item_level_emitter");

    @GuiSync(3)
    public YesNo cmType;

    // Only synced once on menu-open, and only used on client
    private long reportingValue;

    public ItemLevelEmitterMenu(int id, final Inventory ip, final ItemLevelEmitterPart te) {
        super(TYPE, id, ip, te);

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

        var inv = getHost().getSubInventory(ISegmentedInventory.CONFIG);
        this.addSlot(new FakeTypeOnlySlot(inv, 0), SlotSemantic.CONFIG);
    }

    @Override
    protected boolean supportCapacity() {
        return false;
    }

    @Override
    protected void loadSettingsFromHost(IConfigManager cm) {
        this.setCraftingMode(cm.getSetting(Settings.CRAFT_VIA_REDSTONE));
        this.setFuzzyMode(cm.getSetting(Settings.FUZZY_MODE));
        this.setRedStoneMode(cm.getSetting(Settings.REDSTONE_EMITTER));
    }

    @Override
    public YesNo getCraftingMode() {
        return this.cmType;
    }

    @Override
    public void setCraftingMode(final YesNo cmType) {
        this.cmType = cmType;
    }

}
