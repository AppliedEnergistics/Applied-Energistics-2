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

import javax.annotation.Nullable;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.world.server.ServerWorld;

import dev.technici4n.fasttransferlib.api.Simulation;
import dev.technici4n.fasttransferlib.api.energy.EnergyApi;
import dev.technici4n.fasttransferlib.api.energy.EnergyIo;
import dev.technici4n.fasttransferlib.api.energy.EnergyPreconditions;

import appeng.api.config.PowerUnits;
import appeng.api.parts.IPartModel;
import appeng.api.parts.PartApiLookup;
import appeng.items.parts.PartModels;
import appeng.me.GridAccessException;

public class FtlP2PTunnelPart extends P2PTunnelPart<FtlP2PTunnelPart> {
    private static final P2PModels MODELS = new P2PModels("part/p2p/p2p_tunnel_energy");
    private final EnergyIo inputIo = new InputEnergyIo();
    private final EnergyIo outputIo = new OutputEnergyIo();
    private BlockApiCache<EnergyIo, Direction> adjacentCache = null;

    public static final PartApiLookup.PartApiProvider<EnergyIo, Direction, FtlP2PTunnelPart> API_PROVIDER = (tunnel,
            dir) -> {
        if (tunnel.isOutput()) {
            return tunnel.outputIo;
        } else {
            return tunnel.inputIo;
        }
    };

    public FtlP2PTunnelPart(ItemStack is) {
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

    private EnergyIo getAttachedEnergyIo() {
        if (!isActive())
            return EnergyApi.EMPTY;

        if (adjacentCache == null) {
            final TileEntity self = this.getTile();
            adjacentCache = BlockApiCache.create(EnergyApi.SIDED, (ServerWorld) self.getWorld(),
                    self.getPos().offset(getSide().getFacing()));
        }
        @Nullable
        EnergyIo adjacentIo = adjacentCache.find(getSide().getOpposite().getFacing());

        return adjacentIo != null ? adjacentIo : EnergyApi.EMPTY;
    }

    private class InputEnergyIo implements EnergyIo {
        @Override
        public double insert(double amount, Simulation simulation) {
            EnergyPreconditions.notNegative(amount);

            double total = 0;

            try {
                int outputTunnels = getOutputs().size();

                if (outputTunnels == 0 || amount < 1e-9) {
                    return amount;
                }

                double amountPerOutput = amount / outputTunnels;
                double overflow = 0;

                for (FtlP2PTunnelPart target : getOutputs()) {
                    EnergyIo output = target.getAttachedEnergyIo();
                    double toSend = amountPerOutput + overflow;
                    double received = toSend - output.insert(toSend, simulation);

                    overflow += toSend - received;
                    total += received;
                }

                if (simulation.isActing()) {
                    queueTunnelDrain(PowerUnits.TR, total);
                }
            } catch (GridAccessException ignored) {
            }

            return amount - total;
        }

        @Override
        public boolean supportsExtraction() {
            return false;
        }

        @Override
        public double getEnergy() {
            double total = 0;

            try {
                for (FtlP2PTunnelPart t : getOutputs()) {
                    total += t.getAttachedEnergyIo().getEnergy();
                }
            } catch (GridAccessException ignored) {
                return 0;
            }

            return total;
        }

        @Override
        public double getEnergyCapacity() {
            double total = 0;

            try {
                for (FtlP2PTunnelPart t : getOutputs()) {
                    total += t.getAttachedEnergyIo().getEnergyCapacity();
                }
            } catch (GridAccessException ignored) {
                return 0;
            }

            return total;
        }
    }

    private class OutputEnergyIo implements EnergyIo {
        @Override
        public double extract(double maxAmount, Simulation simulation) {
            double extracted = getAttachedEnergyIo().extract(maxAmount, simulation);

            if (simulation.isActing()) {
                queueTunnelDrain(PowerUnits.TR, extracted);
            }

            return extracted;
        }

        @Override
        public boolean supportsInsertion() {
            return false;
        }

        @Override
        public boolean supportsExtraction() {
            return getAttachedEnergyIo().supportsExtraction();
        }

        @Override
        public double getEnergy() {
            return getAttachedEnergyIo().getEnergy();
        }

        @Override
        public double getEnergyCapacity() {
            return getAttachedEnergyIo().getEnergyCapacity();
        }
    }
}
