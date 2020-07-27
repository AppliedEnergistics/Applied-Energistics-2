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

package appeng.tile.misc;

import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.Tickable;

import appeng.tile.AEBaseBlockEntity;
import appeng.util.Platform;

public class LightDetectorBlockEntity extends AEBaseBlockEntity implements Tickable {

    private int lastCheck = 30;
    private int lastLight = 0;

    public LightDetectorBlockEntity(BlockEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
    }

    public boolean isReady() {
        return this.lastLight > 0;
    }

    @Override
    public void tick() {
        this.lastCheck++;
        if (this.lastCheck > 30) {
            this.lastCheck = 0;
            this.updateLight();
        }
    }

    public void updateLight() {
        final int val = this.world.getLightLevel(this.pos);

        if (this.lastLight != val) {
            this.lastLight = val;
            Platform.notifyBlocksOfNeighbors(this.world, this.pos);
        }
    }

    @Override
    public boolean canBeRotated() {
        return false;
    }
}
