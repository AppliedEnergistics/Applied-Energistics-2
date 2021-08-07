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

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;

import appeng.api.implementations.guiobjects.IGuiItem;
import appeng.api.implementations.items.IAEWrench;
import appeng.api.parts.IPartHost;
import appeng.api.parts.SelectedPart;
import appeng.api.util.DimensionalBlockPos;
import appeng.api.util.INetworkToolAgent;
import appeng.menu.AEBaseContainer;
import appeng.menu.ContainerLocator;
import appeng.menu.ContainerOpener;
import appeng.menu.me.networktool.NetworkStatusContainer;
import appeng.menu.me.networktool.NetworkToolContainer;
import appeng.core.Api;
import appeng.core.AppEng;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.ClickPacket;
import appeng.items.AEBaseItem;
import appeng.items.contents.NetworkToolViewer;
import appeng.util.InteractionUtil;
import appeng.util.Platform;

public class NetworkToolItem extends AEBaseItem implements IGuiItem, IAEWrench {

    public NetworkToolItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public NetworkToolViewer getGuiObject(final ItemStack is, int playerInventorySlot, final Level level,
            final BlockPos pos) {
        if (pos == null) {
            return new NetworkToolViewer(is, null, level.isClientSide());
        }
        var host = Api.instance().grid().getNodeHost(level, pos);
        return new NetworkToolViewer(is, host, level.isClientSide());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(final Level level, final Player p, final InteractionHand hand) {
        if (level.isClientSide()) {
            final HitResult mop = AppEng.instance().getCurrentMouseOver();

            if (mop == null || mop.getType() == Type.MISS) {
                NetworkHandler.instance().sendToServer(new ClickPacket(hand));
            }
        }

        return new InteractionResultHolder<>(InteractionResult.sidedSuccess(level.isClientSide()),
                p.getItemInHand(hand));
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Level level = context.getLevel();
        final BlockHitResult mop = new BlockHitResult(context.getClickLocation(), context.getClickedFace(),
                context.getClickedPos(), context.isInside());
        final BlockEntity te = level.getBlockEntity(context.getClickedPos());

        if (te instanceof IPartHost) {
            final SelectedPart part = ((IPartHost) te).selectPart(mop.getLocation());

            if (part.part != null || part.facade != null) {
                if (part.part instanceof INetworkToolAgent && !((INetworkToolAgent) part.part).showNetworkInfo(mop)) {
                    return InteractionResult.FAIL;
                } else if (InteractionUtil.isInAlternateUseMode(context.getPlayer())) {
                    return InteractionResult.PASS;
                }
            }
        } else if (te instanceof INetworkToolAgent && !((INetworkToolAgent) te).showNetworkInfo(mop)) {
            return InteractionResult.FAIL;
        }

        if (level.isClientSide()) {
            NetworkHandler.instance().sendToServer(new ClickPacket(context));
        }

        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public boolean doesSneakBypassUse(ItemStack stack, LevelReader level, BlockPos pos, Player player) {
        return true;
    }

    public boolean serverSideToolLogic(UseOnContext useContext) {
        BlockPos pos = useContext.getClickedPos();
        Player p = useContext.getPlayer();
        Level level = p.level;
        InteractionHand hand = useContext.getHand();
        Direction side = useContext.getClickedFace();

        if (!Platform.hasPermissions(new DimensionalBlockPos(level, pos), p)) {
            return false;
        }

        // The network tool has special behavior for machines hosting world-accessible nodes
        var nodeHost = Api.instance().grid().getNodeHost(level, pos);

        var bs = level.getBlockState(pos);
        if (!InteractionUtil.isInAlternateUseMode(p)) {
            if (nodeHost == null && bs.rotate(level, pos, Rotation.CLOCKWISE_90) != bs) {
                bs.neighborChanged(level, pos, Blocks.AIR, pos, false);
                p.swing(hand);
                return !level.isClientSide;
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
            BlockHitResult rtr = new BlockHitResult(useContext.getClickLocation(), side, pos, false);
            bs.use(level, p, hand, rtr);
        }

        return false;
    }

    @Override
    public boolean canWrench(final ItemStack wrench, final Player player, final BlockPos pos) {
        return true;
    }

}
