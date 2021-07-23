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

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.Explosion.BlockInteraction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.protocol.Packet;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;

import appeng.core.AEConfig;
import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEEntities;
import appeng.core.sync.packets.MockExplosionPacket;

public final class TinyTNTPrimedEntity extends PrimedTnt implements IEntityAdditionalSpawnData {

    private LivingEntity placedBy;

    public TinyTNTPrimedEntity(EntityType<? extends TinyTNTPrimedEntity> type, Level worldIn) {
        super(type, worldIn);
        this.blocksBuilding = true;
    }

    public TinyTNTPrimedEntity(final Level w, final double x, final double y, final double z,
                               final LivingEntity igniter) {
        super(AEEntities.TINY_TNT_PRIMED, w);
        this.setPos(x, y, z);
        double d0 = w.random.nextDouble() * ((float) Math.PI * 2F);
        this.setDeltaMovement(-Math.sin(d0) * 0.02D, 0.2F, -Math.cos(d0) * 0.02D);
        this.setFuse(80);
        this.xo = x;
        this.yo = y;
        this.zo = z;
        this.placedBy = igniter;
    }

    @Nullable
    @Override
    public LivingEntity getOwner() {
        return this.placedBy;
    }

    /**
     * Called to update the entity's position/logic.
     */
    @Override
    public void tick() {
        this.updateInWaterStateAndDoFluidPushing();

        this.xo = this.getX();
        this.yo = this.getY();
        this.zo = this.getZ();
        this.setDeltaMovement(this.getDeltaMovement().subtract(0, 0.03999999910593033D, 0));
        this.move(MoverType.SELF, this.getDeltaMovement());
        this.setDeltaMovement(this.getDeltaMovement().multiply(0.9800000190734863D, 0.9800000190734863D, 0.9800000190734863D));

        if (this.onGround) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.699999988079071D, 0.699999988079071D, -0.5D));
        }

        if (this.isInWater() && !this.level.isClientSide()) // put out the fuse.
        {
            ItemStack tntStack = AEBlocks.TINY_TNT.stack();

            final ItemEntity item = new ItemEntity(this.level, this.getX(), this.getY(), this.getZ(),
                    tntStack);

            item.setDeltaMovement(this.getDeltaMovement());
            item.xo = this.xo;
            item.yo = this.yo;
            item.zo = this.zo;

            this.level.addFreshEntity(item);
            this.remove();
        }

        if (this.getLife() <= 0) {
            this.remove();

            if (!this.level.isClientSide) {
                this.explode();
            }
        } else {
            this.level.addParticle(ParticleTypes.SMOKE, this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D,
                    0.0D);
        }
        this.setFuse(this.getLife() - 1);
    }

    // override :P
    @Override
    protected void explode() {
        this.level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.GENERIC_EXPLODE,
                SoundSource.BLOCKS, 4.0F,
                (1.0F + (this.level.random.nextFloat() - this.level.random.nextFloat()) * 0.2F) * 32.9F);

        if (this.isInWater()) {
            return;
        }

        final Explosion ex = new Explosion(this.level, this, null, null, this.getX(), this.getY(), this.getZ(),
                0.2f, false, BlockInteraction.BREAK);
        final AABB area = new AABB(this.getX() - 1.5, this.getY() - 1.5f, this.getZ() - 1.5,
                this.getX() + 1.5, this.getY() + 1.5, this.getZ() + 1.5);
        final List<Entity> list = this.level.getEntities(this, area);

        ForgeEventFactory.onExplosionDetonate(this.level, ex, list, 0.2f * 2d);

        for (final Entity e : list) {
            e.hurt(DamageSource.explosion(ex), 6);
        }

        if (AEConfig.instance().isTinyTntBlockDamageEnabled()) {
            this.setPos(this.getX(), this.getY() - 0.25, this.getZ());

            for (int x = (int) (this.getX() - 2); x <= this.getX() + 2; x++) {
                for (int y = (int) (this.getY() - 2); y <= this.getY() + 2; y++) {
                    for (int z = (int) (this.getZ() - 2); z <= this.getZ() + 2; z++) {
                        final BlockPos point = new BlockPos(x, y, z);
                        final BlockState state = this.level.getBlockState(point);
                        final Block block = state.getBlock();

                        if (block != null && !block.isAir(state, this.level, point)) {
                            float strength = (float) (2.3f
                                    - ((x + 0.5f - this.getX()) * (x + 0.5f - this.getX())
                                            + (y + 0.5f - this.getY()) * (y + 0.5f - this.getY())
                                            + (z + 0.5f - this.getZ()) * (z + 0.5f - this.getZ())));

                            final float resistance = block.getExplosionResistance(state, this.level, point, ex);
                            strength -= (resistance + 0.3F) * 0.11f;

                            if (strength > 0.01 && state.getMaterial() != Material.AIR) {
                                if (block.dropFromExplosion(ex)) {
                                    block.dropResources(state, this.level, point);
                                }

                                block.onBlockExploded(null, this.level, point, ex);
                            }
                        }
                    }
                }
            }
        }

        AppEng.instance().sendToAllNearExcept(null, this.getX(), this.getY(), this.getZ(), 64, this.level,
                new MockExplosionPacket(this.getX(), this.getY(), this.getZ()));
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        buffer.writeByte(this.getLife());
    }

    @Override
    public void readSpawnData(FriendlyByteBuf additionalData) {
        this.setFuse(additionalData.readByte());
    }
}
