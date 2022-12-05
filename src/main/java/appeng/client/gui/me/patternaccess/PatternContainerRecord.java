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

package appeng.client.gui.me.patternaccess;

import appeng.api.implementations.blockentities.PatternContainerGroup;
import appeng.helpers.iface.PatternProviderLogic;
import appeng.menu.implementations.PatternAccessTermMenu;
import appeng.util.inv.AppEngInternalInventory;

/**
 * This class is used on the client-side to represent a pattern provider and it's inventory as it is shown in the
 * {@link PatternAccessTermScreen}'s table.
 */
public class PatternContainerRecord implements Comparable<PatternContainerRecord> {

    private final PatternContainerGroup group;
    private final String searchName;

    /**
     * Identifier for this pattern provider on the server-side. See {@link PatternAccessTermMenu}
     */
    private final long serverId;

    // The client-side representation of the machine's inventory, which is only used for display purposes
    private final AppEngInternalInventory inventory;

    /**
     * Used to sort this record in the pattern access terminal's table, comes from
     * {@link PatternProviderLogic#getSortValue()}
     */
    private final long order;

    public PatternContainerRecord(long serverId, int slots, long order, PatternContainerGroup group) {
        this.inventory = new AppEngInternalInventory(slots);
        this.group = group;
        this.searchName = group.name().getString().toLowerCase();
        this.serverId = serverId;
        this.order = order;
    }

    public PatternContainerGroup getGroup() {
        return group;
    }

    public String getSearchName() {
        return searchName;
    }

    @Override
    public int compareTo(PatternContainerRecord o) {
        return Long.compare(this.order, o.order);
    }

    public long getServerId() {
        return this.serverId;
    }

    public AppEngInternalInventory getInventory() {
        return inventory;
    }

}
