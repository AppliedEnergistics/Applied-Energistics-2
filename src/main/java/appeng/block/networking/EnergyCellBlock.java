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
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.NonNullList;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.block.AEBaseTileBlock;
import appeng.helpers.AEMaterials;
import appeng.tile.networking.EnergyCellTileEntity;

public class EnergyCellBlock extends AEBaseTileBlock<EnergyCellTileEntity> {

    public static final int MAX_FULLNESS = 4;

    public static final IntegerProperty ENERGY_STORAGE = IntegerProperty.create("fullness", 0, MAX_FULLNESS);

    public EnergyCellBlock() {
        super(defaultProps(AEMaterials.GLASS));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> itemStacks) {
        super.fillItemGroup(group, itemStacks);

        final ItemStack charged = new ItemStack(this, 1);
        final CompoundNBT tag = charged.getOrCreateTag();
        tag.putDouble("internalCurrentPower", this.getMaxPower());
        tag.putDouble("internalMaxPower", this.getMaxPower());

        itemStacks.add(charged);
    }

    public double getMaxPower() {
        return 200000.0;
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(ENERGY_STORAGE);
    }

}
