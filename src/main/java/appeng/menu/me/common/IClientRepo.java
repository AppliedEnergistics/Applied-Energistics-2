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

package appeng.menu.me.common;

import java.util.List;
import java.util.Set;

/**
 * Represents a client-side only repository of {@link GridInventoryEntry} entries that represent the network content
 * currently known to the client. This is actively synchronized by the server via {@link IncrementalUpdateHelper}.
 */
public interface IClientRepo {

    /**
     * Handle incoming updates from the server.
     *
     * @param fullUpdate Completely replace the repo contents.
     * @param entries    The updated entries.
     */
    void handleUpdate(boolean fullUpdate, List<GridInventoryEntry> entries);

    /**
     * @return All entries in this repository, regardless of any filter.
     */
    Set<GridInventoryEntry> getAllEntries();

}
