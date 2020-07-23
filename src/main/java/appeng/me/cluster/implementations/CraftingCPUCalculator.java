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

package appeng.me.cluster.implementations;

import java.util.Iterator;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.events.MENetworkCraftingCpuChange;
import appeng.api.util.AEPartLocation;
import appeng.me.cluster.IAEMultiBlock;
import appeng.me.cluster.MBCalculator;
import appeng.tile.crafting.CraftingBlockEntity;

public class CraftingCPUCalculator extends MBCalculator<CraftingBlockEntity, CraftingCPUCluster> {

    public CraftingCPUCalculator(final CraftingBlockEntity t) {
        super(t);
    }

    @Override
    public boolean checkMultiblockScale(final BlockPos min, final BlockPos max) {
        if (max.getX() - min.getX() > 16) {
            return false;
        }

        if (max.getY() - min.getY() > 16) {
            return false;
        }

        if (max.getZ() - min.getZ() > 16) {
            return false;
        }

        return true;
    }

    @Override
    public CraftingCPUCluster createCluster(final World w, final BlockPos min, final BlockPos max) {
        return new CraftingCPUCluster(min, max);
    }

    @Override
    public boolean verifyInternalStructure(final World w, final BlockPos min, final BlockPos max) {
        boolean storage = false;

        for (BlockPos blockPos : BlockPos.iterate(min, max)) {
            final IAEMultiBlock<?> te = (IAEMultiBlock<?>) w.getBlockEntity(blockPos);

            if (te == null || !te.isValid()) {
                return false;
            }

            if (!storage && te instanceof CraftingBlockEntity) {
                storage = ((CraftingBlockEntity) te).getStorageBytes() > 0;
            }
        }

        return storage;
    }

    @Override
    public void updateTiles(final CraftingCPUCluster c, final World w, final BlockPos min, final BlockPos max) {
        for (BlockPos blockPos : BlockPos.iterate(min, max)) {
            final CraftingBlockEntity te = (CraftingBlockEntity) w.getBlockEntity(blockPos);
            te.updateStatus(c);
            c.addTile(te);
        }

        c.done();

        final Iterator<CraftingBlockEntity> i = c.getTiles();
        while (i.hasNext()) {
            final IGridHost gh = i.next();
            final IGridNode n = gh.getGridNode(AEPartLocation.INTERNAL);
            if (n != null) {
                final IGrid g = n.getGrid();
                if (g != null) {
                    g.postEvent(new MENetworkCraftingCpuChange(n));
                    return;
                }
            }
        }
    }

    @Override
    public boolean isValidTile(final BlockEntity te) {
        return te instanceof CraftingBlockEntity;
    }
}
