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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import appeng.api.implementations.menuobjects.IMenuItem;
import appeng.api.networking.GridHelper;
import appeng.api.parts.IPartHost;
import appeng.api.util.DimensionalBlockPos;
import appeng.api.util.INetworkToolAware;
import appeng.hooks.AEToolItem;
import appeng.items.AEBaseItem;
import appeng.items.contents.NetworkToolMenuHost;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocators;
import appeng.menu.me.networktool.NetworkStatusMenu;
import appeng.menu.me.networktool.NetworkToolMenu;
import appeng.util.Platform;

public class NetworkToolItem extends AEBaseItem implements IMenuItem, AEToolItem {

    public NetworkToolItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public NetworkToolMenuHost getMenuHost(Player player, int inventorySlot, ItemStack stack, BlockPos pos) {
        var level = player.level;
        if (pos == null) {
            return new NetworkToolMenuHost(player, inventorySlot, stack, null);
        }
        var host = GridHelper.getNodeHost(level, pos);
        return new NetworkToolMenuHost(player, inventorySlot, stack, host);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player p, InteractionHand hand) {
        if (!level.isClientSide()) {
            MenuOpener.open(NetworkToolMenu.TYPE, p, MenuLocators.forHand(p, hand));
        }

        return new InteractionResultHolder<>(InteractionResult.sidedSuccess(level.isClientSide()),
                p.getItemInHand(hand));
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        // Disassembly is handled by the generic wrench handler
        if (context.isSecondaryUseActive()) {
            return InteractionResult.PASS;
        }

        Level level = context.getLevel();

        // Suppress the network tool's own behavior in case the block wants to allow normal operations to work
        // (i.e. putting a network tool into a conversion or storage monitor).
        var te = level.getBlockEntity(context.getClickedPos());
        if (te instanceof IPartHost partHost) {
            var part = partHost.selectPartWorld(context.getClickLocation());

            if (part.part != null || part.facade != null) {
                if (part.part instanceof INetworkToolAware toolAgent && !toolAgent.showNetworkInfo(context)) {
                    return InteractionResult.PASS;
                }
            }
        } else if (te instanceof INetworkToolAware toolAgent && !toolAgent.showNetworkInfo(context)) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide()) {
            if (!showNetworkToolGui(context)) {
                return InteractionResult.FAIL;
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    private boolean showNetworkToolGui(UseOnContext useContext) {
        if (useContext.getPlayer() == null) {
            return false;
        }

        BlockPos pos = useContext.getClickedPos();
        Player p = useContext.getPlayer();
        Level level = useContext.getLevel();
        InteractionHand hand = useContext.getHand();

        if (!Platform.hasPermissions(new DimensionalBlockPos(level, pos), p)) {
            return false;
        }

        // The network tool has special behavior for machines hosting world-accessible nodes
        var nodeHost = GridHelper.getNodeHost(level, pos);

        if (nodeHost != null) {
            MenuOpener.open(NetworkStatusMenu.TYPE, p, MenuLocators.forItemUseContext(useContext));
        } else {
            MenuOpener.open(NetworkToolMenu.TYPE, p, MenuLocators.forHand(p, hand));
        }

        return true;
    }

}
