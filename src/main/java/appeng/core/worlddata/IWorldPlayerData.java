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

import java.util.UUID;

import javax.annotation.Nullable;

import com.mojang.authlib.GameProfile;

/**
 * @author thatsIch
 * @version rv3 - 30.05.2015
 * @since rv3 30.05.2015
 */
public interface IWorldPlayerData {
    /**
     * Gets the UUID of the Minecraft profile associated with the given ME player id.
     *
     * @param playerID An ME player id.
     * @return Null if the ME player id is unknown, otherwise the unique id of the Minecraft profile it originates from.
     */
    @Nullable
    UUID getProfileId(int playerID);

    int getMePlayerId(GameProfile profile);
}
