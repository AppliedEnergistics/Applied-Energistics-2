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


import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Arrays;
import java.util.Collection;


/**
 * Allows the rendering of an item to be customized.
 */
public interface IItemRendering {

    /**
     * Registers a custom item mesh definition that will be used to dynamically determine the
     * item model to be used for rendering by inspecting the item stack (i.e. for NBT data).
     * Please
     */
    @SideOnly(Side.CLIENT)
    IItemRendering meshDefinition(ItemMeshDefinition meshDefinition);

    /**
     * Registers an item model for meta=0, see {@link #model(int, ModelResourceLocation)}.
     */
    @SideOnly(Side.CLIENT)
    default IItemRendering model(ModelResourceLocation model) {
        return model(0, model);
    }

    /**
     * Registers an item model for a given meta.
     */
    @SideOnly(Side.CLIENT)
    IItemRendering model(int meta, ModelResourceLocation model);

    /**
     * Convenient override for {@link #variants(Collection)}.
     */
    @SideOnly(Side.CLIENT)
    default IItemRendering variants(ResourceLocation... resources) {
        return variants(Arrays.asList(resources));
    }

    /**
     * Registers the item variants of this item. This are all models that need to be loaded for this item.
     * This has no direct effect on rendering, but is used to load models that are used for example by
     * the ItemMeshDefinition.
     * <p>
     * Models registered via {@link #model(int, ModelResourceLocation)} are automatically added here.
     */
    @SideOnly(Side.CLIENT)
    IItemRendering variants(Collection<ResourceLocation> resources);

    /**
     * Registers a custom item color definition that inspects an item stack and tint and
     * returns a color multiplier.
     */
    @SideOnly(Side.CLIENT)
    IItemRendering color(IItemColor itemColor);

    /**
     * Registers a built-in model under the given resource path.
     */
    @SideOnly(Side.CLIENT)
    IItemRendering builtInModel(String name, IModel model);

}
