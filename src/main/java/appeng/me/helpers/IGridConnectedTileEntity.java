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

package appeng.me.helpers;

import java.util.function.Consumer;

import net.minecraft.entity.player.PlayerEntity;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.block.IOwnerAwareTile;
import appeng.me.ManagedGridNode;
import appeng.tile.AEBaseTileEntity;

/**
 * Interface implemented by the various AE2 tile entities that connect to the grid, to support callbacks from the tile
 * entities main grid node.
 */
public interface IGridConnectedTileEntity extends IActionHost, IOwnerAwareTile {

    /**
     * @return The main node that the tile entity uses to connect to the grid.
     */
    IManagedGridNode getMainNode();

    /**
     * @see ManagedGridNode#ifPresent(Consumer)
     */
    default boolean ifGridPresent(Consumer<IGrid> action) {
        return getMainNode().ifPresent(action);
    }

    /**
     * Used to break the tile when the grid detects a security violation. Implemented in
     * {@link AEBaseTileEntity#securityBreak()}
     */
    void securityBreak();

    /**
     * Used to save changes in the grid nodes contained in the tile entity to disk. Implemented in
     * {@link AEBaseTileEntity#saveChanges()}
     */
    void saveChanges();

    /**
     * Called when the tile entities main grid nodes power or channel assignment state changes. Primarily used to send
     * rendering updates to the client.
     */
    default void onMainNodeStateChanged(IGridNodeListener.State reason) {
    }

    @Override
    default IGridNode getActionableNode() {
        return getMainNode().getNode();
    }

    @Override
    default void setOwner(PlayerEntity owner) {
        getMainNode().setOwningPlayer(owner);
    }

}
