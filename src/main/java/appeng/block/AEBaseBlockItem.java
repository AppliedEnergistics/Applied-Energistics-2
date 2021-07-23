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

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.api.util.IOrientable;
import appeng.api.util.IOrientableBlock;
import appeng.block.misc.LightDetectorBlock;
import appeng.block.misc.SkyCompassBlock;
import appeng.block.networking.WirelessBlock;
import appeng.tile.AEBaseTileEntity;

public class AEBaseBlockItem extends BlockItem {

    private final AEBaseBlock blockType;

    public AEBaseBlockItem(final Block id, net.minecraft.world.item.Item.Properties props) {
        super(id, props);
        this.blockType = (AEBaseBlock) id;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public final void appendHoverText(final ItemStack itemStack, final Level world, final List<net.minecraft.network.chat.Component> toolTip,
                                      final net.minecraft.world.item.TooltipFlag advancedTooltips) {
        this.addCheckedInformation(itemStack, world, toolTip, advancedTooltips);
    }

    @OnlyIn(Dist.CLIENT)
    public void addCheckedInformation(final net.minecraft.world.item.ItemStack itemStack, final Level world, final List<net.minecraft.network.chat.Component> toolTip,
                                      final net.minecraft.world.item.TooltipFlag advancedTooltips) {
        this.blockType.appendHoverText(itemStack, world, toolTip, advancedTooltips);
    }

    @Override
    public boolean isBookEnchantable(final net.minecraft.world.item.ItemStack itemstack1, final net.minecraft.world.item.ItemStack itemstack2) {
        return false;
    }

    @Override
    public String getDescriptionId(final net.minecraft.world.item.ItemStack is) {
        return this.blockType.getDescriptionId();
    }

    @Override
    public InteractionResult place(BlockPlaceContext context) {

        Direction up = null;
        Direction forward = null;

        Direction side = context.getClickedFace();
        Player player = context.getPlayer();

        if (this.blockType instanceof AEBaseTileBlock) {
            if (this.blockType instanceof LightDetectorBlock) {
                up = side;
                if (up == net.minecraft.core.Direction.UP || up == net.minecraft.core.Direction.DOWN) {
                    forward = Direction.SOUTH;
                } else {
                    forward = Direction.UP;
                }
            } else if (this.blockType instanceof WirelessBlock || this.blockType instanceof SkyCompassBlock) {
                forward = side;
                if (forward == Direction.UP || forward == Direction.DOWN) {
                    up = net.minecraft.core.Direction.SOUTH;
                } else {
                    up = Direction.UP;
                }
            } else {
                up = net.minecraft.core.Direction.UP;
                forward = context.getHorizontalDirection().getOpposite();

                if (player != null) {
                    if (player.xRot > 65) {
                        up = forward.getOpposite();
                        forward = Direction.UP;
                    } else if (player.xRot < -65) {
                        up = forward.getOpposite();
                        forward = Direction.DOWN;
                    }
                }
            }
        }

        IOrientable ori = null;
        if (this.blockType instanceof IOrientableBlock) {
            ori = ((IOrientableBlock) this.blockType).getOrientable(context.getLevel(), context.getClickedPos());
            up = side;
            forward = Direction.SOUTH;
            if (up.getStepY() == 0) {
                forward = Direction.UP;
            }
        }

        if (!this.blockType.isValidOrientation(context.getLevel(), context.getClickedPos(), forward, up)) {
            return InteractionResult.FAIL;
        }

        InteractionResult result = super.place(context);
        if (!result.consumesAction()) {
            return result;
        }

        if (this.blockType instanceof AEBaseTileBlock && !(this.blockType instanceof LightDetectorBlock)) {
            final AEBaseTileEntity tile = ((AEBaseTileBlock<?>) this.blockType).getTileEntity(context.getLevel(),
                    context.getClickedPos());
            ori = tile;

            if (tile == null) {
                return result;
            }

            if (ori.canBeRotated() && !this.blockType.hasCustomRotation()) {
                ori.setOrientation(forward, up);
            }

            if (tile instanceof IOwnerAwareTile ownerAwareTile) {
                ownerAwareTile.setOwner(player);
            }

            tile.onPlacement(context);
        } else if (this.blockType instanceof IOrientableBlock) {
            ori.setOrientation(forward, up);
        }

        return result;

    }
}
