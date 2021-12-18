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

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.api.parts.PartItemStack;
import appeng.hooks.AEToolItem;
import appeng.items.AEBaseItem;
import appeng.items.parts.ColoredPartItem;
import appeng.items.parts.PartItem;

/**
 * This tool will try to place anything that is registered as a {@link PartItem} (and not a colored one) onto an
 * existing cable to quickly test parts and their rendering.
 */
public class DebugPartPlacerItem extends AEBaseItem implements AEToolItem {

    public DebugPartPlacerItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide()) {
            return InteractionResult.PASS;
        }

        Player player = context.getPlayer();
        BlockPos pos = context.getClickedPos();

        if (player == null) {
            return InteractionResult.PASS;
        }

        if (!player.getAbilities().instabuild) {
            player.sendMessage(new TextComponent("Only usable in creative mode"), Util.NIL_UUID);
            return InteractionResult.FAIL;
        }

        BlockEntity te = level.getBlockEntity(pos);
        if (!(te instanceof IPartHost center)) {
            player.sendMessage(new TextComponent("Right-click something that will accept parts"),
                    Util.NIL_UUID);
            return InteractionResult.FAIL;
        }
        IPart cable = center.getPart(null);
        if (cable == null) {
            player.sendMessage(new TextComponent("Clicked part host must have an INSIDE part"), Util.NIL_UUID);
            return InteractionResult.FAIL;
        }

        Direction face = context.getClickedFace();
        Vec3i offset = face.getNormal();
        Direction[] perpendicularFaces = Arrays.stream(Direction.values()).filter(d -> d.getAxis() != face.getAxis())
                .toArray(Direction[]::new);

        BlockPos nextPos = pos;
        for (Item item : Registry.ITEM) {
            if (!(item instanceof PartItem)) {
                continue;
            }

            if (item instanceof ColoredPartItem) {
                continue; // Cables and such
            }

            nextPos = nextPos.offset(offset);
            if (!level.setBlockAndUpdate(nextPos, te.getBlockState())) {
                continue;
            }

            BlockEntity t = level.getBlockEntity(nextPos);
            if (!(t instanceof IPartHost partHost)) {
                continue;
            }

            if (!partHost.addPart(cable.getItemStack(PartItemStack.PICK), null, player)) {
                continue;
            }
            for (Direction dir : perpendicularFaces) {
                ItemStack itemStack = new ItemStack(item, 1);
                partHost.addPart(itemStack, dir, player);
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide());
    }

}
