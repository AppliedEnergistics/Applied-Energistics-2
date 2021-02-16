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

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvironmentInterface;
import net.minecraft.block.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tag.Tag;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.EmptyBlockView;

import appeng.api.exceptions.MissingDefinitionException;
import appeng.api.features.AEFeature;
import appeng.api.parts.IAlphaPassItem;
import appeng.api.util.AEPartLocation;
import appeng.core.AEConfig;
import appeng.core.Api;
import appeng.core.AppEng;
import appeng.facade.FacadePart;
import appeng.facade.IFacadeItem;
import appeng.hooks.AEToolItem;
import appeng.items.AEBaseItem;
import appeng.mixins.tags.BlockTagsAccessor;

@EnvironmentInterface(value = EnvType.CLIENT, itf = IAlphaPassItem.class)
public class FacadeItem extends AEBaseItem implements IFacadeItem, IAlphaPassItem, AEToolItem {

    /**
     * Block tag used to explicitly whitelist blocks for use in facades.
     */
    private static final Tag.Identified<Block> BLOCK_WHITELIST = BlockTagsAccessor
            .register(AppEng.makeId("whitelisted/facades").toString());

    private static final String NBT_ITEM_ID = "item";

    public FacadeItem(Settings properties) {
        super(properties);
    }

    @Override
    public ActionResult onItemUseFirst(ItemStack stack, ItemUsageContext context) {
        return Api.instance().partHelper().placeBus(stack, context.getBlockPos(), context.getSide(),
                context.getPlayer(), context.getHand(), context.getWorld());
    }

    @Override
    public Text getName(ItemStack is) {
        try {
            final ItemStack in = this.getTextureItem(is);
            if (!in.isEmpty()) {
                return super.getName(is).copy().append(" - ").append(in.getName());
            }
        } catch (final Throwable ignored) {

        }

        return super.getName(is);
    }

    @Override
    public void appendStacks(ItemGroup group, DefaultedList<ItemStack> items) {
    }

    public ItemStack createFacadeForItem(final ItemStack itemStack, final boolean returnItem) {
        if (itemStack.isEmpty() || itemStack.hasTag() || !(itemStack.getItem() instanceof BlockItem)) {
            return ItemStack.EMPTY;
        }

        BlockItem blockItem = (BlockItem) itemStack.getItem();
        Block block = blockItem.getBlock();
        if (block == Blocks.AIR) {
            return ItemStack.EMPTY;
        }

        // We only support the default state for facades. Sorry.
        BlockState blockState = block.getDefaultState();

        final boolean areTileEntitiesEnabled = AEConfig.instance().isFeatureEnabled(AEFeature.TILE_ENTITY_FACADES);
        final boolean isWhiteListed = BLOCK_WHITELIST.contains(block);
        final boolean isModel = blockState.getRenderType() == BlockRenderType.MODEL;

        final BlockState defaultState = block.getDefaultState();
        final boolean isTileEntity = block instanceof BlockEntityProvider;
        final boolean isFullCube = defaultState.isOpaqueFullCube(EmptyBlockView.INSTANCE, BlockPos.ORIGIN);

        final boolean isTileEntityAllowed = !isTileEntity || (areTileEntitiesEnabled && isWhiteListed);
        final boolean isBlockAllowed = isFullCube || isWhiteListed;

        if (isModel && isTileEntityAllowed && isBlockAllowed) {
            if (returnItem) {
                return itemStack;
            }

            final ItemStack is = new ItemStack(this);
            final CompoundTag data = new CompoundTag();
            Identifier itemId = Registry.ITEM.getId(itemStack.getItem());
            data.putString(NBT_ITEM_ID, itemId.toString());
            is.setTag(data);
            return is;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public FacadePart createPartFromItemStack(final ItemStack is, final AEPartLocation side) {
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

        Identifier itemId = new Identifier(nbt.getString(NBT_ITEM_ID));
        Item baseItem = Registry.ITEM.getOrEmpty(itemId).orElse(null);

        if (baseItem == null) {
            return ItemStack.EMPTY;
        }

        return new ItemStack(baseItem);
    }

    @Override
    public BlockState getTextureBlockState(ItemStack is) {

        ItemStack baseItemStack = this.getTextureItem(is);

        if (baseItemStack.isEmpty()) {
            return Blocks.GLASS.getDefaultState();
        }

        Block block = Block.getBlockFromItem(baseItemStack.getItem());

        if (block == Blocks.AIR) {
            return Blocks.GLASS.getDefaultState();
        }

        return block.getDefaultState();
    }

    public ItemStack createFromID(final int id) {
        ItemStack facadeStack = Api.instance().definitions().items().facade().maybeStack(1).orElseThrow(
                () -> new MissingDefinitionException("Tried to create a facade, while facades are being deactivated."));

        // Convert back to a registry name...
        Item item = Registry.ITEM.get(id);
        if (item == Items.AIR) {
            return ItemStack.EMPTY;
        }
        Identifier longId = Registry.ITEM.getId(item);

        final CompoundTag facadeTag = new CompoundTag();
        facadeTag.putString(NBT_ITEM_ID, longId.toString());
        facadeStack.setTag(facadeTag);

        return facadeStack;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public boolean useAlphaPass(final ItemStack is) {
        BlockState blockState = this.getTextureBlockState(is);

        if (blockState == null) {
            return false;
        }

        return RenderLayers.getBlockLayer(blockState) == RenderLayer.getTranslucent()
                || RenderLayers.getBlockLayer(blockState) == RenderLayer.getTranslucentNoCrumbling();
    }
}
