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

package appeng.parts.networking;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

import appeng.api.networking.GridFlags;
import appeng.api.networking.GridHelper;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.api.util.AECableType;
import appeng.core.AppEng;
import appeng.items.parts.PartModels;
import appeng.me.energy.IEnergyOverlayGridConnection;
import appeng.me.service.EnergyService;
import appeng.parts.AEBasePart;
import appeng.parts.PartModel;

/**
 * A quartz fiber consists of two grid nodes which are not connected directly.
 * <p/>
 * Both grid nodes expose the energy services of both by providing an {@link IEnergyOverlayGridConnection} service.
 * <p/>
 * Since the quart fiber is part of both grids, removing it will also invalidate the overlay energy grid of both sides
 * when it is added or removed from a grid.
 */
public class QuartzFiberPart extends AEBasePart {

    @PartModels
    private static final IPartModel MODELS = new PartModel(new ResourceLocation(AppEng.MOD_ID, "part/quartz_fiber"));

    private final IManagedGridNode outerNode;

    public QuartzFiberPart(IPartItem<?> partItem) {
        super(partItem);
        this.getMainNode()
                .setIdlePowerUsage(0)
                .setFlags(GridFlags.CANNOT_CARRY)
                // Expose the energy service of the outer-node on the main-node
                .addService(IEnergyOverlayGridConnection.class, this::getTheirEnergyServices);
        this.outerNode = GridHelper.createManagedNode(this, NodeListener.INSTANCE)
                .setTagName("outer")
                .setIdlePowerUsage(0)
                .setVisualRepresentation(partItem)
                .setFlags(GridFlags.CANNOT_CARRY)
                .setInWorldNode(true)
                // Expose the energy service of the main-node on the outer-node
                .addService(IEnergyOverlayGridConnection.class, this::getOurEnergyServices);
    }

    private List<EnergyService> getOurEnergyServices() {
        var grid = Objects.requireNonNull(getMainNode().getGrid());
        return Collections.singletonList((EnergyService) grid.getService(IEnergyService.class));
    }

    private List<EnergyService> getTheirEnergyServices() {
        var grid = Objects.requireNonNull(outerNode.getGrid());
        return Collections.singletonList((EnergyService) grid.getService(IEnergyService.class));
    }

    @Override
    public void getBoxes(IPartCollisionHelper bch) {
        bch.addBox(6, 6, 10, 10, 10, 16);
    }

    @Override
    public void readFromNBT(CompoundTag extra, HolderLookup.Provider registries) {
        super.readFromNBT(extra, registries);
        this.outerNode.loadFromNBT(extra);
    }

    @Override
    public void writeToNBT(CompoundTag extra, HolderLookup.Provider registries) {
        super.writeToNBT(extra, registries);
        this.outerNode.saveToNBT(extra);
    }

    @Override
    public void removeFromWorld() {
        super.removeFromWorld();
        this.outerNode.destroy();
    }

    @Override
    public void addToWorld() {
        super.addToWorld();
        this.outerNode.create(getLevel(), getBlockEntity().getBlockPos());
    }

    @Override
    public void setPartHostInfo(Direction side, IPartHost host, BlockEntity blockEntity) {
        super.setPartHostInfo(side, host, blockEntity);
        this.outerNode.setExposedOnSides(EnumSet.of(side));
    }

    @Override
    public IGridNode getExternalFacingNode() {
        return this.outerNode.getNode();
    }

    @Override
    public float getCableConnectionLength(AECableType cable) {
        return 16;
    }

    @Override
    public void onPlacement(Player player) {
        super.onPlacement(player);
        this.outerNode.setOwningPlayer(player);
    }

    @Override
    public IPartModel getStaticModels() {
        return MODELS;
    }

}
