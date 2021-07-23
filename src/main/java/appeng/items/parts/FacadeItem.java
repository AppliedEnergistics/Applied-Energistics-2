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

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.core.Registry;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.item.Item.Properties;
import net.minecraftforge.registries.ForgeRegistries;

import appeng.api.parts.IAlphaPassItem;
import appeng.api.util.AEPartLocation;
import appeng.core.AEConfig;
import appeng.core.Api;
import appeng.core.AppEng;
import appeng.core.definitions.AEItems;
import appeng.facade.FacadePart;
import appeng.facade.IFacadeItem;
import appeng.items.AEBaseItem;

public class FacadeItem extends AEBaseItem implements IFacadeItem, IAlphaPassItem {

    /**
     * Block tag used to explicitly whitelist blocks for use in facades.
     */
    private static final Named<net.minecraft.world.level.block.Block> BLOCK_WHITELIST = BlockTags
            .createOptional(AppEng.makeId("whitelisted/facades"));

    private static final String NBT_ITEM_ID = "item";

    public FacadeItem(net.minecraft.world.item.Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult onItemUseFirst(net.minecraft.world.item.ItemStack stack, UseOnContext context) {
        return Api.instance().partHelper().placeBus(stack, context.getClickedPos(), context.getClickedFace(), context.getPlayer(),
                context.getHand(), context.getLevel());
    }

    @Override
    public net.minecraft.network.chat.Component getName(ItemStack is) {
        try {
            final net.minecraft.world.item.ItemStack in = this.getTextureItem(is);
            if (!in.isEmpty()) {
                return super.getName(is).copy().append(" - ").append(in.getHoverName());
            }
        } catch (final Throwable ignored) {

        }

        return super.getName(is);
    }

    @Override
    public void fillItemCategory(CreativeModeTab group, NonNullList<net.minecraft.world.item.ItemStack> items) {
    }

    public net.minecraft.world.item.ItemStack createFacadeForItem(final ItemStack itemStack, final boolean returnItem) {
        if (itemStack.isEmpty() || itemStack.hasTag() || !(itemStack.getItem() instanceof BlockItem)) {
            return net.minecraft.world.item.ItemStack.EMPTY;
        }

        net.minecraft.world.item.BlockItem blockItem = (net.minecraft.world.item.BlockItem) itemStack.getItem();
        net.minecraft.world.level.block.Block block = blockItem.getBlock();
        if (block == Blocks.AIR) {
            return net.minecraft.world.item.ItemStack.EMPTY;
        }

        // We only support the default state for facades. Sorry.
        BlockState blockState = block.defaultBlockState();

        final boolean areTileEntitiesEnabled = AEConfig.instance().isBlockEntityFacadesEnabled();
        final boolean isWhiteListed = BLOCK_WHITELIST.contains(block);
        final boolean isModel = blockState.getRenderShape() == RenderShape.MODEL;

        final net.minecraft.world.level.block.state.BlockState defaultState = block.defaultBlockState();
        final boolean isTileEntity = block.hasTileEntity(defaultState);
        final boolean isFullCube = defaultState.isRedstoneConductor(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);

        final boolean isTileEntityAllowed = !isTileEntity || areTileEntitiesEnabled && isWhiteListed;
        final boolean isBlockAllowed = isFullCube || isWhiteListed;

        if (isModel && isTileEntityAllowed && isBlockAllowed) {
            if (returnItem) {
                return itemStack;
            }

            final net.minecraft.world.item.ItemStack is = new net.minecraft.world.item.ItemStack(this);
            final CompoundTag data = new CompoundTag();
            data.putString(NBT_ITEM_ID, itemStack.getItem().getRegistryName().toString());
            is.setTag(data);
            return is;
        }
        return net.minecraft.world.item.ItemStack.EMPTY;
    }

    @Override
    public FacadePart createPartFromItemStack(final net.minecraft.world.item.ItemStack is, final AEPartLocation side) {
        final net.minecraft.world.item.ItemStack in = this.getTextureItem(is);
        if (!in.isEmpty()) {
            return new FacadePart(is, side);
        }
        return null;
    }

    @Override
    public net.minecraft.world.item.ItemStack getTextureItem(net.minecraft.world.item.ItemStack is) {
        CompoundTag nbt = is.getTag();

        if (nbt == null) {
            return net.minecraft.world.item.ItemStack.EMPTY;
        }

        ResourceLocation itemId = new ResourceLocation(nbt.getString(NBT_ITEM_ID));
        net.minecraft.world.item.Item baseItem = ForgeRegistries.ITEMS.getValue(itemId);

        if (baseItem == null) {
            return net.minecraft.world.item.ItemStack.EMPTY;
        }

        return new net.minecraft.world.item.ItemStack(baseItem, 1);
    }

    @Override
    public BlockState getTextureBlockState(net.minecraft.world.item.ItemStack is) {

        net.minecraft.world.item.ItemStack baseItemStack = this.getTextureItem(is);

        if (baseItemStack.isEmpty()) {
            return Blocks.GLASS.defaultBlockState();
        }

        net.minecraft.world.level.block.Block block = Block.byItem(baseItemStack.getItem());

        if (block == Blocks.AIR) {
            return net.minecraft.world.level.block.Blocks.GLASS.defaultBlockState();
        }

        return block.defaultBlockState();
    }

    public ItemStack createFromID(final int id) {
        net.minecraft.world.item.ItemStack facadeStack = AEItems.FACADE.stack();

        // Convert back to a registry name...
        Item item = Registry.ITEM.byId(id);
        if (item == Items.AIR) {
            return net.minecraft.world.item.ItemStack.EMPTY;
        }

        final CompoundTag facadeTag = new CompoundTag();
        facadeTag.putString(NBT_ITEM_ID, item.getRegistryName().toString());
        facadeStack.setTag(facadeTag);

        return facadeStack;
    }

    @Override
    public boolean useAlphaPass(final net.minecraft.world.item.ItemStack is) {
        net.minecraft.world.level.block.state.BlockState blockState = this.getTextureBlockState(is);

        if (blockState == null) {
            return false;
        }

        return ItemBlockRenderTypes.canRenderInLayer(blockState, RenderType.translucent())
                || ItemBlockRenderTypes.canRenderInLayer(blockState, RenderType.translucentNoCrumbling());
    }
}
