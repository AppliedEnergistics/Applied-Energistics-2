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

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.collection.DefaultedList;

import appeng.block.AEBaseTileBlock;
import appeng.helpers.AEMaterials;
import appeng.tile.networking.EnergyCellBlockEntity;

public class EnergyCellBlock extends AEBaseTileBlock<EnergyCellBlockEntity> {

    public static final int MAX_FULLNESS = 4;

    public static final IntProperty ENERGY_STORAGE = IntProperty.of("fullness", 0, MAX_FULLNESS);

    public EnergyCellBlock() {
        super(defaultProps(AEMaterials.GLASS));
    }

    @Override
    public void addStacksForDisplay(ItemGroup group, DefaultedList<ItemStack> itemStacks) {
        super.addStacksForDisplay(group, itemStacks);

        final ItemStack charged = new ItemStack(this, 1);
        final CompoundTag tag = charged.getOrCreateTag();
        tag.putDouble("internalCurrentPower", this.getMaxPower());
        tag.putDouble("internalMaxPower", this.getMaxPower());

        itemStacks.add(charged);
    }

    public double getMaxPower() {
        return 200000.0;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(ENERGY_STORAGE);
    }

}
