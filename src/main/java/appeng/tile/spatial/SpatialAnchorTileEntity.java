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

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import com.google.common.collect.Multiset;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.world.ForgeChunkManager;

import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.movable.IMovableTile;
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
import appeng.api.util.AEColor;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.client.render.overlay.IOverlayDataSource;
import appeng.client.render.overlay.OverlayManager;
import appeng.me.GridAccessException;
import appeng.services.ChunkLoadingService;
import appeng.tile.grid.AENetworkTileEntity;
import appeng.util.ConfigManager;
import appeng.util.IConfigManagerHost;

public class SpatialAnchorTileEntity extends AENetworkTileEntity
        implements IGridTickable, IConfigManagerHost, IConfigurableObject, IOverlayDataSource, IMovableTile {

    /**
     * Loads this radius after being move via a spatial transfer. This accounts for the anchor not being placed in the
     * center of the SCS, but not as much as trying to fully load a 128 cubic cell with 8x8 chunks. This would need to
     * load a 17x17 square.
     * 
     * TODO: 1.17 Break API compat and pass plot id or plot-descriptor to {@link IMovableTile}.
     */
    private static final int SPATIAL_TRANSFER_TEMPORARY_CHUNK_RANGE = 4;

    private final ConfigManager manager = new ConfigManager(this);
    private final Set<ChunkPos> chunks = new HashSet<>();
    private int powerlessTicks = 0;
    private boolean initialized = false;
    private boolean displayOverlay = false;
    private boolean isActive = false;

    public SpatialAnchorTileEntity(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
        this.getProxy().setFlags(GridFlags.REQUIRE_CHANNEL);
        this.manager.registerSetting(Settings.OVERLAY_MODE, YesNo.NO);
    }

    @Override
    public CompoundNBT write(CompoundNBT data) {
        super.write(data);
        this.manager.writeToNBT(data);
        return data;
    }

    @Override
    public void read(BlockState blockState, CompoundNBT data) {
        super.read(blockState, data);
        this.manager.readFromNBT(data);
    }

    @Override
    protected void writeToStream(PacketBuffer data) throws IOException {
        super.writeToStream(data);
        data.writeBoolean(this.isActive());
        data.writeBoolean(displayOverlay);
        if (this.displayOverlay) {
            data.writeLongArray(chunks.stream().mapToLong(ChunkPos::asLong).toArray());
        }
    }

    @Override
    protected boolean readFromStream(PacketBuffer data) throws IOException {
        boolean ret = super.readFromStream(data);

        final boolean isActive = data.readBoolean();
        ret = isActive != this.isActive || ret;
        this.isActive = isActive;

        boolean newDisplayOverlay = data.readBoolean();
        ret = newDisplayOverlay != this.displayOverlay || ret;
        this.displayOverlay = newDisplayOverlay;

        // Cleanup old data and remove it from the overlay manager as safeguard
        this.chunks.clear();
        OverlayManager.getInstance().removeHandlers(this);

        if (this.displayOverlay) {
            this.chunks.addAll(Arrays.stream(data.readLongArray(null)).mapToObj(ChunkPos::new)
                    .collect(Collectors.toSet()));
            // Register it again to render the overlay
            OverlayManager.getInstance().showArea(this);
        }

        return ret;
    }

    @Override
    public AECableType getCableConnectionType(final AEPartLocation dir) {
        return AECableType.SMART;
    }

    @Override
    public DimensionalCoord getLocation() {
        return new DimensionalCoord(this);
    }

    @Override
    public Set<ChunkPos> getOverlayChunks() {
        return this.chunks;
    }

    @Override
    public TileEntity getOverlayTileEntity() {
        return this;
    }

    @Override
    public DimensionalCoord getOverlaySourceLocation() {
        return this.getLocation();
    }

    @Override
    public int getOverlayColor() {
        return 0x80000000 | AEColor.TRANSPARENT.mediumVariant;
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
            // Need to wake up the anchor to potentially perform another cleanup
            this.wakeUp();
        }
    }

    @MENetworkEventSubscribe
    public void powerChange(final MENetworkPowerStatusChange powerChange) {
        this.markForUpdate();
        this.wakeUp();
    }

    @MENetworkEventSubscribe
    public void powerChange(final MENetworkChannelsChanged powerChange) {
        this.markForUpdate();
        this.wakeUp();
    }

    @Override
    public void updateSetting(IConfigManager manager, Settings settingName, Enum<?> newValue) {
        if (settingName == Settings.OVERLAY_MODE) {
            this.displayOverlay = newValue == YesNo.YES;
            this.markForUpdate();
        }
    }

    @Override
    public void remove() {
        super.remove();
        if (isRemote()) {
            OverlayManager.getInstance().removeHandlers(this);
        } else {
            this.releaseAll();
        }
    }

    @Override
    public IConfigManager getConfigManager() {
        return this.manager;
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
        } else {
            this.cleanUp();
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

    public boolean isPowered() {
        return this.getProxy().isActive() && this.getProxy().isPowered();
    }

    public boolean isActive() {
        if (world != null && !world.isRemote) {
            return isPowered();
        } else {
            return this.isActive;
        }
    }

    /**
     * Used to restore loaded chunks from {@link ForgeChunkManager}
     */
    public void registerChunk(ChunkPos chunkPos) {
        this.chunks.add(chunkPos);
        this.updatePowerConsumption();
    }

    private void updatePowerConsumption() {
        int energy = 80 + (this.chunks.size() * (this.chunks.size() + 1)) / 2;
        this.getProxy().setIdlePowerUsage(energy);
    }

    /**
     * Performs a cleanup of the loaded chunks and adds missing ones as well as removes any chunk no longer part of the
     * network.
     */
    private void cleanUp() {
        try {
            Multiset<ChunkPos> requiredChunks = this.getProxy().getStatistics().getChunks().get(this.getServerWorld());

            // Release all chunks, which are no longer part of the network.s
            for (Iterator<ChunkPos> iterator = chunks.iterator(); iterator.hasNext();) {
                ChunkPos chunkPos = iterator.next();

                if (!requiredChunks.contains(chunkPos)) {
                    this.release(chunkPos, false);
                    iterator.remove();
                }
            }

            // Force missing chunks
            for (ChunkPos chunkPos : requiredChunks) {
                if (!this.chunks.contains(chunkPos)) {
                    this.force(chunkPos);
                }
            }
        } catch (GridAccessException e) {
        }

    }

    /**
     * Adds the chunk to the current loaded list.
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
        this.markForUpdate();

        return forced;
    }

    private boolean release(ChunkPos chunkPos, boolean remove) {
        ServerWorld world = this.getServerWorld();
        boolean removed = ChunkLoadingService.getInstance().releaseChunk(world, this.getPos(), chunkPos, true);

        if (removed && remove) {
            this.chunks.remove(chunkPos);
        }

        this.updatePowerConsumption();
        this.markForUpdate();

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

    @Override
    public boolean prepareToMove() {
        // Just in case there are still some chunks left, as the world will change.F
        this.releaseAll();

        return true;
    }

    @Override
    public void doneMoving() {
        // reset the init state to keep the temporary loaded area until the network is ready.
        this.initialized = false;

        // Temporarily load an area after a spatial transfer until the network is constructed and cleanup is performed.
        int d = SPATIAL_TRANSFER_TEMPORARY_CHUNK_RANGE;
        ChunkPos center = new ChunkPos(this.getPos());
        for (int x = center.x - d; x <= center.x + d; x++) {
            for (int z = center.z - d; z <= center.z + d; z++) {
                this.force(new ChunkPos(x, z));
            }
        }

    }
}
