/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
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

package appeng.debug;

import java.util.Arrays;

import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.Direction;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.registries.ForgeRegistries;

import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.api.parts.PartItemStack;
import appeng.api.util.AEPartLocation;
import appeng.items.AEBaseItem;
import appeng.items.parts.ColoredPartItem;
import appeng.items.parts.PartItem;

/**
 * This tool will try to place anything that is registered as a {@link PartItem} (and not a colored one) onto an
 * existing cable to quickly test parts and their rendering.
 */
public class DebugPartPlacerItem extends AEBaseItem {

    public DebugPartPlacerItem(net.minecraft.world.item.Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult onItemUseFirst(net.minecraft.world.item.ItemStack stack, UseOnContext context) {
        Level world = context.getLevel();
        if (world.isClientSide()) {
            return InteractionResult.PASS;
        }

        Player player = context.getPlayer();
        BlockPos pos = context.getClickedPos();

        if (player == null) {
            return InteractionResult.PASS;
        }

        if (!player.abilities.instabuild) {
            player.sendMessage(new TextComponent("Only usable in creative mode"), net.minecraft.Util.NIL_UUID);
            return InteractionResult.FAIL;
        }

        BlockEntity te = world.getBlockEntity(pos);
        if (!(te instanceof IPartHost)) {
            player.sendMessage(new TextComponent("Right-click something that will accept parts"),
                    net.minecraft.Util.NIL_UUID);
            return InteractionResult.FAIL;
        }
        IPartHost center = (IPartHost) te;
        IPart cable = center.getPart(AEPartLocation.INTERNAL);
        if (cable == null) {
            player.sendMessage(new TextComponent("Clicked part host must have an INSIDE part"), Util.NIL_UUID);
            return InteractionResult.FAIL;
        }

        net.minecraft.core.Direction face = context.getClickedFace();
        Vec3i offset = face.getNormal();
        Direction[] perpendicularFaces = Arrays.stream(net.minecraft.core.Direction.values()).filter(d -> d.getAxis() != face.getAxis())
                .toArray(net.minecraft.core.Direction[]::new);

        BlockPos nextPos = pos;
        for (net.minecraft.world.item.Item item : ForgeRegistries.ITEMS) {
            if (!(item instanceof PartItem)) {
                continue;
            }

            if (item instanceof ColoredPartItem) {
                continue; // Cables and such
            }

            nextPos = nextPos.offset(offset);
            if (!world.setBlockAndUpdate(nextPos, te.getBlockState())) {
                continue;
            }

            BlockEntity t = world.getBlockEntity(nextPos);
            if (!(t instanceof IPartHost)) {
                continue;
            }

            IPartHost partHost = (IPartHost) t;
            if (partHost.addPart(cable.getItemStack(PartItemStack.PICK), AEPartLocation.INTERNAL, player,
                    null) == null) {
                continue;
            }
            for (Direction dir : perpendicularFaces) {
                net.minecraft.world.item.ItemStack itemStack = new ItemStack(item, 1);
                partHost.addPart(itemStack, AEPartLocation.fromFacing(dir), player, null);
            }
        }

        return InteractionResult.sidedSuccess(world.isClientSide());
    }

}
