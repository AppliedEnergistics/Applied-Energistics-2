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
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import appeng.api.util.AEColor;

public class CraftingParticle extends TextureSheetParticle {

    // Offset relative to center of block, is the starting point of the particle movement
    private final float offsetX;
    private final float offsetY;
    private final float offsetZ;

    public CraftingParticle(ClientLevel level, double x, double y, double z, SpriteSet sprite, ItemStack item) {
        super(level, x, y, z);

        // Use spherical coordinates to pick a point around the center
        double theta = 2 * Math.PI * random.nextFloat();
        double phi = Math.acos(2 * random.nextFloat() - 1);
        float radius = 0.5f;

        offsetX = (float) (radius * Math.sin(phi) * Math.cos(theta));
        offsetY = (float) (radius * Math.sin(phi) * Math.sin(theta));
        offsetZ = (float) (radius * Math.cos(phi));

        var color = AEColor.TRANSPARENT.mediumVariant;

        this.gravity = 0;
        this.rCol = ARGB.redFloat(color);
        this.gCol = ARGB.greenFloat(color);
        this.bCol = ARGB.blueFloat(color);
        this.pickSprite(sprite);
        this.lifetime = 8; // MA spawns particles every 4 ticks
        this.hasPhysics = false; // we're INSIDE the block anyway

        // Add the item break particle that we'll cross-fade with
        var itemParticle = new ItemParticle(level, x, y, z, item);
        Minecraft.getInstance().particleEngine.add(itemParticle);
    }

    private float getLife(float partialTicks) {
        return Math.clamp((this.age + partialTicks) / this.lifetime, 0, 1);
    }

    @Override
    public float getQuadSize(float partialTicks) {
        var f = getLife(partialTicks);
        return Mth.lerp(f, 0.26f, 0.13f);
    }

    @Override
    protected int getLightColor(float partialTick) {
        return LightTexture.FULL_BRIGHT;
    }

    @Override
    public void render(VertexConsumer buffer, Camera renderInfo, float partialTicks) {
        var f = getLife(partialTicks);
        float offX = (float) x + Mth.lerp(f, offsetX, 0);
        float offY = (float) y + Mth.lerp(f, offsetY, 0);
        float offZ = (float) z + Mth.lerp(f, offsetZ, 0);
        this.alpha = Mth.lerp(easeOutCirc(f), 1.3f, 0.1f);

        // I believe this particle is same as breaking particle, but should not exit the
        // original block it was
        // spawned in (which is encased in glass)
        Vec3 camPos = renderInfo.getPosition();
        offX -= camPos.x;
        offY -= camPos.y;
        offZ -= camPos.z;

        renderRotatedQuad(buffer, renderInfo.rotation(), offX, offY, offZ, partialTicks);
    }

    // https://easings.net/#easeOutCirc
    private static float easeOutCirc(float x) {
        return (float) Math.sqrt(1 - Math.pow(x - 1, 2));
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public void tick() {
        if (this.age++ >= this.lifetime) {
            this.remove();
        }
    }

    public static class Factory implements ParticleProvider<ItemParticleOption> {
        private final SpriteSet spriteSet;

        public Factory(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public Particle createParticle(ItemParticleOption data, ClientLevel level, double x, double y, double z,
                double xSpeed, double ySpeed, double zSpeed) {
            return new CraftingParticle(level, x, y, z, spriteSet, data.getItem());
        }
    }

    private class ItemParticle extends TextureSheetParticle {
        private final float uo;
        private final float vo;

        public ItemParticle(ClientLevel level, double x, double y, double z, ItemStack stack) {
            super(level, x, y, z);

            var renderState = new ItemStackRenderState();
            Minecraft.getInstance().getItemModelResolver().updateForTopItem(renderState, stack,
                    ItemDisplayContext.GROUND, level, null, 0);
            TextureAtlasSprite breakingParticle = renderState.pickParticleIcon(this.random);
            if (breakingParticle != null) {
                setSprite(breakingParticle);
            } else {
                setSprite(Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS)
                        .apply(MissingTextureAtlasSprite.getLocation()));
            }
            this.uo = this.random.nextFloat() * 3.0F;
            this.vo = this.random.nextFloat() * 3.0F;

            this.gravity = 0;
            this.bCol = 1;
            this.gCol = 0.9f;
            this.rCol = 1;
            this.lifetime = CraftingParticle.this.lifetime;
            this.hasPhysics = false; // we're INSIDE the block anyway
        }

        @Override
        public float getQuadSize(float partialticks) {
            return CraftingParticle.this.getQuadSize(partialticks) / 6;
        }

        @Override
        protected int getLightColor(float partialTick) {
            return CraftingParticle.this.getLightColor(partialTick);
        }

        @Override
        protected float getU0() {
            return this.sprite.getU((this.uo + 1.0F) / 4.0F);
        }

        @Override
        protected float getU1() {
            return this.sprite.getU(this.uo / 4.0F);
        }

        @Override
        protected float getV0() {
            return this.sprite.getV(this.vo / 4.0F);
        }

        @Override
        protected float getV1() {
            return this.sprite.getV((this.vo + 1.0F) / 4.0F);
        }

        @Override
        public void render(VertexConsumer buffer, Camera renderInfo, float partialTicks) {
            var f = getLife(partialTicks);
            float offX = (float) x + Mth.lerp(easeOutCirc(f), offsetX, 0);
            float offY = (float) y + Mth.lerp(easeOutCirc(f), offsetY, 0);
            float offZ = (float) z + Mth.lerp(easeOutCirc(f), offsetZ, 0);

            // I believe this particle is same as breaking particle, but should not exit the
            // original block it was
            // spawned in (which is encased in glass)
            Vec3 camPos = renderInfo.getPosition();
            offX -= camPos.x;
            offY -= camPos.y;
            offZ -= camPos.z;

            /// this.alpha = 0.1f + (1.2f - Mth.lerp(easeOutCirc(f), 1.2f, 0f));
            this.alpha = 0.1f + (1.2f - Mth.lerp(easeOutCirc(f), 1.2f, 0f));

            renderRotatedQuad(buffer, renderInfo.rotation(), offX, offY, offZ, partialTicks);
        }

        @Override
        public ParticleRenderType getRenderType() {
            return ParticleRenderType.TERRAIN_SHEET;
        }

        @Override
        public void tick() {
            if (this.age++ >= this.lifetime) {
                this.remove();
            }
        }
    }
}
