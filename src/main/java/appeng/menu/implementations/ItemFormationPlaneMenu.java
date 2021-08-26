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
import net.minecraftforge.items.IItemHandler;

import appeng.api.config.SecurityPermissions;
import appeng.api.config.Settings;
import appeng.api.config.Upgrades;
import appeng.api.config.YesNo;
import appeng.api.implementations.blockentities.ISegmentedInventory;
import appeng.api.util.IConfigManager;
import appeng.client.gui.implementations.ItemFormationPlaneScreen;
import appeng.menu.SlotSemantic;
import appeng.menu.guisync.GuiSync;
import appeng.menu.slot.FakeTypeOnlySlot;
import appeng.menu.slot.OptionalTypeOnlyFakeSlot;
import appeng.parts.automation.FormationPlanePart;

/**
 * This is used by both annihilation and formation planes.
 *
 * @see ItemFormationPlaneScreen
 */
public class ItemFormationPlaneMenu extends UpgradeableMenu<FormationPlanePart> {

    public static final MenuType<ItemFormationPlaneMenu> TYPE = MenuTypeBuilder
            .create(ItemFormationPlaneMenu::new, FormationPlanePart.class)
            .requirePermission(SecurityPermissions.BUILD)
            .build("formationplane");

    @GuiSync(7)
    public YesNo placeMode;

    public ItemFormationPlaneMenu(int id, final Inventory ip, final FormationPlanePart host) {
        super(TYPE, id, ip, host);
    }

    @Override
    protected void setupConfig() {
        final IItemHandler config = this.getHost().getSubInventory(ISegmentedInventory.CONFIG);
        for (int y = 0; y < 7; y++) {
            for (int x = 0; x < 9; x++) {
                int invIdx = y * 9 + x;
                if (y < 2) {
                    this.addSlot(new FakeTypeOnlySlot(config, invIdx), SlotSemantic.CONFIG);
                } else {
                    this.addSlot(new OptionalTypeOnlyFakeSlot(config, this, invIdx, y - 2), SlotSemantic.CONFIG);
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
        this.setFuzzyMode(cm.getSetting(Settings.FUZZY_MODE));
        this.setPlaceMode(cm.getSetting(Settings.PLACE_BLOCK));
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
}
