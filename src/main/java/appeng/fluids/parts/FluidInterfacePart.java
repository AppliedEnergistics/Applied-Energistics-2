/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2018, AlgorithmX2, All rights reserved.
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

package appeng.fluids.parts;

import java.util.EnumSet;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;

import appeng.api.config.Upgrades;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartModel;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.IStorageMonitorable;
import appeng.api.storage.data.IAEStack;
import appeng.api.util.AECableType;
import appeng.api.util.IConfigManager;
import appeng.container.ContainerLocator;
import appeng.container.ContainerOpener;
import appeng.core.AppEng;
import appeng.core.definitions.AEParts;
import appeng.fluids.container.FluidInterfaceContainer;
import appeng.fluids.helper.DualityFluidInterface;
import appeng.fluids.helper.IConfigurableFluidInventory;
import appeng.fluids.helper.IFluidInterfaceHost;
import appeng.helpers.IPriorityHost;
import appeng.items.parts.PartModels;
import appeng.me.ManagedGridNode;
import appeng.parts.AEBasePart;
import appeng.parts.BasicStatePart;
import appeng.parts.PartModel;

public class FluidInterfacePart extends BasicStatePart
        implements IStorageMonitorable, IFluidInterfaceHost, IPriorityHost, IConfigurableFluidInventory {
    public static final ResourceLocation MODEL_BASE = new ResourceLocation(AppEng.MOD_ID, "part/fluid_interface_base");

    private static final IGridNodeListener<FluidInterfacePart> NODE_LISTENER = new AEBasePart.NodeListener<>() {
        @Override
        public void onGridChanged(FluidInterfacePart nodeOwner, IGridNode node) {
            super.onGridChanged(nodeOwner, node);
            nodeOwner.duality.gridChanged();
        }
    };

    @PartModels
    public static final PartModel MODELS_OFF = new PartModel(MODEL_BASE,
            new ResourceLocation(AppEng.MOD_ID, "part/fluid_interface_off"));

    @PartModels
    public static final PartModel MODELS_ON = new PartModel(MODEL_BASE,
            new ResourceLocation(AppEng.MOD_ID, "part/fluid_interface_on"));

    @PartModels
    public static final PartModel MODELS_HAS_CHANNEL = new PartModel(MODEL_BASE,
            new ResourceLocation(AppEng.MOD_ID, "part/fluid_interface_has_channel"));

    private final DualityFluidInterface duality = new DualityFluidInterface(this.getMainNode(), this);

    public FluidInterfacePart(final ItemStack is) {
        super(is);
    }

    @Override
    protected ManagedGridNode createMainNode() {
        return new ManagedGridNode(this, NODE_LISTENER);
    }

    @Override
    public DualityFluidInterface getDualityFluidInterface() {
        return this.duality;
    }

    @Override
    protected void onMainNodeStateChanged(IGridNodeListener.ActiveChangeReason reason) {
        this.duality.notifyNeighbors();
    }

    @Override
    public void getBoxes(final IPartCollisionHelper bch) {
        bch.addBox(2, 2, 14, 14, 14, 16);
        bch.addBox(5, 5, 12, 11, 11, 14);
    }

    @Override
    public void readFromNBT(final CompoundNBT data) {
        super.readFromNBT(data);
        this.duality.readFromNBT(data);
    }

    @Override
    public void writeToNBT(final CompoundNBT data) {
        super.writeToNBT(data);
        this.duality.writeToNBT(data);
    }

    @Override
    public float getCableConnectionLength(AECableType cable) {
        return 4;
    }

    @Override
    public boolean onPartActivate(final PlayerEntity p, final Hand hand, final Vector3d pos) {
        if (!isRemote()) {
            ContainerOpener.openContainer(FluidInterfaceContainer.TYPE, p, ContainerLocator.forPart(this));
        }

        return true;
    }

    @Override
    public <T extends IAEStack<T>> IMEMonitor<T> getInventory(IStorageChannel<T> channel) {
        return this.duality.getInventory(channel);
    }

    @Override
    public EnumSet<Direction> getTargets() {
        return EnumSet.of(this.getSide().getDirection());
    }

    @Override
    public TileEntity getTileEntity() {
        return super.getHost().getTile();
    }

    @Override
    public IPartModel getStaticModels() {
        if (this.isActive() && this.isPowered()) {
            return MODELS_HAS_CHANNEL;
        } else if (this.isPowered()) {
            return MODELS_ON;
        } else {
            return MODELS_OFF;
        }
    }

    @Override
    public int getPriority() {
        return this.duality.getPriority();
    }

    @Override
    public void setPriority(final int newValue) {
        this.duality.setPriority(newValue);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capabilityClass) {
        return this.duality.getCapability(capabilityClass, this.getSide().getDirection());
    }

    @Override
    public int getInstalledUpgrades(Upgrades u) {
        return this.duality.getInstalledUpgrades(u);
    }

    @Override
    public IConfigManager getConfigManager() {
        return this.duality.getConfigManager();
    }

    @Override
    public IItemHandler getInventoryByName(String name) {
        return this.duality.getInventoryByName(name);
    }

    @Override
    public IFluidHandler getFluidInventoryByName(final String name) {
        return this.duality.getFluidInventoryByName(name);
    }

    @Override
    public ItemStack getItemStackRepresentation() {
        return AEParts.FLUID_INTERFACE.stack();
    }

    @Override
    public ContainerType<?> getContainerType() {
        return FluidInterfaceContainer.TYPE;
    }

    @Override
    public void saveChanges() {
        getHost().markForSave();
    }
}
