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

import java.util.Set;
import java.util.function.BiConsumer;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;

import org.jetbrains.annotations.NotNull;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.server.ServerWorld;

import appeng.api.exceptions.FailedConnectionException;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IConfigurableGridNode;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridConnection;
import appeng.api.networking.IGridHelper;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.api.networking.events.GridEvent;
import appeng.me.GridConnection;
import appeng.me.GridEventBus;
import appeng.me.GridNode;
import appeng.me.InWorldGridNode;

/**
 * @author yueh
 * @version rv5
 * @since rv5
 */
public class ApiGrid implements IGridHelper {

    @Override
    public <T extends GridEvent> void addEventHandler(Class<T> eventClass, BiConsumer<IGrid, T> handler) {
        GridEventBus.subscribe(eventClass, handler);
    }

    @Nullable
    @Override
    public IInWorldGridNodeHost getNodeHost(IWorld world, BlockPos pos) {
        if (world.isBlockLoaded(pos)) {
            final TileEntity te = world.getTileEntity(pos);
            if (te instanceof IInWorldGridNodeHost host) {
                return host;
            }
        }
        return null;
    }

    @NotNull
    @Override
    public <T> IConfigurableGridNode createInWorldGridNode(@NotNull T logicalHost,
            @NotNull IGridNodeListener<T> listener, @NotNull ServerWorld world, @NotNull BlockPos pos,
            @NotNull Set<GridFlags> flags) {
        Preconditions.checkNotNull(logicalHost);
        Preconditions.checkNotNull(listener);
        Preconditions.checkNotNull(world);
        Preconditions.checkNotNull(pos);
        Preconditions.checkNotNull(flags);

        return new InWorldGridNode(world, pos, logicalHost, listener, flags);
    }

    @NotNull
    @Override
    public <T> IConfigurableGridNode createInternalGridNode(@NotNull T logicalHost,
            @NotNull IGridNodeListener<T> listener, @NotNull ServerWorld world, @NotNull Set<GridFlags> flags) {
        Preconditions.checkNotNull(logicalHost);
        Preconditions.checkNotNull(listener);
        Preconditions.checkNotNull(world);
        Preconditions.checkNotNull(flags);

        return new GridNode(world, logicalHost, listener, flags);
    }

    @Override
    public IGridConnection createGridConnection(final IGridNode a, final IGridNode b) throws FailedConnectionException {
        Preconditions.checkNotNull(a);
        Preconditions.checkNotNull(b);

        return GridConnection.create(a, b, null);
    }

}
