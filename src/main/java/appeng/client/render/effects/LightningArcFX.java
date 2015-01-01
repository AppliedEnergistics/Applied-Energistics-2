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

import java.util.Random;

import net.minecraft.world.World;

public class LightningArcFX extends LightningFX
{
	private static final Random RANDOM_GENERATOR = new Random();

	final double rx;
	final double ry;
	final double rz;

	public LightningArcFX(World w, double x, double y, double z, double ex, double ey, double ez, double r, double g, double b) {
		super( w, x, y, z, r, g, b, 6 );

		this.rx = ex - x;
		this.ry = ey - y;
		this.rz = ez - z;

		this.regen();
	}

	@Override
	protected void regen()
	{
		double i = 1.0 / (this.steps - 1);
		double lastDirectionX = this.rx * i;
		double lastDirectionY = this.ry * i;
		double lastDirectionZ = this.rz * i;

		double len = Math.sqrt( lastDirectionX * lastDirectionX + lastDirectionY * lastDirectionY + lastDirectionZ * lastDirectionZ );
		for (int s = 0; s < this.steps; s++)
		{
			this.Steps[s][0] = (lastDirectionX + ( RANDOM_GENERATOR.nextDouble() - 0.5) * len * 1.2) / 2.0;
			this.Steps[s][1] = (lastDirectionY + ( RANDOM_GENERATOR.nextDouble() - 0.5) * len * 1.2) / 2.0;
			this.Steps[s][2] = (lastDirectionZ + ( RANDOM_GENERATOR.nextDouble() - 0.5) * len * 1.2) / 2.0;
		}

	}

}
