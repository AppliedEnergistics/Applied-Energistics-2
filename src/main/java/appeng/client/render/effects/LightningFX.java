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

import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LightningFX extends TextureSheetParticle {

    private static final RandomSource RANDOM_GENERATOR = RandomSource.create();
    private static final int STEPS = 5;
    private static final int BRIGHTNESS = 13 << 4;

    private final float[][] precomputedSteps;
    private final float[] vertices = new float[3];
    private final float[] verticesWithUV = new float[3];
    private boolean hasData = false;

    private LightningFX(ClientLevel level, double x, double y, double z, double r,
            double g, double b) {
        this(level, x, y, z, r, g, b, 6);
        this.regen();
    }

    protected LightningFX(ClientLevel level, double x, double y, double z, double r,
            double g, double b, int maxAge) {
        super(level, x, y, z, r, g, b);
        this.precomputedSteps = new float[LightningFX.STEPS][3];
        this.xd = 0;
        this.yd = 0;
        this.zd = 0;
        this.lifetime = maxAge;
    }

    protected void regen() {
        float lastDirectionX = (RANDOM_GENERATOR.nextFloat() - 0.5f) * 0.9f;
        float lastDirectionY = (RANDOM_GENERATOR.nextFloat() - 0.5f) * 0.9f;
        float lastDirectionZ = (RANDOM_GENERATOR.nextFloat() - 0.5f) * 0.9f;
        for (int s = 0; s < LightningFX.STEPS; s++) {
            this.precomputedSteps[s][0] = lastDirectionX = (lastDirectionX
                    + (RANDOM_GENERATOR.nextFloat() - 0.5f) * 0.9f) / 2.0f;
            this.precomputedSteps[s][1] = lastDirectionY = (lastDirectionY
                    + (RANDOM_GENERATOR.nextFloat() - 0.5f) * 0.9f) / 2.0f;
            this.precomputedSteps[s][2] = lastDirectionZ = (lastDirectionZ
                    + (RANDOM_GENERATOR.nextFloat() - 0.5f) * 0.9f) / 2.0f;
        }
    }

    protected int getSteps() {
        return LightningFX.STEPS;
    }

    @Override
    public ParticleRenderType getRenderType() {
        // TODO: FIXME
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        if (this.age++ >= this.lifetime) {
            this.remove();
        }

        this.yd -= 0.04D * this.gravity;
        this.move(this.xd, this.yd, this.zd);
        this.xd *= 0.9800000190734863D;
        this.yd *= 0.9800000190734863D;
        this.zd *= 0.9800000190734863D;
    }

    @Override
    public void render(VertexConsumer buffer, Camera renderInfo, float partialTicks) {
        Vec3 Vector3d = renderInfo.getPosition();
        float centerX = (float) (Mth.lerp(partialTicks, this.xo, this.x) - Vector3d.x());
        float centerY = (float) (Mth.lerp(partialTicks, this.yo, this.y) - Vector3d.y());
        float centerZ = (float) (Mth.lerp(partialTicks, this.zo, this.z) - Vector3d.z());

        final float j = 1.0f;
        float red = this.rCol * j * 0.9f;
        float green = this.gCol * j * 0.95f;
        float blue = this.bCol * j;
        final float alpha = this.alpha;

        if (this.age == 3) {
            this.regen();
        }

        float u = this.getU0() + (this.getU1() - this.getU0()) / 2;
        float v = this.getV0() + (this.getV1() - this.getV0()) / 2;

        float scale = 0.02f;// 0.02F * this.particleScale;

        final float[] a = new float[3];
        final float[] b = new float[3];

        float ox = 0;
        float oy = 0;
        float oz = 0;

        final Player p = Minecraft.getInstance().player;

        // FIXME: Billboard rotation is not applied to the particle yet,
        // FIXME The old version apparently did this by hand using rX,rZ -> replicate
        // using the quaternion

        for (int layer = 0; layer < 2; layer++) {
            if (layer == 0) {
                scale = 0.04f;
                // FIXME offX *= 0.001;
                // FIXME offY *= 0.001;
                // FIXME offZ *= 0.001;
                red = this.rCol * j * 0.4f;
                green = this.gCol * j * 0.25f;
                blue = this.bCol * j * 0.45f;
            } else {
                // FIXME offX = 0;
                // FIXME offY = 0;
                // FIXME offZ = 0;
                scale = 0.02f;
                red = this.rCol * j * 0.9f;
                green = this.gCol * j * 0.65f;
                blue = this.bCol * j * 0.85f;
            }

            for (int cycle = 0; cycle < 3; cycle++) {
                this.clear();

                // FIXME removed interpPos here, check if this is correct
                float x = centerX; // FIXME - offX;
                float y = centerY; // FIXME - offY;
                float z = centerZ; // FIXME - offZ;

                for (int s = 0; s < LightningFX.STEPS; s++) {
                    final float xN = x + this.precomputedSteps[s][0];
                    final float yN = y + this.precomputedSteps[s][1];
                    final float zN = z + this.precomputedSteps[s][2];

                    final float xD = xN - x;
                    final float yD = yN - y;
                    final float zD = zN - z;

                    if (cycle == 0) {
                        ox = yD * 0 - 1 * zD;
                        oy = zD * 0 - 0 * xD;
                        oz = xD * 1 - 0 * yD;
                    }
                    if (cycle == 1) {
                        ox = yD * 1 - 0 * zD;
                        oy = zD * 0 - 1 * xD;
                        oz = xD * 0 - 0 * yD;
                    }
                    if (cycle == 2) {
                        ox = yD * 0 - 0 * zD;
                        oy = zD * 1 - 0 * xD;
                        oz = xD * 0 - 1 * yD;
                    }

                    final float ss = Mth.sqrt(ox * ox + oy * oy + oz * oz)
                            / (((float) LightningFX.STEPS - (float) s) / LightningFX.STEPS * scale);
                    ox /= ss;
                    oy /= ss;
                    oz /= ss;

                    a[0] = x + ox;
                    a[1] = y + oy;
                    a[2] = z + oz;

                    b[0] = x;
                    b[1] = y;
                    b[2] = z;

                    this.draw(red, green, blue, buffer, a, b, u, v);

                    x = xN;
                    y = yN;
                    z = zN;
                }
            }
        }
    }

    private void clear() {
        this.hasData = false;
    }

    private void draw(float red, float green, float blue, VertexConsumer tess, float[] a, float[] b,
            float u, float v) {
        if (this.hasData) {
            tess.addVertex(a[0], a[1], a[2]).setUv(u, v).setColor(red, green, blue, this.alpha)
                    .setUv2(BRIGHTNESS, BRIGHTNESS);
            tess.addVertex(this.vertices[0], this.vertices[1], this.vertices[2]).setUv(u, v)
                    .setColor(red, green, blue, this.alpha).setUv2(BRIGHTNESS, BRIGHTNESS);
            tess.addVertex(this.verticesWithUV[0], this.verticesWithUV[1], this.verticesWithUV[2]).setUv(u, v)
                    .setColor(red, green, blue, this.alpha).setUv2(BRIGHTNESS, BRIGHTNESS);
            tess.addVertex(b[0], b[1], b[2]).setUv(u, v).setColor(red, green, blue, this.alpha)
                    .setUv2(BRIGHTNESS, BRIGHTNESS);
        }
        this.hasData = true;
        for (int x = 0; x < 3; x++) {
            this.vertices[x] = a[x];
            this.verticesWithUV[x] = b[x];
        }
    }

    protected float[][] getPrecomputedSteps() {
        return this.precomputedSteps;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Factory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteSet;

        public Factory(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType typeIn, ClientLevel level, double x, double y, double z,
                double xSpeed, double ySpeed, double zSpeed) {
            LightningFX lightningFX = new LightningFX(level, x, y, z, xSpeed, ySpeed, zSpeed);
            lightningFX.pickSprite(this.spriteSet);
            return lightningFX;
        }
    }

}
