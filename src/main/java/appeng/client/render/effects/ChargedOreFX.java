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

package appeng.client.render.effects;


import net.minecraft.client.particle.ParticleRedstone;
import net.minecraft.world.World;


public class ChargedOreFX extends ParticleRedstone {

    public ChargedOreFX(final World w, final double x, final double y, final double z, final float r, final float g, final float b) {
        super(w, x, y, z, 0.21f, 0.61f, 1.0f);
    }

    @Override
    public int getBrightnessForRender(final float par1) {
        int j1 = super.getBrightnessForRender(par1);
        j1 = Math.max(j1 >> 20, j1 >> 4);
        j1 += 3;
        if (j1 > 15) {
            j1 = 15;
        }
        return j1 << 20 | j1 << 4;
    }
}
