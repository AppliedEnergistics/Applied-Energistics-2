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
import appeng.api.implementations.items.IAEWrench;
import appeng.api.parts.IPartHost;
import appeng.api.parts.SelectedPart;
import appeng.api.util.DimensionalBlockPos;
import appeng.api.util.INetworkToolAgent;
import appeng.container.AEBaseContainer;
import appeng.container.ContainerLocator;
import appeng.container.ContainerOpener;
import appeng.container.me.networktool.NetworkStatusContainer;
import appeng.container.me.networktool.NetworkToolContainer;
import appeng.core.Api;
import appeng.core.AppEng;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.ClickPacket;
import appeng.items.AEBaseItem;
import appeng.items.contents.NetworkToolViewer;
import appeng.util.InteractionUtil;
import appeng.util.Platform;

import net.minecraft.item.Item.Properties;

public class NetworkToolItem extends AEBaseItem implements IGuiItem, IAEWrench {

    public NetworkToolItem(Properties properties) {
        super(properties);
    }

    @Override
    public NetworkToolViewer getGuiObject(final ItemStack is, int playerInventorySlot, final World world,
            final BlockPos pos) {
        if (pos == null) {
            return new NetworkToolViewer(is, null, world.isClientSide());
        }
        var host = Api.instance().grid().getNodeHost(world, pos);
        return new NetworkToolViewer(is, host, world.isClientSide());
    }

    @Override
    public ActionResult<ItemStack> use(final World w, final PlayerEntity p, final Hand hand) {
        if (w.isClientSide()) {
            final RayTraceResult mop = AppEng.instance().getCurrentMouseOver();

            if (mop == null || mop.getType() == RayTraceResult.Type.MISS) {
                NetworkHandler.instance().sendToServer(new ClickPacket(hand));
            }
        }

        return new ActionResult<>(ActionResultType.sidedSuccess(w.isClientSide()), p.getItemInHand(hand));
    }

    @Override
    public ActionResultType onItemUseFirst(ItemStack stack, ItemUseContext context) {
        World w = context.getLevel();
        final BlockRayTraceResult mop = new BlockRayTraceResult(context.getClickLocation(), context.getClickedFace(),
                context.getClickedPos(), context.isInside());
        final TileEntity te = w.getBlockEntity(context.getClickedPos());

        if (te instanceof IPartHost) {
            final SelectedPart part = ((IPartHost) te).selectPart(mop.getLocation());

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

        if (w.isClientSide()) {
            NetworkHandler.instance().sendToServer(new ClickPacket(context));
        }

        return ActionResultType.sidedSuccess(w.isClientSide());
    }

    @Override
    public boolean doesSneakBypassUse(ItemStack stack, IWorldReader world, BlockPos pos, PlayerEntity player) {
        return true;
    }

    public boolean serverSideToolLogic(ItemUseContext useContext) {
        BlockPos pos = useContext.getClickedPos();
        PlayerEntity p = useContext.getPlayer();
        World w = p.level;
        Hand hand = useContext.getHand();
        Direction side = useContext.getClickedFace();

        if (!Platform.hasPermissions(new DimensionalBlockPos(w, pos), p)) {
            return false;
        }

        // The network tool has special behavior for machines hosting world-accessible nodes
        var nodeHost = Api.instance().grid().getNodeHost(w, pos);

        var bs = w.getBlockState(pos);
        if (!InteractionUtil.isInAlternateUseMode(p)) {
            if (nodeHost == null && bs.rotate(w, pos, Rotation.CLOCKWISE_90) != bs) {
                bs.neighborChanged(w, pos, Blocks.AIR, pos, false);
                p.swing(hand);
                return !w.isClientSide;
            }
        }

        if (!InteractionUtil.isInAlternateUseMode(p)) {
            if (p.containerMenu instanceof AEBaseContainer) {
                return true;
            }

            if (nodeHost != null) {
                ContainerOpener.openContainer(NetworkStatusContainer.TYPE, p,
                        ContainerLocator.forItemUseContext(useContext));
            } else {
                ContainerOpener.openContainer(NetworkToolContainer.TYPE, p, ContainerLocator.forHand(p, hand));
            }

            return true;
        } else {
            BlockRayTraceResult rtr = new BlockRayTraceResult(useContext.getClickLocation(), side, pos, false);
            bs.use(w, p, hand, rtr);
        }

        return false;
    }

    @Override
    public boolean canWrench(final ItemStack wrench, final PlayerEntity player, final BlockPos pos) {
        return true;
    }

}
