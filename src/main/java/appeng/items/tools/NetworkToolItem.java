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

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
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
import appeng.hooks.AEToolItem;
import appeng.items.AEBaseItem;
import appeng.items.contents.NetworkToolViewer;
import appeng.util.PartHostWrenching;
import appeng.util.Platform;

public class NetworkToolItem extends AEBaseItem implements IGuiItem, IAEWrench, AEToolItem {

    public NetworkToolItem(Properties properties) {
        super(properties);
    }

    @Override
    public IGuiItemObject getGuiObject(final ItemStack is, int playerInventorySlot, final World world,
            final BlockPos pos) {
        if (pos == null) {
            return new NetworkToolViewer(is, null);
        }
        final TileEntity te = world.getTileEntity(pos);
        return new NetworkToolViewer(is, (IGridHost) (te instanceof IGridHost ? te : null));
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(final World w, final PlayerEntity p, final Hand hand) {
        if (Platform.isClient()) {
            final RayTraceResult mop = AppEng.instance().getRTR();

            if (mop == null || mop.getType() == RayTraceResult.Type.field_1333) {
                NetworkHandler.instance().sendToServer(new ClickPacket(hand));
            }
        }

        return new ActionResult<>(ActionResultType.field_5812, p.getHeldItem(hand));
    }

    @Override
    public ActionResultType onItemUseFirst(ItemStack stack, ItemUseContext context) {
        final BlockRayTraceResult mop = new BlockRayTraceResult(context.getHitVec(), context.getFace(), context.getPos(),
                context.isInside());
        final TileEntity te = context.getWorld().getTileEntity(context.getPos());

        if (te instanceof IPartHost) {
            Vector3d relativePosition = mop.getHitVec().subtract(mop.getPos().getX(), mop.getPos().getY(),
                    mop.getPos().getZ());
            IPartHost host = (IPartHost) te;
            final SelectedPart part = host.selectPart(relativePosition);

            if (part.part != null || part.facade != null) {
                if (part.part instanceof INetworkToolAgent && !((INetworkToolAgent) part.part).showNetworkInfo(mop)) {
                    return ActionResultType.field_5814;
                } else if (context.getPlayer().isCrouching()) {
                    PartHostWrenching.wrenchPart(context.getWorld(), context.getPos(), host, part);
                    return ActionResultType.field_5812;
                }
            }
        } else if (te instanceof INetworkToolAgent && !((INetworkToolAgent) te).showNetworkInfo(mop)) {
            return ActionResultType.field_5814;
        }

        if (Platform.isClient()) {
            NetworkHandler.instance().sendToServer(new ClickPacket(context));
        }

        return ActionResultType.field_5812;
    }

    public boolean serverSideToolLogic(ItemUseContext useContext) {
        BlockPos pos = useContext.getPos();
        PlayerEntity p = useContext.getPlayer();
        World w = p.world;
        Hand hand = useContext.getHand();
        Direction side = useContext.getFace();

        if (!Platform.hasPermissions(new DimensionalCoord(w, pos), p)) {
            return false;
        }

        final BlockState bs = w.getBlockState(pos);
        if (!p.isCrouching()) {
            final TileEntity te = w.getTileEntity(pos);
            if (!(te instanceof IGridHost)) {
                BlockState rotatedState = bs.rotate(Rotation.field_11463);
                if (rotatedState != bs) {
                    w.setBlockState(pos, rotatedState, 3);
                    bs.neighborChanged(w, pos, Blocks.AIR, pos, false);
                    p.swingArm(hand);
                    return !w.isRemote;
                }
            }
        }

        if (!p.isCrouching()) {
            if (p.openContainer instanceof AEBaseContainer) {
                return true;
            }

            final TileEntity te = w.getTileEntity(pos);

            if (te instanceof IGridHost) {
                ContainerOpener.openContainer(NetworkStatusContainer.TYPE, p,
                        ContainerLocator.forItemUseContext(useContext));
            } else {
                ContainerOpener.openContainer(NetworkToolContainer.TYPE, p, ContainerLocator.forHand(p, hand));
            }

            return true;
        } else {
            BlockRayTraceResult rtr = new BlockRayTraceResult(useContext.getHitVec(), side, pos, false);
            bs.onBlockActivated(w, p, hand, rtr);
        }

        return false;
    }

    @Override
    public boolean canWrench(final ItemStack wrench, final PlayerEntity player, final BlockPos pos) {
        return true;
    }
}
