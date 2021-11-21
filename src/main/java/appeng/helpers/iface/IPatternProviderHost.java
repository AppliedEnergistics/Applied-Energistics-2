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

package appeng.helpers.iface;

import java.util.EnumSet;

import javax.annotation.Nonnull;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.helpers.IPriorityHost;

public interface IPatternProviderHost extends IConfigurableObject, IPriorityHost {
    DualityPatternProvider getDuality();

    /**
     * @return The block entity that is in-world and hosts the interface.
     */
    BlockEntity getBlockEntity();

    EnumSet<Direction> getTargets();

    void saveChanges();

    @Override
    default int getPriority() {
        return getDuality().getPriority();
    }

    @Override
    default void setPriority(int newValue) {
        getDuality().setPriority(newValue);
    }

    @Nonnull
    @Override
    default IConfigManager getConfigManager() {
        return getDuality().getConfigManager();
    }
}
