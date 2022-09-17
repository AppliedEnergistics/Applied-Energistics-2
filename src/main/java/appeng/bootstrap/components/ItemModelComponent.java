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

package appeng.bootstrap.components;


import appeng.bootstrap.IModelRegistry;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.fml.relauncher.Side;

import javax.annotation.Nonnull;
import java.util.Map;


/**
 * Registers the models that should by used for an item, including the ability to
 * distinguish by meta.
 */
public class ItemModelComponent implements IModelRegistrationComponent {

    private final Item item;

    private final Map<Integer, ModelResourceLocation> modelsByMeta;

    public ItemModelComponent(@Nonnull Item item, @Nonnull Map<Integer, ModelResourceLocation> modelsByMeta) {
        this.item = item;
        this.modelsByMeta = modelsByMeta;
    }

    @Override
    public void modelRegistration(Side side, IModelRegistry registry) {
        this.modelsByMeta.forEach((meta, model) ->
        {
            registry.setCustomModelResourceLocation(this.item, meta, model);
        });
    }

}
