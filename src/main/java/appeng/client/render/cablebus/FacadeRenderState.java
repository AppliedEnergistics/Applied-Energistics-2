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

package appeng.client.render.cablebus;

import net.minecraft.block.BlockState;

/**
 * Captures the state required to render a facade properly.
 */
public class FacadeRenderState {

    // The block state to use for rendering this facade
    private final BlockState sourceBlock;

    private final boolean transparent;

    public FacadeRenderState(BlockState sourceBlock, boolean transparent) {
        this.sourceBlock = sourceBlock;
        this.transparent = transparent;
    }

    public BlockState getSourceBlock() {
        return this.sourceBlock;
    }

    public boolean isTransparent() {
        return this.transparent;
    }
}
