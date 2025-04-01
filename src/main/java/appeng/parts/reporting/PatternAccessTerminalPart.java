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

package appeng.parts.reporting;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import appeng.api.config.Settings;
import appeng.api.config.ShowPatternProviders;
import appeng.api.parts.IPartItem;
import appeng.api.storage.ILinkStatus;
import appeng.api.storage.IPatternAccessTermMenuHost;
import appeng.api.util.IConfigManager;
import appeng.menu.MenuOpener;
import appeng.menu.implementations.PatternAccessTermMenu;
import appeng.menu.locator.MenuLocators;

public class PatternAccessTerminalPart extends AbstractDisplayPart implements IPatternAccessTermMenuHost {

    private final IConfigManager configManager = IConfigManager.builder(() -> {
        this.getHost().markForSave();
    })
            .registerSetting(Settings.TERMINAL_SHOW_PATTERN_PROVIDERS, ShowPatternProviders.VISIBLE)
            .build();

    public PatternAccessTerminalPart(IPartItem<?> partItem) {
        super(partItem, true);
    }

    @Override
    public boolean onUseWithoutItem(Player player, Vec3 pos) {
        if (!super.onUseWithoutItem(player, pos) && !isClientSide()) {
            MenuOpener.open(PatternAccessTermMenu.TYPE, player, MenuLocators.forPart(this));
        }
        return true;
    }

    @Override
    public IConfigManager getConfigManager() {
        return configManager;
    }

    public void writeToNBT(CompoundTag tag, HolderLookup.Provider registries) {
        super.writeToNBT(tag, registries);
        configManager.writeToNBT(tag, registries);
    }

    public void readFromNBT(CompoundTag tag, HolderLookup.Provider registries) {
        super.readFromNBT(tag, registries);
        configManager.readFromNBT(tag, registries);
    }

    @Override
    public ILinkStatus getLinkStatus() {
        return ILinkStatus.ofManagedNode(getMainNode());
    }
}
