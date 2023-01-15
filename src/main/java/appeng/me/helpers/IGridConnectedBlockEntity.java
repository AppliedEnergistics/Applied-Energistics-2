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

import java.util.EnumSet;
import java.util.Set;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.api.orientation.BlockOrientation;
import appeng.block.IOwnerAwareBlockEntity;
import appeng.blockentity.AEBaseBlockEntity;
import appeng.me.InWorldGridNode;

/**
 * Interface implemented by the various AE2 block entities that connect to the grid, to support callbacks from the block
 * entities main grid node.
 */
public interface IGridConnectedBlockEntity extends IActionHost, IOwnerAwareBlockEntity, IInWorldGridNodeHost {

    /**
     * @return The main node that the block entity uses to connect to the grid.
     */
    IManagedGridNode getMainNode();

    /**
     * @param orientation The current orientation of the block.
     * @return The sides of this block that are exposed for ME grid connections.
     */
    default Set<Direction> getGridConnectableSides(BlockOrientation orientation) {
        return EnumSet.allOf(Direction.class);
    }

    @Nullable
    default IGridNode getGridNode() {
        return getMainNode().getNode();
    }

    @Override
    default IGridNode getGridNode(Direction dir) {
        var node = this.getMainNode().getNode();

        // We use the node rather than getGridConnectableSides since the node is already using absolute sides
        if (node instanceof InWorldGridNode inWorldGridNode
                && inWorldGridNode.isExposedOnSide(dir)) {
            return node;
        }

        return null;
    }

    /**
     * @see IManagedGridNode#ifPresent(Consumer)
     */
    default boolean ifGridPresent(Consumer<IGrid> action) {
        return getMainNode().ifPresent(action);
    }

    /**
     * Used to break the block entity when the grid detects a security violation. Implemented in
     * {@link AEBaseBlockEntity#securityBreak()}
     */
    void securityBreak();

    /**
     * Used to save changes in the grid nodes contained in the block entity to disk. Implemented in
     * {@link AEBaseBlockEntity#saveChanges()}
     */
    void saveChanges();

    /**
     * Called when the block entities main grid nodes power or channel assignment state changes. Primarily used to send
     * rendering updates to the client.
     */
    default void onMainNodeStateChanged(IGridNodeListener.State reason) {
    }

    @Override
    default IGridNode getActionableNode() {
        return getMainNode().getNode();
    }

    @Override
    default void setOwner(Player owner) {
        getMainNode().setOwningPlayer(owner);
    }
}
