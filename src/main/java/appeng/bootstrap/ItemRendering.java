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


import appeng.bootstrap.components.ItemColorComponent;
import appeng.bootstrap.components.ItemMeshDefinitionComponent;
import appeng.bootstrap.components.ItemModelComponent;
import appeng.bootstrap.components.ItemVariantsComponent;
import com.google.common.collect.ImmutableMap;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.*;


class ItemRendering implements IItemRendering {

    @SideOnly(Side.CLIENT)
    private IItemColor itemColor;

    @SideOnly(Side.CLIENT)
    private ItemMeshDefinition itemMeshDefinition;

    @SideOnly(Side.CLIENT)
    private final Map<Integer, ModelResourceLocation> itemModels = new HashMap<>();

    @SideOnly(Side.CLIENT)
    private final Set<ResourceLocation> variants = new HashSet<>();

    @SideOnly(Side.CLIENT)
    private final Map<String, IModel> builtInModels = new HashMap<>();

    @Override
    @SideOnly(Side.CLIENT)
    public IItemRendering meshDefinition(ItemMeshDefinition meshDefinition) {
        this.itemMeshDefinition = meshDefinition;
        return this;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IItemRendering model(int meta, ModelResourceLocation model) {
        this.itemModels.put(meta, model);
        return this;
    }

    @Override
    public IItemRendering variants(Collection<ResourceLocation> resources) {
        this.variants.addAll(resources);
        return this;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IItemRendering color(IItemColor itemColor) {
        this.itemColor = itemColor;
        return this;
    }

    @Override
    public IItemRendering builtInModel(String name, IModel model) {
        this.builtInModels.put(name, model);
        return this;
    }

    void apply(FeatureFactory factory, Item item) {
        if (this.itemMeshDefinition != null) {
            factory.addBootstrapComponent(new ItemMeshDefinitionComponent(item, this.itemMeshDefinition));
        }

        if (!this.itemModels.isEmpty()) {
            factory.addBootstrapComponent(new ItemModelComponent(item, this.itemModels));
        }

        Set<ResourceLocation> resources = new HashSet<>(this.variants);

        // Register a default item model if neither items by meta nor an item mesh definition exist
        if (this.itemMeshDefinition == null && this.itemModels.isEmpty()) {
            ModelResourceLocation model;

            // For block items, the default will try to use the default state of the associated block
            if (item instanceof ItemBlock) {
                Block block = ((ItemBlock) item).getBlock();

                // We can only do this once the blocks are actually registered...
                StateMapperHelper helper = new StateMapperHelper(item.getRegistryName());
                model = helper.getModelResourceLocation(block.getDefaultState());
            } else {
                model = new ModelResourceLocation(item.getRegistryName(), "inventory");
            }
            factory.addBootstrapComponent(new ItemModelComponent(item, ImmutableMap.of(0, model)));
        }

        // TODO : 1.12
        this.builtInModels.forEach(factory::addBuiltInModel);

        if (!resources.isEmpty()) {
            factory.addBootstrapComponent(new ItemVariantsComponent(item, resources));
        } else if (this.itemMeshDefinition != null) {
            // Adding an empty variant list here will prevent Vanilla from trying to load the default item model in this
            // case
            factory.addBootstrapComponent(new ItemVariantsComponent(item, Collections.emptyList()));
        }

        if (this.itemColor != null) {
            factory.addBootstrapComponent(new ItemColorComponent(item, this.itemColor));
        }
    }

    private static class StateMapperHelper extends StateMapperBase {

        private final ResourceLocation registryName;

        public StateMapperHelper(ResourceLocation registryName) {
            this.registryName = registryName;
        }

        @Override
        protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
            return new ModelResourceLocation(this.registryName, this.getPropertyString(state.getProperties()));
        }
    }
}
