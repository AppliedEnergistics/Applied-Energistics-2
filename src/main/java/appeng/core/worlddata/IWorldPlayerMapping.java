/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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

package appeng.core.worlddata;


import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.UUID;


/**
 * @author thatsIch
 * @version rv3 - 30.05.2015
 * @since rv3 30.05.2015
 */
public interface IWorldPlayerMapping {
    /**
     * Tries to retrieve the UUID of a player.
     * Might not be stored inside of the map.
     * Should not happen though.
     *
     * @param id ID of the to be searched player
     * @return maybe the UUID of the searched player
     */
    @Nonnull
    Optional<UUID> get(int id);

    /**
     * Put in new players when they join the server
     *
     * @param id   id of new player
     * @param uuid UUID of new player
     */
    void put(int id, @Nonnull UUID uuid);
}
