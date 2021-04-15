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

package appeng.container.implementations;

import appeng.api.config.FuzzyMode;
import appeng.api.config.SecurityPermissions;
import appeng.api.config.Settings;
import appeng.api.config.Upgrades;
import appeng.api.config.YesNo;
import appeng.container.SlotSemantic;
import appeng.container.guisync.GuiSync;
import appeng.container.slot.FakeTypeOnlySlot;
import appeng.container.slot.OptionalTypeOnlyFakeSlot;
import appeng.parts.automation.FormationPlanePart;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.items.IItemHandler;

/**
 * This is used by both annihilation and formation planes.
 *
 * @see appeng.client.gui.implementations.FormationPlaneScreen
 */
public class FormationPlaneContainer extends UpgradeableContainer {

    public static final ContainerType<FormationPlaneContainer> TYPE = ContainerTypeBuilder
            .create(FormationPlaneContainer::new, FormationPlanePart.class)
            .requirePermission(SecurityPermissions.BUILD)
            .build("formationplane");

    @GuiSync(7)
    public YesNo placeMode;

    public FormationPlaneContainer(int id, final PlayerInventory ip, final FormationPlanePart te) {
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
    public void detectAndSendChanges() {
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
