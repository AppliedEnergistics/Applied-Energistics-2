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

package appeng.util.fluid;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;

/**
 * Helps with playing fill/empty sounds for fluids to players.
 */
public final class FluidSoundHelper {

    private FluidSoundHelper() {
    }

    public static void playFillSound(Player player, FluidVariant fluid) {
        if (fluid.isBlank()) {
            return;
        }

        fluid.getFluid().getPickupSound().ifPresent(sound -> playSound(player, sound));
    }

    public static void playEmptySound(Player player, FluidVariant fluid) {
        if (fluid.isBlank()) {
            return;
        }

// TODO: FABRIC 117 No equivalent available right now
//        SoundEvent fillSound = fluid.getFluid().getAttributes().getEmptySound(fluid);
//        if (fillSound == null) {
//            return;
//        }
        if (fluid.getFluid() == Fluids.LAVA) {
            playSound(player, SoundEvents.BUCKET_EMPTY_LAVA);
        } else {
            playSound(player, SoundEvents.BUCKET_EMPTY);
        }
    }

    /**
     * @see net.minecraftforge.fluids.FluidUtil#tryFillContainer(ItemStack, Storage<FluidVariant>, int, Player, boolean)
     */
    private static void playSound(Player player, SoundEvent fillSound) {
        // This should just play the sound for the player themselves
        player.playNotifySound(fillSound, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

}
