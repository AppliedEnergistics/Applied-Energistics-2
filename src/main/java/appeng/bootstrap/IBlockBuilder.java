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


import appeng.api.definitions.IBlockDefinition;
import appeng.bootstrap.definitions.TileEntityDefinition;
import appeng.core.features.AEFeature;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;

import java.util.function.BiFunction;
import java.util.function.Function;


public interface IBlockBuilder {
    IBlockBuilder bootstrap(BiFunction<Block, Item, IBootstrapComponent> component);

    IBlockBuilder features(AEFeature... features);

    IBlockBuilder addFeatures(AEFeature... features);

    IBlockBuilder rendering(BlockRenderingCustomizer callback);

    IBlockBuilder tileEntity(TileEntityDefinition tileEntityDefinition);

    /**
     * Don't register an item for this block.
     */
    IBlockBuilder disableItem();

    /**
     * Forces this block's item to uses a custom model, instead of using the default block state as the item model.
     * The model has the same name as the registry name.
     */
    IBlockBuilder useCustomItemModel();

    IBlockBuilder item(Function<Block, ItemBlock> factory);

    <T extends IBlockDefinition> T build();
}
