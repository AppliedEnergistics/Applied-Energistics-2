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

package appeng.container.me.networktool;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

/**
 * Represents the status of machines grouped by their {@link IGridBlock#getMachineRepresentation() item representation}.
 */
public class MachineGroup {
    /**
     * The item stack used for grouping machines together, which is also used for showing the group in the UI.
     *
     * @see IGridBlock#getMachineRepresentation()
     */
    private final ItemStack display;

    /**
     * Summed up idle power usage of this machine group in AE/t.
     */
    private double idlePowerUsage;

    /**
     * The number of machines in this group.
     */
    private int count;

    MachineGroup(ItemStack display) {
        this.display = display;
    }

    /**
     * Reads back a machine group previously {@link #write(PacketBuffer) written}.
     */
    static MachineGroup read(PacketBuffer data) {
        ItemStack stack = data.readItemStack();
        MachineGroup entry = new MachineGroup(stack);
        entry.idlePowerUsage = data.readDouble();
        entry.count = data.readVarInt();
        return entry;
    }

    void write(PacketBuffer data) {
        data.writeItemStack(display, true);
        data.writeDouble(idlePowerUsage);
        data.writeVarInt(count);
    }

    public ItemStack getDisplay() {
        return display;
    }

    public double getIdlePowerUsage() {
        return idlePowerUsage;
    }

    void setIdlePowerUsage(double idlePowerUsage) {
        this.idlePowerUsage = idlePowerUsage;
    }

    public int getCount() {
        return count;
    }

    void setCount(int count) {
        this.count = count;
    }
}
