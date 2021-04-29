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

package appeng.container.implementations;

import java.util.HashMap;
import java.util.Map.Entry;

import com.google.common.collect.Multiset;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import appeng.api.config.SecurityPermissions;
import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IMachineSet;
import appeng.api.util.AEPartLocation;
import appeng.container.AEBaseContainer;
import appeng.container.guisync.GuiSync;
import appeng.me.GridAccessException;
import appeng.me.cache.StatisticsCache;
import appeng.tile.spatial.SpatialAnchorTileEntity;

/**
 * @see appeng.client.gui.implementations.SpatialAnchorScreen
 */
public class SpatialAnchorContainer extends AEBaseContainer {

    public static final ContainerType<SpatialAnchorContainer> TYPE = ContainerTypeBuilder
            .create(SpatialAnchorContainer::new, SpatialAnchorTileEntity.class)
            .requirePermission(SecurityPermissions.BUILD)
            .build("spatialanchor");

    private static final int UPDATE_DELAY = 20;

    private IGrid network;

    // Updated with the delay for an immediate sync after construction
    private int delay = UPDATE_DELAY;

    @GuiSync(0)
    public long powerConsumption;
    @GuiSync(1)
    public int loadedChunks;
    @GuiSync(2)
    public YesNo overlayMode = YesNo.NO;

    @GuiSync(10)
    public int allLoadedWorlds;
    @GuiSync(11)
    public int allLoadedChunks;

    @GuiSync(20)
    public int allWorlds;
    @GuiSync(21)
    public int allChunks;

    public SpatialAnchorContainer(int id, final PlayerInventory ip, final SpatialAnchorTileEntity spatialAnchor) {
        super(TYPE, id, ip, spatialAnchor);

        if (isServer()) {
            this.network = spatialAnchor.getGridNode(AEPartLocation.INTERNAL).getGrid();
        }
    }

    @Override
    public void detectAndSendChanges() {
        this.verifyPermissions(SecurityPermissions.BUILD, false);

        if (isServer()) {
            SpatialAnchorTileEntity anchor = (SpatialAnchorTileEntity) this.getTileEntity();
            this.setOverlayMode((YesNo) anchor.getConfigManager().getSetting(Settings.OVERLAY_MODE));

            this.delay++;
            if (this.delay > UPDATE_DELAY && this.network != null) {
                StatisticsCache statistics = this.network.getCache(StatisticsCache.class);

                this.powerConsumption = (long) anchor.getProxy().getIdlePowerUsage();
                this.loadedChunks = ((SpatialAnchorTileEntity) this.getTileEntity()).countLoadedChunks();

                try {
                    HashMap<World, Integer> stats = new HashMap<>();
                    IMachineSet anchors = anchor.getProxy().getGrid().getMachines(SpatialAnchorTileEntity.class);

                    for (IGridNode machine : anchors) {
                        SpatialAnchorTileEntity a = (SpatialAnchorTileEntity) machine.getMachine();
                        World world = machine.getGridBlock().getLocation().getWorld();
                        stats.merge(world, a.countLoadedChunks(), Math::max);
                    }

                    this.allLoadedChunks = stats.values().stream().reduce(Integer::sum).orElse(0);
                    this.allLoadedWorlds = stats.keySet().size();
                } catch (GridAccessException ignored) {
                }

                this.allWorlds = statistics.getChunks().size();
                this.allChunks = 0;
                for (Entry<IWorld, Multiset<ChunkPos>> entry : statistics.getChunks().entrySet()) {
                    this.allChunks += entry.getValue().elementSet().size();
                }

                this.delay = 0;
            }
        }

        super.detectAndSendChanges();
    }

    public YesNo getOverlayMode() {
        return this.overlayMode;
    }

    public void setOverlayMode(final YesNo mode) {
        this.overlayMode = mode;
    }
}
