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

package appeng.block;


import appeng.api.util.IOrientable;
import appeng.api.util.IOrientableBlock;
import appeng.block.misc.BlockLightDetector;
import appeng.block.misc.BlockSkyCompass;
import appeng.block.networking.BlockWireless;
import appeng.me.helpers.IGridProxyable;
import appeng.tile.AEBaseTile;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;


public class AEBaseItemBlock extends ItemBlock {

    private final AEBaseBlock blockType;

    public AEBaseItemBlock(final Block id) {
        super(id);
        this.blockType = (AEBaseBlock) id;
        this.hasSubtypes = this.blockType.hasSubtypes();
    }

    @Override
    public int getMetadata(final int dmg) {
        if (this.hasSubtypes) {
            return dmg;
        }
        return 0;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public final void addInformation(final ItemStack itemStack, final World world, final List<String> toolTip, final ITooltipFlag advancedTooltips) {
        this.addCheckedInformation(itemStack, world, toolTip, advancedTooltips);
    }

    @SideOnly(Side.CLIENT)
    public void addCheckedInformation(final ItemStack itemStack, final World world, final List<String> toolTip, final ITooltipFlag advancedTooltips) {
        this.blockType.addInformation(itemStack, world, toolTip, advancedTooltips);
    }

    @Override
    public boolean isBookEnchantable(final ItemStack itemstack1, final ItemStack itemstack2) {
        return false;
    }

    @Override
    public String getUnlocalizedName(final ItemStack is) {
        return this.blockType.getUnlocalizedName(is);
    }

    @Override
    public boolean placeBlockAt(final ItemStack stack, final EntityPlayer player, final World w, final BlockPos pos, final EnumFacing side, final float hitX, final float hitY, final float hitZ, final IBlockState newState) {
        EnumFacing up = null;
        EnumFacing forward = null;

        if (this.blockType instanceof AEBaseTileBlock) {
            if (this.blockType instanceof BlockLightDetector) {
                up = side;
                if (up == EnumFacing.UP || up == EnumFacing.DOWN) {
                    forward = EnumFacing.SOUTH;
                } else {
                    forward = EnumFacing.UP;
                }
            } else if (this.blockType instanceof BlockWireless || this.blockType instanceof BlockSkyCompass) {
                forward = side;
                if (forward == EnumFacing.UP || forward == EnumFacing.DOWN) {
                    up = EnumFacing.SOUTH;
                } else {
                    up = EnumFacing.UP;
                }
            } else {
                up = EnumFacing.UP;

                final byte rotation = (byte) (MathHelper.floor((player.rotationYaw * 4F) / 360F + 2.5D) & 3);

                switch (rotation) {
                    default:
                    case 0:
                        forward = EnumFacing.SOUTH;
                        break;
                    case 1:
                        forward = EnumFacing.WEST;
                        break;
                    case 2:
                        forward = EnumFacing.NORTH;
                        break;
                    case 3:
                        forward = EnumFacing.EAST;
                        break;
                }

                if (player.rotationPitch > 65) {
                    up = forward.getOpposite();
                    forward = EnumFacing.UP;
                } else if (player.rotationPitch < -65) {
                    up = forward.getOpposite();
                    forward = EnumFacing.DOWN;
                }
            }
        }

        IOrientable ori = null;
        if (this.blockType instanceof IOrientableBlock) {
            ori = ((IOrientableBlock) this.blockType).getOrientable(w, pos);
            up = side;
            forward = EnumFacing.SOUTH;
            if (up.getFrontOffsetY() == 0) {
                forward = EnumFacing.UP;
            }
        }

        if (!this.blockType.isValidOrientation(w, pos, forward, up)) {
            return false;
        }

        if (super.placeBlockAt(stack, player, w, pos, side, hitX, hitY, hitZ, newState)) {
            if (this.blockType instanceof AEBaseTileBlock && !(this.blockType instanceof BlockLightDetector)) {
                final AEBaseTile tile = ((AEBaseTileBlock) this.blockType).getTileEntity(w, pos);
                ori = tile;

                if (tile == null) {
                    return true;
                }

                if (ori.canBeRotated() && !this.blockType.hasCustomRotation()) {
                    ori.setOrientation(forward, up);
                }

                if (tile instanceof IGridProxyable) {
                    ((IGridProxyable) tile).getProxy().setOwner(player);
                }

                tile.onPlacement(stack, player, side);
            } else if (this.blockType instanceof IOrientableBlock) {
                ori.setOrientation(forward, up);
            }

            return true;
        }
        return false;
    }
}
