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

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import appeng.block.AEBaseBlock;
import appeng.entity.TinyTNTPrimedEntity;

public class TinyTNTBlock extends AEBaseBlock {

    private static final VoxelShape SHAPE = VoxelShapes
            .create(new AxisAlignedBB(0.25f, 0.0f, 0.25f, 0.75f, 0.5f, 0.75f));

    public TinyTNTBlock(AbstractBlock.Properties props) {
        super(defaultProps(Material.EXPLOSIVE).noOcclusion());
    }

    @Override
    public int getLightBlock(BlockState state, IBlockReader worldIn, BlockPos pos) {
        return 2; // FIXME: Validate that this is the correct value range
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return SHAPE;
    }

    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player,
            Hand handIn, BlockRayTraceResult hit) {
        ItemStack heldItem = player.getItemInHand(handIn);
        if (!heldItem.isEmpty() && heldItem.getItem() == Items.FLINT_AND_STEEL) {
            this.startFuse(world, pos, player);
            world.removeBlock(pos, false);
            heldItem.hurtAndBreak(1, player, p -> {
                p.broadcastBreakEvent(handIn);
            }); // FIXME Check if onBroken is equivalent
            return ActionResultType.SUCCESS;
        } else {
            return super.use(state, world, pos, player, handIn, hit);
        }
    }

    public void startFuse(final World w, final BlockPos pos, final LivingEntity igniter) {
        if (!w.isClientSide) {
            final TinyTNTPrimedEntity primedTinyTNTEntity = new TinyTNTPrimedEntity(w, pos.getX() + 0.5F,
                    pos.getY() + 0.5F, pos.getZ() + 0.5F, igniter);
            w.addFreshEntity(primedTinyTNTEntity);
            w.playSound(null, primedTinyTNTEntity.getX(), primedTinyTNTEntity.getY(),
                    primedTinyTNTEntity.getZ(), SoundEvents.TNT_PRIMED, SoundCategory.BLOCKS, 1, 1);
        }
    }

    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos,
            boolean isMoving) {
        if (world.getBestNeighborSignal(pos) > 0) {
            this.startFuse(world, pos, null);
            world.removeBlock(pos, false);
        }
    }

    @Override
    public void onPlace(BlockState state, World w, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, w, pos, oldState, isMoving);

        if (w.getBestNeighborSignal(pos) > 0) {
            this.startFuse(w, pos, null);
            w.removeBlock(pos, false);
        }
    }

    @Override
    public void stepOn(final World w, final BlockPos pos, final Entity entity) {
        if (entity instanceof AbstractArrowEntity && !w.isClientSide) {
            final AbstractArrowEntity entityarrow = (AbstractArrowEntity) entity;

            if (entityarrow.isOnFire()) {
                LivingEntity igniter = null;
                // Check if the shooter still exists
                if (w instanceof ServerWorld) {
                    Entity shooter = entityarrow.getOwner();
                    if (shooter instanceof LivingEntity) {
                        igniter = (LivingEntity) shooter;
                    }
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
    public void wasExploded(final World w, final BlockPos pos, final Explosion exp) {
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
