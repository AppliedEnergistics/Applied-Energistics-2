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

import java.util.Map;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.authlib.GameProfile;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;

import appeng.core.AELog;
import appeng.core.AppEng;

/**
 * Handles the matching between UUIDs and internal IDs for security systems. This whole system could be replaced by
 * storing directly the UUID, using a lot more traffic though
 *
 * @author thatsIch
 * @version rv3 - 30.05.2015
 * @since rv3 30.05.2015
 */
final class PlayerData extends SavedData implements IWorldPlayerData {

    public static final String NAME = AppEng.MOD_ID + "_players";
    public static final String TAG_PLAYER_IDS = "playerIds";
    public static final String TAG_PROFILE_IDS = "profileIds";

    private final BiMap<UUID, Integer> mapping = HashBiMap.create();

    // Caches the highest assigned player id + 1
    private int nextPlayerId = 0;

    @Nullable
    @Override
    public UUID getProfileId(final int playerId) {
        return this.mapping.inverse().get(playerId);
    }

    @Override
    public int getMePlayerId(@Nonnull final GameProfile profile) {
        Preconditions.checkNotNull(profile);

        final UUID uuid = profile.getId();
        Integer playerId = mapping.get(uuid);

        if (playerId == null) {
            playerId = this.nextPlayerId++;
            this.mapping.put(profile.getId(), playerId);
            setDirty();

            AELog.info("Assigning ME player id %s to Minecraft profile %s (%s)", playerId, profile.getId(),
                    profile.getName());
        }

        return playerId;
    }

    public static PlayerData load(CompoundTag nbt) {
        int[] playerIds = nbt.getIntArray(TAG_PLAYER_IDS);
        long[] profileIds = nbt.getLongArray(TAG_PROFILE_IDS);

        if (playerIds.length * 2 != profileIds.length) {
            throw new IllegalStateException("Plaer ID mapping is corrupted. " + playerIds.length + " player IDs vs. "
                    + profileIds.length + " profile IDs (latter must be 2 * the former)");
        }

        var result = new PlayerData();
        int highestPlayerId = -1;
        for (int i = 0; i < playerIds.length; i++) {
            int playerId = playerIds[i];
            UUID profileId = new UUID(profileIds[i * 2], profileIds[i * 2 + 1]);
            highestPlayerId = Math.max(playerId, highestPlayerId);
            result.mapping.put(profileId, playerId);
            AELog.debug("AE player ID %s is assigned to profile ID %s", playerId, profileId);
        }
        result.nextPlayerId = highestPlayerId + 1;
        return result;
    }

    @Override
    public CompoundTag save(CompoundTag compound) {
        int index = 0;
        int[] playerIds = new int[mapping.size()];
        long[] profileIds = new long[mapping.size() * 2];
        for (Map.Entry<UUID, Integer> entry : mapping.entrySet()) {
            profileIds[index * 2] = entry.getKey().getMostSignificantBits();
            profileIds[index * 2 + 1] = entry.getKey().getLeastSignificantBits();
            playerIds[index++] = entry.getValue();
        }

        compound.putIntArray(TAG_PLAYER_IDS, playerIds);
        compound.putLongArray(TAG_PROFILE_IDS, profileIds);

        return compound;
    }

}
