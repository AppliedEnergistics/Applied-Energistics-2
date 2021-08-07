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

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.world.ForgeChunkManager;

import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.movable.IMovableBlockEntity;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.events.statistics.GridChunkEvent.GridChunkAdded;
import appeng.api.networking.events.statistics.GridChunkEvent.GridChunkRemoved;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.util.AECableType;
import appeng.api.util.AEColor;
import appeng.api.util.DimensionalBlockPos;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.client.render.overlay.IOverlayDataSource;
import appeng.client.render.overlay.OverlayManager;
import appeng.core.Api;
import appeng.me.service.StatisticsService;
import appeng.services.ChunkLoadingService;
import appeng.tile.grid.AENetworkTileEntity;
import appeng.util.ConfigManager;
import appeng.util.IConfigManagerHost;

public class SpatialAnchorTileEntity extends AENetworkTileEntity
        implements IGridTickable, IConfigManagerHost, IConfigurableObject, IOverlayDataSource, IMovableBlockEntity {

    static {
        Api.instance().grid().addNodeOwnerEventHandler(GridChunkAdded.class, SpatialAnchorTileEntity.class,
                SpatialAnchorTileEntity::chunkAdded);
        Api.instance().grid().addNodeOwnerEventHandler(GridChunkRemoved.class, SpatialAnchorTileEntity.class,
                SpatialAnchorTileEntity::chunkRemoved);
    }

    /**
     * Loads this radius after being move via a spatial transfer. This accounts for the anchor not being placed in the
     * center of the SCS, but not as much as trying to fully load a 128 cubic cell with 8x8 chunks. This would need to
     * load a 17x17 square.
     * <p>
     * TODO: 1.17 Break API compat and pass plot id or plot-descriptor to {@link IMovableBlockEntity}.
     */
    private static final int SPATIAL_TRANSFER_TEMPORARY_CHUNK_RANGE = 4;

    private final ConfigManager manager = new ConfigManager(this);
    private final Set<ChunkPos> chunks = new HashSet<>();
    private int powerlessTicks = 0;
    private boolean initialized = false;
    private boolean displayOverlay = false;
    private boolean isActive = false;

    public SpatialAnchorTileEntity(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState blockState) {
        super(tileEntityTypeIn, pos, blockState);
        getMainNode().setFlags(GridFlags.REQUIRE_CHANNEL)
                .addService(IGridTickable.class, this);
        this.manager.registerSetting(Settings.OVERLAY_MODE, YesNo.NO);
    }

    @Override
    public CompoundTag save(CompoundTag data) {
        super.save(data);
        this.manager.writeToNBT(data);
        return data;
    }

    @Override
    public void load(CompoundTag data) {
        super.load(data);
        this.manager.readFromNBT(data);
    }

    @Override
    protected void writeToStream(FriendlyByteBuf data) throws IOException {
        super.writeToStream(data);
        data.writeBoolean(this.isActive());
        data.writeBoolean(displayOverlay);
        if (this.displayOverlay) {
            data.writeLongArray(chunks.stream().mapToLong(ChunkPos::toLong).toArray());
        }
    }

    @Override
    protected boolean readFromStream(FriendlyByteBuf data) throws IOException {
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
    public AECableType getCableConnectionType(Direction dir) {
        return AECableType.SMART;
    }

    @Override
    public Set<ChunkPos> getOverlayChunks() {
        return this.chunks;
    }

    @Override
    public BlockEntity getOverlayTileEntity() {
        return this;
    }

    @Override
    public DimensionalBlockPos getOverlaySourceLocation() {
        return new DimensionalBlockPos(this);
    }

    @Override
    public int getOverlayColor() {
        return 0x80000000 | AEColor.TRANSPARENT.mediumVariant;
    }

    public void chunkAdded(final GridChunkAdded changed) {
        if (changed.getWorld() == this.getServerWorld()) {
            this.force(changed.getChunkPos());
        }
    }

    public void chunkRemoved(final GridChunkRemoved changed) {
        if (changed.getWorld() == this.getServerWorld()) {
            this.release(changed.getChunkPos(), true);
            // Need to wake up the anchor to potentially perform another cleanup
            this.wakeUp();
        }
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State reason) {
        if (reason != IGridNodeListener.State.GRID_BOOT) {
            this.markForUpdate();
            this.wakeUp();
        }
    }

    @Override
    public void updateSetting(IConfigManager manager, Settings settingName, Enum<?> newValue) {
        if (settingName == Settings.OVERLAY_MODE) {
            this.displayOverlay = newValue == YesNo.YES;
            this.markForUpdate();
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
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
        getMainNode().ifPresent((grid, node) -> {
            grid.getTickManager().alertDevice(node);
        });
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
        if (!this.initialized && this.getMainNode().isActive() && this.getMainNode().isPowered()) {
            this.forceAll();
            this.initialized = true;
        } else {
            this.cleanUp();
        }

        // Be a bit lenient to not unload all chunks immediately upon power loss
        if (this.powerlessTicks > 200) {
            if (!this.getMainNode().isPowered() || !this.getMainNode().isActive()) {
                this.releaseAll();
            }
            this.powerlessTicks = 0;

            // Put anchor to sleep until another power change.
            return TickRateModulation.SLEEP;
        }

        // Count ticks without power
        if (!this.getMainNode().isPowered() || !this.getMainNode().isActive()) {
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
        return this.getMainNode().isActive() && this.getMainNode().isPowered();
    }

    public boolean isActive() {
        if (level != null && !level.isClientSide) {
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
        int energy = 80 + this.chunks.size() * (this.chunks.size() + 1) / 2;
        this.getMainNode().setIdlePowerUsage(energy);
    }

    /**
     * Performs a cleanup of the loaded chunks and adds missing ones as well as removes any chunk no longer part of the
     * network.
     */
    private void cleanUp() {
        var grid = getMainNode().getGrid();
        if (grid == null) {
            return;
        }

        Multiset<ChunkPos> requiredChunks = grid.getService(StatisticsService.class).getChunks()
                .get(this.getServerWorld());

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

    }

    /**
     * Adds the chunk to the current loaded list.
     */
    private boolean force(ChunkPos chunkPos) {
        // Avoid loading chunks after the anchor is destroyed
        if (this.isRemoved()) {
            return false;
        }

        ServerLevel world = this.getServerWorld();
        boolean forced = ChunkLoadingService.getInstance().forceChunk(world, this.getBlockPos(), chunkPos, true);

        if (forced) {
            this.chunks.add(chunkPos);
        }

        this.updatePowerConsumption();
        this.markForUpdate();

        return forced;
    }

    private boolean release(ChunkPos chunkPos, boolean remove) {
        ServerLevel world = this.getServerWorld();
        boolean removed = ChunkLoadingService.getInstance().releaseChunk(world, this.getBlockPos(), chunkPos, true);

        if (removed && remove) {
            this.chunks.remove(chunkPos);
        }

        this.updatePowerConsumption();
        this.markForUpdate();

        return removed;
    }

    private void forceAll() {
        getMainNode().ifPresent(grid -> {
            var statistics = grid.getService(StatisticsService.class);
            for (ChunkPos chunkPos : statistics.getChunks().get(this.getServerWorld())
                    .elementSet()) {
                this.force(chunkPos);
            }
        });
    }

    private void releaseAll() {
        for (ChunkPos chunk : this.chunks) {
            this.release(chunk, false);
        }
        this.chunks.clear();
    }

    private ServerLevel getServerWorld() {
        if (this.getLevel() instanceof ServerLevel) {
            return (ServerLevel) this.getLevel();
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
        ChunkPos center = new ChunkPos(this.getBlockPos());
        for (int x = center.x - d; x <= center.x + d; x++) {
            for (int z = center.z - d; z <= center.z + d; z++) {
                this.force(new ChunkPos(x, z));
            }
        }

    }
}
