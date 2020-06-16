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

import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.particles.ItemParticleData;
import net.minecraft.particles.ParticleType;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.api.util.AEPartLocation;
import appeng.core.AppEng;

@OnlyIn(Dist.CLIENT)
public class CraftingFx extends BreakingParticle {

    public static final BasicParticleType TYPE = new BasicParticleType(false);

    static {
        TYPE.setRegistryName(AppEng.MOD_ID, "crafting_fx");
    }

    private final int startBlkX;
    private final int startBlkY;
    private final int startBlkZ;

    public CraftingFx(final World par1World, final double x, final double y, final double z,
            final IAnimatedSprite sprite) {
        super(par1World, x, y, z, new ItemStack(Items.DIAMOND));
        this.particleGravity = 0;
        this.particleBlue = 1;
        this.particleGreen = 0.9f;
        this.particleRed = 1;
        this.particleAlpha = 1.3f;
        this.particleScale = 1.5f;
        this.selectSpriteRandomly(sprite);
        this.maxAge /= 1.2;

        this.startBlkX = MathHelper.floor(this.posX);
        this.startBlkY = MathHelper.floor(this.posY);
        this.startBlkZ = MathHelper.floor(this.posZ);
    }

    @Override
    public void renderParticle(IVertexBuilder buffer, ActiveRenderInfo renderInfo, float partialTicks) {

        if (partialTicks < 0 || partialTicks > 1) {
            return;
        }

        float offX = (float) (MathHelper.lerp(partialTicks, this.prevPosX, this.posX));
        float offY = (float) (MathHelper.lerp(partialTicks, this.prevPosY, this.posY));
        float offZ = (float) (MathHelper.lerp(partialTicks, this.prevPosZ, this.posZ));

        final int blkX = MathHelper.floor(offX);
        final int blkY = MathHelper.floor(offY);
        final int blkZ = MathHelper.floor(offZ);
        // I believe this particle is same as breaking particle, but should not exit the
        // original block it was
        // spawned in (which is encased in glass)
        if (blkX == this.startBlkX && blkY == this.startBlkY && blkZ == this.startBlkZ) {
            Vec3d vec3d = renderInfo.getProjectedView();
            offX -= vec3d.x;
            offY -= vec3d.y;
            offZ -= vec3d.z;

            Vector3f[] avector3f = new Vector3f[] { new Vector3f(-1.0F, -1.0F, 0.0F), new Vector3f(-1.0F, 1.0F, 0.0F),
                    new Vector3f(1.0F, 1.0F, 0.0F), new Vector3f(1.0F, -1.0F, 0.0F) };
            float scale = this.getScale(partialTicks);

            for (int i = 0; i < 4; ++i) {
                Vector3f vector3f = avector3f[i];
                vector3f.transform(renderInfo.getRotation());
                vector3f.mul(scale);
                vector3f.add(offX, offY, offZ);
            }

            float minU = this.getMinU();
            float maxU = this.getMaxU();
            float minV = this.getMinV();
            float maxV = this.getMaxV();
            int j = this.getBrightnessForRender(partialTicks);
            buffer.pos(avector3f[0].getX(), avector3f[0].getY(), avector3f[0].getZ()).tex(maxU, maxV)
                    .color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j)
                    .endVertex();
            buffer.pos(avector3f[1].getX(), avector3f[1].getY(), avector3f[1].getZ()).tex(maxU, minV)
                    .color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j)
                    .endVertex();
            buffer.pos(avector3f[2].getX(), avector3f[2].getY(), avector3f[2].getZ()).tex(minU, minV)
                    .color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j)
                    .endVertex();
            buffer.pos(avector3f[3].getX(), avector3f[3].getY(), avector3f[3].getZ()).tex(minU, maxV)
                    .color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j)
                    .endVertex();
        }
    }

    public void fromItem(final AEPartLocation d) {
        this.posX += 0.2 * d.xOffset;
        this.posY += 0.2 * d.yOffset;
        this.posZ += 0.2 * d.zOffset;
        this.particleScale *= 0.8f;
    }

    @Override
    public void tick() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;

        if (this.age++ >= this.maxAge) {
            this.setExpired();
        }

        this.motionY -= 0.04D * this.particleGravity;
        this.move(this.motionX, this.motionY, this.motionZ);
        this.motionX *= 0.9800000190734863D;
        this.motionY *= 0.9800000190734863D;
        this.motionZ *= 0.9800000190734863D;
        this.particleScale *= 0.51f;
        this.particleAlpha *= 0.51f;
    }

    public void setMotionX(float motionX) {
        this.motionX = motionX;
    }

    public void setMotionY(float motionY) {
        this.motionY = motionY;
    }

    public void setMotionZ(float motionZ) {
        this.motionZ = motionZ;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Factory implements IParticleFactory<BasicParticleType> {
        private final IAnimatedSprite spriteSet;

        public Factory(IAnimatedSprite p_i50477_1_) {
            this.spriteSet = p_i50477_1_;
        }

        public Particle makeParticle(BasicParticleType data, World worldIn, double x, double y, double z, double xSpeed,
                double ySpeed, double zSpeed) {
            return new CraftingFx(worldIn, x, y, z, spriteSet);
        }
    }

}
