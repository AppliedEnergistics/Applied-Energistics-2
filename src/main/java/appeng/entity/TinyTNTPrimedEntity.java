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

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.item.TNTEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.Explosion.Mode;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;

import appeng.api.AEApi;
import appeng.api.features.AEFeature;
import appeng.core.AEConfig;
import appeng.core.AppEng;
import appeng.core.sync.packets.MockExplosionPacket;
import appeng.util.Platform;

public final class TinyTNTPrimedEntity extends TNTEntity implements IEntityAdditionalSpawnData {

    public static EntityType<TinyTNTPrimedEntity> TYPE;

    public TinyTNTPrimedEntity(EntityType<? extends TinyTNTPrimedEntity> type, World worldIn) {
        super(type, worldIn);
        this.preventEntitySpawning = true;
    }

    public TinyTNTPrimedEntity(final World w, final double x, final double y, final double z,
            final LivingEntity igniter) {
        super(w, x, y, z, igniter);
    }

    /**
     * Called to update the entity's position/logic.
     */
    @Override
    public void tick() {
        this.handleWaterMovement();

        this.prevX = this.getX();
        this.prevY = this.getY();
        this.prevZ = this.getZ();
        this.setMotion(this.getMotion().subtract(0, 0.03999999910593033D, 0));
        this.move(MoverType.SELF, this.getMotion());
        this.setMotion(this.getMotion().mul(0.9800000190734863D, 0.9800000190734863D, 0.9800000190734863D));

        if (this.onGround) {
            this.setMotion(this.getMotion().mul(0.699999988079071D, 0.699999988079071D, -0.5D));
        }

        if (this.isInWater() && Platform.isServer()) // put out the fuse.
        {
            AEApi.instance().definitions().blocks().tinyTNT().maybeStack(1).ifPresent(tntStack -> {
                final ItemEntity item = new ItemEntity(this.world, this.getX(), this.getY(), this.getZ(),
                        tntStack);

                item.setMotion(this.getMotion());
                item.prevX = this.prevX;
                item.prevY = this.prevY;
                item.prevZ = this.prevZ;

                this.world.spawnEntity(item);
                this.remove();
            });
        }

        if (this.getFuse() <= 0) {
            this.remove();

            if (!this.world.isClient) {
                this.explode();
            }
        } else {
            this.world.addParticle(ParticleTypes.SMOKE, this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D,
                    0.0D);
        }
        this.setFuse(this.getFuse() - 1);
    }

    // override :P
    @Override
    protected void explode() {
        this.world.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_GENERIC_EXPLODE,
                SoundCategory.BLOCKS, 4.0F,
                (1.0F + (this.world.rand.nextFloat() - this.world.rand.nextFloat()) * 0.2F) * 32.9F);

        if (this.isInWater()) {
            return;
        }

        final Explosion ex = new Explosion(this.world, this, this.getX(), this.getY(), this.getZ(), 0.2f,
                false, Mode.BREAK);
        final Box area = new Box(this.getX() - 1.5, this.getY() - 1.5f, this.getZ() - 1.5,
                this.getX() + 1.5, this.getY() + 1.5, this.getZ() + 1.5);
        final List<Entity> list = this.world.getEntities(this, area);

        net.minecraftforge.event.ForgeEventFactory.onExplosionDetonate(this.world, ex, list, 0.2f * 2d);

        for (final Entity e : list) {
            e.attackEntityFrom(DamageSource.causeExplosionDamage(ex), 6);
        }

        if (AEConfig.instance().isFeatureEnabled(AEFeature.TINY_TNT_BLOCK_DAMAGE)) {
            this.setPosition(this.getX(), this.getY() - 0.25, this.getPosZ());

            for (int x = (int) (this.getX() - 2); x <= this.getX() + 2; x++) {
                for (int y = (int) (this.getY() - 2); y <= this.getY() + 2; y++) {
                    for (int z = (int) (this.getZ() - 2); z <= this.getZ() + 2; z++) {
                        final BlockPos point = new BlockPos(x, y, z);
                        final BlockState state = this.world.getBlockState(point);
                        final Block block = state.getBlock();

                        if (block != null && !block.isAir(state, this.world, point)) {
                            float strength = (float) (2.3f
                                    - (((x + 0.5f) - this.getPosX()) * ((x + 0.5f) - this.getPosX())
                                            + ((y + 0.5f) - this.getPosY()) * ((y + 0.5f) - this.getPosY())
                                            + ((z + 0.5f) - this.getPosZ()) * ((z + 0.5f) - this.getPosZ())));

                            final float resistance = block.getExplosionResistance(state, this.world, point, this, ex);
                            strength -= (resistance + 0.3F) * 0.11f;

                            if (strength > 0.01) {
                                if (block.getMaterial(state) != Material.AIR) {
                                    if (block.canDropFromExplosion(ex)) {
                                        block.spawnDrops(state, this.world, point);
                                    }

                                    block.onBlockExploded(null, this.world, point, ex);
                                }
                            }
                        }
                    }
                }
            }
        }

        AppEng.proxy.sendToAllNearExcept(null, this.getX(), this.getY(), this.getZ(), 64, this.world,
                new MockExplosionPacket(this.getX(), this.getY(), this.getPosZ()));
    }

    @Override
    public void writeSpawnData(PacketByteBuf buffer) {
        buffer.writeByte(this.getFuse());
    }

    @Override
    public void readSpawnData(PacketByteBuf additionalData) {
        this.setFuse(additionalData.readByte());
    }
}
