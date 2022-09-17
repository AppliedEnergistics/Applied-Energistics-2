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

package appeng.block.qnb;


import appeng.tile.qnb.TileQuantumBridge;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.List;


public class BlockQuantumRing extends BlockQuantumBase {

    public BlockQuantumRing() {
        super(Material.IRON);
    }

    @Override
    public Iterable<AxisAlignedBB> getSelectedBoundingBoxesFromPool(final World w, final BlockPos pos, final Entity thePlayer, final boolean b) {
        double onePixel = 2.0 / 16.0;
        final TileQuantumBridge bridge = this.getTileEntity(w, pos);
        if (bridge != null && bridge.isCorner()) {
            onePixel = 4.0 / 16.0;
        } else if (bridge != null && bridge.isFormed()) {
            onePixel = 1.0 / 16.0;
        }
        return Collections.singletonList(new AxisAlignedBB(onePixel, onePixel, onePixel, 1.0 - onePixel, 1.0 - onePixel, 1.0 - onePixel));
    }

    @Override
    public void addCollidingBlockToList(final World w, final BlockPos pos, final AxisAlignedBB bb, final List<AxisAlignedBB> out, final Entity e) {
        double onePixel = 2.0 / 16.0;
        final TileQuantumBridge bridge = this.getTileEntity(w, pos);
        if (bridge != null && bridge.isCorner()) {
            onePixel = 4.0 / 16.0;
        } else if (bridge != null && bridge.isFormed()) {
            onePixel = 1.0 / 16.0;
        }
        out.add(new AxisAlignedBB(onePixel, onePixel, onePixel, 1.0 - onePixel, 1.0 - onePixel, 1.0 - onePixel));
    }
}
