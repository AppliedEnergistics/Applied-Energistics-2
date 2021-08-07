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

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;

import appeng.api.config.SecurityPermissions;
import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.container.AEBaseContainer;
import appeng.container.guisync.GuiSync;
import appeng.me.service.StatisticsService;
import appeng.tile.spatial.SpatialAnchorBlockEntity;

/**
 * @see appeng.client.gui.implementations.SpatialAnchorScreen
 */
public class SpatialAnchorContainer extends AEBaseContainer {

    public static final MenuType<SpatialAnchorContainer> TYPE = ContainerTypeBuilder
            .create(SpatialAnchorContainer::new, SpatialAnchorBlockEntity.class)
            .requirePermission(SecurityPermissions.BUILD)
            .build("spatialanchor");

    private static final int UPDATE_DELAY = 20;

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

    public SpatialAnchorContainer(int id, final Inventory ip, final SpatialAnchorBlockEntity spatialAnchor) {
        super(TYPE, id, ip, spatialAnchor);
    }

    @Override
    public void broadcastChanges() {
        this.verifyPermissions(SecurityPermissions.BUILD, false);

        if (isServer()) {
            SpatialAnchorBlockEntity anchor = (SpatialAnchorBlockEntity) this.getTileEntity();
            this.setOverlayMode((YesNo) anchor.getConfigManager().getSetting(Settings.OVERLAY_MODE));

            var gridNode = anchor.getGridNode();

            this.delay++;
            if (this.delay > UPDATE_DELAY && gridNode != null && gridNode.getGrid() != null) {
                var grid = gridNode.getGrid();

                var statistics = grid.getService(StatisticsService.class);

                this.powerConsumption = (long) gridNode.getIdlePowerUsage();
                this.loadedChunks = anchor.countLoadedChunks();

                HashMap<LevelAccessor, Integer> stats = new HashMap<>();

                for (var machine : grid.getMachines(SpatialAnchorBlockEntity.class)) {
                    LevelAccessor world = machine.getLevel();
                    stats.merge(world, machine.countLoadedChunks(), Math::max);
                }

                this.allLoadedChunks = stats.values().stream().reduce(Integer::sum).orElse(0);
                this.allLoadedWorlds = stats.keySet().size();

                this.allWorlds = statistics.getChunks().size();
                this.allChunks = 0;
                for (Entry<LevelAccessor, Multiset<ChunkPos>> entry : statistics.getChunks().entrySet()) {
                    this.allChunks += entry.getValue().elementSet().size();
                }

                this.delay = 0;
            }
        }

        super.broadcastChanges();
    }

    public YesNo getOverlayMode() {
        return this.overlayMode;
    }

    public void setOverlayMode(final YesNo mode) {
        this.overlayMode = mode;
    }
}
