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

package appeng.block.grindstone;


import appeng.api.implementations.tiles.ICrankable;
import appeng.block.AEBaseTileBlock;
import appeng.core.stats.Stats;
import appeng.tile.AEBaseTile;
import appeng.tile.grindstone.TileCrank;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;

import javax.annotation.Nullable;


public class BlockCrank extends AEBaseTileBlock {

    public BlockCrank() {
        super(Material.WOOD);

        this.setLightOpacity(0);
        this.setHarvestLevel("axe", 0);
        this.setFullSize(this.setOpaque(false));
    }

    @Override
    public boolean onActivated(final World w, final BlockPos pos, final EntityPlayer player, final EnumHand hand, final @Nullable ItemStack heldItem, final EnumFacing side, final float hitX, final float hitY, final float hitZ) {
        if (player instanceof FakePlayer || player == null) {
            this.dropCrank(w, pos);
            return true;
        }

        final AEBaseTile tile = this.getTileEntity(w, pos);
        if (tile instanceof TileCrank) {
            if (((TileCrank) tile).power()) {
                Stats.TurnedCranks.addToPlayer(player, 1);
            }
        }

        return true;
    }

    private void dropCrank(final World world, final BlockPos pos) {
        world.destroyBlock(pos, true); // w.destroyBlock( x, y, z, true );
        world.notifyBlockUpdate(pos, this.getDefaultState(), world.getBlockState(pos), 3);
    }

    @Override
    public void onBlockPlacedBy(final World world, final BlockPos pos, final IBlockState state, final EntityLivingBase placer, final ItemStack stack) {
        final AEBaseTile tile = this.getTileEntity(world, pos);
        if (tile != null) {
            final EnumFacing mnt = this.findCrankable(world, pos);
            EnumFacing forward = EnumFacing.UP;
            if (mnt == EnumFacing.UP || mnt == EnumFacing.DOWN) {
                forward = EnumFacing.SOUTH;
            }
            tile.setOrientation(forward, mnt.getOpposite());
        } else {
            this.dropCrank(world, pos);
        }
    }

    @Override
    public boolean isValidOrientation(final World w, final BlockPos pos, final EnumFacing forward, final EnumFacing up) {
        final TileEntity te = w.getTileEntity(pos);
        return !(te instanceof TileCrank) || this.isCrankable(w, pos, up.getOpposite());
    }

    private EnumFacing findCrankable(final World world, final BlockPos pos) {
        for (final EnumFacing dir : EnumFacing.VALUES) {
            if (this.isCrankable(world, pos, dir)) {
                return dir;
            }
        }
        return null;
    }

    private boolean isCrankable(final World world, final BlockPos pos, final EnumFacing offset) {
        final BlockPos o = pos.offset(offset);
        final TileEntity te = world.getTileEntity(o);

        return te instanceof ICrankable && ((ICrankable) te).canCrankAttach(offset.getOpposite());
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos) {

        final AEBaseTile tile = this.getTileEntity(world, pos);
        if (tile != null) {
            if (!this.isCrankable(world, pos, tile.getUp().getOpposite())) {
                this.dropCrank(world, pos);
            }
        } else {
            this.dropCrank(world, pos);
        }
    }

    @Override
    public boolean canPlaceBlockAt(final World world, final BlockPos pos) {
        return this.findCrankable(world, pos) != null;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean canPlaceTorchOnTop(IBlockState state, IBlockAccess world, BlockPos pos) {
        return false;
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }

}
