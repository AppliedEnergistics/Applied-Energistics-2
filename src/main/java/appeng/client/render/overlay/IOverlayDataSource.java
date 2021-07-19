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

package appeng.client.render.overlay;

import java.util.Set;

import javax.annotation.Nonnull;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.ChunkPos;

import appeng.api.util.DimensionalBlockPos;

/**
 * A source providing data for a chunk overlay.
 */
public interface IOverlayDataSource {

    /**
     * @return A set of chunks to display
     */
    @Nonnull
    Set<ChunkPos> getOverlayChunks();

    /**
     * {@link TileEntity} in case needed for additional data.
     *
     * @return the tile entity providing the data
     */
    @Nonnull
    TileEntity getOverlayTileEntity();

    /**
     * The location of the source with world and blockpos
     * <p>
     * Used as cache key for later lookups.
     * <p>
     * World is needed to hide the particular render when the player is in another world
     *
     * @return the location of this source.
     */
    @Nonnull
    DimensionalBlockPos getOverlaySourceLocation();

    /**
     * The color used for the overlay as ARGB
     */
    int getOverlayColor();

}
