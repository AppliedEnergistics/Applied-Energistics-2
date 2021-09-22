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

package appeng.client.gui.me.interfaceterminal;

import javax.annotation.Nonnull;

import appeng.helpers.iface.DualityPatternProvider;
import net.minecraft.network.chat.Component;

import appeng.menu.implementations.InterfaceTerminalMenu;
import appeng.util.inv.AppEngInternalInventory;

/**
 * This class is used on the client-side to represent an interface and it's inventory as it is shown in the
 * {@link InterfaceTerminalScreen}'s table.
 */
public class InterfaceRecord implements Comparable<InterfaceRecord> {

    private final String displayName;
    private final String searchName;

    /**
     * Identifier for this interface on the server-side. See {@link InterfaceTerminalMenu}
     */
    private final long serverId;

    // The client-side representation of the machine's inventory, which is only used for display purposes
    private final AppEngInternalInventory inventory;

    /**
     * Used to sort this record in the interface terminal's table, comes from
     * {@link DualityPatternProvider#getSortValue()}
     */
    private final long order;

    public InterfaceRecord(long serverId, int slots, long order, Component name) {
        this.inventory = new AppEngInternalInventory(slots);
        this.displayName = name.getString();
        this.searchName = this.displayName.toLowerCase();
        this.serverId = serverId;
        this.order = order;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getSearchName() {
        return searchName;
    }

    @Override
    public int compareTo(@Nonnull final InterfaceRecord o) {
        return Long.compare(this.order, o.order);
    }

    public long getServerId() {
        return this.serverId;
    }

    public AppEngInternalInventory getInventory() {
        return inventory;
    }

}
