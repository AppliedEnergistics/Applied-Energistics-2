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

package appeng.core.features;


import com.google.common.base.Preconditions;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;


public class BlockStackSrc implements IStackSrc {

    private final Block block;
    private final int damage;
    private final boolean enabled;

    public BlockStackSrc(final Block block, final int damage, final ActivityState state) {
        Preconditions.checkNotNull(block);
        Preconditions.checkArgument(damage >= 0);
        Preconditions.checkNotNull(state);
        Preconditions.checkArgument(state == ActivityState.Enabled || state == ActivityState.Disabled);

        this.block = block;
        this.damage = damage;
        this.enabled = state == ActivityState.Enabled;
    }

    @Nullable
    @Override
    public ItemStack stack(final int i) {
        return new ItemStack(this.block, i, this.damage);
    }

    @Override
    public Item getItem() {
        return null;
    }

    @Override
    public int getDamage() {
        return this.damage;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }
}
