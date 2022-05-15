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

import javax.annotation.Nullable;

import com.mojang.math.Vector3f;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
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
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import appeng.api.util.AEAxisAlignedBB;
import appeng.block.AEBaseEntityBlock;
import appeng.blockentity.misc.ChargerBlockEntity;
import appeng.client.render.FacingToRotation;
import appeng.client.render.effects.LightningArcParticleData;
import appeng.core.AEConfig;
import appeng.core.AppEngClient;
import appeng.util.InteractionUtil;

public class ChargerBlock extends AEBaseEntityBlock<ChargerBlockEntity> {

    public ChargerBlock() {
        super(defaultProps(Material.METAL).noOcclusion());
    }

    @Override
    public int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return 2; // FIXME Double check this (esp. value range)
    }

    @Override
    public InteractionResult onActivated(Level level, BlockPos pos, Player player,
            InteractionHand hand,
            @Nullable ItemStack heldItem, BlockHitResult hit) {
        if (InteractionUtil.isInAlternateUseMode(player)) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide()) {
            final ChargerBlockEntity tc = this.getBlockEntity(level, pos);
            if (tc != null) {
                tc.activate(player);
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource r) {
        if (!AEConfig.instance().isEnableEffects()) {
            return;
        }

        var blockEntity = this.getBlockEntity(level, pos);
        if (blockEntity != null && blockEntity.isWorking()) {
            if (r.nextFloat() < 0.5) {
                return;
            }

            var rotation = FacingToRotation.get(blockEntity.getForward(), blockEntity.getUp());

            for (int bolts = 0; bolts < 3; bolts++) {
                // Slightly offset the lightning arc on the x/z plane
                var xOff = Mth.randomBetween(r, -0.15f, 0.15f);
                var zOff = Mth.randomBetween(r, -0.15f, 0.15f);

                // Compute two points in the charger block. One at the bottom, and one on the top.
                // Account for the rotation while doing this.
                var center = new Vector3f(pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f);
                var origin = new Vector3f(xOff, -0.3f, zOff);
                origin.transform(rotation.getRot());
                origin.add(center);
                var target = new Vector3f(xOff, 0.3f, zOff);
                target.transform(rotation.getRot());
                target.add(center);

                // Split the arcs between arc coming from the top/bottom of the charger since it's symmetrical
                if (r.nextBoolean()) {
                    var tmp = target;
                    target = origin;
                    origin = tmp;
                }

                if (AppEngClient.instance().shouldAddParticles(r)) {
                    Minecraft.getInstance().particleEngine.createParticle(
                            new LightningArcParticleData(new Vec3(target)),
                            origin.x(),
                            origin.y(),
                            origin.z(),
                            0.0, 0.0, 0.0);
                }
            }
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {

        final ChargerBlockEntity blockEntity = this.getBlockEntity(level, pos);
        if (blockEntity != null) {
            final double twoPixels = 2.0 / 16.0;
            final Direction up = blockEntity.getUp();
            final Direction forward = blockEntity.getForward();
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
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
            CollisionContext context) {
        return Shapes.create(new AABB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0));
    }

}
