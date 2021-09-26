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

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.energy.IEnergyGridProvider;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartModel;
import appeng.api.util.AECableType;
import appeng.core.AppEng;
import appeng.items.parts.PartModels;
import appeng.me.service.EnergyService;
import appeng.parts.AEBasePart;
import appeng.parts.PartModel;

public class QuartzFiberPart extends AEBasePart {

    @PartModels
    private static final IPartModel MODELS = new PartModel(new ResourceLocation(AppEng.MOD_ID, "part/quartz_fiber"));

    private final IManagedGridNode outerNode;

    public QuartzFiberPart(final ItemStack is) {
        super(is);
        var energyBridge = new GridBridgeProvider();
        this.getMainNode()
                .setIdlePowerUsage(0)
                .setFlags(GridFlags.CANNOT_CARRY)
                .addService(IEnergyGridProvider.class, energyBridge);
        this.outerNode = AEApi.grid().createManagedNode(this, NodeListener.INSTANCE)
                .setTagName("outer")
                .setIdlePowerUsage(0)
                .setVisualRepresentation(is)
                .setFlags(GridFlags.CANNOT_CARRY)
                .setInWorldNode(true)
                .addService(IEnergyGridProvider.class, energyBridge);
    }

    @Override
    public void getBoxes(final IPartCollisionHelper bch) {
        bch.addBox(6, 6, 10, 10, 10, 16);
    }

    @Override
    public void readFromNBT(final CompoundTag extra) {
        super.readFromNBT(extra);
        this.outerNode.loadFromNBT(extra);
    }

    @Override
    public void writeToNBT(final CompoundTag extra) {
        super.writeToNBT(extra);
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
    public void setPartHostInfo(final Direction side, final IPartHost host, final BlockEntity blockEntity) {
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
    public void onPlacement(final Player player, final InteractionHand hand, final ItemStack held,
            final Direction side) {
        super.onPlacement(player, hand, held, side);
        this.outerNode.setOwningPlayer(player);
    }

    @Override
    public IPartModel getStaticModels() {
        return MODELS;
    }

    /**
     * A provider of energy grids that makes both connected energy grids accessible to each other.
     */
    private class GridBridgeProvider implements IEnergyGridProvider {

        @Override
        public Collection<IEnergyGridProvider> providers() {
            var providers = new ArrayList<IEnergyGridProvider>(2);

            getMainNode().ifPresent(grid -> {
                var eg = (EnergyService) grid.getEnergyService();
                providers.add(eg);
            });

            outerNode.ifPresent(grid -> {
                var eg = (EnergyService) grid.getEnergyService();
                providers.add(eg);
            });

            return providers;
        }

        @Override
        public double extractProviderPower(final double amt, final Actionable mode) {
            return 0;
        }

        @Override
        public double injectProviderPower(final double amt, final Actionable mode) {
            return amt;
        }

        @Override
        public double getProviderEnergyDemand(final double amt) {
            return 0;
        }

        @Override
        public double getProviderStoredEnergy() {
            return 0;
        }

        @Override
        public double getProviderMaxEnergy() {
            return 0;
        }

    }

}
