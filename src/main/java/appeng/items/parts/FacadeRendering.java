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


import appeng.bootstrap.IItemRendering;
import appeng.bootstrap.ItemRenderingCustomizer;
import appeng.client.render.FacadeItemModel;


/**
 * Handles rendering customization for facade items. Please note that this works very differently
 * from actually rendering a Facade in a cable bus.
 */
public class FacadeRendering extends ItemRenderingCustomizer {
    @Override
    public void customize(IItemRendering rendering) {
        // This actually just uses the path it will look for by default, no custom model redirection needed
        rendering.builtInModel("models/item/facade", new FacadeItemModel());
    }
}
