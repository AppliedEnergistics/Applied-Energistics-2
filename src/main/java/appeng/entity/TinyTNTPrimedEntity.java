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
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.item.ItemEntity;
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
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;

import appeng.api.features.AEFeature;
import appeng.core.AEConfig;
import appeng.core.Api;
import appeng.core.AppEng;
import appeng.core.sync.packets.MockExplosionPacket;
import appeng.util.Platform;

public final class TinyTNTPrimedEntity extends TNTEntity implements IEntityAdditionalSpawnData {

    public static EntityType<TinyTNTPrimedEntity> TYPE;

    private LivingEntity placedBy;

    public TinyTNTPrimedEntity(EntityType<? extends TinyTNTPrimedEntity> type, World worldIn) {
        super(type, worldIn);
        this.blocksBuilding = true;
    }

    public TinyTNTPrimedEntity(final World w, final double x, final double y, final double z,
            final LivingEntity igniter) {
        super(TYPE, w);
        this.setPos(x, y, z);
        double d0 = w.random.nextDouble() * (double) ((float) Math.PI * 2F);
        this.setDeltaMovement(-Math.sin(d0) * 0.02D, (double) 0.2F, -Math.cos(d0) * 0.02D);
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
        this.setDeltaMovement(
                this.getDeltaMovement().multiply(0.9800000190734863D, 0.9800000190734863D, 0.9800000190734863D));

        if (this.onGround) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.699999988079071D, 0.699999988079071D, -0.5D));
        }

        if (this.isInWater() && Platform.isServer()) // put out the fuse.
        {
            Api.instance().definitions().blocks().tinyTNT().maybeStack(1).ifPresent(tntStack -> {
                final ItemEntity item = new ItemEntity(this.level, this.getX(), this.getY(), this.getZ(),
                        tntStack);

                item.setDeltaMovement(this.getDeltaMovement());
                item.xo = this.xo;
                item.yo = this.yo;
                item.zo = this.zo;

                this.level.addFreshEntity(item);
                this.remove();
            });
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
                SoundCategory.BLOCKS, 4.0F,
                (1.0F + (this.level.random.nextFloat() - this.level.random.nextFloat()) * 0.2F) * 32.9F);

        if (this.isInWater()) {
            return;
        }

        final Explosion ex = new Explosion(this.level, this, null, null, this.getX(), this.getY(), this.getZ(),
                0.2f, false, Explosion.Mode.BREAK);
        final AxisAlignedBB area = new AxisAlignedBB(this.getX() - 1.5, this.getY() - 1.5f, this.getZ() - 1.5,
                this.getX() + 1.5, this.getY() + 1.5, this.getZ() + 1.5);
        final List<Entity> list = this.level.getEntities(this, area);

        net.minecraftforge.event.ForgeEventFactory.onExplosionDetonate(this.level, ex, list, 0.2f * 2d);

        for (final Entity e : list) {
            e.hurt(DamageSource.explosion(ex), 6);
        }

        if (AEConfig.instance().isFeatureEnabled(AEFeature.TINY_TNT_BLOCK_DAMAGE)) {
            this.setPos(this.getX(), this.getY() - 0.25, this.getZ());

            for (int x = (int) (this.getX() - 2); x <= this.getX() + 2; x++) {
                for (int y = (int) (this.getY() - 2); y <= this.getY() + 2; y++) {
                    for (int z = (int) (this.getZ() - 2); z <= this.getZ() + 2; z++) {
                        final BlockPos point = new BlockPos(x, y, z);
                        final BlockState state = this.level.getBlockState(point);
                        final Block block = state.getBlock();

                        if (block != null && !block.isAir(state, this.level, point)) {
                            float strength = (float) (2.3f
                                    - (((x + 0.5f) - this.getX()) * ((x + 0.5f) - this.getX())
                                            + ((y + 0.5f) - this.getY()) * ((y + 0.5f) - this.getY())
                                            + ((z + 0.5f) - this.getZ()) * ((z + 0.5f) - this.getZ())));

                            final float resistance = block.getExplosionResistance(state, this.level, point, ex);
                            strength -= (resistance + 0.3F) * 0.11f;

                            if (strength > 0.01) {
                                if (state.getMaterial() != Material.AIR) {
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
        }

        AppEng.proxy.sendToAllNearExcept(null, this.getX(), this.getY(), this.getZ(), 64, this.level,
                new MockExplosionPacket(this.getX(), this.getY(), this.getZ()));
    }

    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void writeSpawnData(PacketBuffer buffer) {
        buffer.writeByte(this.getLife());
    }

    @Override
    public void readSpawnData(PacketBuffer additionalData) {
        this.setFuse(additionalData.readByte());
    }
}
