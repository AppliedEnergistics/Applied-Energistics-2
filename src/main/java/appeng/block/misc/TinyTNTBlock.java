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

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import appeng.block.AEBaseBlock;
import appeng.entity.TinyTNTPrimedEntity;

public class TinyTNTBlock extends AEBaseBlock {

    private static final VoxelShape SHAPE = Shapes
            .create(new AABB(0.25f, 0.0f, 0.25f, 0.75f, 0.5f, 0.75f));

    public TinyTNTBlock(BlockBehaviour.Properties props) {
        super(props);
    }

    @Override
    public int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return 2; // FIXME: Validate that this is the correct value range
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
            InteractionHand handIn, BlockHitResult hit) {
        ItemStack heldItem = player.getItemInHand(handIn);
        if (!heldItem.isEmpty() && heldItem.getItem() == Items.FLINT_AND_STEEL) {
            this.startFuse(level, pos, player);
            level.removeBlock(pos, false);
            heldItem.hurtAndBreak(1, player, p -> {
                p.broadcastBreakEvent(handIn);
            }); // FIXME Check if onBroken is equivalent
            return InteractionResult.sidedSuccess(level.isClientSide());
        } else {
            return super.use(state, level, pos, player, handIn, hit);
        }
    }

    public void startFuse(Level level, BlockPos pos, LivingEntity igniter) {
        if (!level.isClientSide) {
            final TinyTNTPrimedEntity primedTinyTNTEntity = new TinyTNTPrimedEntity(level, pos.getX() + 0.5F,
                    pos.getY() + 0.5F, pos.getZ() + 0.5F, igniter);
            level.addFreshEntity(primedTinyTNTEntity);
            level.playSound(null, primedTinyTNTEntity.getX(), primedTinyTNTEntity.getY(),
                    primedTinyTNTEntity.getZ(), SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1, 1);
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block blockIn, BlockPos fromPos,
            boolean isMoving) {
        if (level.getBestNeighborSignal(pos) > 0) {
            this.startFuse(level, pos, null);
            level.removeBlock(pos, false);
        }
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);

        if (level.getBestNeighborSignal(pos) > 0) {
            this.startFuse(level, pos, null);
            level.removeBlock(pos, false);
        }
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (!level.isClientSide && entity instanceof AbstractArrow arrow) {

            if (arrow.isOnFire()) {
                LivingEntity igniter = null;
                // Check if the shooter still exists
                Entity shooter = arrow.getOwner();
                if (shooter instanceof LivingEntity) {
                    igniter = (LivingEntity) shooter;
                }
                this.startFuse(level, pos, igniter);
                level.removeBlock(pos, false);
            }
        }
    }

    @Override
    public boolean dropFromExplosion(Explosion exp) {
        return false;
    }

    @Override
    public void wasExploded(Level level, BlockPos pos, Explosion exp) {
        super.wasExploded(level, pos, exp);
        if (!level.isClientSide) {
            final TinyTNTPrimedEntity primedTinyTNTEntity = new TinyTNTPrimedEntity(level, pos.getX() + 0.5F,
                    pos.getY() + 0.5F, pos.getZ() + 0.5F, exp.getSourceMob());
            primedTinyTNTEntity
                    .setFuse(level.random.nextInt(primedTinyTNTEntity.getFuse() / 4)
                            + primedTinyTNTEntity.getFuse() / 8);
            level.addFreshEntity(primedTinyTNTEntity);
        }
    }

}
