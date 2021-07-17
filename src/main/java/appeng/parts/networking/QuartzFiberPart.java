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

import appeng.me.service.EnergyGridService;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;

import appeng.api.config.Actionable;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGridProvider;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartModel;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.core.AppEng;
import appeng.items.parts.PartModels;
import appeng.me.GridAccessException;
import appeng.me.ManagedGridNode;
import appeng.parts.AEBasePart;
import appeng.parts.PartModel;

public class QuartzFiberPart extends AEBasePart {

    @PartModels
    private static final IPartModel MODELS = new PartModel(new ResourceLocation(AppEng.MOD_ID, "part/quartz_fiber"));

    private final ManagedGridNode outerNode;

    public QuartzFiberPart(final ItemStack is) {
        super(is);
        var energyBridge = new GridBridgeProvider();
        this.getMainNode()
                .setIdlePowerUsage(0)
                .setFlags(GridFlags.CANNOT_CARRY)
                .addService(IEnergyGridProvider.class, energyBridge);
        this.outerNode = new ManagedGridNode(this, NodeListener.INSTANCE)
                .setTagName("outer")
                .setIdlePowerUsage(0)
                .setVisualRepresentation(is)
                .setFlags(GridFlags.CANNOT_CARRY)
                .addService(IEnergyGridProvider.class, energyBridge);
    }

    @Override
    public void getBoxes(final IPartCollisionHelper bch) {
        bch.addBox(6, 6, 10, 10, 10, 16);
    }

    @Override
    public void readFromNBT(final CompoundNBT extra) {
        super.readFromNBT(extra);
        this.outerNode.readFromNBT(extra);
    }

    @Override
    public void writeToNBT(final CompoundNBT extra) {
        super.writeToNBT(extra);
        this.outerNode.writeToNBT(extra);
    }

    @Override
    public void removeFromWorld() {
        super.removeFromWorld();
        this.outerNode.remove();
    }

    @Override
    public void addToWorld() {
        super.addToWorld();
        this.outerNode.create(getWorld(), getTile().getPos());
    }

    @Override
    public void setPartHostInfo(final AEPartLocation side, final IPartHost host, final TileEntity tile) {
        super.setPartHostInfo(side, host, tile);
        this.outerNode.setExposedOnSides(EnumSet.of(side.getDirection()));
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
    public void onPlacement(final PlayerEntity player, final Hand hand, final ItemStack held,
            final AEPartLocation side) {
        super.onPlacement(player, hand, held, side);
        this.outerNode.setOwner(player);
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

            try {
                var eg = (EnergyGridService) getMainNode().getEnergy();
                providers.add(eg);
            } catch (final GridAccessException e) {
                // :P
            }

            try {
                var eg = (EnergyGridService) outerNode.getEnergy();
                providers.add(eg);
            } catch (final GridAccessException e) {
                // :P
            }

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
