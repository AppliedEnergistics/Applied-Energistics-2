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

package appeng.hooks;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.network.PacketDistributor;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import appeng.core.network.serverbound.RequestClosestMeteoritePacket;
import appeng.server.services.compass.ServerCompassService;

/**
 * The server-side version of this is called {@link ServerCompassService}.
 */
public final class CompassManager {
    public static final CompassManager INSTANCE = new CompassManager();
    private static final int REFRESH_CACHE_AFTER = 30000;
    private static final int EXPIRE_CACHE_AFTER = 60000;
    private final Long2ObjectOpenHashMap<CachedResult> requests = new Long2ObjectOpenHashMap<>();

    private CompassManager() {
    }

    public void postResult(ChunkPos requestedPos, @Nullable BlockPos closestMeteorite) {
        this.requests.put(requestedPos.toLong(), new CachedResult(closestMeteorite, System.currentTimeMillis()));
    }

    @Nullable
    public BlockPos getClosestMeteorite(BlockPos pos, boolean prefetch) {
        return getClosestMeteorite(new ChunkPos(pos), prefetch);
    }

    @Nullable
    public BlockPos getClosestMeteorite(ChunkPos chunkPos, boolean prefetch) {
        var now = System.currentTimeMillis();

        // Expire cached results
        var it = this.requests.values().iterator();
        while (it.hasNext()) {
            var res = it.next();
            var age = now - res.received();
            if (age > EXPIRE_CACHE_AFTER) {
                it.remove();
            }
        }

        BlockPos result = null;
        boolean request;

        var cached = this.requests.get(chunkPos.toLong());
        if (cached != null) {
            result = cached.closestMeteoritePos();
            var age = now - cached.received();
            request = age > REFRESH_CACHE_AFTER;
        } else {
            request = true;
        }

        // Find the closest existing result
        if (result == null) {
            result = findClosestKnownResult(chunkPos);
        }

        if (request) {
            this.requests.put(chunkPos.toLong(), new CachedResult(result, now));
            PacketDistributor.sendToServer(new RequestClosestMeteoritePacket(chunkPos));
        }

        // Prefetch meteor positions from the server for adjacent blocks, so they are
        // available more quickly when we're moving
        if (prefetch) {
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (i != 0 || j != 0) {
                        getClosestMeteorite(new ChunkPos(chunkPos.x + i, chunkPos.z + j), false);
                    }
                }
            }
        }

        return result;
    }

    @Nullable
    private BlockPos findClosestKnownResult(ChunkPos chunkPos) {
        // If there was no cached result, reuse the closest existing result
        var closestDistance = Long.MAX_VALUE;
        BlockPos result = null;
        for (var entry : this.requests.long2ObjectEntrySet()) {
            var closestPos = entry.getValue().closestMeteoritePos();
            if (closestPos != null) {
                var distance = chunkPos.distanceSquared(entry.getLongKey());
                if (distance < closestDistance) {
                    closestDistance = distance;
                    result = closestPos;
                }
            }
        }
        return result;
    }

    private record CachedResult(@Nullable BlockPos closestMeteoritePos, long received) {
    }
}
