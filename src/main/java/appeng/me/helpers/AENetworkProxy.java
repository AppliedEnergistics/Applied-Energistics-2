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

package appeng.me.helpers;

import java.util.Collections;
import java.util.EnumSet;

import com.mojang.authlib.GameProfile;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;

import appeng.api.networking.GridFlags;
import appeng.api.networking.GridNotification;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridBlock;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.events.MENetworkPowerIdleChange;
import appeng.api.networking.pathing.IPathingGrid;
import appeng.api.networking.security.ISecurityGrid;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.networking.ticking.ITickManager;
import appeng.api.util.AEColor;
import appeng.api.util.DimensionalCoord;
import appeng.api.util.IOrientable;
import appeng.core.Api;
import appeng.core.worlddata.WorldData;
import appeng.hooks.TickHandler;
import appeng.me.GridAccessException;
import appeng.me.cache.P2PCache;
import appeng.parts.networking.CablePart;
import appeng.tile.AEBaseTileEntity;
import appeng.util.Platform;

public class AENetworkProxy implements IGridBlock {

    private final IGridProxyable gp;
    private final boolean worldNode;
    private final String nbtName; // name
    private AEColor myColor = AEColor.TRANSPARENT;
    private CompoundNBT data = null; // input
    private ItemStack myRepInstance = ItemStack.EMPTY;
    private boolean isReady = false;
    private IGridNode node = null;
    private EnumSet<Direction> validSides;
    private EnumSet<GridFlags> flags = EnumSet.noneOf(GridFlags.class);
    private double idleDraw = 1.0;
    private PlayerEntity owner;

    public AENetworkProxy(final IGridProxyable te, final String nbtName, final ItemStack visual,
            final boolean inWorld) {
        this.gp = te;
        this.nbtName = nbtName;
        this.worldNode = inWorld;
        this.myRepInstance = visual;
        this.validSides = EnumSet.allOf(Direction.class);
    }

    public void setVisualRepresentation(final ItemStack is) {
        this.myRepInstance = is;
    }

    public void writeToNBT(final CompoundNBT tag) {
        if (this.node != null) {
            this.node.saveToNBT(this.nbtName, tag);
        }
    }

    public void setValidSides(final EnumSet<Direction> validSides) {
        this.validSides = validSides;
        if (this.node != null) {
            this.node.updateState();
        }
    }

    public void validate() {
        if (this.gp instanceof AEBaseTileEntity) {
            TickHandler.instance().addInit((AEBaseTileEntity) this.gp);
        }
    }

    public void onChunkUnloaded() {
        this.isReady = false;
        this.remove();
    }

    public void remove() {
        this.isReady = false;
        if (this.node != null) {
            this.node.destroy();
            this.node = null;
        }
    }

    public void onReady() {
        this.isReady = true;

        // send orientation based directionality to the node.
        if (this.gp instanceof IOrientable) {
            final IOrientable ori = (IOrientable) this.gp;
            if (ori.canBeRotated()) {
                ori.setOrientation(ori.getForward(), ori.getUp());
            }
        }

        this.getNode();
    }

    public IGridNode getNode() {
        if (this.node == null && Platform.isServer() && this.isReady) {
            this.node = Api.instance().grid().createGridNode(this);
            this.readFromNBT(this.data);
            this.node.updateState();
        }

        return this.node;
    }

    public void readFromNBT(final CompoundNBT tag) {
        this.data = tag;
        if (this.node != null && this.data != null) {
            this.node.loadFromNBT(this.nbtName, this.data);
            this.data = null;
        } else if (this.node != null && this.owner != null) {
            final GameProfile profile = this.owner.getGameProfile();
            final int playerID = WorldData.instance().playerData().getMePlayerId(profile);

            this.node.setPlayerID(playerID);
            this.owner = null;
        }
    }

    public IPathingGrid getPath() throws GridAccessException {
        final IGrid grid = this.getGrid();
        if (grid == null) {
            throw new GridAccessException();
        }
        final IPathingGrid pg = grid.getCache(IPathingGrid.class);
        if (pg == null) {
            throw new GridAccessException();
        }
        return pg;
    }

    /**
     * short cut!
     *
     * @return grid of node
     * @throws GridAccessException of node or grid is null
     */
    public IGrid getGrid() throws GridAccessException {
        if (this.node == null) {
            throw new GridAccessException();
        }
        final IGrid grid = this.node.getGrid();
        if (grid == null) {
            throw new GridAccessException();
        }
        return grid;
    }

