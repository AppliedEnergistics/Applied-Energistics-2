/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
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

package appeng.parts.automation;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import appeng.api.config.RedstoneMode;
import appeng.api.config.Settings;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.networking.energy.IEnergyWatcher;
import appeng.api.networking.energy.IEnergyWatcherNode;
import appeng.api.parts.IPartItem;
import appeng.api.util.IConfigManagerBuilder;
import appeng.menu.MenuOpener;
import appeng.menu.implementations.EnergyLevelEmitterMenu;
import appeng.menu.locator.MenuLocators;

public class EnergyLevelEmitterPart extends AbstractLevelEmitterPart {
    private IEnergyWatcher energyWatcher;

    private final IEnergyWatcherNode energyWatcherNode = new IEnergyWatcherNode() {
        @Override
        public void updateWatcher(IEnergyWatcher newWatcher) {
            energyWatcher = newWatcher;
            configureWatchers();
        }

        @Override
        public void onThresholdPass(IEnergyService energyGrid) {
            lastReportedValue = (long) energyGrid.getStoredPower();
            updateState();
        }
    };

    public EnergyLevelEmitterPart(IPartItem<?> partItem) {
        super(partItem);

        getMainNode().addService(IEnergyWatcherNode.class, energyWatcherNode);
    }

    @Override
    protected void registerSettings(IConfigManagerBuilder builder) {
        super.registerSettings(builder);
        builder.registerSetting(Settings.REDSTONE_EMITTER, RedstoneMode.HIGH_SIGNAL);
    }

    @Override
    protected int getUpgradeSlots() {
        return 0;
    }

    @Override
    protected void configureWatchers() {
        if (this.energyWatcher != null) {
            this.energyWatcher.reset();
        }

        if (this.energyWatcher != null) {
            this.energyWatcher.add(getReportingValue());
        }

        getMainNode().ifPresent(grid -> {
            // update to power...
            this.lastReportedValue = (long) grid.getEnergyService().getStoredPower();
            this.updateState();
        });
    }

    @Override
    protected boolean hasDirectOutput() {
        return false;
    }

    @Override
    protected boolean getDirectOutput() {
        throw new UnsupportedOperationException("hasDirectOutput is false...");
    }

    @Override
    public boolean onUseWithoutItem(Player player, Vec3 pos) {
        if (!isClientSide()) {
            MenuOpener.open(EnergyLevelEmitterMenu.TYPE, player, MenuLocators.forPart(this));
        }
        return true;
    }
}
