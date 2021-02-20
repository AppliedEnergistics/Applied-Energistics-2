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

package appeng.tile.spatial;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;

import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.world.ForgeChunkManager;

import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.events.statistics.MENetworkChunkEvent.MENetworkChunkAdded;
import appeng.api.networking.events.statistics.MENetworkChunkEvent.MENetworkChunkRemoved;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import appeng.me.GridAccessException;
import appeng.services.ChunkLoadingService;
import appeng.tile.grid.AENetworkTileEntity;

public class SpatialAnchorTileEntity extends AENetworkTileEntity implements IGridTickable {

    private final Set<ChunkPos> chunks = new HashSet<>();
    private int powerlessTicks = 0;
    private boolean initialized = false;

    public SpatialAnchorTileEntity(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
        this.getProxy().setFlags(GridFlags.REQUIRE_CHANNEL);
    }

    @Override
    public AECableType getCableConnectionType(final AEPartLocation dir) {
        return AECableType.SMART;
    }

    @Override
    public DimensionalCoord getLocation() {
        return new DimensionalCoord(this);
    }

    @MENetworkEventSubscribe
    public void chunkAdded(final MENetworkChunkAdded changed) {
        if (changed.getWorld() == this.getServerWorld()) {
            this.force(changed.getChunkPos());
        }
    }

    @MENetworkEventSubscribe
    public void chunkRemoved(final MENetworkChunkRemoved changed) {
        if (changed.getWorld() == this.getServerWorld()) {
            this.release(changed.getChunkPos(), true);
        }
    }

    @MENetworkEventSubscribe
    public void powerChange(final MENetworkPowerStatusChange powerChange) {
        this.wakeUp();
    }

    @MENetworkEventSubscribe
    public void powerChange(final MENetworkChannelsChanged powerChange) {
        this.wakeUp();
    }

    private void wakeUp() {
        // Wake the anchor to allow for unloading chunks some time after power loss
        try {
            this.getProxy().getTick().alertDevice(this.getProxy().getNode());
        } catch (GridAccessException e) {
            // Can be ignored
        }
    }

    @Override
    @Nonnull
    public TickingRequest getTickingRequest(@Nonnull IGridNode node) {
        return new TickingRequest(20, 20, false, true);
    }

    @Override
    @Nonnull
    public TickRateModulation tickingRequest(@Nonnull IGridNode node, int ticksSinceLastCall) {
        // Initialize once the network is ready and there are no entries marked as loaded.
        if (!this.initialized && this.getProxy().isActive() && this.getProxy().isPowered()) {
            this.forceAll();
            this.initialized = true;
        }

        // Be a bit lenient to not unload all chunks immediately upon power loss
        if (this.powerlessTicks > 200) {
            if (!this.getProxy().isPowered() || !this.getProxy().isActive()) {
                this.releaseAll();
            }
            this.powerlessTicks = 0;

            // Put anchor to sleep until another power change.
            return TickRateModulation.SLEEP;
        }

        // Count ticks without power
        if (!this.getProxy().isPowered() || !this.getProxy().isActive()) {
            this.powerlessTicks += ticksSinceLastCall;
            return TickRateModulation.SAME;
        }

        // Default to sleep
        return TickRateModulation.SLEEP;
    }

    public Set<ChunkPos> getLoadedChunks() {
        return this.chunks;
    }

    public int countLoadedChunks() {
        return this.chunks.size();
    }

    /**
     * Used to restore loaded chunks from {@link ForgeChunkManager}
     * 
     * @param world
     * @param chunkPos
     */
    public void registerChunk(ChunkPos chunkPos) {
        this.chunks.add(chunkPos);
        this.updatePowerConsumption();
    }

    /**
     * Releases all chunks when destroyed.
     */
    public void destroy() {
        this.releaseAll();
    }

    private void updatePowerConsumption() {
        try {
            final int worlds = this.getProxy().getStatistics().worlds().size();
            final int powerRequired = (int) Math.pow(this.chunks.size(), 2 + worlds * .1);

            this.getProxy().setIdlePowerUsage(powerRequired);
        } catch (GridAccessException e) {
        }
    }

    /**
     * Adds the chunk to the current loaded list.
     * 
     * @param chunkPos
     * @return
     */
    private boolean force(ChunkPos chunkPos) {
        // Avoid loading chunks after the anchor is destroyed
        if (this.isRemoved()) {
            return false;
        }

        ServerWorld world = this.getServerWorld();
        boolean forced = ChunkLoadingService.getInstance().forceChunk(world, this.getPos(), chunkPos, true);

        if (forced) {
            this.chunks.add(chunkPos);
        }

        this.updatePowerConsumption();

        return forced;
    }

    /**
     * @param chunkPos
     * @return
     */
    private boolean release(ChunkPos chunkPos, boolean remove) {
        ServerWorld world = this.getServerWorld();
        boolean removed = ChunkLoadingService.getInstance().releaseChunk(world, this.getPos(), chunkPos, true);

        if (removed && remove) {
            this.chunks.remove(chunkPos);
        }

        this.updatePowerConsumption();

        return removed;
    }

    private void forceAll() {
        try {
            for (ChunkPos chunkPos : this.getProxy().getStatistics().getChunks().get(this.getServerWorld())
                    .elementSet()) {
                this.force(chunkPos);
            }
        } catch (GridAccessException e) {
        }
    }

    private void releaseAll() {
        for (ChunkPos chunk : this.chunks) {
            this.release(chunk, false);
        }
        this.chunks.clear();
    }

    private ServerWorld getServerWorld() {
        if (this.getWorld() instanceof ServerWorld) {
            return (ServerWorld) this.getWorld();
        }
        throw new IllegalStateException("Cannot be called on a client");
    }
}
