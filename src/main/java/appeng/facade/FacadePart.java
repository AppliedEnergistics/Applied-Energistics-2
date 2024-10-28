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

import java.util.Collection;
import java.util.Objects;

import javax.annotation.Nullable;

import net.minecraft.Util;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DebugStickState;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;

import appeng.api.parts.IFacadePart;
import appeng.api.parts.IPartCollisionHelper;
import appeng.core.definitions.AEItems;
import appeng.util.InteractionUtil;

public class FacadePart implements IFacadePart {

    private BlockState facade;
    private final Direction side;

    public FacadePart(BlockState facade, Direction side) {
        Objects.requireNonNull(side, "side");
        Objects.requireNonNull(facade, "facade");
        this.facade = facade;
        this.side = side;
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

    private boolean handleInteraction(Player player, boolean shouldCycleState, ItemStack debugStack) {
        Holder<Block> holder = getBlockState().getBlockHolder();
        StateDefinition<Block, BlockState> statedefinition = holder.value().getStateDefinition();
        Collection<Property<?>> collection = statedefinition.getProperties();
        if (collection.isEmpty()) {
            message(player,
                    Component.translatable(Items.DEBUG_STICK.getDescriptionId() + ".empty",
                            holder.getRegisteredName()));
            return false;
        }

        DebugStickState debugstickstate = debugStack.getOrDefault(DataComponents.DEBUG_STICK_STATE,
                DebugStickState.EMPTY);

        Property<?> property = debugstickstate.properties().get(holder);
        if (shouldCycleState) {
            if (property == null) {
                property = collection.iterator().next();
            }

            setBlockState(cycleState(getBlockState(), property, player.isSecondaryUseActive()));
            message(player, Component.translatable(Items.DEBUG_STICK.getDescriptionId() + ".update",
                    property.getName(), getNameHelper(getBlockState(), property)));
        } else {
            property = getRelative(collection, property, player.isSecondaryUseActive());
            debugStack.set(DataComponents.DEBUG_STICK_STATE, debugstickstate.withProperty(holder, property));
            message(player, Component.translatable(Items.DEBUG_STICK.getDescriptionId() + ".select",
                    property.getName(), getNameHelper(getBlockState(), property)));
        }
        return true;
    }

    private static <T extends Comparable<T>> BlockState cycleState(BlockState state, Property<T> property,
            boolean backwards) {
        return state.setValue(property, getRelative(property.getPossibleValues(), state.getValue(property), backwards));
    }

    private static <T> T getRelative(Iterable<T> allowedValues, @Nullable T currentValue, boolean backwards) {
        return backwards ? Util.findPreviousInIterable(allowedValues, currentValue)
                : Util.findNextInIterable(allowedValues, currentValue);
    }

    private static void message(Player player, Component messageComponent) {
        if (player instanceof ServerPlayer serverPlayer)
            serverPlayer.sendSystemMessage(messageComponent, true);
    }

    private static <T extends Comparable<T>> String getNameHelper(BlockState state, Property<T> property) {
        return property.getName(state.getValue(property));
    }
}
