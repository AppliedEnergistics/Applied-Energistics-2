/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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

package appeng.block.misc;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.Items;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;

import appeng.block.AEBaseBlock;
import appeng.entity.TinyTNTPrimedEntity;

public class TinyTNTBlock extends AEBaseBlock {

    private static final VoxelShape SHAPE = Shapes
            .create(new AABB(0.25f, 0.0f, 0.25f, 0.75f, 0.5f, 0.75f));

    public TinyTNTBlock(net.minecraft.world.level.block.state.BlockBehaviour.Properties props) {
        super(props);
    }

    @Override
    public int getLightBlock(BlockState state, BlockGetter worldIn, net.minecraft.core.BlockPos pos) {
        return 2; // FIXME: Validate that this is the correct value range
    }

    @Override
    public VoxelShape getShape(net.minecraft.world.level.block.state.BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public InteractionResult use(net.minecraft.world.level.block.state.BlockState state, Level world, BlockPos pos, Player player,
                                 InteractionHand handIn, BlockHitResult hit) {
        ItemStack heldItem = player.getItemInHand(handIn);
        if (!heldItem.isEmpty() && heldItem.getItem() == Items.FLINT_AND_STEEL) {
            this.startFuse(world, pos, player);
            world.removeBlock(pos, false);
            heldItem.hurtAndBreak(1, player, p -> {
                p.broadcastBreakEvent(handIn);
            }); // FIXME Check if onBroken is equivalent
            return InteractionResult.sidedSuccess(world.isClientSide());
        } else {
            return super.use(state, world, pos, player, handIn, hit);
        }
    }

    public void startFuse(final Level w, final net.minecraft.core.BlockPos pos, final net.minecraft.world.entity.LivingEntity igniter) {
        if (!w.isClientSide) {
            final TinyTNTPrimedEntity primedTinyTNTEntity = new TinyTNTPrimedEntity(w, pos.getX() + 0.5F,
                    pos.getY() + 0.5F, pos.getZ() + 0.5F, igniter);
            w.addFreshEntity(primedTinyTNTEntity);
            w.playSound(null, primedTinyTNTEntity.getX(), primedTinyTNTEntity.getY(),
                    primedTinyTNTEntity.getZ(), SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1, 1);
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level world, net.minecraft.core.BlockPos pos, Block blockIn, BlockPos fromPos,
                                boolean isMoving) {
        if (world.getBestNeighborSignal(pos) > 0) {
            this.startFuse(world, pos, null);
            world.removeBlock(pos, false);
        }
    }

    @Override
    public void onPlace(BlockState state, Level w, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, w, pos, oldState, isMoving);

        if (w.getBestNeighborSignal(pos) > 0) {
            this.startFuse(w, pos, null);
            w.removeBlock(pos, false);
        }
    }

    @Override
    public void stepOn(final Level w, final BlockPos pos, final net.minecraft.world.entity.Entity entity) {
        if (entity instanceof AbstractArrow && !w.isClientSide) {
            final AbstractArrow entityarrow = (AbstractArrow) entity;

            if (entityarrow.isOnFire()) {
                net.minecraft.world.entity.LivingEntity igniter = null;
                // Check if the shooter still exists
                Entity shooter = entityarrow.getOwner();
                if (shooter instanceof LivingEntity) {
                    igniter = (net.minecraft.world.entity.LivingEntity) shooter;
                }
                this.startFuse(w, pos, igniter);
                w.removeBlock(pos, false);
            }
        }
    }

    @Override
    public boolean dropFromExplosion(final Explosion exp) {
        return false;
    }

    @Override
    public void wasExploded(final Level w, final net.minecraft.core.BlockPos pos, final Explosion exp) {
        super.wasExploded(w, pos, exp);
        if (!w.isClientSide) {
            final TinyTNTPrimedEntity primedTinyTNTEntity = new TinyTNTPrimedEntity(w, pos.getX() + 0.5F,
                    pos.getY() + 0.5F, pos.getZ() + 0.5F, exp.getSourceMob());
            primedTinyTNTEntity
                    .setFuse(w.random.nextInt(primedTinyTNTEntity.getLife() / 4) + primedTinyTNTEntity.getLife() / 8);
            w.addFreshEntity(primedTinyTNTEntity);
        }
    }

}
