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

package appeng.me.cache;


import appeng.api.config.SecurityPermissions;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridStorage;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkSecurityChange;
import appeng.api.networking.security.ISecurityGrid;
import appeng.api.networking.security.ISecurityProvider;
import appeng.core.worlddata.WorldData;
import appeng.me.GridNode;
import com.google.common.base.Preconditions;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.EntityPlayer;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;


public class SecurityCache implements ISecurityGrid {

    private final IGrid myGrid;
    private final List<ISecurityProvider> securityProvider = new ArrayList<>();
    private final HashMap<Integer, EnumSet<SecurityPermissions>> playerPerms = new HashMap<>();
    private long securityKey = -1;

    public SecurityCache(final IGrid g) {
        this.myGrid = g;
    }

    @MENetworkEventSubscribe
    public void updatePermissions(final MENetworkSecurityChange ev) {
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
    public void onUpdateTick() {

    }

    @Override
    public void removeNode(final IGridNode gridNode, final IGridHost machine) {
        if (machine instanceof ISecurityProvider) {
            this.securityProvider.remove(machine);
            this.updateSecurityKey();
        }
    }

    private void updateSecurityKey() {
        final long lastCode = this.securityKey;

        if (this.securityProvider.size() == 1) {
            this.securityKey = this.securityProvider.get(0).getSecurityKey();
        } else {
            this.securityKey = -1;
        }

        if (lastCode != this.securityKey) {
            this.getGrid().postEvent(new MENetworkSecurityChange());
            for (final IGridNode n : this.getGrid().getNodes()) {
                ((GridNode) n).setLastSecurityKey(this.securityKey);
            }
        }
    }

    @Override
    public void addNode(final IGridNode gridNode, final IGridHost machine) {
        if (machine instanceof ISecurityProvider) {
            this.securityProvider.add((ISecurityProvider) machine);
            this.updateSecurityKey();
        } else {
            ((GridNode) gridNode).setLastSecurityKey(this.securityKey);
        }
    }

    @Override
    public void onSplit(final IGridStorage destinationStorage) {

    }

    @Override
    public void onJoin(final IGridStorage sourceStorage) {

    }

    @Override
    public void populateGridStorage(final IGridStorage destinationStorage) {

    }

    @Override
    public boolean isAvailable() {
        return this.securityProvider.size() == 1 && this.securityProvider.get(0).isSecurityEnabled();
    }

    @Override
    public boolean hasPermission(final EntityPlayer player, final SecurityPermissions perm) {
        Preconditions.checkNotNull(player);
        Preconditions.checkNotNull(perm);

        final GameProfile profile = player.getGameProfile();
        final int playerID = WorldData.instance().playerData().getPlayerID(profile);

        return this.hasPermission(playerID, perm);
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
