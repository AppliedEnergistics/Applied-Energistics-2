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

import java.util.List;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

import appeng.api.util.IOrientable;
import appeng.api.util.IOrientableBlock;
import appeng.block.misc.LightDetectorBlock;
import appeng.block.misc.SkyCompassBlock;
import appeng.block.networking.WirelessBlock;
import appeng.me.helpers.IGridProxyable;
import appeng.tile.AEBaseTileEntity;

public class AEBaseBlockItem extends BlockItem {

    private final AEBaseBlock blockType;

    public AEBaseBlockItem(final Block id, Item.Properties props) {
        super(id, props);
        this.blockType = (AEBaseBlock) id;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public final void addInformation(final ItemStack itemStack, final World world, final List<ITextComponent> toolTip,
            final ITooltipFlag advancedTooltips) {
        this.addCheckedInformation(itemStack, world, toolTip, advancedTooltips);
    }

    @Environment(EnvType.CLIENT)
    public void addCheckedInformation(final ItemStack itemStack, final World world, final List<ITextComponent> toolTip,
            final ITooltipFlag advancedTooltips) {
        this.blockType.addInformation(itemStack, world, toolTip, advancedTooltips);
    }

    @Override
    public String getTranslationKey(final ItemStack is) {
        return this.blockType.getTranslationKey();
    }

    @Override
    public ActionResultType tryPlace(BlockItemUseContext context) {

        Direction up = null;
        Direction forward = null;

        Direction side = context.getFace();
        PlayerEntity player = context.getPlayer();

        if (this.blockType instanceof AEBaseTileBlock) {
            if (this.blockType instanceof LightDetectorBlock) {
                up = side;
                if (up == Direction.UP || up == Direction.DOWN) {
                    forward = Direction.SOUTH;
                } else {
                    forward = Direction.UP;
                }
            } else if (this.blockType instanceof WirelessBlock || this.blockType instanceof SkyCompassBlock) {
                forward = side;
                if (forward == Direction.UP || forward == Direction.DOWN) {
                    up = Direction.SOUTH;
                } else {
                    up = Direction.UP;
                }
            } else {
                up = Direction.UP;
                forward = context.getPlacementHorizontalFacing().getOpposite();

                if (player != null) {
                    if (player.rotationPitch > 65) {
                        up = forward.getOpposite();
                        forward = Direction.UP;
                    } else if (player.rotationPitch < -65) {
                        up = forward.getOpposite();
                        forward = Direction.DOWN;
                    }
                }
            }
        }

        IOrientable ori = null;
        if (this.blockType instanceof IOrientableBlock) {
            ori = ((IOrientableBlock) this.blockType).getOrientable(context.getWorld(), context.getPos());
            up = side;
            forward = Direction.SOUTH;
            if (up.getYOffset() == 0) {
                forward = Direction.UP;
            }
        }

        if (!this.blockType.isValidOrientation(context.getWorld(), context.getPos(), forward, up)) {
            return ActionResultType.FAIL;
        }

        ActionResultType result = super.tryPlace(context);
        if (!result.isSuccessOrConsume()) {
            return result;
        }

        if (this.blockType instanceof AEBaseTileBlock && !(this.blockType instanceof LightDetectorBlock)) {
            final AEBaseTileEntity tile = ((AEBaseTileBlock<?>) this.blockType).getTileEntity(context.getWorld(),
                    context.getPos());
            ori = tile;

            if (tile == null) {
                return result;
            }

            if (ori.canBeRotated() && !this.blockType.hasCustomRotation()) {
                ori.setOrientation(forward, up);
            }

            if (tile instanceof IGridProxyable) {
                ((IGridProxyable) tile).getProxy().setOwner(player);
            }

            tile.onPlacement(context);
        } else if (this.blockType instanceof IOrientableBlock) {
            ori.setOrientation(forward, up);
        }

        return result;

    }
}
