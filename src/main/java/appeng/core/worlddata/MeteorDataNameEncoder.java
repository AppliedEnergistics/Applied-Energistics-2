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

package appeng.core.worlddata;


import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;


/**
 * encodes data into a common name
 *
 * @author thatsIch
 * @version rv3 - 05.06.2015
 * @since rv3 05.06.2015
 */
public class MeteorDataNameEncoder {
    private static final char DATA_SEPARATOR = '_';
    private static final char BASE_EXTENSION_SEPARATOR = '.';
    private static final String FILE_EXTENSION = "dat";

    private final char dataSeparator;
    @Nonnull
    private final String fileExtension;
    private final char baseExtSeparator;
    private final int bitScale;

    /**
     * @param bitScale how often the coordinates will be shifted right (will scale coordinates down)
     */
    public MeteorDataNameEncoder(final int bitScale) {
        this(DATA_SEPARATOR, BASE_EXTENSION_SEPARATOR, FILE_EXTENSION, bitScale);
    }

    private MeteorDataNameEncoder(final char dataSeparator, final char baseExtSeparator, @Nonnull final String fileExtension, final int bitScale) {
        Preconditions.checkNotNull(fileExtension);
        Preconditions.checkArgument(!fileExtension.isEmpty());
        Preconditions.checkArgument(bitScale >= 0);

        this.dataSeparator = dataSeparator;
        this.baseExtSeparator = baseExtSeparator;
        this.fileExtension = fileExtension;
        this.bitScale = bitScale;
    }

    /**
     * @param dimension ID of the processed dimension. Can be any integer
     * @param chunkX    X coordinate of the chunk. Can be any integer
     * @param chunkZ    Z coordinate of the chunk. Can be any integer
     * @return encoded file name suggestion in form of <tt>dim_x_y.dat</tt> where <tt>x</tt> and <tt>y</tt> will be
     * shifted to stay conform with the vanilla chunk system
     * @since rv3 05.06.2015
     */
    public String encode(final int dimension, final int chunkX, final int chunkZ) {
        final int shiftedX = chunkX >> this.bitScale;
        final int shiftedZ = chunkZ >> this.bitScale;

        return String.format("%d%c%d%c%d%c%s", dimension, this.dataSeparator, shiftedX, this.dataSeparator, shiftedZ, this.baseExtSeparator,
                this.fileExtension);
    }
}
