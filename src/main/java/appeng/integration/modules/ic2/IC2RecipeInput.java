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

package appeng.integration.modules.ic2;


import ic2.api.recipe.IRecipeInput;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;


/**
 * Implementation of IRecipeInput for the macerator recipe.
 *
 * @author GuntherDW
 */
public class IC2RecipeInput implements IRecipeInput {
    @Nonnull
    private final ItemStack itemstack;
    private final int amount;

    public IC2RecipeInput(ItemStack in, int amount) {
        this.itemstack = in;
        this.amount = amount;
    }

    @Override
    public boolean matches(ItemStack itemStack) {
        return this.itemstack.isItemEqual(itemStack);
    }

    @Override
    public int getAmount() {
        return this.amount;
    }

    @Override
    public List<ItemStack> getInputs() {
        return Collections.unmodifiableList(Collections.singletonList(this.itemstack));
    }
}
