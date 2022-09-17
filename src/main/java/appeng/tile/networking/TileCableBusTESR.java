/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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

package appeng.tile.networking;


import appeng.block.networking.BlockCableBus;


public class TileCableBusTESR extends TileCableBus {

    /**
     * Changes this tile to the non-TESR version, if none of the parts require dynamic rendering.
     */
    @Override
    protected void updateTileSetting() {
        if (!this.getCableBus().isRequiresDynamicRender()) {
            try {
                final TileCableBus tcb = (TileCableBus) BlockCableBus.getNoTesrTile().newInstance();
                tcb.copyFrom(this);
                this.getWorld().setTileEntity(this.pos, tcb);
            } catch (final Throwable ignored) {

            }
        }
    }
}
