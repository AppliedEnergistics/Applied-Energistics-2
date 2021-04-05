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

package appeng.bootstrap;

import appeng.tile.AEBaseTileEntity;

/**
 * A callback that allows the rendering of a block entity to be customized. Sadly this class is required and no lambdas
 * can be used due to them not being able to be annotated with @OnlyIn(CLIENT).
 */
public interface TileEntityRenderingCustomizer<T extends AEBaseTileEntity> {

    /**
     * Declared as a default method because we will carve out the implementations that override this method on the
     * Server side.
     */
    default void customize(TileEntityRendering<T> rendering) {
    }

}
