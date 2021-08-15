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

package appeng.me.service;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;

import com.google.common.base.Preconditions;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import appeng.api.AEApi;
import appeng.api.config.SecurityPermissions;
import appeng.api.features.IPlayerRegistry;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridServiceProvider;
import appeng.api.networking.events.GridSecurityChange;
import appeng.api.networking.security.ISecurityProvider;
import appeng.api.networking.security.ISecurityService;
import appeng.me.GridNode;

public class SecurityService implements ISecurityService, IGridServiceProvider {

    static {
        AEApi.grid().addGridServiceEventHandler(GridSecurityChange.class, ISecurityService.class,
                (service, event) -> {
                    ((SecurityService) service).updatePermissions();
                });
    }

    private final IGrid myGrid;
    private final List<ISecurityProvider> securityProvider = new ArrayList<>();
    private final HashMap<Integer, EnumSet<SecurityPermissions>> playerPerms = new HashMap<>();
    private long securityKey = -1;

    public SecurityService(final IGrid g) {
        this.myGrid = g;
    }

    private void updatePermissions() {
        this.playerPerms.clear();
        if (this.securityProvider.isEmpty()) {
            return;
        }

        this.securityProvider.get(0).readPermissions(this.playerPerms);
    }

    public long getSecurityKey() {
        return this.securityKey;
    }

    @Override
    public void removeNode(final IGridNode gridNode) {
        var security = gridNode.getService(ISecurityProvider.class);
        if (security != null) {
            this.securityProvider.remove(security);
            this.updateSecurityKey();
        }
    }

    private void updateSecurityKey() {
        final long lastCode = this.securityKey;

        /*
         * Placing a security station will propagate the security station's owner to all connected grid nodes to prevent
         * the network from not reforming due to different owners later.
         */
        int newOwner = -1;
        if (this.securityProvider.size() == 1) {
            ISecurityProvider securityProvider = this.securityProvider.get(0);
            this.securityKey = securityProvider.getSecurityKey();
            newOwner = securityProvider.getOwner();
        } else {
            this.securityKey = -1;
        }

        if (lastCode != this.securityKey) {
            this.getGrid().postEvent(new GridSecurityChange());
            for (var n : this.getGrid().getNodes()) {
                GridNode gridNode = (GridNode) n;
                gridNode.setLastSecurityKey(this.securityKey);
                if (gridNode.getOwningPlayerId() != newOwner) {
                    gridNode.setOwningPlayerId(newOwner);
                }
            }
        }
    }

    @Override
    public void addNode(final IGridNode gridNode) {
        var security = gridNode.getService(ISecurityProvider.class);

        if (security != null) {
            this.securityProvider.add(security);
            this.updateSecurityKey();
        } else {
            ((GridNode) gridNode).setLastSecurityKey(this.securityKey);
        }
    }

    @Override
    public boolean isAvailable() {
        return this.securityProvider.size() == 1 && this.securityProvider.get(0).isSecurityEnabled();
    }

    @Override
    public boolean hasPermission(final Player player, final SecurityPermissions perm) {
        Preconditions.checkNotNull(player);
        Preconditions.checkNotNull(perm);

        if (player instanceof ServerPlayer serverPlayer) {
            var playerID = IPlayerRegistry.getPlayerId(serverPlayer);
            return this.hasPermission(playerID, perm);
        } else {
            return false;
        }
    }

    @Override
    public boolean hasPermission(final int playerID, final SecurityPermissions perm) {
        if (this.isAvailable()) {
            final EnumSet<SecurityPermissions> perms = this.playerPerms.get(playerID);

            if (perms == null) {
                if (playerID == -1) // no default?
                {
                    return false;
                } else {
                    return this.hasPermission(-1, perm);
                }
            }

            return perms.contains(perm);
        }
        return true;
    }

    @Override
    public int getOwner() {
        if (this.isAvailable()) {
            return this.securityProvider.get(0).getOwner();
        }
        return -1;
    }

    public IGrid getGrid() {
        return this.myGrid;
    }
}
