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

package appeng.client.render.crafting;

import net.minecraft.world.item.ItemStack;

/**
 * Stores client-side only state about the ongoing animation for a molecular assembler.
 */
public class AssemblerAnimationStatus {

    private final net.minecraft.world.item.ItemStack is;

    private final byte speed;

    private final int ticksRequired;

    private float accumulatedTicks;

    private float ticksUntilParticles;

    public AssemblerAnimationStatus(byte speed, ItemStack is) {
        this.speed = speed;
        this.is = is;
        this.ticksRequired = (int) Math.ceil(Math.max(1, 100.0f / speed)) + 2;
    }

    public ItemStack getIs() {
        return is;
    }

    public byte getSpeed() {
        return speed;
    }

    public float getAccumulatedTicks() {
        return accumulatedTicks;
    }

    public void setAccumulatedTicks(float accumulatedTicks) {
        this.accumulatedTicks = accumulatedTicks;
    }

    public float getTicksUntilParticles() {
        return ticksUntilParticles;
    }

    public void setTicksUntilParticles(float ticksUntilParticles) {
        this.ticksUntilParticles = ticksUntilParticles;
    }

    public boolean isExpired() {
        return accumulatedTicks > ticksRequired;
    }
}
