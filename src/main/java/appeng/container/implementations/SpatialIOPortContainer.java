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

package appeng.container.implementations;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.network.PacketBuffer;

import appeng.api.config.SecurityPermissions;
import appeng.api.networking.IGrid;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.spatial.ISpatialCache;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import appeng.container.AEBaseContainer;
import appeng.container.ContainerLocator;
import appeng.container.guisync.GuiSync;
import appeng.container.slot.OutputSlot;
import appeng.container.slot.RestrictedInputSlot;
import appeng.tile.spatial.SpatialIOPortTileEntity;

public class SpatialIOPortContainer extends AEBaseContainer {

    public static ContainerType<SpatialIOPortContainer> TYPE;

    private static final ContainerHelper<SpatialIOPortContainer, SpatialIOPortTileEntity> helper = new ContainerHelper<>(
            SpatialIOPortContainer::new, SpatialIOPortTileEntity.class, SecurityPermissions.BUILD);

    @GuiSync(0)
    public long currentPower;
    @GuiSync(1)
    public long maxPower;
    @GuiSync(2)
    public long reqPower;
    @GuiSync(3)
    public long eff;
    private IGrid network;
    private int delay = 40;

    @GuiSync(31)
    public int xSize;
    @GuiSync(32)
    public int ySize;
    @GuiSync(33)
    public int zSize;

    public SpatialIOPortContainer(int id, final PlayerInventory ip, final SpatialIOPortTileEntity spatialIOPort) {
        super(TYPE, id, ip, spatialIOPort, null);

        if (isServer()) {
            this.network = spatialIOPort.getGridNode(AEPartLocation.INTERNAL).getGrid();
        }

        this.addSlot(new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.SPATIAL_STORAGE_CELLS,
                spatialIOPort.getInternalInventory(), 0, 52, 48, this.getPlayerInventory()));
        this.addSlot(new OutputSlot(spatialIOPort.getInternalInventory(), 1, 113, 48,
                RestrictedInputSlot.PlacableItemType.SPATIAL_STORAGE_CELLS.IIcon));

        this.bindPlayerInventory(ip, 0, 197 - /* height of player inventory */82);
    }

    public static SpatialIOPortContainer fromNetwork(int windowId, PlayerInventory inv, PacketBuffer buf) {
        return helper.fromNetwork(windowId, inv, buf);
    }

    public static boolean open(PlayerEntity player, ContainerLocator locator) {
        return helper.open(player, locator);
    }

    @Override
    public void broadcastChanges() {
        this.verifyPermissions(SecurityPermissions.BUILD, false);

        if (isServer()) {
            this.delay++;
            if (this.delay > 15 && this.network != null) {
                this.delay = 0;

                final IEnergyGrid eg = this.network.getCache(IEnergyGrid.class);
                final ISpatialCache sc = this.network.getCache(ISpatialCache.class);
                if (eg != null) {
                    this.setCurrentPower((long) (100.0 * eg.getStoredPower()));
                    this.setMaxPower((long) (100.0 * eg.getMaxStoredPower()));
                    this.setRequiredPower((long) (100.0 * sc.requiredPower()));
                    this.setEfficency((long) (100.0f * sc.currentEfficiency()));

                    final DimensionalCoord min = sc.getMin();
                    final DimensionalCoord max = sc.getMax();

                    if (min != null && max != null && sc.isValidRegion()) {
                        this.xSize = sc.getMax().x - sc.getMin().x - 1;
                        this.ySize = sc.getMax().y - sc.getMin().y - 1;
                        this.zSize = sc.getMax().z - sc.getMin().z - 1;
                    } else {
                        this.xSize = 0;
                        this.ySize = 0;
                        this.zSize = 0;
                    }
                }
            }
        }

        super.broadcastChanges();
    }

    public long getCurrentPower() {
        return this.currentPower;
    }

    private void setCurrentPower(final long currentPower) {
        this.currentPower = currentPower;
    }

    public long getMaxPower() {
        return this.maxPower;
    }

    private void setMaxPower(final long maxPower) {
        this.maxPower = maxPower;
    }

    public long getRequiredPower() {
        return this.reqPower;
    }

    private void setRequiredPower(final long reqPower) {
        this.reqPower = reqPower;
    }

    public long getEfficency() {
        return this.eff;
    }

    private void setEfficency(final long eff) {
        this.eff = eff;
    }
}
