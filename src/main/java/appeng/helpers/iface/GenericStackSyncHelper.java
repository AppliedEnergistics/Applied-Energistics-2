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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import appeng.api.storage.data.IAEStack;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.GenericSlotPacket;

public class GenericStackSyncHelper {
    private final GenericStackInv inv;
    private final GenericStackInv cache;
    private final int idOffset;

    public GenericStackSyncHelper(GenericStackInv inv, int idOffset) {
        this.inv = inv;
        this.cache = new GenericStackInv(null, inv.size());
        this.idOffset = idOffset;
    }

    public void sendFull(Player player) {
        this.sendDiffMap(this.createDiffMap(true), player);
    }

    public void sendDiff(Player player) {
        this.sendDiffMap(this.createDiffMap(false), player);
    }

    public void readPacket(final Map<Integer, IAEStack> data) {
        for (int i = 0; i < this.inv.size(); ++i) {
            if (data.containsKey(i + this.idOffset)) {
                this.inv.setStack(i, data.get(i + this.idOffset));
            }
        }
    }

    private void sendDiffMap(final Map<Integer, IAEStack> data, Player player) {
        if (data.isEmpty()) {
            return;
        }

        if (player instanceof ServerPlayer serverPlayer) {
            NetworkHandler.instance().sendTo(new GenericSlotPacket(data), serverPlayer);
        }
    }

    private Map<Integer, IAEStack> createDiffMap(final boolean full) {
        final Map<Integer, IAEStack> ret = new HashMap<>();
        for (int i = 0; i < this.inv.size(); ++i) {
            if (full || !this.equalsSlot(i)) {
                ret.put(i + this.idOffset, this.inv.getStack(i));
            }
            if (!full) {
                this.cache.setStack(i, this.inv.getStack(i));
            }
        }
        return ret;
    }

    private boolean equalsSlot(int slot) {
        var stackA = this.inv.getStack(slot);
        var stackB = this.cache.getStack(slot);

        if (!Objects.equals(stackA, stackB)) {
            return false;
        }

        return stackA == null || stackA.getStackSize() == stackB.getStackSize();
    }
}
