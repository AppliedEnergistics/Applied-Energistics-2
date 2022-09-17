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

package appeng.entity;


import appeng.api.AEApi;
import appeng.core.AEConfig;
import appeng.core.AppEng;
import appeng.core.features.AEFeature;
import appeng.core.sync.packets.PacketMockExplosion;
import appeng.helpers.Reflected;
import appeng.util.Platform;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;

import java.util.List;


public final class EntityTinyTNTPrimed extends EntityTNTPrimed implements IEntityAdditionalSpawnData {

    private static final float SIZE = .5f;

    @Reflected
    public EntityTinyTNTPrimed(final World w) {
        super(w);
        this.setSize(SIZE, SIZE);
    }

    public EntityTinyTNTPrimed(final World w, final double x, final double y, final double z, final EntityLivingBase igniter) {
        super(w, x, y, z, igniter);
        this.setSize(SIZE, SIZE);
        // this.yOffset = this.height / 2.0F;
    }

    /**
     * Called to update the entity's position/logic.
     */
    @Override
    public void onUpdate() {
        this.handleWaterMovement();

        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        this.motionY -= 0.03999999910593033D;
        this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
        this.motionX *= 0.9800000190734863D;
        this.motionY *= 0.9800000190734863D;
        this.motionZ *= 0.9800000190734863D;

        if (this.onGround) {
            this.motionX *= 0.699999988079071D;
            this.motionZ *= 0.699999988079071D;
            this.motionY *= -0.5D;
        }

        if (this.isInWater() && Platform.isServer()) // put out the fuse.
        {
            AEApi.instance().definitions().blocks().tinyTNT().maybeStack(1).ifPresent(tntStack ->
            {
                final EntityItem item = new EntityItem(this.world, this.posX, this.posY, this.posZ, tntStack);

                item.motionX = this.motionX;
                item.motionY = this.motionY;
                item.motionZ = this.motionZ;
                item.prevPosX = this.prevPosX;
                item.prevPosY = this.prevPosY;
                item.prevPosZ = this.prevPosZ;

                this.world.spawnEntity(item);
                this.setDead();
            });
        }

        if (this.getFuse() <= 0) {
            this.setDead();

            if (!this.world.isRemote) {
                this.explode();
            }
        } else {
            this.world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, this.posX, this.posY, this.posZ, 0.0D, 0.0D, 0.0D);
        }
        this.setFuse(this.getFuse() - 1);
    }

    // override :P
    void explode() {
        this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 4.0F,
                (1.0F + (this.world.rand.nextFloat() - this.world.rand.nextFloat()) * 0.2F) * 32.9F);

        if (this.isInWater()) {
            return;
        }

        final Explosion ex = new Explosion(this.world, this, this.posX, this.posY, this.posZ, 0.2f, false, false);
        final AxisAlignedBB area = new AxisAlignedBB(this.posX - 1.5, this.posY - 1.5f, this.posZ - 1.5, this.posX + 1.5, this.posY + 1.5, this.posZ + 1.5);
        final List<Entity> list = this.world.getEntitiesWithinAABBExcludingEntity(this, area);

        net.minecraftforge.event.ForgeEventFactory.onExplosionDetonate(this.world, ex, list, 0.2f * 2d);

        for (final Entity e : list) {
            e.attackEntityFrom(DamageSource.causeExplosionDamage(ex), 6);
        }

        if (AEConfig.instance().isFeatureEnabled(AEFeature.TINY_TNT_BLOCK_DAMAGE)) {
            this.posY -= 0.25;

            for (int x = (int) (this.posX - 2); x <= this.posX + 2; x++) {
                for (int y = (int) (this.posY - 2); y <= this.posY + 2; y++) {
                    for (int z = (int) (this.posZ - 2); z <= this.posZ + 2; z++) {
                        final BlockPos point = new BlockPos(x, y, z);
                        final IBlockState state = this.world.getBlockState(point);
                        final Block block = state.getBlock();

                        if (block != null && !block.isAir(state, this.world, point)) {
                            float strength = (float) (2.3f - (((x + 0.5f) - this.posX) * ((x + 0.5f) - this.posX) + ((y + 0.5f) - this.posY) * ((y + 0.5f) - this.posY) + ((z + 0.5f) - this.posZ) * ((z + 0.5f) - this.posZ)));

                            final float resistance = block.getExplosionResistance(this.world, point, this, ex);
                            strength -= (resistance + 0.3F) * 0.11f;

                            if (strength > 0.01) {
                                if (block.getMaterial(state) != Material.AIR) {
                                    if (block.canDropFromExplosion(ex)) {
                                        block.dropBlockAsItemWithChance(this.world, point, state, 1.0F / 1.0f, 0);
                                    }

                                    block.onBlockExploded(this.world, point, ex);
                                }
                            }
                        }
                    }
                }
            }
        }

        AppEng.proxy.sendToAllNearExcept(null, this.posX, this.posY, this.posZ, 64, this.world, new PacketMockExplosion(this.posX, this.posY, this.posZ));
    }

    @Override
    public void writeSpawnData(final ByteBuf data) {
        data.writeByte(this.getFuse());
    }

    @Override
    public void readSpawnData(final ByteBuf data) {
        this.setFuse(data.readByte());
    }
}
