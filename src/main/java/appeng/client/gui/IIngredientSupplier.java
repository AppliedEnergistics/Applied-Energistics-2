/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
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

package appeng.client.gui;

import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;

import alexiil.mc.lib.attributes.fluid.volume.FluidVolume;

/**
 * Allows a widget to expose an ingredient for use with the JEI integration. This is used to allow players to hover
 * non-item slots and press R or U to show related recipes.
 */
public interface IIngredientSupplier {

    /**
     * @return If this widget contains an item, return it for the purposes of JEI integration.
     */
    @Nullable
    default ItemStack getItemIngredient() {
        return null;
    }

    /**
     * @return If this widget contains a fluid, return it for the purposes of JEI integration.
     */
    @Nullable
    default FluidVolume getFluidIngredient() {
        return null;
    }

}
