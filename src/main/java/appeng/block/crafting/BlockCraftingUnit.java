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

package appeng.block.crafting;


import appeng.api.util.AEPartLocation;
import appeng.block.AEBaseTileBlock;
import appeng.client.UnlistedProperty;
import appeng.client.render.crafting.CraftingCubeState;
import appeng.core.sync.GuiBridge;
import appeng.tile.crafting.TileCraftingTile;
import appeng.util.Platform;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

import java.util.EnumSet;


public class BlockCraftingUnit extends AEBaseTileBlock {
    public static final PropertyBool FORMED = PropertyBool.create("formed");
    public static final PropertyBool POWERED = PropertyBool.create("powered");
    public static final UnlistedProperty<CraftingCubeState> STATE = new UnlistedProperty<>("state", CraftingCubeState.class);

    public final CraftingUnitType type;

    public BlockCraftingUnit(final CraftingUnitType type) {
        super(Material.IRON);

        this.type = type;
    }

    @Override
    protected IProperty[] getAEStates() {
        return new IProperty[]{POWERED, FORMED};
    }

    @Override
    public IExtendedBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {

        EnumSet<EnumFacing> connections = EnumSet.noneOf(EnumFacing.class);

        for (EnumFacing facing : EnumFacing.values()) {
            if (this.isConnected(world, pos, facing)) {
                connections.add(facing);
            }
        }

        IExtendedBlockState extState = (IExtendedBlockState) state;

        return extState.withProperty(STATE, new CraftingCubeState(connections));
    }

    private boolean isConnected(IBlockAccess world, BlockPos pos, EnumFacing side) {
        BlockPos adjacentPos = pos.offset(side);
        return world.getBlockState(adjacentPos).getBlock() instanceof BlockCraftingUnit;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new ExtendedBlockState(this, this.getAEStates(), new IUnlistedProperty[]{STATE});
    }

    @Override
    public IBlockState getStateFromMeta(final int meta) {
        return this.getDefaultState().withProperty(POWERED, (meta & 1) == 1).withProperty(FORMED, (meta & 2) == 2);
    }

    @Override
    public int getMetaFromState(final IBlockState state) {
        boolean p = state.getValue(POWERED);
        boolean f = state.getValue(FORMED);
        return (p ? 1 : 0) | (f ? 2 : 0);
    }

    @Override
    public void neighborChanged(final IBlockState state, final World worldIn, final BlockPos pos, final Block blockIn, final BlockPos fromPos) {
        final TileCraftingTile cp = this.getTileEntity(worldIn, pos);
        if (cp != null) {
            cp.updateMultiBlock();
        }
    }

    @Override
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public void breakBlock(final World w, final BlockPos pos, final IBlockState state) {
        final TileCraftingTile cp = this.getTileEntity(w, pos);
        if (cp != null) {
            cp.breakCluster();
        }

        super.breakBlock(w, pos, state);
    }

    @Override
    public boolean onBlockActivated(final World w, final BlockPos pos, final IBlockState state, final EntityPlayer p, final EnumHand hand, final EnumFacing side, final float hitX, final float hitY, final float hitZ) {
        final TileCraftingTile tg = this.getTileEntity(w, pos);

        if (tg != null && !p.isSneaking() && tg.isFormed() && tg.isActive()) {
            if (Platform.isClient()) {
                return true;
            }

            Platform.openGUI(p, tg, AEPartLocation.fromFacing(side), GuiBridge.GUI_CRAFTING_CPU);
            return true;
        }

        return super.onBlockActivated(w, pos, state, p, hand, side, hitX, hitY, hitZ);
    }

    public enum CraftingUnitType {
        UNIT, ACCELERATOR, STORAGE_1K, STORAGE_4K, STORAGE_16K, STORAGE_64K, MONITOR
    }
}