    public ITickManager getTick() throws GridAccessException {
        final IGrid grid = this.getGrid();
        if (grid == null) {
            throw new GridAccessException();
        }
        final ITickManager pg = grid.getCache(ITickManager.class);
        if (pg == null) {
            throw new GridAccessException();
        }
        return pg;
    }

    public IStorageGrid getStorage() throws GridAccessException {
        final IGrid grid = this.getGrid();
        if (grid == null) {
            throw new GridAccessException();
        }

        final IStorageGrid pg = grid.getCache(IStorageGrid.class);

        if (pg == null) {
            throw new GridAccessException();
        }

        return pg;
    }

    public P2PCache getP2P() throws GridAccessException {
        final IGrid grid = this.getGrid();
        if (grid == null) {
            throw new GridAccessException();
        }

        final P2PCache pg = grid.getCache(P2PCache.class);

        if (pg == null) {
            throw new GridAccessException();
        }

        return pg;
    }

    public ISecurityGrid getSecurity() throws GridAccessException {
        final IGrid grid = this.getGrid();
        if (grid == null) {
            throw new GridAccessException();
        }

        final ISecurityGrid sg = grid.getCache(ISecurityGrid.class);

        if (sg == null) {
            throw new GridAccessException();
        }

        return sg;
    }

    public ICraftingGrid getCrafting() throws GridAccessException {
        final IGrid grid = this.getGrid();
        if (grid == null) {
            throw new GridAccessException();
        }

        final ICraftingGrid sg = grid.getCache(ICraftingGrid.class);

        if (sg == null) {
            throw new GridAccessException();
        }

        return sg;
    }

    @Override
    public double getIdlePowerUsage() {
        return this.idleDraw;
    }

    @Override
    public EnumSet<GridFlags> getFlags() {
        return this.flags;
    }

    @Override
    public boolean isWorldAccessible() {
        return this.worldNode;
    }

    @Override
    public DimensionalCoord getLocation() {
        return this.gp.getLocation();
    }

    @Override
    public AEColor getGridColor() {
        return this.getColor();
    }

    @Override
    public void onGridNotification(final GridNotification notification) {
        if (notification == GridNotification.OWNER_CHANGED) {
            gp.saveChanges();
            return;
        }

        if (this.gp instanceof CablePart) {
            ((CablePart) this.gp).markForUpdate();
        }
    }

    @Override
    public EnumSet<Direction> getConnectableSides() {
        return this.validSides;
    }

    @Override
    public IGridHost getMachine() {
        return this.gp;
    }

    @Override
    public void gridChanged() {
        this.gp.gridChanged();
    }

    @Override
    public ItemStack getMachineRepresentation() {
        return this.myRepInstance;
    }

    public void setFlags(final GridFlags... requireChannel) {
        final EnumSet<GridFlags> flags = EnumSet.noneOf(GridFlags.class);

        Collections.addAll(flags, requireChannel);

        this.flags = flags;
    }

    public void setIdlePowerUsage(final double idle) {
        this.idleDraw = idle;

        if (this.node != null) {
            try {
                final IGrid g = this.getGrid();
                g.postEvent(new MENetworkPowerIdleChange(this.node));
            } catch (final GridAccessException e) {
                // not ready for this yet..
            }
        }
    }

    public boolean isReady() {
        return this.isReady;
    }

    public boolean isActive() {
        if (this.node == null) {
            return false;
        }

        return this.node.isActive();
    }

    public boolean isPowered() {
        try {
            return this.getEnergy().isNetworkPowered();
        } catch (final GridAccessException e) {
            return false;
        }
    }

    public IEnergyGrid getEnergy() throws GridAccessException {
        final IGrid grid = this.getGrid();
        if (grid == null) {
            throw new GridAccessException();
        }
        final IEnergyGrid eg = grid.getCache(IEnergyGrid.class);
        if (eg == null) {
            throw new GridAccessException();
        }
        return eg;
    }

    public void setOwner(final PlayerEntity player) {
        this.owner = player;
    }

    public AEColor getColor() {
        return this.myColor;
    }

    public void setColor(final AEColor myColor) {
        this.myColor = myColor;
    }
}
