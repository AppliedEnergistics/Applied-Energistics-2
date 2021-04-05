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

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.api.parts.PartItemStack;
import appeng.api.util.AEPartLocation;
import appeng.hooks.AEToolItem;
import appeng.items.AEBaseItem;
import appeng.items.parts.ColoredPartItem;
import appeng.items.parts.PartItem;

/**
 * This tool will try to place anything that is registered as a {@link PartItem} (and not a colored one) onto an
 * existing cable to quickly test parts and their rendering.
 */
public class DebugPartPlacerItem extends AEBaseItem implements AEToolItem {

    public DebugPartPlacerItem(Properties properties) {
        super(properties);
    }

    @Override
    public ActionResultType onItemUseFirst(ItemStack stack, ItemUseContext context) {
        World world = context.getWorld();
        if (world.isRemote()) {
            return ActionResultType.PASS;
        }

        PlayerEntity player = context.getPlayer();
        BlockPos pos = context.getPos();

        if (player == null) {
            return ActionResultType.PASS;
        }

        if (!player.abilities.isCreativeMode) {
            player.sendMessage(new StringTextComponent("Only usable in creative mode"), Util.DUMMY_UUID);
            return ActionResultType.FAIL;
        }

        TileEntity te = world.getTileEntity(pos);
        if (!(te instanceof IPartHost)) {
            player.sendMessage(new StringTextComponent("Right-click something that will accept parts"),
                    Util.DUMMY_UUID);
            return ActionResultType.FAIL;
        }
        IPartHost center = (IPartHost) te;
        IPart cable = center.getPart(AEPartLocation.INTERNAL);
        if (cable == null) {
            player.sendMessage(new StringTextComponent("Clicked part host must have an INSIDE part"), Util.DUMMY_UUID);
            return ActionResultType.FAIL;
        }

        Direction face = context.getFace();
        Vector3i offset = face.getDirectionVec();
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

            nextPos = nextPos.add(offset);
            if (!world.setBlockState(nextPos, te.getBlockState())) {
                continue;
            }

            TileEntity t = world.getTileEntity(nextPos);
            if (!(t instanceof IPartHost)) {
                continue;
            }

            IPartHost partHost = (IPartHost) t;
            if (partHost.addPart(cable.getItemStack(PartItemStack.PICK), AEPartLocation.INTERNAL, player,
                    null) == null) {
                continue;
            }
            for (Direction dir : perpendicularFaces) {
                ItemStack itemStack = new ItemStack(item, 1);
                partHost.addPart(itemStack, AEPartLocation.fromFacing(dir), player, null);
            }
        }

        return ActionResultType.func_233537_a_(world.isRemote());
    }

}
