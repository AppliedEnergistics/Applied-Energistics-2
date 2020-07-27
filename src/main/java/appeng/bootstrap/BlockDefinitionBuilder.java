/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
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

package appeng.bootstrap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import appeng.api.definitions.IBlockDefinition;
import appeng.api.features.AEFeature;
import appeng.block.AEBaseBlock;
import appeng.block.AEBaseBlockItem;
import appeng.block.AEBaseTileBlock;
import appeng.bootstrap.definitions.TileEntityDefinition;
import appeng.core.AppEng;
import appeng.core.CreativeTab;
import appeng.core.features.BlockDefinition;
import appeng.core.features.TileDefinition;
import appeng.util.Platform;

class BlockDefinitionBuilder implements IBlockBuilder {

    private final FeatureFactory factory;

    private final Identifier id;

    private final Supplier<? extends Block> blockSupplier;

    private final List<BiFunction<Block, Item, IBootstrapComponent>> bootstrapComponents = new ArrayList<>();

    private final EnumSet<AEFeature> features = EnumSet.noneOf(AEFeature.class);

    private ItemGroup itemGroup = CreativeTab.INSTANCE;

    private TileEntityDefinition tileEntityDefinition;

    private boolean disableItem = false;

    private BiFunction<Block, Item.Settings, BlockItem> itemFactory;

    @Environment(EnvType.CLIENT)
    private BlockRendering blockRendering;

    @Environment(EnvType.CLIENT)
    private ItemRendering itemRendering;

    BlockDefinitionBuilder(FeatureFactory factory, String id, Supplier<? extends Block> blockSupplier) {
        this.factory = factory;
        this.id = new Identifier(AppEng.MOD_ID, id);
        this.blockSupplier = blockSupplier;

        if (Platform.hasClientClasses()) {
            this.blockRendering = new BlockRendering(this.id);
            this.itemRendering = new ItemRendering();
        }
    }

    @Override
    public BlockDefinitionBuilder bootstrap(BiFunction<Block, Item, IBootstrapComponent> callback) {
        this.bootstrapComponents.add(callback);
        return this;
    }

    @Override
    public IBlockBuilder features(AEFeature... features) {
        this.features.clear();
        this.addFeatures(features);
        return this;
    }

    @Override
    public IBlockBuilder addFeatures(AEFeature... features) {
        Collections.addAll(this.features, features);
        return this;
    }

    @Override
    public BlockDefinitionBuilder rendering(BlockRenderingCustomizer callback) {
        if (Platform.hasClientClasses()) {
            this.customizeForClient(callback);
        }

        return this;
    }

    @Override
    public IBlockBuilder tileEntity(TileEntityDefinition tileEntityDefinition) {
        this.tileEntityDefinition = tileEntityDefinition;
        return this;
    }

    @Override
    public IBlockBuilder item(BiFunction<Block, Item.Settings, BlockItem> factory) {
        this.itemFactory = factory;
        return this;
    }

    @Override
    public IBlockBuilder disableItem() {
        this.disableItem = true;
        return this;
    }

    @Environment(EnvType.CLIENT)
    private void customizeForClient(BlockRenderingCustomizer callback) {
        callback.customize(this.blockRendering, this.itemRendering);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends IBlockDefinition> T build() {
        // Create block and matching item, and set factory name of both
        Block block = this.blockSupplier.get();

        // Register the item and block with the game
        Registry.register(Registry.BLOCK, id, block);
        BlockItem item = this.constructItemFromBlock(block);
        if (item != null) {
            Registry.register(Registry.ITEM, id, item);
        }

        // Register all extra handlers
        this.bootstrapComponents.forEach(component -> this.factory.addBootstrapComponent(component.apply(block, item)));

        if (this.tileEntityDefinition != null) {
            // Tell the block entity definition about the block we've registered
            this.tileEntityDefinition.addBlock(block);
        }

        if (Platform.hasClientClasses()) {
            this.blockRendering.apply(this.factory, block);

            if (item != null) {
                this.itemRendering.apply(this.factory, item);
            }
        }

        T definition;
        if (block instanceof AEBaseTileBlock) {
            definition = (T) new TileDefinition(this.id.getPath(), (AEBaseTileBlock<?>) block, item, features);
        } else {
            definition = (T) new BlockDefinition(this.id.getPath(), block, item, features);
        }

        if (itemGroup == CreativeTab.INSTANCE) {
            CreativeTab.add(definition);
        }

        return definition;
    }

    @Nullable
    private BlockItem constructItemFromBlock(Block block) {
        if (this.disableItem) {
            return null;
        }

        Item.Settings itemProperties = new Item.Settings();

        if (itemGroup != null) {
            itemProperties.group(itemGroup);
        }
        // FIXME: Allow more/all item properties

        if (this.itemFactory != null) {
            return this.itemFactory.apply(block, itemProperties);
        } else if (block instanceof AEBaseBlock) {
            return new AEBaseBlockItem(block, itemProperties);
        } else {
            return new BlockItem(block, itemProperties);
        }
    }
}
