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

package appeng.fluids.util;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

/**
 * Helps with playing fill/empty sounds for fluids to players.
 */
public final class FluidSoundHelper {

    private FluidSoundHelper() {
    }

    public static void playFillSound(Player player, FluidStack fluidStack) {
        if (fluidStack.isEmpty()) {
            return;
        }

        SoundEvent fillSound = fluidStack.getFluid().getAttributes().getFillSound(fluidStack);
        if (fillSound == null) {
            return;
        }

        playSound(player, fillSound);
    }

    public static void playEmptySound(Player player, FluidStack fluidStack) {
        if (fluidStack.isEmpty()) {
            return;
        }

        SoundEvent fillSound = fluidStack.getFluid().getAttributes().getEmptySound(fluidStack);
        if (fillSound == null) {
            return;
        }

        playSound(player, fillSound);
    }

    /**
     * @see net.minecraftforge.fluids.FluidUtil#tryFillContainer(ItemStack, IFluidHandler, int, Player, boolean)
     */
    private static void playSound(Player player, SoundEvent fillSound) {
        // This should just play the sound for the player themselves
        player.playNotifySound(fillSound, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

}
