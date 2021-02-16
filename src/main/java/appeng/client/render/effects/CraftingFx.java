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

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;

@Environment(EnvType.CLIENT)
public class CraftingFx extends SpriteBillboardParticle {

    // Offset relative to center of block, is the starting point of the particle
    // movement
    private final float offsetX;
    private final float offsetY;
    private final float offsetZ;

    public CraftingFx(ClientWorld world, final double x, final double y, final double z, final SpriteProvider sprite) {
        super(world, x, y, z);

        // Pick a random normal, offset it by 0.35 and use that as the particle origin
        Vec3f off = new Vec3f(random.nextFloat() - 0.5f, random.nextFloat() - 0.5f, random.nextFloat() - 0.5f);
        off.normalize();
        off.scale(0.35f);
        offsetX = off.getX();
        offsetY = off.getY();
        offsetZ = off.getZ();

        this.gravityStrength = 0;
        this.colorBlue = 1;
        this.colorGreen = 0.9f;
        this.colorRed = 1;
        this.setSprite(sprite);
        this.maxAge /= 1.2;
        this.collidesWithWorld = false; // we're INSIDE the block anyway
    }

    @Override
    public void buildGeometry(VertexConsumer buffer, Camera camera, float partialTicks) {

        float f = (this.age + partialTicks) / this.maxAge;

        float offX = (float) x + MathHelper.lerp(f, offsetX, 0);
        float offY = (float) y + MathHelper.lerp(f, offsetY, 0);
        float offZ = (float) z + MathHelper.lerp(f, offsetZ, 0);
        float alpha = MathHelper.lerp(easeOutCirc(f), 1.3f, 0.1f);
        float scale = MathHelper.lerp(easeOutCirc(f), 0.13f, 0.0f);

        // I believe this particle is same as breaking particle, but should not exit the
        // original block it was
        // spawned in (which is encased in glass)
        Vec3d vec3d = camera.getPos();
        offX -= vec3d.x;
        offY -= vec3d.y;
        offZ -= vec3d.z;

        Vec3f[] avector3f = new Vec3f[] { new Vec3f(-1.0F, -1.0F, 0.0F), new Vec3f(-1.0F, 1.0F, 0.0F),
                new Vec3f(1.0F, 1.0F, 0.0F), new Vec3f(1.0F, -1.0F, 0.0F) };

        for (int i = 0; i < 4; ++i) {
            Vec3f vector3f = avector3f[i];
            vector3f.rotate(camera.getRotation());
            vector3f.scale(scale);
            vector3f.add(offX, offY, offZ);
        }

        float minU = this.getMinU();
        float maxU = this.getMaxU();
        float minV = this.getMinV();
        float maxV = this.getMaxV();
        int j = 15728880; // full brightness
        buffer.vertex(avector3f[0].getX(), avector3f[0].getY(), avector3f[0].getZ()).texture(maxU, maxV)
                .color(this.colorRed, this.colorGreen, this.colorBlue, alpha).light(j).next();
        buffer.vertex(avector3f[1].getX(), avector3f[1].getY(), avector3f[1].getZ()).texture(maxU, minV)
                .color(this.colorRed, this.colorGreen, this.colorBlue, alpha).light(j).next();
        buffer.vertex(avector3f[2].getX(), avector3f[2].getY(), avector3f[2].getZ()).texture(minU, minV)
                .color(this.colorRed, this.colorGreen, this.colorBlue, alpha).light(j).next();
        buffer.vertex(avector3f[3].getX(), avector3f[3].getY(), avector3f[3].getZ()).texture(minU, maxV)
                .color(this.colorRed, this.colorGreen, this.colorBlue, alpha).light(j).next();
    }

    // https://easings.net/#easeOutCirc
    private static float easeOutCirc(float x) {
        return (float) Math.sqrt(1 - Math.pow(x - 1, 2));
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public void tick() {
        if (this.age++ >= this.maxAge) {
            this.markDead();
        }
    }

    @Environment(EnvType.CLIENT)
    public static class Factory implements ParticleFactory<DefaultParticleType> {
        private final SpriteProvider spriteSet;

        public Factory(SpriteProvider p_i50477_1_) {
            this.spriteSet = p_i50477_1_;
        }

        @Override
        public Particle createParticle(DefaultParticleType effect, ClientWorld world, double x, double y, double z,
                double xSpeed, double ySpeed, double zSpeed) {
            return new CraftingFx(world, x, y, z, spriteSet);
        }
    }

}
