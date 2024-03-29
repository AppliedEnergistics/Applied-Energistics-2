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

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;

import appeng.api.config.Settings;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.util.IConfigManager;
import appeng.client.gui.implementations.StorageLevelEmitterScreen;
import appeng.core.definitions.AEItems;
import appeng.menu.SlotSemantics;
import appeng.menu.slot.FakeSlot;
import appeng.parts.automation.StorageLevelEmitterPart;

/**
 * @see StorageLevelEmitterScreen
 */
public class StorageLevelEmitterMenu extends UpgradeableMenu<StorageLevelEmitterPart> {

    private static final String ACTION_SET_REPORTING_VALUE = "setReportingValue";

    public static final MenuType<StorageLevelEmitterMenu> TYPE = MenuTypeBuilder
            .create(StorageLevelEmitterMenu::new, StorageLevelEmitterPart.class)
            .withInitialData((host, buffer) -> {
                GenericStack.writeBuffer(host.getConfig().getStack(0), buffer);
                buffer.writeVarLong(host.getReportingValue());
            }, (host, menu, buffer) -> {
                menu.getHost().getConfig().setStack(0, GenericStack.readBuffer(buffer));
                menu.currentValue = buffer.readVarLong();
            })
            .build("storage_level_emitter");

    // Only synced once on menu-open, and only used on client
    private long currentValue;

    public StorageLevelEmitterMenu(MenuType<StorageLevelEmitterMenu> menuType, int id, Inventory ip,
            StorageLevelEmitterPart te) {
        super(menuType, id, ip, te);

        registerClientAction(ACTION_SET_REPORTING_VALUE, Long.class, this::setValue);
    }

    public long getCurrentValue() {
        return currentValue;
    }

    public void setValue(long initialValue) {
        if (isClientSide()) {
            if (initialValue != this.currentValue) {
                this.currentValue = initialValue;
                sendClientAction(ACTION_SET_REPORTING_VALUE, initialValue);
            }
        } else {
            getHost().setReportingValue(initialValue);
        }
    }

    @Override
    protected void setupConfig() {
        var inv = getHost().getConfig().createMenuWrapper();
        this.addSlot(new FakeSlot(inv, 0), SlotSemantics.CONFIG);
    }

    @Override
    public void onSlotChange(Slot s) {
        super.onSlotChange(s);
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
        return getHost().getConfigManager().hasSetting(Settings.FUZZY_MODE) && hasUpgrade(AEItems.FUZZY_CARD);
    }

    @Nullable
    public AEKey getConfiguredFilter() {
        return getHost().getConfig().getKey(0);
    }
}
