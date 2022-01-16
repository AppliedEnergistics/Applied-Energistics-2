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

package appeng.debug;

import com.google.common.math.IntMath;

import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import team.reborn.energy.api.EnergyStorage;

import appeng.blockentity.AEBaseBlockEntity;
import appeng.blockentity.ServerTickingBlockEntity;

public class EnergyGeneratorBlockEntity extends AEBaseBlockEntity implements ServerTickingBlockEntity, EnergyStorage {
    /**
     * The base energy injected each tick. Adjacent energy generators will increase it to pow(base, #generators).
     */
    private static final int BASE_ENERGY = 8;

    public EnergyGeneratorBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
    }

    @Override
    public void serverTick() {
        Level level = this.getLevel();

        int tier = 1;
        for (Direction facing : Direction.values()) {
            final BlockEntity te = level.getBlockEntity(this.getBlockPos().relative(facing));

            if (te instanceof EnergyGeneratorBlockEntity) {
                tier++;
            }
        }

        final int energyToInsert = IntMath.pow(BASE_ENERGY, tier);

        for (Direction facing : Direction.values()) {
            EnergyStorage consumer = EnergyStorage.SIDED.find(getLevel(), getBlockPos().relative(facing),
                    facing.getOpposite());
            if (consumer != null) {
                try (var tx = Transaction.openOuter()) {
                    consumer.insert(energyToInsert, tx);
                    tx.commit();
                }
            }
        }
    }

    @Override
    public long insert(long maxAmount, TransactionContext transaction) {
        return 0;
    }

    @Override
    public long extract(long maxAmount, TransactionContext transaction) {
        return maxAmount;
    }

    @Override
    public long getAmount() {
        return Long.MAX_VALUE;
    }

    @Override
    public long getCapacity() {
        return Long.MAX_VALUE;
    }

    @Override
    public boolean supportsInsertion() {
        return false;
    }
}
