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

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LightningArcFX extends LightningFX {

    private static final RandomSource RANDOM_GENERATOR = RandomSource.create();

    private final double rx;
    private final double ry;
    private final double rz;

    public LightningArcFX(ClientLevel level, double x, double y, double z, double ex,
            double ey, double ez, double r, double g, double b) {
        super(level, x, y, z, r, g, b, 6);

        this.rx = ex - x;
        this.ry = ey - y;
        this.rz = ez - z;

        this.regen();
    }

    @Override
    protected void regen() {
        final double i = 1.0 / (this.getSteps() - 1);
        final double lastDirectionX = this.rx * i;
        final double lastDirectionY = this.ry * i;
        final double lastDirectionZ = this.rz * i;

        final double len = Math.sqrt(
                lastDirectionX * lastDirectionX + lastDirectionY * lastDirectionY + lastDirectionZ * lastDirectionZ);
        for (int s = 0; s < this.getSteps(); s++) {
            final double[][] localSteps = this.getPrecomputedSteps();

            localSteps[s][0] = (lastDirectionX + (RANDOM_GENERATOR.nextDouble() - 0.5) * len * 1.2) / 2.0;
            localSteps[s][1] = (lastDirectionY + (RANDOM_GENERATOR.nextDouble() - 0.5) * len * 1.2) / 2.0;
            localSteps[s][2] = (lastDirectionZ + (RANDOM_GENERATOR.nextDouble() - 0.5) * len * 1.2) / 2.0;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class Factory implements ParticleProvider<LightningArcParticleData> {
        private final SpriteSet spriteSet;

        public Factory(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public Particle createParticle(LightningArcParticleData data, ClientLevel level, double x, double y, double z,
                double xSpeed, double ySpeed, double zSpeed) {
            TextureSheetParticle lightningFX = new LightningArcFX(level, x, y, z, data.target().x, data.target().y,
                    data.target().z, 0, 0, 0);
            lightningFX.pickSprite(this.spriteSet);
            return lightningFX;
        }
    }

}
