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

package appeng.fluids.items;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import alexiil.mc.lib.attributes.fluid.volume.FluidVolume;

@Environment(EnvType.CLIENT)
public class FluidDummyItemColor implements IItemColor {

    @Override
    public int getColor(ItemStack stack, int tintIndex) {

        Item item = stack.getItem();
        if (!(item instanceof FluidDummyItem)) {
            return -1;
        }

        FluidDummyItem fluidItem = (FluidDummyItem) item;
        FluidVolume fluidStack = fluidItem.getFluidStack(stack);

        return fluidStack.getRenderColor();
    }

}
