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

import appeng.api.config.FuzzyMode;
import appeng.api.config.SecurityPermissions;
import appeng.api.config.Settings;
import appeng.api.config.Upgrades;
import appeng.api.config.YesNo;
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
public class ItemFormationPlaneContainer extends UpgradeableContainer {

    public static final MenuType<ItemFormationPlaneContainer> TYPE = ContainerTypeBuilder
            .create(ItemFormationPlaneContainer::new, FormationPlanePart.class)
            .requirePermission(SecurityPermissions.BUILD)
            .build("formationplane");

    @GuiSync(7)
    public YesNo placeMode;

    public ItemFormationPlaneContainer(int id, final Inventory ip, final FormationPlanePart te) {
        super(TYPE, id, ip, te);
    }

    @Override
    protected void setupConfig() {
        final IItemHandler config = this.getUpgradeable().getInventoryByName("config");
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
    public int availableUpgrades() {
        return 5;
    }

    @Override
    public void broadcastChanges() {
        this.verifyPermissions(SecurityPermissions.BUILD, false);

        if (isServer()) {
            this.setFuzzyMode((FuzzyMode) this.getUpgradeable().getConfigManager().getSetting(Settings.FUZZY_MODE));
            this.setPlaceMode((YesNo) this.getUpgradeable().getConfigManager().getSetting(Settings.PLACE_BLOCK));
        }

        this.standardDetectAndSendChanges();
    }

    @Override
    public boolean isSlotEnabled(final int idx) {
        final int upgrades = this.getUpgradeable().getInstalledUpgrades(Upgrades.CAPACITY);

        return upgrades > idx;
    }

    public YesNo getPlaceMode() {
        return this.placeMode;
    }

    private void setPlaceMode(final YesNo placeMode) {
        this.placeMode = placeMode;
    }
}
