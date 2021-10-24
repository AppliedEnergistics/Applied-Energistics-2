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

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;

import appeng.api.ids.AETags;
import appeng.api.parts.IAlphaPassItem;
import appeng.api.parts.PartHelper;
import appeng.core.AEConfig;
import appeng.core.definitions.AEItems;
import appeng.facade.FacadePart;
import appeng.facade.IFacadeItem;
import appeng.hooks.AEToolItem;
import appeng.items.AEBaseItem;

public class FacadeItem extends AEBaseItem implements IFacadeItem, IAlphaPassItem, AEToolItem {

    private static final String NBT_ITEM_ID = "item";

    public FacadeItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        return PartHelper.placeBus(stack, context.getClickedPos(), context.getClickedFace(),
                context.getPlayer(),
                context.getHand(), context.getLevel());
    }

    @Override
    public Component getName(ItemStack is) {
        try {
            final ItemStack in = this.getTextureItem(is);
            if (!in.isEmpty()) {
                return super.getName(is).copy().append(" - ").append(in.getHoverName());
            }
        } catch (final Throwable ignored) {

        }

        return super.getName(is);
    }

    @Override
    public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> items) {
    }

    public ItemStack createFacadeForItem(final ItemStack itemStack, final boolean returnItem) {
        if (itemStack.isEmpty() || itemStack.hasTag() || !(itemStack.getItem() instanceof BlockItem blockItem)) {
            return ItemStack.EMPTY;
        }

        Block block = blockItem.getBlock();
        if (block == Blocks.AIR) {
            return ItemStack.EMPTY;
        }

        // We only support the default state for facades. Sorry.
        BlockState blockState = block.defaultBlockState();

        final boolean areBlockEntitiesEnabled = AEConfig.instance().isBlockEntityFacadesEnabled();
        final boolean isWhiteListed = AETags.FACADE_BLOCK_WHITELIST.contains(block);
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

    public ItemStack createFacadeForItemUnchecked(final ItemStack itemStack) {
        final ItemStack is = new ItemStack(this);
        final CompoundTag data = new CompoundTag();
        data.putString(NBT_ITEM_ID, Registry.ITEM.getKey(itemStack.getItem()).toString());
        is.setTag(data);
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
        CompoundTag nbt = is.getTag();

        if (nbt == null) {
            return ItemStack.EMPTY;
        }

        ResourceLocation itemId = new ResourceLocation(nbt.getString(NBT_ITEM_ID));
        Item baseItem = Registry.ITEM.get(itemId);

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

    public ItemStack createFromID(final int id) {
        ItemStack facadeStack = AEItems.FACADE.stack();

        // Convert back to a registry name...
        Item item = Registry.ITEM.byId(id);
        if (item == Items.AIR) {
            return ItemStack.EMPTY;
        }

        final CompoundTag facadeTag = new CompoundTag();
        facadeTag.putString(NBT_ITEM_ID, Registry.ITEM.getKey(item).toString());
        facadeStack.setTag(facadeTag);

        return facadeStack;
    }

    @Override
    public boolean useAlphaPass(final ItemStack is) {
        BlockState blockState = this.getTextureBlockState(is);

        if (blockState == null) {
            return false;
        }

        return ItemBlockRenderTypes.getChunkRenderType(blockState) == RenderType.translucent()
                || ItemBlockRenderTypes.getChunkRenderType(blockState) == RenderType.translucentNoCrumbling();
    }
}
