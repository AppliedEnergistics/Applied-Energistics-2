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

package appeng.core.api;


import appeng.api.exceptions.FailedConnectionException;
import appeng.api.networking.IGridBlock;
import appeng.api.networking.IGridConnection;
import appeng.api.networking.IGridHelper;
import appeng.api.networking.IGridNode;
import appeng.api.util.AEPartLocation;
import appeng.me.GridConnection;
import appeng.me.GridNode;
import appeng.util.Platform;
import com.google.common.base.Preconditions;


/**
 * @author yueh
 * @version rv5
 * @since rv5
 */
public class ApiGrid implements IGridHelper {

    @Override
    public IGridNode createGridNode(final IGridBlock blk) {
        Preconditions.checkNotNull(blk);

        if (Platform.isClient()) {
            throw new IllegalStateException("Grid features for " + blk + " are server side only.");
        }

        return new GridNode(blk);
    }

    @Override
    public IGridConnection createGridConnection(final IGridNode a, final IGridNode b) throws FailedConnectionException {
        Preconditions.checkNotNull(a);
        Preconditions.checkNotNull(b);

        return GridConnection.create(a, b, AEPartLocation.INTERNAL);
    }

}
