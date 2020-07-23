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

package appeng.items.tools;

import appeng.hooks.AEToolItem;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;

import appeng.api.implementations.guiobjects.IGuiItem;
import appeng.api.implementations.guiobjects.IGuiItemObject;
import appeng.api.implementations.items.IAEWrench;
import appeng.api.networking.IGridHost;
import appeng.api.parts.IPartHost;
import appeng.api.parts.SelectedPart;
import appeng.api.util.DimensionalCoord;
import appeng.api.util.INetworkToolAgent;
import appeng.container.AEBaseContainer;
import appeng.container.ContainerLocator;
import appeng.container.ContainerOpener;
import appeng.container.implementations.NetworkStatusContainer;
import appeng.container.implementations.NetworkToolContainer;
import appeng.core.AppEng;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.ClickPacket;
import appeng.items.AEBaseItem;
import appeng.items.contents.NetworkToolViewer;
import appeng.util.Platform;

public class NetworkToolItem extends AEBaseItem implements IGuiItem, IAEWrench, AEToolItem {

    public NetworkToolItem(Settings properties) {
        super(properties);
    }

    @Override
    public IGuiItemObject getGuiObject(final ItemStack is, int playerInventorySlot, final World world,
            final BlockPos pos) {
        if (pos == null) {
            return new NetworkToolViewer(is, null);
        }
        final BlockEntity te = world.getBlockEntity(pos);
        return new NetworkToolViewer(is, (IGridHost) (te instanceof IGridHost ? te : null));
    }

    @Override
    public TypedActionResult<ItemStack> use(final World w, final PlayerEntity p, final Hand hand) {
        if (Platform.isClient()) {
            final HitResult mop = AppEng.instance().getRTR();

            if (mop == null || mop.getType() == HitResult.Type.MISS) {
                NetworkHandler.instance().sendToServer(new ClickPacket(hand));
            }
        }

        return new TypedActionResult<>(ActionResult.SUCCESS, p.getStackInHand(hand));
    }

    @Override
    public ActionResult onItemUseFirst(ItemStack stack, ItemUsageContext context) {
        final BlockHitResult mop = new BlockHitResult(context.getHitPos(), context.getSide(),
                context.getBlockPos(), context.hitsInsideBlock());
        final BlockEntity te = context.getWorld().getBlockEntity(context.getBlockPos());

        if (te instanceof IPartHost) {
            final SelectedPart part = ((IPartHost) te).selectPart(mop.getPos());

            if (part.part != null || part.facade != null) {
                if (part.part instanceof INetworkToolAgent && !((INetworkToolAgent) part.part).showNetworkInfo(mop)) {
                    return ActionResult.FAIL;
                } else if (context.getPlayer().isInSneakingPose()) {
                    return ActionResult.PASS;
                }
            }
        } else if (te instanceof INetworkToolAgent && !((INetworkToolAgent) te).showNetworkInfo(mop)) {
            return ActionResult.FAIL;
        }

        if (Platform.isClient()) {
            NetworkHandler.instance().sendToServer(new ClickPacket(context));
        }

        return ActionResult.SUCCESS;
    }

// FIXME FABRIC: No direct equivalent
// FIXME FABRIC: Might already be handled by onItemUseFirst though
// FIXME FABRIC    @Override
// FIXME FABRIC    public boolean doesSneakBypassUse(ItemStack stack, WorldView world, BlockPos pos, PlayerEntity player) {
// FIXME FABRIC        return true;
// FIXME FABRIC    }

    public boolean serverSideToolLogic(ItemUsageContext useContext) {
        BlockPos pos = useContext.getBlockPos();
        PlayerEntity p = useContext.getPlayer();
        World w = p.world;
        Hand hand = useContext.getHand();
        Direction side = useContext.getSide();

        if (!Platform.hasPermissions(new DimensionalCoord(w, pos), p)) {
            return false;
        }

        final BlockState bs = w.getBlockState(pos);
        if (!p.isInSneakingPose()) {
            final BlockEntity te = w.getBlockEntity(pos);
            if (!(te instanceof IGridHost)) {
                BlockState rotatedState = bs.rotate(BlockRotation.CLOCKWISE_90);
                if (rotatedState != bs) {
                    w.setBlockState(pos, rotatedState, 3);
                    bs.neighborUpdate(w, pos, Blocks.AIR, pos, false);
                    p.swingHand(hand);
                    return !w.isClient;
                }
            }
        }

        if (!p.isInSneakingPose()) {
            if (p.currentScreenHandler instanceof AEBaseContainer) {
                return true;
            }

            final BlockEntity te = w.getBlockEntity(pos);

            if (te instanceof IGridHost) {
                ContainerOpener.openContainer(NetworkStatusContainer.TYPE, p,
                        ContainerLocator.forItemUseContext(useContext));
            } else {
                ContainerOpener.openContainer(NetworkToolContainer.TYPE, p, ContainerLocator.forHand(p, hand));
            }

            return true;
        } else {
            BlockHitResult rtr = new BlockHitResult(useContext.getHitPos(), side, pos, false);
            bs.onUse(w, p, hand, rtr);
        }

        return false;
    }

    @Override
    public boolean canWrench(final ItemStack wrench, final PlayerEntity player, final BlockPos pos) {
        return true;
    }
}
