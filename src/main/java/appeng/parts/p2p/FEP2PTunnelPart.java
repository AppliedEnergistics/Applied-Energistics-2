/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2017, AlgorithmX2, All rights reserved.
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

package appeng.parts.p2p;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;

import appeng.api.config.PowerUnits;
import appeng.api.parts.IPartModel;
import appeng.capabilities.Capabilities;
import appeng.items.parts.PartModels;
import appeng.me.GridAccessException;

public class FEP2PTunnelPart extends P2PTunnelPart<FEP2PTunnelPart> {
    private static final P2PModels MODELS = new P2PModels("part/p2p/p2p_tunnel_fe");
    private static final IEnergyStorage NULL_ENERGY_STORAGE = new NullEnergyStorage();
    private final IEnergyStorage inputHandler = new InputEnergyStorage();
    private final IEnergyStorage outputHandler = new OutputEnergyStorage();

    public FEP2PTunnelPart(ItemStack is) {
        super(is);
    }

    @Override
    protected float getPowerDrainPerTick() {
        return 2.0f;
    }

    @PartModels
    public static List<IPartModel> getModels() {
        return MODELS.getModels();
    }

    @Override
    public IPartModel getStaticModels() {
        return MODELS.getModel(this.isPowered(), this.isActive());
    }

    @Override
    public void onTunnelNetworkChange() {
        this.getHost().notifyNeighbors();
    }

    private IEnergyStorage getAttachedEnergyStorage() {
        LazyOptional<IEnergyStorage> energyStorageOpt = LazyOptional.empty();
        if (this.isActive()) {
            final TileEntity self = this.getTile();
            final TileEntity te = self.getWorld().getTileEntity(self.getPos().offset(this.getSide().getFacing()));

            if (te != null) {
                energyStorageOpt = te.getCapability(Capabilities.FORGE_ENERGY,
                        this.getSide().getOpposite().getFacing());
            }
        }
        return energyStorageOpt.orElse(NULL_ENERGY_STORAGE);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability) {
        if (capability == Capabilities.FORGE_ENERGY) {
            if (this.isOutput()) {
                return (LazyOptional<T>) LazyOptional.of(() -> this.outputHandler);
            }
            return (LazyOptional<T>) LazyOptional.of(() -> this.inputHandler);
        }
        return super.getCapability(capability);
    }

    private class InputEnergyStorage implements IEnergyStorage {
        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            return 0;
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            int total = 0;

            try {
                final int outputTunnels = FEP2PTunnelPart.this.getOutputs().size();

                if (outputTunnels == 0 | maxReceive == 0) {
                    return 0;
                }

                final int amountPerOutput = maxReceive / outputTunnels;
                int overflow = amountPerOutput == 0 ? maxReceive : maxReceive % amountPerOutput;

                for (FEP2PTunnelPart target : FEP2PTunnelPart.this.getOutputs()) {
                    final IEnergyStorage output = target.getAttachedEnergyStorage();
                    final int toSend = amountPerOutput + overflow;
                    final int received = output.receiveEnergy(toSend, simulate);

                    overflow = toSend - received;
                    total += received;
                }

                if (!simulate) {
                    FEP2PTunnelPart.this.queueTunnelDrain(PowerUnits.RF, total);
                }
            } catch (GridAccessException ignored) {
            }

            return total;
        }

        @Override
        public boolean canExtract() {
            return false;
        }

        @Override
        public boolean canReceive() {
            return true;
        }

        @Override
        public int getMaxEnergyStored() {
            int total = 0;

            try {
                for (FEP2PTunnelPart t : FEP2PTunnelPart.this.getOutputs()) {
                    total += t.getAttachedEnergyStorage().getMaxEnergyStored();
                }
            } catch (GridAccessException e) {
                return 0;
            }

            return total;
        }

        @Override
        public int getEnergyStored() {
            int total = 0;

            try {
                for (FEP2PTunnelPart t : FEP2PTunnelPart.this.getOutputs()) {
                    total += t.getAttachedEnergyStorage().getEnergyStored();
                }
            } catch (GridAccessException e) {
                return 0;
            }

            return total;
        }
    }

    private class OutputEnergyStorage implements IEnergyStorage {
        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            final int total = FEP2PTunnelPart.this.getAttachedEnergyStorage().extractEnergy(maxExtract, simulate);

            if (!simulate) {
                FEP2PTunnelPart.this.queueTunnelDrain(PowerUnits.RF, total);
            }

            return total;
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            return 0;
        }

        @Override
        public boolean canExtract() {
            return FEP2PTunnelPart.this.getAttachedEnergyStorage().canExtract();
        }

        @Override
        public boolean canReceive() {
            return false;
        }

        @Override
        public int getMaxEnergyStored() {
            return FEP2PTunnelPart.this.getAttachedEnergyStorage().getMaxEnergyStored();
        }

        @Override
        public int getEnergyStored() {
            return FEP2PTunnelPart.this.getAttachedEnergyStorage().getEnergyStored();
        }
    }

    private static class NullEnergyStorage implements IEnergyStorage {

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            return 0;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            return 0;
        }

        @Override
        public int getEnergyStored() {
            return 0;
        }

        @Override
        public int getMaxEnergyStored() {
            return 0;
        }

        @Override
        public boolean canExtract() {
            return false;
        }

        @Override
        public boolean canReceive() {
            return false;
        }
    }
}
