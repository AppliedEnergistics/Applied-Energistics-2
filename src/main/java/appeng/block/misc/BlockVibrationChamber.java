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


import appeng.api.util.AEPartLocation;
import appeng.block.AEBaseTileBlock;
import appeng.core.AEConfig;
import appeng.core.sync.GuiBridge;
import appeng.tile.AEBaseTile;
import appeng.tile.misc.TileVibrationChamber;
import appeng.util.Platform;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Random;


public final class BlockVibrationChamber extends AEBaseTileBlock {

    // Indicates that the vibration chamber is currently working
    private static final PropertyBool ACTIVE = PropertyBool.create("active");

    public BlockVibrationChamber() {
        super(Material.IRON);
        this.setHardness(4.2F);
        this.setDefaultState(this.getDefaultState().withProperty(ACTIVE, false));
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
        TileVibrationChamber te = this.getTileEntity(world, pos);
        boolean active = te != null && te.isOn;

        return super.getActualState(state, world, pos)
                .withProperty(ACTIVE, active);
    }

    @Override
    protected IProperty[] getAEStates() {
        return new IProperty[]{ACTIVE};
    }

    @Override
    public boolean onActivated(final World w, final BlockPos pos, final EntityPlayer player, final EnumHand hand, final @Nullable ItemStack heldItem, final EnumFacing side, final float hitX, final float hitY, final float hitZ) {
        if (player.isSneaking()) {
            return false;
        }

        if (Platform.isServer()) {
            final TileVibrationChamber tc = this.getTileEntity(w, pos);
            if (tc != null && !player.isSneaking()) {
                Platform.openGUI(player, tc, AEPartLocation.fromFacing(side), GuiBridge.GUI_VIBRATION_CHAMBER);
                return true;
            }
        }

        return true;
    }

    @Override
    public void randomDisplayTick(final IBlockState state, final World w, final BlockPos pos, final Random r) {
        if (!AEConfig.instance().isEnableEffects()) {
            return;
        }

        final AEBaseTile tile = this.getTileEntity(w, pos);
        if (tile instanceof TileVibrationChamber) {
            final TileVibrationChamber tc = (TileVibrationChamber) tile;
            if (tc.isOn) {
                float f1 = pos.getX() + 0.5F;
                float f2 = pos.getY() + 0.5F;
                float f3 = pos.getZ() + 0.5F;

                final EnumFacing forward = tc.getForward();
                final EnumFacing up = tc.getUp();

                final int west_x = forward.getFrontOffsetY() * up.getFrontOffsetZ() - forward.getFrontOffsetZ() * up.getFrontOffsetY();
                final int west_y = forward.getFrontOffsetZ() * up.getFrontOffsetX() - forward.getFrontOffsetX() * up.getFrontOffsetZ();
                final int west_z = forward.getFrontOffsetX() * up.getFrontOffsetY() - forward.getFrontOffsetY() * up.getFrontOffsetX();

                f1 += forward.getFrontOffsetX() * 0.6;
                f2 += forward.getFrontOffsetY() * 0.6;
                f3 += forward.getFrontOffsetZ() * 0.6;

                final float ox = r.nextFloat();
                final float oy = r.nextFloat() * 0.2f;

                f1 += up.getFrontOffsetX() * (-0.3 + oy);
                f2 += up.getFrontOffsetY() * (-0.3 + oy);
                f3 += up.getFrontOffsetZ() * (-0.3 + oy);

                f1 += west_x * (0.3 * ox - 0.15);
                f2 += west_y * (0.3 * ox - 0.15);
                f3 += west_z * (0.3 * ox - 0.15);

                w.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, f1, f2, f3, 0.0D, 0.0D, 0.0D);
                w.spawnParticle(EnumParticleTypes.FLAME, f1, f2, f3, 0.0D, 0.0D, 0.0D);
            }
        }
    }
}
