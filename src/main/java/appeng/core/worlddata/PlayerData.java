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


import appeng.core.AppEng;
import com.google.common.base.Preconditions;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;


/**
 * Handles the matching between UUIDs and internal IDs for security systems.
 * This whole system could be replaced by storing directly the UUID,
 * using a lot more traffic though
 *
 * @author thatsIch
 * @version rv3 - 30.05.2015
 * @since rv3 30.05.2015
 */
final class PlayerData implements IWorldPlayerData, IOnWorldStartable, IOnWorldStoppable {
    private static final String LAST_PLAYER_CATEGORY = "Counters";
    private static final String LAST_PLAYER_KEY = "lastPlayer";
    private static final int LAST_PLAYER_DEFAULT = 0;

    private final Configuration config;
    private final IWorldPlayerMapping playerMapping;

    private int lastPlayerID;

    public PlayerData(@Nonnull final Configuration configFile) {
        Preconditions.checkNotNull(configFile);

        this.config = configFile;

        final ConfigCategory playerList = this.config.getCategory("players");
        this.playerMapping = new PlayerMapping(playerList);
    }

    @Nullable
    @Override
    public EntityPlayer getPlayerFromID(final int playerID) {
        final Optional<UUID> maybe = this.playerMapping.get(playerID);

        if (maybe.isPresent()) {
            final UUID uuid = maybe.get();
            for (final EntityPlayer player : AppEng.proxy.getPlayers()) {
                if (player.getUniqueID().equals(uuid)) {
                    return player;
                }
            }
        }

        return null;
    }

    @Override
    public int getPlayerID(@Nonnull final GameProfile profile) {
        Preconditions.checkNotNull(profile);
        Preconditions.checkNotNull(this.config.getCategory("players"));
        Preconditions.checkState(profile.isComplete());

        final ConfigCategory players = this.config.getCategory("players");
        final String uuid = profile.getId().toString();
        final Property maybePlayerID = players.get(uuid);

        if (maybePlayerID != null && maybePlayerID.isIntValue()) {
            return maybePlayerID.getInt();
        } else {
            final int newPlayerID = this.nextPlayer();
            final Property newPlayer = new Property(uuid, String.valueOf(newPlayerID), Property.Type.INTEGER);
            players.put(uuid, newPlayer);
            this.playerMapping.put(newPlayerID, profile.getId()); // add to reverse map
            this.config.save();

            return newPlayerID;
        }
    }

    private int nextPlayer() {
        final int r = this.lastPlayerID;
        this.lastPlayerID++;
        this.config.get(LAST_PLAYER_CATEGORY, LAST_PLAYER_KEY, this.lastPlayerID).set(this.lastPlayerID);
        return r;
    }

    @Override
    public void onWorldStart() {
        this.lastPlayerID = this.config.get(LAST_PLAYER_CATEGORY, LAST_PLAYER_KEY, LAST_PLAYER_DEFAULT).getInt(LAST_PLAYER_DEFAULT);

        this.config.save();
    }

    @Override
    public void onWorldStop() {
        this.config.save();

        this.lastPlayerID = 0;
    }
}
