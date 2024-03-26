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

package appeng.blockentity.networking;

import java.util.EnumSet;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import appeng.api.networking.energy.IPassiveEnergyGenerator;
import appeng.api.orientation.BlockOrientation;
import appeng.api.orientation.RelativeSide;
import appeng.api.util.AECableType;
import appeng.blockentity.grid.AENetworkBlockEntity;
import appeng.core.AEConfig;

public class CrystalResonanceGeneratorBlockEntity extends AENetworkBlockEntity {
    // This needs to be synchronized to allow visual indication / Jade tooltips
    private boolean suppressed;

    public CrystalResonanceGeneratorBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos,
            BlockState blockState) {
        super(blockEntityType, pos, blockState);
        // we're supposed to be *generating* power, so no consumption
        getMainNode().setIdlePowerUsage(0);
        getMainNode().addService(IPassiveEnergyGenerator.class, new IPassiveEnergyGenerator() {
            @Override
            public double getRate() {
                return AEConfig.instance().getCrystalResonanceGeneratorRate();
            }

            @Override
            public boolean isSuppressed() {
                return CrystalResonanceGeneratorBlockEntity.this.suppressed;
            }

            @Override
            public void setSuppressed(boolean suppressed) {
                if (suppressed != CrystalResonanceGeneratorBlockEntity.this.suppressed) {
                    CrystalResonanceGeneratorBlockEntity.this.suppressed = suppressed;
                    markForUpdate();
                }
            }
        });
    }

    public boolean isSuppressed() {
        return suppressed;
    }

    @Override
    protected boolean readFromStream(RegistryFriendlyByteBuf data) {
        super.readFromStream(data);
        this.suppressed = data.readBoolean();
        return false;
    }

    @Override
    protected void writeToStream(RegistryFriendlyByteBuf data) {
        super.writeToStream(data);
        data.writeBoolean(this.suppressed);
    }

    @Override
    protected void saveVisualState(CompoundTag data) {
        super.saveVisualState(data);
        data.putBoolean("suppressed", this.suppressed);
    }

    @Override
    protected void loadVisualState(CompoundTag data) {
        super.loadVisualState(data);
        this.suppressed = data.getBoolean("suppressed");
    }

    @Override
    public Set<Direction> getGridConnectableSides(BlockOrientation orientation) {
        return EnumSet.of(orientation.getSide(RelativeSide.BACK));
    }

    @Override
    public AECableType getCableConnectionType(Direction dir) {
        return AECableType.SMART;
    }
}
