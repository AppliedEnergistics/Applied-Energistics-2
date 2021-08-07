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

package appeng.core.registries;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.MinecraftForge;

import appeng.api.events.LocatableEventAnnounce;
import appeng.api.events.LocatableEventAnnounce.LocatableEvent;
import appeng.api.features.ILocatable;
import appeng.api.features.ILocatableRegistry;
import appeng.core.AELog;
import appeng.util.Platform;

public final class LocatableRegistry implements ILocatableRegistry {
    private final Map<Long, ILocatable> set;

    public LocatableRegistry() {
        this.set = new HashMap<>();
        MinecraftForge.EVENT_BUS.addListener(this::updateLocatable);
    }

    private void updateLocatable(final LocatableEventAnnounce e) {
        // TODO 1.17: Break compat and add isRemote() or getWorld() to ILocatable
        if (e.target instanceof BlockEntity blockEntity) {
            if (blockEntity.getLevel() == null || blockEntity.getLevel().isClientSide()) {
                return;
            }
        } else if (Platform.isClient()) {
            return; // IGNORE!
        }

        if (e.change == LocatableEvent.REGISTER) {
            AELog.debug("Registering locatable %s: %s", e.target.getLocatableSerial(), e.target);
            this.set.put(e.target.getLocatableSerial(), e.target);
        } else if (e.change == LocatableEvent.UNREGISTER) {
            AELog.debug("Unregistering locatable %s: %s", e.target.getLocatableSerial(), e.target);
            this.set.remove(e.target.getLocatableSerial());
        }
    }

    @Override
    public ILocatable getLocatableBy(final long serial) {
        return this.set.get(serial);
    }
}
