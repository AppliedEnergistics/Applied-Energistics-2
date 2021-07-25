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

package appeng.fluids.helper;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import appeng.api.storage.data.IAEFluidStack;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.FluidSlotPacket;
import appeng.fluids.util.AEFluidInventory;
import appeng.fluids.util.IAEFluidTank;

public class FluidSyncHelper {
    private final IAEFluidTank inv;
    private final IAEFluidTank cache;
    private final int idOffset;

    public FluidSyncHelper(final IAEFluidTank inv, final int idOffset) {
        this.inv = inv;
        this.cache = new AEFluidInventory(null, inv.getSlots());
        this.idOffset = idOffset;
    }

    public void sendFull(Player player) {
        this.sendDiffMap(this.createDiffMap(true), player);
    }

    public void sendDiff(Player player) {
        this.sendDiffMap(this.createDiffMap(false), player);
    }

    public void readPacket(final Map<Integer, IAEFluidStack> data) {
        for (int i = 0; i < this.inv.getSlots(); ++i) {
            if (data.containsKey(i + this.idOffset)) {
                this.inv.setFluidInSlot(i, data.get(i + this.idOffset));
            }
        }
    }

    private void sendDiffMap(final Map<Integer, IAEFluidStack> data, Player player) {
        if (data.isEmpty()) {
            return;
        }

        if (player instanceof ServerPlayer serverPlayer) {
            NetworkHandler.instance().sendTo(new FluidSlotPacket(data), serverPlayer);
        }
    }

    private Map<Integer, IAEFluidStack> createDiffMap(final boolean full) {
        final Map<Integer, IAEFluidStack> ret = new HashMap<>();
        for (int i = 0; i < this.inv.getSlots(); ++i) {
            if (full || !this.equalsSlot(i)) {
                ret.put(i + this.idOffset, this.inv.getFluidInSlot(i));
            }
            if (!full) {
                this.cache.setFluidInSlot(i, this.inv.getFluidInSlot(i));
            }
        }
        return ret;
    }

    private boolean equalsSlot(int slot) {
        final IAEFluidStack stackA = this.inv.getFluidInSlot(slot);
        final IAEFluidStack stackB = this.cache.getFluidInSlot(slot);

        if (!Objects.equals(stackA, stackB)) {
            return false;
        }

        return stackA == null || stackA.getStackSize() == stackB.getStackSize();
    }
}
