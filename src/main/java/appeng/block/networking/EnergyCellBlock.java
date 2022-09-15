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

package appeng.block.networking;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.block.AEBaseEntityBlock;
import appeng.blockentity.networking.EnergyCellBlockEntity;
import appeng.helpers.AEMaterials;

public class EnergyCellBlock extends AEBaseEntityBlock<EnergyCellBlockEntity> {

    public static final int MAX_FULLNESS = 4;

    public static final IntegerProperty ENERGY_STORAGE = IntegerProperty.create("fullness", 0, MAX_FULLNESS);

    private final double maxPower;
    private final double chargeRate;
    private final int priority;

    public EnergyCellBlock(double maxPower, double chargeRate, int priority) {
        super(defaultProps(AEMaterials.GLASS));
        this.maxPower = maxPower;
        this.chargeRate = chargeRate;
        this.priority = priority;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> itemStacks) {
        super.fillItemCategory(group, itemStacks);

        final ItemStack charged = new ItemStack(this, 1);
        final CompoundTag tag = charged.getOrCreateTag();
        tag.putDouble("internalCurrentPower", this.getMaxPower());
        tag.putDouble("internalMaxPower", this.getMaxPower());

        itemStacks.add(charged);
    }

    public double getMaxPower() {
        return this.maxPower;
    }

    public double getChargeRate() {
        return this.chargeRate;
    }

    public int getPriority() {
        return this.priority;
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ENERGY_STORAGE);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        var cell = getBlockEntity(level, pos);
        if (cell != null) {
            var currentPower = cell.getAECurrentPower();
            var maxPower = cell.getAEMaxPower();
            var fillFactor = currentPower / maxPower;
            return Mth.floor(fillFactor * 14.0F) + (currentPower > 0 ? 1 : 0);
        }
        return 0;
    }
}
