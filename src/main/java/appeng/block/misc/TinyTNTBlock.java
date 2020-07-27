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

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;

import appeng.block.AEBaseBlock;
import appeng.entity.TinyTNTPrimedEntity;

public class TinyTNTBlock extends AEBaseBlock {

    private static final VoxelShape SHAPE = VoxelShapes.cuboid(new Box(0.25f, 0.0f, 0.25f, 0.75f, 0.5f, 0.75f));

    public TinyTNTBlock(Settings props) {
        super(props);
    }

    @Override
    public int getOpacity(BlockState state, BlockView worldIn, BlockPos pos) {
        return 2; // FIXME: Validate that this is the correct value range
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView worldIn, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public ActionResult onUse(BlockState state, World w, BlockPos pos, PlayerEntity player, Hand hand,
            final BlockHitResult hit) {
        ItemStack heldItem = player.getStackInHand(hand);
        if (heldItem.getItem() == Items.FLINT_AND_STEEL) {
            this.startFuse(w, pos, player);
            w.removeBlock(pos, false);
            heldItem.damage(1, player, p -> {
                p.sendToolBreakStatus(hand);
            }); // FIXME Check if onBroken is equivalent
            return ActionResult.SUCCESS;
        } else {
            return super.onUse(state, w, pos, player, hand, hit);
        }
    }

    public void startFuse(final World w, final BlockPos pos, final LivingEntity igniter) {
        if (!w.isClient) {
            final TinyTNTPrimedEntity primedTinyTNTEntity = new TinyTNTPrimedEntity(w, pos.getX() + 0.5F, pos.getY(),
                    pos.getZ() + 0.5F, igniter);
            w.spawnEntity(primedTinyTNTEntity);
            w.playSound(null, primedTinyTNTEntity.getX(), primedTinyTNTEntity.getY(), primedTinyTNTEntity.getZ(),
                    SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.BLOCKS, 1, 1);
        }
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos,
            boolean notify) {
        if (world.isReceivingRedstonePower(pos)) {
            this.startFuse(world, pos, null);
            world.removeBlock(pos, false);
        }
    }

    @Override
    public void onBlockAdded(BlockState state, World w, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onBlockAdded(state, w, pos, oldState, isMoving);

        if (w.getReceivedStrongRedstonePower(pos) > 0) {
            this.startFuse(w, pos, null);
            w.removeBlock(pos, false);
        }
    }

    @Override
    public void onSteppedOn(final World w, final BlockPos pos, final Entity entity) {
        if (entity instanceof PersistentProjectileEntity && !w.isClient) {
            final PersistentProjectileEntity entityarrow = (PersistentProjectileEntity) entity;

            if (entityarrow.isOnFire()) {
                LivingEntity igniter = null;
                // Check if the shooter still exists
                Entity shooter = entityarrow.getOwner();
                if (shooter instanceof LivingEntity) {
                    igniter = (LivingEntity) shooter;
                }
                this.startFuse(w, pos, igniter);
                w.removeBlock(pos, false);
            }
        }
    }

    @Override
    public boolean shouldDropItemsOnExplosion(final Explosion exp) {
        return false;
    }

    @Override
    public void onDestroyedByExplosion(final World w, final BlockPos pos, final Explosion exp) {
        super.onDestroyedByExplosion(w, pos, exp);
        if (!w.isClient) {
            final TinyTNTPrimedEntity primedTinyTNTEntity = new TinyTNTPrimedEntity(w, pos.getX() + 0.5F,
                    pos.getY() + 0.5F, pos.getZ() + 0.5F, exp.getCausingEntity());
            primedTinyTNTEntity
                    .setFuse(w.random.nextInt(primedTinyTNTEntity.getFuse() / 4) + primedTinyTNTEntity.getFuse() / 8);
            w.spawnEntity(primedTinyTNTEntity);
        }
    }

}
