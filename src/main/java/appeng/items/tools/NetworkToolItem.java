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
import net.minecraft.world.IWorldReader;
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
import appeng.util.InteractionUtil;
import appeng.util.Platform;

public class NetworkToolItem extends AEBaseItem implements IGuiItem, IAEWrench {

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
        if (w.isRemote()) {
            final RayTraceResult mop = AppEng.proxy.getRTR();

            if (mop == null || mop.getType() == RayTraceResult.Type.MISS) {
                NetworkHandler.instance().sendToServer(new ClickPacket(hand));
            }
        }

        return new ActionResult<>(ActionResultType.func_233537_a_(w.isRemote()), p.getHeldItem(hand));
    }

    @Override
    public ActionResultType onItemUseFirst(ItemStack stack, ItemUseContext context) {
        World w = context.getWorld();
        final BlockRayTraceResult mop = new BlockRayTraceResult(context.getHitVec(), context.getFace(),
                context.getPos(), context.isInside());
        final TileEntity te = w.getTileEntity(context.getPos());

        if (te instanceof IPartHost) {
            final SelectedPart part = ((IPartHost) te).selectPart(mop.getHitVec());

            if (part.part != null || part.facade != null) {
                if (part.part instanceof INetworkToolAgent && !((INetworkToolAgent) part.part).showNetworkInfo(mop)) {
                    return ActionResultType.FAIL;
                } else if (InteractionUtil.isInAlternateUseMode(context.getPlayer())) {
                    return ActionResultType.PASS;
                }
            }
        } else if (te instanceof INetworkToolAgent && !((INetworkToolAgent) te).showNetworkInfo(mop)) {
            return ActionResultType.FAIL;
        }

        if (Platform.isClient()) {
            NetworkHandler.instance().sendToServer(new ClickPacket(context));
        }

        return ActionResultType.func_233537_a_(w.isRemote());
    }

    @Override
    public boolean doesSneakBypassUse(ItemStack stack, IWorldReader world, BlockPos pos, PlayerEntity player) {
        return true;
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
        if (!InteractionUtil.isInAlternateUseMode(p)) {
            final TileEntity te = w.getTileEntity(pos);
            if (!(te instanceof IGridHost)) {
                if (bs.rotate(w, pos, Rotation.CLOCKWISE_90) != bs) {
                    bs.neighborChanged(w, pos, Blocks.AIR, pos, false);
                    p.swingArm(hand);
                    return !w.isRemote;
                }
            }
        }

        if (!InteractionUtil.isInAlternateUseMode(p)) {
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
