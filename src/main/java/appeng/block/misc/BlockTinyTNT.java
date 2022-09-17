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


import appeng.block.AEBaseBlock;
import appeng.entity.EntityTinyTNTPrimed;
import appeng.helpers.ICustomCollision;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;


public class BlockTinyTNT extends AEBaseBlock implements ICustomCollision {

    public BlockTinyTNT() {
        super(Material.TNT);

        this.boundingBox = new AxisAlignedBB(0.25f, 0.0f, 0.25f, 0.75f, 0.5f, 0.75f);

        this.setLightOpacity(2);
        this.setFullSize(false);
        this.setOpaque(false);

        this.setSoundType(SoundType.GROUND);
        this.setHardness(0F);
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean onActivated(final World w, final BlockPos pos, final EntityPlayer player, final EnumHand hand, final @Nullable ItemStack heldItem, final EnumFacing side, final float hitX, final float hitY, final float hitZ) {
        if (heldItem != null && heldItem.getItem() == Items.FLINT_AND_STEEL) {
            this.startFuse(w, pos, player);
            w.setBlockToAir(pos);
            heldItem.damageItem(1, player);
            return true;
        } else {
            return super.onActivated(w, pos, player, hand, heldItem, side, hitX, hitY, hitZ);
        }
    }

    public void startFuse(final World w, final BlockPos pos, final EntityLivingBase igniter) {
        if (!w.isRemote) {
            final EntityTinyTNTPrimed primedTinyTNTEntity = new EntityTinyTNTPrimed(w, pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F, igniter);
            w.spawnEntity(primedTinyTNTEntity);
            w.playSound(null, primedTinyTNTEntity.posX, primedTinyTNTEntity.posY, primedTinyTNTEntity.posZ, SoundEvents.ENTITY_TNT_PRIMED,
                    SoundCategory.BLOCKS, 1, 1);
        }
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos) {
        if (world.isBlockIndirectlyGettingPowered(pos) > 0) {
            this.startFuse(world, pos, null);
            world.setBlockToAir(pos);
        }
    }

    @Override
    public void onBlockAdded(final World w, final BlockPos pos, final IBlockState state) {
        super.onBlockAdded(w, pos, state);

        if (w.isBlockIndirectlyGettingPowered(pos) > 0) {
            this.startFuse(w, pos, null);
            w.setBlockToAir(pos);
        }
    }

    @Override
    public void onEntityWalk(final World w, final BlockPos pos, final Entity entity) {
        if (entity instanceof EntityArrow && !w.isRemote) {
            final EntityArrow entityarrow = (EntityArrow) entity;

            if (entityarrow.isBurning()) {
                this.startFuse(w, pos, entityarrow.shootingEntity instanceof EntityLivingBase ? (EntityLivingBase) entityarrow.shootingEntity : null);
                w.setBlockToAir(pos);
            }
        }
    }

    @Override
    public boolean canDropFromExplosion(final Explosion exp) {
        return false;
    }

    @Override
    public void onBlockExploded(final World w, final BlockPos pos, final Explosion exp) {
        super.onBlockExploded(w, pos, exp);
        if (!w.isRemote) {
            final EntityTinyTNTPrimed primedTinyTNTEntity = new EntityTinyTNTPrimed(w, pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F, exp
                    .getExplosivePlacedBy());
            primedTinyTNTEntity.setFuse(w.rand.nextInt(primedTinyTNTEntity.getFuse() / 4) + primedTinyTNTEntity.getFuse() / 8);
            w.spawnEntity(primedTinyTNTEntity);
        }
    }

    @Override
    public Iterable<AxisAlignedBB> getSelectedBoundingBoxesFromPool(final World w, final BlockPos pos, final Entity thePlayer, final boolean b) {
        return Collections.singletonList(new AxisAlignedBB(0.25, 0, 0.25, 0.75, 0.5, 0.75));
    }

    @Override
    public void addCollidingBlockToList(final World w, final BlockPos pos, final AxisAlignedBB bb, final List<AxisAlignedBB> out, final Entity e) {
        out.add(new AxisAlignedBB(0.25, 0, 0.25, 0.75, 0.5, 0.75));
    }
}
