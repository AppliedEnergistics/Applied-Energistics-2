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

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemModelsProperties;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.api.definitions.IBlockDefinition;
import appeng.api.features.AEFeature;
import appeng.block.AEBaseBlock;
import appeng.block.AEBaseBlockItem;
import appeng.block.AEBaseBlockItemChargeable;
import appeng.block.AEBaseTileBlock;
import appeng.bootstrap.components.IBlockRegistrationComponent;
import appeng.bootstrap.components.IClientSetupComponent;
import appeng.bootstrap.components.IItemRegistrationComponent;
import appeng.bootstrap.definitions.TileEntityDefinition;
import appeng.core.AEItemGroup;
import appeng.core.AppEng;
import appeng.core.CreativeTab;
import appeng.core.features.BlockDefinition;
import appeng.core.features.TileDefinition;
import appeng.util.Platform;

class BlockDefinitionBuilder implements IBlockBuilder {

    private final FeatureFactory factory;

    private final String registryName;

    private final Supplier<? extends Block> blockSupplier;

    private final List<BiFunction<Block, Item, IBootstrapComponent>> bootstrapComponents = new ArrayList<>();

    private final EnumSet<AEFeature> features = EnumSet.noneOf(AEFeature.class);

    private ItemGroup itemGroup = CreativeTab.INSTANCE;

    private TileEntityDefinition tileEntityDefinition;

    private boolean disableItem = false;

    private BiFunction<Block, Item.Properties, BlockItem> itemFactory;

    @OnlyIn(Dist.CLIENT)
    private BlockRendering blockRendering;

    @OnlyIn(Dist.CLIENT)
    private ItemRendering itemRendering;

    BlockDefinitionBuilder(FeatureFactory factory, String id, Supplier<? extends Block> blockSupplier) {
        this.factory = factory;
        this.registryName = id;
        this.blockSupplier = blockSupplier;

        if (Platform.hasClientClasses()) {
            this.blockRendering = new BlockRendering();
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
    public IBlockBuilder item(BiFunction<Block, Item.Properties, BlockItem> factory) {
        this.itemFactory = factory;
        return this;
    }

    @Override
    public IBlockBuilder disableItem() {
        this.disableItem = true;
        return this;
    }

    @OnlyIn(Dist.CLIENT)
    private void customizeForClient(BlockRenderingCustomizer callback) {
        callback.customize(this.blockRendering, this.itemRendering);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends IBlockDefinition> T build() {
        // Create block and matching item, and set factory name of both
        Block block = this.blockSupplier.get();
        block.setRegistryName(AppEng.MOD_ID, this.registryName);

        BlockItem item = this.constructItemFromBlock(block);
        if (item != null) {
            item.setRegistryName(AppEng.MOD_ID, this.registryName);
        }

        // Register the client-only item model property for chargeable items
        if (item instanceof AEBaseBlockItemChargeable) {
            AEBaseBlockItemChargeable chargeable = (AEBaseBlockItemChargeable) item;
            this.factory.addBootstrapComponent(new IClientSetupComponent() {
                @Override
                @OnlyIn(Dist.CLIENT)
                public void setup() {
                    ItemModelsProperties.func_239418_a_(item, new ResourceLocation("appliedenergistics2:fill_level"),
                            (is, world, entity) -> {
                                double curPower = chargeable.getAECurrentPower(is);
                                double maxPower = chargeable.getAEMaxPower(is);

                                return (int) Math.round(100 * curPower / maxPower);
                            });
                }
            });
        }

        // Register the item and block with the game
        this.factory.addBootstrapComponent((IBlockRegistrationComponent) (side, registry) -> registry.register(block));
        if (item != null) {
            this.factory
                    .addBootstrapComponent((IItemRegistrationComponent) (side, registry) -> registry.register(item));
        }

        // Register all extra handlers
        this.bootstrapComponents.forEach(component -> this.factory.addBootstrapComponent(component.apply(block, item)));

        if (this.tileEntityDefinition != null) {
            // Tell the tile entity definition about the block we've registered
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
            definition = (T) new TileDefinition(this.registryName, (AEBaseTileBlock<?>) block, item, features);
        } else {
            definition = (T) new BlockDefinition(this.registryName, block, item, features);
        }

        if (itemGroup instanceof AEItemGroup) {
            ((AEItemGroup) itemGroup).add(definition);
        }

        return definition;
    }

    @Nullable
    private BlockItem constructItemFromBlock(Block block) {
        if (this.disableItem) {
            return null;
        }

        Item.Properties itemProperties = new Item.Properties();

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
