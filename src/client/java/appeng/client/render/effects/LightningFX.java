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
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

public class LightningFX extends Particle {

    static final int SEGMENTS = 5;
    static final int BRIGHTNESS = 13 << 4;

    protected final float[] precomputedSteps;
    private final TextureAtlasSprite sprite;

    private LightningFX(ClientLevel level, double x, double y, double z, double r,
            double g, double b, TextureAtlasSprite sprite) {
        this(level, x, y, z, r, g, b, 6, sprite);
        this.regen();
    }

    protected LightningFX(ClientLevel level, double x, double y, double z, double r,
            double g, double b, int maxAge, TextureAtlasSprite sprite) {
        super(level, x, y, z, r, g, b);
        this.sprite = sprite;
        this.precomputedSteps = new float[LightningFX.SEGMENTS * 3];
        this.xd = 0;
        this.yd = 0;
        this.zd = 0;
        this.lifetime = maxAge;
    }

    protected void regen() {
        float lastDirectionX = (random.nextFloat() - 0.5f) * 0.9f;
        float lastDirectionY = (random.nextFloat() - 0.5f) * 0.9f;
        float lastDirectionZ = (random.nextFloat() - 0.5f) * 0.9f;
        for (int s = 0; s < LightningFX.SEGMENTS; s++) {
            this.precomputedSteps[s * 3 + 0] = lastDirectionX = (lastDirectionX
                    + (random.nextFloat() - 0.5f) * 0.9f) / 2.0f;
            this.precomputedSteps[s * 3 + 1] = lastDirectionY = (lastDirectionY
                    + (random.nextFloat() - 0.5f) * 0.9f) / 2.0f;
            this.precomputedSteps[s * 3 + 2] = lastDirectionZ = (lastDirectionZ
                    + (random.nextFloat() - 0.5f) * 0.9f) / 2.0f;
        }
    }

    protected int getSteps() {
        return LightningFX.SEGMENTS;
    }

    @Override
    public ParticleRenderType getGroup() {
        return LightningFXGroup.GROUP;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        if (this.age++ >= this.lifetime) {
            this.remove();
        }

        if (age == 3) {
            regen();
        }

        this.yd -= 0.04D * this.gravity;
        this.move(this.xd, this.yd, this.zd);
        this.xd *= 0.9800000190734863D;
        this.yd *= 0.9800000190734863D;
        this.zd *= 0.9800000190734863D;
    }

    public record ParticleState(float centerX, float centerY, float centerZ,
            float u, float v, float[] precomputedSteps) {
    }

    public ParticleState extract(float partialTicks, Vec3 cameraPos) {
        float centerX = (float) (Mth.lerp(partialTicks, this.xo, this.x) - cameraPos.x());
        float centerY = (float) (Mth.lerp(partialTicks, this.yo, this.y) - cameraPos.y());
        float centerZ = (float) (Mth.lerp(partialTicks, this.zo, this.z) - cameraPos.z());

        // This picks the middle of the white pixel of the generic_0 particle texture
        float u = sprite.getU(0.5f + 0.5f / 16f);
        float v = sprite.getV(0.5f + 0.5f / 16f);

        return new ParticleState(
                centerX, centerY, centerZ,
                u, v, precomputedSteps.clone());
    }

    public static class Factory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteSet;

        public Factory(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType typeIn, ClientLevel level, double x, double y, double z,
                double xSpeed, double ySpeed, double zSpeed, RandomSource random) {
            return new LightningFX(level, x, y, z, xSpeed, ySpeed, zSpeed, spriteSet.get(random));
        }
    }

}
