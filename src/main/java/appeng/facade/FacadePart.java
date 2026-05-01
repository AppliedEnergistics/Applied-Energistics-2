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

package appeng.facade;

import java.util.Objects;

import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Util;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import appeng.api.ids.AEComponents;
import appeng.api.parts.IFacadePart;
import appeng.api.parts.IPartCollisionHelper;
import appeng.core.definitions.AEItems;
import appeng.core.localization.PlayerMessages;
import appeng.util.InteractionUtil;

public class FacadePart implements IFacadePart {

    private final Direction side;
    private BlockState facade;

    public FacadePart(BlockState facade, Direction side) {
        this.side = Objects.requireNonNull(side, "side");
        this.facade = Objects.requireNonNull(facade, "facade");
    }

    @Override
    public ItemStack getItemStack() {
        return AEItems.FACADE.get().createFacadeForItemUnchecked(getTextureItem());
    }

    @Override
    public void getBoxes(IPartCollisionHelper ch, boolean itemEntity) {
        if (itemEntity) {
            // the box is 15.9 for annihilation planes to pick up collision events.
            ch.addBox(0.0, 0.0, 15, 16.0, 16.0, 15.9);
        } else {
            // prevent weird snag behavior
            ch.addBox(0.0, 0.0, 15, 16.0, 16.0, 16.0);
        }
    }

    @Override
    public Direction getSide() {
        return this.side;
    }

    @Override
    public Item getItem() {
        return facade.getBlock().asItem();
    }

    @Override
    public ItemStack getTextureItem() {
        return new ItemStack(getItem());
    }

    @Override
    public BlockState getBlockState() {
        return facade;
    }

    private void setBlockState(BlockState blockState) {
        facade = blockState;
    }

    public boolean onUseItemOn(ItemStack heldItem, Player player, InteractionHand hand, Vec3 pos) {
        if (!InteractionUtil.canWrenchRotate(heldItem))
            return false;

        return handleInteraction(player, true, heldItem);
    }

    public boolean onClicked(Player player, Vec3 pos) {
        ItemStack heldItem = player.getMainHandItem();
        if (!InteractionUtil.canWrenchRotate(heldItem))
            return false;

        return handleInteraction(player, false, heldItem);
    }

    private boolean handleInteraction(Player player, boolean shouldCycleState, ItemStack heldItem) {
        var holder = getBlockState().typeHolder();
        var statedefinition = holder.value().getStateDefinition();
        var properties = statedefinition.getProperties();
        if (properties.isEmpty()) {
            return false;
        }

        var firstProperty = properties.iterator().next();
        var cyclePropertyName = heldItem.getOrDefault(AEComponents.FACADE_CYCLE_PROPERTY, firstProperty.getName());
        var property = statedefinition.getProperty(cyclePropertyName);
        if (property == null) {
            // Fall back to the first property if the wrench was set to a property that does not exist on this facade
            property = firstProperty;
        }

        if (shouldCycleState) {
            var newState = getBlockState().cycle(property);
            setBlockState(newState);

            // If we reached the default value of the property, we consider that wrapping and show
            // a message indicating to the player that they can left-click to change which property is cycled
            var defaultValue = getBlockState().getBlock().defaultBlockState().getValue(property);
            if (Objects.equals(newState.getValue(property), defaultValue)) {
                message(player, PlayerMessages.FacadePropertyWrapped.text(property.getName()));
            }
        } else {
            property = Util.findNextInIterable(properties, property);
            if (property == firstProperty) {
                heldItem.remove(AEComponents.FACADE_CYCLE_PROPERTY);
            } else {
                heldItem.set(AEComponents.FACADE_CYCLE_PROPERTY, property.getName());
            }
            message(player, PlayerMessages.FacadePropertySelected.text(property.getName()));
        }
        return true;
    }

    private static void message(Player player, Component messageComponent) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.sendSystemMessage(messageComponent, true);
        }
    }
}
