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

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.network.PacketBuffer;

import appeng.api.config.SecurityPermissions;
import appeng.api.networking.IGrid;
import appeng.api.util.AEPartLocation;
import appeng.container.AEBaseContainer;
import appeng.container.ContainerLocator;
import appeng.container.guisync.GuiSync;
import appeng.tile.spatial.SpatialAnchorTileEntity;

public class SpatialAnchorContainer extends AEBaseContainer {

    public static ContainerType<SpatialAnchorContainer> TYPE;

    private static final ContainerHelper<SpatialAnchorContainer, SpatialAnchorTileEntity> helper = new ContainerHelper<>(
            SpatialAnchorContainer::new, SpatialAnchorTileEntity.class, SecurityPermissions.BUILD);

    private IGrid network;
    private int delay = 40;

    @GuiSync(0)
    public long PowerConsumption;

    @GuiSync(31)
    public int xSize;
    @GuiSync(32)
    public int ySize;
    @GuiSync(33)
    public int zSize;

    public SpatialAnchorContainer(int id, final PlayerInventory ip, final SpatialAnchorTileEntity spatialAnchor) {
        super(TYPE, id, ip, spatialAnchor, null);

        if (isServer()) {
            this.network = spatialAnchor.getGridNode(AEPartLocation.INTERNAL).getGrid();
        }

        this.bindPlayerInventory(ip, 0, 197 - /* height of player inventory */82);
    }

    public static SpatialAnchorContainer fromNetwork(int windowId, PlayerInventory inv, PacketBuffer buf) {
        return helper.fromNetwork(windowId, inv, buf);
    }

    public static boolean open(PlayerEntity player, ContainerLocator locator) {
        return helper.open(player, locator);
    }

    @Override
    public void detectAndSendChanges() {
        this.verifyPermissions(SecurityPermissions.BUILD, false);

        if (isServer()) {
            this.delay++;
            if (this.delay > 15 && this.network != null) {
                this.delay = 0;
            }
        }

        super.detectAndSendChanges();
    }
}
