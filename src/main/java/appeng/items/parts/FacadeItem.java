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

package appeng.items.parts;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

import appeng.api.ids.AEComponents;
import appeng.api.ids.AETags;
import appeng.api.implementations.items.IFacadeItem;
import appeng.api.parts.IFacadePart;
import appeng.api.parts.IPartHost;
import appeng.api.parts.PartHelper;
import appeng.core.AEConfig;
import appeng.core.definitions.AEItems;
import appeng.facade.FacadePart;
import appeng.items.AEBaseItem;

public class FacadeItem extends AEBaseItem implements IFacadeItem {

    public FacadeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        if (stack.getItem() != this) {
            return InteractionResult.PASS;
        }

        var level = context.getLevel();
        var pos = context.getClickedPos();
        var player = context.getPlayer();

        var facade = createPartFromItemStack(stack, context.getClickedFace());
        if (facade == null || !placeFacade(facade, level, pos)) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide && player != null && !player.isCreative()) {
            stack.grow(-1);
            if (stack.isEmpty()) {
                player.setItemInHand(context.getHand(), ItemStack.EMPTY);
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    public static boolean canPlaceFacade(IPartHost host, IFacadePart facade) {
        // Can only place a facade on cables if there's actually a cable at the center to hold them
        if (host.getPart(null) == null) {
            return false;
        }

        return host.getFacadeContainer().canAddFacade(facade);
    }

    private static boolean placeFacade(FacadePart facade, Level level, BlockPos blockPos) {
        var host = PartHelper.getPartHost(level, blockPos);
        if (host == null) {
            return false;
        }

        if (!canPlaceFacade(host, facade)) {
            return false;
        }

        if (!host.getFacadeContainer().addFacade(facade)) {
            return false;
        }

        // Play a placement sound of the underlying block
        BlockState blockState = facade.getBlockState();
        SoundType soundType = blockState.getSoundType();
        level.playSound(null, blockPos, soundType.getPlaceSound(), SoundSource.BLOCKS,
                (soundType.getVolume() + 1.0F) / 2.0F,
                soundType.getPitch() * 0.8F);

        host.markForSave();
        host.markForUpdate();
        return true;
    }

    public static IFacadePart createFacade(ItemStack held, Direction side) {
        if (held.getItem() instanceof IFacadeItem) {
            return ((IFacadeItem) held.getItem()).createPartFromItemStack(held, side);
        }

        return null;
    }

    @Override
    public Component getName(ItemStack is) {
        try {
            final ItemStack in = this.getTextureItem(is);
            if (!in.isEmpty()) {
                return super.getName(is).copy().append(" - ").append(in.getHoverName());
            }
        } catch (Throwable ignored) {

        }

        return super.getName(is);
    }

    @Override
    public void addToMainCreativeTab(CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output output) {
        // Don't show in creative mode, since it's not useful without NBT
    }

    public ItemStack createFacadeForItem(ItemStack itemStack, boolean returnItem) {
        if (itemStack.isEmpty() || !itemStack.getComponentsPatch().isEmpty()
                || !(itemStack.getItem() instanceof BlockItem blockItem)) {
            return ItemStack.EMPTY;
        }

        Block block = blockItem.getBlock();
        if (block == Blocks.AIR) {
            return ItemStack.EMPTY;
        }

        // We only support the default state for facades. Sorry.
        BlockState blockState = block.defaultBlockState();

        final boolean areBlockEntitiesEnabled = AEConfig.instance().isBlockEntityFacadesEnabled();
        final boolean isWhiteListed = block.builtInRegistryHolder().is(AETags.FACADE_BLOCK_WHITELIST);
        final boolean isModel = blockState.getRenderShape() == RenderShape.MODEL;

        final BlockState defaultState = block.defaultBlockState();
        final boolean isBlockEntity = defaultState.hasBlockEntity();
        final boolean isFullCube = defaultState.isRedstoneConductor(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);

        final boolean isBlockEntityAllowed = !isBlockEntity || areBlockEntitiesEnabled && isWhiteListed;
        final boolean isBlockAllowed = isFullCube || isWhiteListed;

        if (isModel && isBlockEntityAllowed && isBlockAllowed) {
            if (returnItem) {
                return itemStack;
            }

            return createFacadeForItemUnchecked(itemStack);
        }
        return ItemStack.EMPTY;
    }

    public ItemStack createFacadeForItemUnchecked(ItemStack itemStack) {
        var is = new ItemStack(this);
        is.set(AEComponents.FACADE_ITEM, itemStack.getItemHolder());
        return is;
    }

    @Override
    public FacadePart createPartFromItemStack(ItemStack is, Direction side) {
        final ItemStack in = this.getTextureItem(is);
        if (!in.isEmpty()) {
            return new FacadePart(is, side);
        }
        return null;
    }

    @Override
    public ItemStack getTextureItem(ItemStack is) {
        var baseItem = is.get(AEComponents.FACADE_ITEM);

        if (baseItem == null) {
            return ItemStack.EMPTY;
        }

        return new ItemStack(baseItem, 1);
    }

    @Override
    public BlockState getTextureBlockState(ItemStack is) {

        ItemStack baseItemStack = this.getTextureItem(is);

        if (baseItemStack.isEmpty()) {
            return Blocks.GLASS.defaultBlockState();
        }

        Block block = Block.byItem(baseItemStack.getItem());

        if (block == Blocks.AIR) {
            return Blocks.GLASS.defaultBlockState();
        }

        return block.defaultBlockState();
    }

    public ItemStack createFromID(int id) {
        // Convert back to a registry name...
        Item item = BuiltInRegistries.ITEM.byId(id);
        if (item == Items.AIR) {
            return ItemStack.EMPTY;
        }

        var facadeStack = AEItems.FACADE.stack();
        facadeStack.set(AEComponents.FACADE_ITEM, item.getDefaultInstance().getItemHolder());
        return facadeStack;
    }
}
