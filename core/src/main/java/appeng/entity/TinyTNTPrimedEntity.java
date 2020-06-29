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
import appeng.api.features.AEFeature;
import appeng.core.AEConfig;
import appeng.util.Platform;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.entity.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;

import java.util.List;

public final class TinyTNTPrimedEntity extends TntEntity {

    public static EntityType<TinyTNTPrimedEntity> TYPE;

    public TinyTNTPrimedEntity(EntityType<? extends TinyTNTPrimedEntity> type, World worldIn) {
        super(type, worldIn);
        this.inanimate = true;
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
        this.updateWaterState();

        this.prevX = this.getX();
        this.prevY = this.getY();
        this.prevZ = this.getZ();
        this.setVelocity(this.getVelocity().subtract(0, 0.03999999910593033D, 0));
        this.move(MovementType.SELF, this.getVelocity());
        this.setVelocity(this.getVelocity().multiply(0.9800000190734863D, 0.9800000190734863D, 0.9800000190734863D));

        if (this.onGround) {
            this.setVelocity(this.getVelocity().multiply(0.699999988079071D, 0.699999988079071D, -0.5D));
        }

        if (this.isSubmergedInWater() && Platform.isServer()) // put out the fuse.
        {
            AEApi.instance().definitions().blocks().tinyTNT().maybeStack(1).ifPresent(tntStack -> {
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
                                if (state.getMaterial() == Material.AIR) {
                                    if (block.shouldDropItemsOnExplosion(ex)) {
                                        Block.dropStacks(state, this.world, blockPos);
                                    }

                                    block.onDestroyedByExplosion(this.world, blockPos, ex);
                                }
                            }
                        }
                    }
                }
            }
        }

        throw new IllegalStateException();
// FIXME FABRIC
//        AppEng.proxy.sendToAllNearExcept(null, this.getX(), this.getY(), this.getZ(), 64, this.world,
//                new MockExplosionPacket(this.getX(), this.getY(), this.getZ()));
    }

}
