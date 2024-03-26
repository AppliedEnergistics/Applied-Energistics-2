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

package appeng.client.gui.style;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;

import appeng.api.stacks.AEFluidKey;

/**
 * Creates a {@link Blitter} to draw fluids into the user interface.
 */
public final class FluidBlitter {

    private FluidBlitter() {
    }

    public static Blitter create(AEFluidKey fluidKey) {
        return create(fluidKey.toStack(1));
    }

    public static Blitter create(FluidStack stack) {
        if (stack.isEmpty() && stack.getFluid() != Fluids.EMPTY) {
            stack = new FluidStack(stack.getFluidHolder(), 1, stack.getComponentsPatch());
        }

        Fluid fluid = stack.getFluid();

        var attributes = IClientFluidTypeExtensions.of(fluid);
        TextureAtlasSprite sprite = Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(attributes.getStillTexture(stack));

        return Blitter.sprite(sprite)
                .colorRgb(attributes.getTintColor(stack))
                // Most fluid texture have transparency, but we want an opaque slot
                .blending(false);
    }

}
