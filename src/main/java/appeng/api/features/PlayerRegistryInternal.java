/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2025, TeamAppliedEnergistics, All rights reserved.
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

package appeng.api.features;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.UUIDUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import appeng.core.AELog;
import appeng.core.AppEng;

/**
 * Handles the matching between UUIDs and internal IDs for security systems. This whole system could be replaced by
 * storing directly the UUID, using a lot more traffic though
 */
final class PlayerRegistryInternal extends SavedData implements IPlayerRegistry {

    private static final String NAME = AppEng.MOD_ID + "_players";

    private record PlayerRegistryData(List<Integer> playerIds, List<UUID> profileIds) {
    }

    private static final Codec<PlayerRegistryData> PLAYER_REGISTRY_DATA_CODEC = RecordCodecBuilder.create(
            builder -> builder.group(
                    Codec.INT.listOf().fieldOf("player_ids").forGetter(PlayerRegistryData::playerIds),
                    UUIDUtil.CODEC.listOf().fieldOf("profile_ids").forGetter(PlayerRegistryData::profileIds))
                    .apply(builder, PlayerRegistryData::new));

    private static final SavedDataType<PlayerRegistryInternal> TYPE = new SavedDataType<>(
            NAME,
            context -> new PlayerRegistryInternal(context.levelOrThrow().getServer()),
            context -> RecordCodecBuilder.create(builder -> builder.group(
                    PLAYER_REGISTRY_DATA_CODEC.fieldOf("players").forGetter(PlayerRegistryInternal::getData))
                    .apply(builder, data -> new PlayerRegistryInternal(context.levelOrThrow().getServer(), data))));

    private final BiMap<UUID, Integer> mapping = HashBiMap.create();

    private final MinecraftServer server;

    // Caches the highest assigned player id + 1
    private int nextPlayerId = 0;

    private PlayerRegistryInternal(MinecraftServer server) {
        this.server = server;
    }

    private PlayerRegistryInternal(MinecraftServer server, PlayerRegistryData data) {
        this.server = server;
        load(data);
    }

    private PlayerRegistryData getData() {
        List<Integer> playerIds = new ArrayList<>(mapping.size());
        List<UUID> profileIds = new ArrayList<>(mapping.size());
        for (var entry : mapping.entrySet()) {
            playerIds.add(entry.getValue());
            profileIds.add(entry.getKey());
        }

        return new PlayerRegistryData(playerIds, profileIds);
    }

    static PlayerRegistryInternal get(MinecraftServer server) {
        var overworld = server.getLevel(ServerLevel.OVERWORLD);
        if (overworld == null) {
            throw new IllegalStateException("Cannot retrieve player data for a server that has no overworld.");
        }
        return overworld.getDataStorage().computeIfAbsent(TYPE);
    }

    @Nullable
    @Override
    public UUID getProfileId(int playerId) {
        return this.mapping.inverse().get(playerId);
    }

    @Override
    public int getPlayerId(UUID profileId) {
        Objects.requireNonNull(profileId, "profileId");

        Integer playerId = mapping.get(profileId);

        if (playerId == null) {
            playerId = this.nextPlayerId++;
            this.mapping.put(profileId, playerId);
            setDirty();

            var player = server.getPlayerList().getPlayer(profileId);
            var name = player != null ? player.getGameProfile().name() : "[UNKNOWN]";
            AELog.info("Assigning ME player id %s to Minecraft profile %s (%s)", playerId, profileId, name);
        }

        return playerId;
    }

    private void load(PlayerRegistryData data) {
        var playerIds = data.playerIds();
        var profileIds = data.profileIds();

        if (playerIds.size() != profileIds.size()) {
            throw new IllegalStateException("Player ID mapping is corrupted. " + playerIds.size() + " player IDs vs. "
                    + profileIds.size() + " profile IDs");
        }

        int highestPlayerId = -1;
        for (int i = 0; i < playerIds.size(); i++) {
            int playerId = playerIds.get(i);
            UUID profileId = profileIds.get(i);
            highestPlayerId = Math.max(playerId, highestPlayerId);
            mapping.put(profileId, playerId);
            AELog.debug("AE player ID %s is assigned to profile ID %s", playerId, profileId);
        }
        nextPlayerId = highestPlayerId + 1;
    }

}
