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

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.item.TNTEntity;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import appeng.api.features.AEFeature;
import appeng.core.AEConfig;
import appeng.core.Api;
import appeng.core.sync.packets.ICustomEntity;
import appeng.core.sync.packets.SpawnEntityPacket;
import appeng.util.Platform;

public final class TinyTNTPrimedEntity extends TNTEntity implements ICustomEntity {

    public static EntityType<TinyTNTPrimedEntity> TYPE;

    private LivingEntity causingEntity;

    public TinyTNTPrimedEntity(EntityType<? extends TinyTNTPrimedEntity> type, World worldIn) {
        super(type, worldIn);
        this.preventEntitySpawning = true;
    }

    public TinyTNTPrimedEntity(final World world, final double x, final double y, final double z,
            final LivingEntity igniter) {
        super(TYPE, world);
        this.setPosition(x, y, z);
        double d = world.rand.nextDouble() * 6.2831854820251465D;
        this.setMotion(-Math.sin(d) * 0.02D, 0.20000000298023224D, -Math.cos(d) * 0.02D);
        this.setFuse(80);
        this.prevPosX = x;
        this.prevPosY = y;
        this.prevPosZ = z;
        this.causingEntity = igniter;
    }

    @Nullable
    @Override
    public LivingEntity getTntPlacedBy() {
        return causingEntity;
    }

    /**
     * Called to update the entity's position/logic.
     */
    @Override
    public void tick() {

        if (!this.hasNoGravity()) {
            this.setMotion(this.getMotion().add(0.0D, -0.04D, 0.0D));
        }

        this.move(MoverType.field_6308, this.getMotion());
        this.setMotion(this.getMotion().scale(0.98D));
        if (this.onGround) {
            // Bounce up
            this.setMotion(this.getMotion().mul(0.7D, -0.5D, 0.7D));
        }

        if (this.getFuseDataManager() <= 0) {
            this.remove();

            if (!this.world.isRemote) {
                this.explode();
            }
        } else {
            this.method_5876();
            if (this.canSwim() && Platform.isServer()) // put out the fuse.
            {
                Api.instance().definitions().blocks().tinyTNT().maybeStack(1).ifPresent(tntStack -> {
                    final ItemEntity item = new ItemEntity(this.world, this.getX(), this.getY(), this.getZ(), tntStack);

                    item.setVelocity(this.getVelocity());
                    item.prevX = this.prevX;
                    item.prevY = this.prevY;
                    item.prevZ = this.prevZ;

                    this.world.spawnEntity(item);
                    this.remove();
                });
            }

            this.world.addParticle(ParticleTypes.SMOKE, this.getPosX(), this.getPosY(), this.getPosZ(), 0.0D, 0.0D, 0.0D);
        }
        this.setFuse(this.getFuseDataManager() - 1);
    }

    private void explode() {
        this.world.playSound(null, this.getPosX(), this.getPosY(), this.getPosZ(), SoundEvents.ENTITY_GENERIC_EXPLODE,
                SoundCategory.field_15245, 4.0F,
                (1.0F + (this.world.rand.nextFloat() - this.world.rand.nextFloat()) * 0.2F) * 32.9F);

        if (this.canSwim()) {
            return;
        }

        final Explosion ex = new Explosion(this.world, this, null, null, this.getPosX(), this.getPosY(), this.getPosZ(), 0.2f,
                false, Explosion.Mode.field_18686);

        final AxisAlignedBB area = new AxisAlignedBB(this.getPosX() - 1.5, this.getPosY() - 1.5f, this.getPosZ() - 1.5, this.getPosX() + 1.5,
                this.getPosY() + 1.5, this.getPosZ() + 1.5);
        final List<Entity> list = this.world.getEntitiesWithinAABBExcludingEntity(this, area);

        for (final Entity e : list) {
            e.attackEntityFrom(DamageSource.causeExplosionDamage(ex), 6);
        }

        if (AEConfig.instance().isFeatureEnabled(AEFeature.TINY_TNT_BLOCK_DAMAGE)) {
            this.setPosition(this.getPosX(), this.getPosY() - 0.25, this.getPosZ());

            // For reference see Explosion.affectWorld
            for (int x = (int) (this.getPosX() - 2); x <= this.getPosX() + 2; x++) {
                for (int y = (int) (this.getPosY() - 2); y <= this.getPosY() + 2; y++) {
                    for (int z = (int) (this.getPosZ() - 2); z <= this.getPosZ() + 2; z++) {
                        final BlockPos blockPos = new BlockPos(x, y, z);
                        final BlockState state = this.world.getBlockState(blockPos);
                        final Block block = state.getBlock();

                        if (block != null && !state.isAir()) {
                            float strength = (float) (2.3f - (((x + 0.5f) - this.getPosX()) * ((x + 0.5f) - this.getPosX())
                                    + ((y + 0.5f) - this.getPosY()) * ((y + 0.5f) - this.getPosY())
                                    + ((z + 0.5f) - this.getPosZ()) * ((z + 0.5f) - this.getPosZ())));

                            final float resistance = block.getExplosionResistance();
                            strength -= (resistance + 0.3F) * 0.11f;

                            if (strength > 0.01) {
                                if (state.getMaterial() != Material.AIR) {
                                    if (block.canDropFromExplosion(ex)) {
                                        Block.spawnDrops(state, this.world, blockPos);
                                    }

                                    this.world.setBlockState(blockPos, Blocks.AIR.getDefaultState(), 3);
                                    block.onExplosionDestroy(this.world, blockPos, ex);
                                }
                            }
                        }
                    }
                }
            }
        }

        this.world.addParticle(ParticleTypes.EXPLOSION, this.getPosX(), this.getPosY(), this.getPosZ(), 1.0D, 0.0D, 0.0D);
    }

    @Override
    public IPacket<?> createSpawnPacket() {
        return SpawnEntityPacket.create(this);
    }

    @Override
    public void writeAdditionalSpawnData(PacketBuffer buf) {
        buf.writeByte(this.getFuseDataManager());
    }

    @Override
    public void readAdditionalSpawnData(PacketBuffer buf) {
        this.setFuse(buf.readByte());
    }

}
