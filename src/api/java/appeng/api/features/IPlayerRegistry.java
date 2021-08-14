/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013 - 2015 AlgorithmX2
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package appeng.api.features;

import java.util.UUID;
import java.util.function.Function;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.mojang.authlib.GameProfile;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

/**
 * Maintains a save specific list of userids and username combinations this greatly simplifies storage internally and
 * gives a common place to look up and get IDs for the security framework.
 */
public interface IPlayerRegistry {
    class Holder {
        static Function<MinecraftServer, IPlayerRegistry> lookup;
    }

    /**
     * Gets the mapping between {@link GameProfile} ids and AE2's player ids for the given server.
     */
    static IPlayerRegistry getMapping(MinecraftServer server) {
        Preconditions.checkState(Holder.lookup != null, "AE2 is not initialized yet.");
        return Holder.lookup.apply(server);
    }

    /**
     * Convenience method to get the player registry that's associated with the server that hosts the given level. Null
     * will be returned for client-side levels.
     */
    @Nullable
    static IPlayerRegistry getMapping(Level level) {
        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            return getMapping(serverLevel.getServer());
        }
        return null;
    }

    /**
     * Convenience method to get the ME player id associated with a connected player.
     */
    static int getPlayerId(ServerPlayer player) {
        return getMapping(player.getServer()).getPlayerId(player.getGameProfile());
    }

    /**
     * Convenience method to get a connected {@link ServerPlayer} for a given ME player id. This can return null for two
     * reasons: the ME player id is unknown, or the player associated with the given ID is not logged onto the server.
     */
    @Nullable
    static ServerPlayer getConnected(MinecraftServer server, int playerId) {
        var uuid = getMapping(server).getProfileId(playerId);
        if (uuid == null) {
            // No such player
            return null;
        }

        return server.getPlayerList().getPlayer(uuid);
    }

    /**
     * Queries AE2's internal player ID for the given {@link GameProfile}. If AE2 has not assigned an ID to that player
     * yet, it will be automatically assigned.
     * <p/>
     *
     * @return -1 if the given profile has no id set. Usually, the game should create a stable UUID even for offline
     *         players by hashing their name.
     */
    default int getPlayerId(GameProfile gameProfile) {
        var profileId = gameProfile.getId();
        if (profileId == null) {
            return -1;
        }

        return getPlayerId(profileId);
    }

    /**
     * Queries AE2's internal player ID for the given {@link GameProfile#getId() profile UUID}. If AE2 has not assigned
     * an ID to that player yet, it will be automatically assigned.
     */
    int getPlayerId(UUID profileId);

    /**
     * Find the stored {@link GameProfile#getId() profile UUID} that is stored for the given ME player id, if any.
     *
     * @return Null if no such player is known.
     */
    @Nullable
    UUID getProfileId(int playerId);

}
