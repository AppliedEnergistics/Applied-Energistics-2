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


import appeng.api.config.Actionable;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.energy.IEnergyGridProvider;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartModel;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.core.AppEng;
import appeng.items.parts.PartModels;
import appeng.me.GridAccessException;
import appeng.me.helpers.AENetworkProxy;
import appeng.parts.AEBasePart;
import appeng.parts.PartModel;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;


public class PartQuartzFiber extends AEBasePart implements IEnergyGridProvider {

    @PartModels
    private static final IPartModel MODELS = new PartModel(new ResourceLocation(AppEng.MOD_ID, "part/quartz_fiber"));

    private final AENetworkProxy outerProxy = new AENetworkProxy(this, "outer", this.getProxy().getMachineRepresentation(), true);

    public PartQuartzFiber(final ItemStack is) {
        super(is);
        this.getProxy().setIdlePowerUsage(0);
        this.getProxy().setFlags(GridFlags.CANNOT_CARRY);
        this.outerProxy.setIdlePowerUsage(0);
        this.outerProxy.setFlags(GridFlags.CANNOT_CARRY);
    }

    @Override
    public AECableType getCableConnectionType(final AEPartLocation dir) {
        return AECableType.GLASS;
    }

    @Override
    public void getBoxes(final IPartCollisionHelper bch) {
        bch.addBox(6, 6, 10, 10, 10, 16);
    }

    @Override
    public void readFromNBT(final NBTTagCompound extra) {
        super.readFromNBT(extra);
        this.outerProxy.readFromNBT(extra);
    }

    @Override
    public void writeToNBT(final NBTTagCompound extra) {
        super.writeToNBT(extra);
        this.outerProxy.writeToNBT(extra);
    }

    @Override
    public void removeFromWorld() {
        super.removeFromWorld();
        this.outerProxy.invalidate();
    }

    @Override
    public void addToWorld() {
        super.addToWorld();
        this.outerProxy.onReady();
    }

    @Override
    public void setPartHostInfo(final AEPartLocation side, final IPartHost host, final TileEntity tile) {
        super.setPartHostInfo(side, host, tile);
        this.outerProxy.setValidSides(EnumSet.of(side.getFacing()));
    }

    @Override
    public IGridNode getExternalFacingNode() {
        return this.outerProxy.getNode();
    }

    @Override
    public float getCableConnectionLength(AECableType cable) {
        return 16;
    }

    @Override
    public void onPlacement(final EntityPlayer player, final EnumHand hand, final ItemStack held, final AEPartLocation side) {
        super.onPlacement(player, hand, held, side);
        this.outerProxy.setOwner(player);
    }

    @Override
    public Collection<IEnergyGridProvider> providers() {
        Collection<IEnergyGridProvider> providers = new ArrayList<>();

        try {
            final IEnergyGrid eg = this.getProxy().getEnergy();

            providers.add(eg);
        } catch (final GridAccessException e) {
            // :P
        }

        try {
            final IEnergyGrid eg = this.outerProxy.getEnergy();

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

    @Override
    public IPartModel getStaticModels() {
        return MODELS;
    }
}
