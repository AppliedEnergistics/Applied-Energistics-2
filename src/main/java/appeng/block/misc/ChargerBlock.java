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

package appeng.block.misc;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.api.util.AEAxisAlignedBB;
import appeng.block.AEBaseTileBlock;
import appeng.client.render.effects.ParticleTypes;
import appeng.core.AEConfig;
import appeng.core.AppEngClient;
import appeng.core.definitions.AEItems;
import appeng.tile.misc.ChargerTileEntity;
import appeng.util.InteractionUtil;

public class ChargerBlock extends AEBaseTileBlock<ChargerTileEntity> {

    public ChargerBlock() {
        super(defaultProps(Material.METAL).noOcclusion());
    }

    @Override
    public int getLightBlock(BlockState state, BlockGetter worldIn, BlockPos pos) {
        return 2; // FIXME Double check this (esp. value range)
    }

    @Override
    public InteractionResult onActivated(final Level w, final BlockPos pos, final Player player,
            final InteractionHand hand,
            final @Nullable ItemStack heldItem, final BlockHitResult hit) {
        if (InteractionUtil.isInAlternateUseMode(player)) {
            return InteractionResult.PASS;
        }

        if (!w.isClientSide()) {
            final ChargerTileEntity tc = this.getTileEntity(w, pos);
            if (tc != null) {
                tc.activate(player);
            }
        }

        return InteractionResult.sidedSuccess(w.isClientSide());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void animateTick(final BlockState state, final Level w, final BlockPos pos, final Random r) {
        if (!AEConfig.instance().isEnableEffects()) {
            return;
        }

        if (r.nextFloat() < 0.98) {
            return;
        }

        final ChargerTileEntity tile = this.getTileEntity(w, pos);
        if (tile != null && AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED
                .isSameAs(tile.getInternalInventory().getStackInSlot(0))) {
            final double xOff = 0.0;
            final double yOff = 0.0;
            final double zOff = 0.0;

            for (int bolts = 0; bolts < 3; bolts++) {
                if (AppEngClient.instance().shouldAddParticles(r)) {
                    Minecraft.getInstance().particleEngine.createParticle(ParticleTypes.LIGHTNING,
                            xOff + 0.5 + pos.getX(),
                            yOff + 0.5 + pos.getY(), zOff + 0.5 + pos.getZ(), 0.0, 0.0, 0.0);
                }
            }
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter w, BlockPos pos, CollisionContext context) {

        final ChargerTileEntity tile = this.getTileEntity(w, pos);
        if (tile != null) {
            final double twoPixels = 2.0 / 16.0;
            final Direction up = tile.getUp();
            final Direction forward = tile.getForward();
            final AEAxisAlignedBB bb = new AEAxisAlignedBB(twoPixels, twoPixels, twoPixels, 1.0 - twoPixels,
                    1.0 - twoPixels, 1.0 - twoPixels);

            if (up.getStepX() != 0) {
                bb.minX = 0;
                bb.maxX = 1;
            }
            if (up.getStepY() != 0) {
                bb.minY = 0;
                bb.maxY = 1;
            }
            if (up.getStepZ() != 0) {
                bb.minZ = 0;
                bb.maxZ = 1;
            }

            switch (forward) {
                case DOWN:
                    bb.maxY = 1;
                    break;
                case UP:
                    bb.minY = 0;
                    break;
                case NORTH:
                    bb.maxZ = 1;
                    break;
                case SOUTH:
                    bb.minZ = 0;
                    break;
                case EAST:
                    bb.minX = 0;
                    break;
                case WEST:
                    bb.maxX = 1;
                    break;
                default:
                    break;
            }

            return Shapes.create(bb.getBoundingBox());
        }
        return Shapes.create(new AABB(0.0, 0, 0.0, 1.0, 1.0, 1.0));
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter worldIn, BlockPos pos,
            CollisionContext context) {
        return Shapes.create(new AABB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0));
    }

}
