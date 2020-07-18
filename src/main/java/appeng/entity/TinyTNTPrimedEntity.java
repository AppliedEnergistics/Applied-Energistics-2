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

import appeng.core.Api;
import appeng.api.features.AEFeature;
import appeng.core.AEConfig;
import appeng.core.sync.packets.ICustomEntity;
import appeng.core.sync.packets.SpawnEntityPacket;
import appeng.util.Platform;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.entity.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;

import javax.annotation.Nullable;
import java.util.List;

public final class TinyTNTPrimedEntity extends TntEntity implements ICustomEntity {

    public static EntityType<TinyTNTPrimedEntity> TYPE;

    private LivingEntity causingEntity;

    public TinyTNTPrimedEntity(EntityType<? extends TinyTNTPrimedEntity> type, World worldIn) {
        super(type, worldIn);
        this.inanimate = true;
    }

    public TinyTNTPrimedEntity(final World world, final double x, final double y, final double z,
                               final LivingEntity igniter) {
        super(TYPE, world);
        this.updatePosition(x, y, z);
        double d = world.random.nextDouble() * 6.2831854820251465D;
        this.setVelocity(-Math.sin(d) * 0.02D, 0.20000000298023224D, -Math.cos(d) * 0.02D);
        this.setFuse(80);
        this.prevX = x;
        this.prevY = y;
        this.prevZ = z;
        this.causingEntity = igniter;
    }

    @Nullable
    @Override
    public LivingEntity getCausingEntity() {
        return causingEntity;
    }

    /**
     * Called to update the entity's position/logic.
     */
    @Override
    public void tick() {

        if (!this.hasNoGravity()) {
            this.setVelocity(this.getVelocity().add(0.0D, -0.04D, 0.0D));
        }

        this.move(MovementType.SELF, this.getVelocity());
        this.setVelocity(this.getVelocity().multiply(0.98D));
        if (this.onGround) {
            // Bounce up
            this.setVelocity(this.getVelocity().multiply(0.7D, -0.5D, 0.7D));
        }

        if (this.getFuse() <= 0) {
            this.remove();

            if (!this.world.isClient) {
                this.explode();
            }
        } else {
            this.updateWaterState();
            if (this.isSubmergedInWater() && Platform.isServer()) // put out the fuse.
            {
                Api.instance().definitions().blocks().tinyTNT().maybeStack(1).ifPresent(tntStack -> {
                    final ItemEntity item = new ItemEntity(this.world, this.getX(), this.getY(), this.getZ(),
                            tntStack);

                    item.setVelocity(this.getVelocity());
                    item.prevX = this.prevX;
                    item.prevY = this.prevY;
                    item.prevZ = this.prevZ;

                    this.world.spawnEntity(item);
                    this.remove();
                });
            }

            this.world.addParticle(ParticleTypes.SMOKE, this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D,
                    0.0D);
        }
        this.setFuse(this.getFuse() - 1);
    }

    private void explode() {
        this.world.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_GENERIC_EXPLODE,
                SoundCategory.BLOCKS, 4.0F,
                (1.0F + (this.world.random.nextFloat() - this.world.random.nextFloat()) * 0.2F) * 32.9F);

        if (this.isSubmergedInWater()) {
            return;
        }

        final Explosion ex = new Explosion(this.world, this, this.getX(), this.getY(), this.getZ(), 0.2f,
                false, Explosion.DestructionType.BREAK);

        final Box area = new Box(this.getX() - 1.5, this.getY() - 1.5f, this.getZ() - 1.5,
                this.getX() + 1.5, this.getY() + 1.5, this.getZ() + 1.5);
        final List<Entity> list = this.world.getEntities(this, area);

        for (final Entity e : list) {
            e.damage(DamageSource.explosion(ex), 6);
        }

        if (AEConfig.instance().isFeatureEnabled(AEFeature.TINY_TNT_BLOCK_DAMAGE)) {
            this.updatePosition(this.getX(), this.getY() - 0.25, this.getZ());

            // For reference see Explosion.affectWorld
            for (int x = (int) (this.getX() - 2); x <= this.getX() + 2; x++) {
                for (int y = (int) (this.getY() - 2); y <= this.getY() + 2; y++) {
                    for (int z = (int) (this.getZ() - 2); z <= this.getZ() + 2; z++) {
                        final BlockPos blockPos = new BlockPos(x, y, z);
                        final BlockState state = this.world.getBlockState(blockPos);
                        final Block block = state.getBlock();

                        if (block != null && !state.isAir()) {
                            float strength = (float) (2.3f
                                    - (((x + 0.5f) - this.getX()) * ((x + 0.5f) - this.getX())
                                    + ((y + 0.5f) - this.getY()) * ((y + 0.5f) - this.getY())
                                    + ((z + 0.5f) - this.getZ()) * ((z + 0.5f) - this.getZ())));

                            final float resistance = block.getBlastResistance();
                            strength -= (resistance + 0.3F) * 0.11f;

                            if (strength > 0.01) {
                                if (state.getMaterial() != Material.AIR) {
                                    if (block.shouldDropItemsOnExplosion(ex)) {
                                        Block.dropStacks(state, this.world, blockPos);
                                    }

                                    this.world.setBlockState(blockPos, Blocks.AIR.getDefaultState(), 3);
                                    block.onDestroyedByExplosion(this.world, blockPos, ex);
                                }
                            }
                        }
                    }
                }
            }
        }

        this.world.addParticle(ParticleTypes.EXPLOSION, this.getX(), this.getY(), this.getZ(), 1.0D, 0.0D, 0.0D);
    }

    @Override
    public Packet<?> createSpawnPacket() {
        return SpawnEntityPacket.create(this);
    }

    @Override
    public void writeAdditionalSpawnData(PacketByteBuf buf) {
        buf.writeByte(this.getFuse());
    }

    @Override
    public void readAdditionalSpawnData(PacketByteBuf buf) {
        this.setFuse(buf.readByte());
    }

}
