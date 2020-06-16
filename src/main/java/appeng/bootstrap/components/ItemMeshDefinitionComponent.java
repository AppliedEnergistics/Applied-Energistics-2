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

import net.minecraftforge.api.distmarker.Dist;

import appeng.bootstrap.IModelRegistry;

/**
 * Registers a custom item mesh definition that can be used to dynamically
 * determine the item model based on item stack properties.
 */
public class ItemMeshDefinitionComponent implements IModelRegistrationComponent {

// FIXME	private final Item item;
// FIXME
// FIXME	private final ItemMeshDefinition meshDefinition;

// FIXME	public ItemMeshDefinitionComponent( @Nonnull Item item, @Nonnull ItemMeshDefinition meshDefinition )
// FIXME	{
// FIXME		this.item = item;
// FIXME		this.meshDefinition = meshDefinition;
// FIXME	}

    @Override
    public void modelRegistration(Dist dist, IModelRegistry registry) {
        // FIXME registry.setCustomMeshDefinition( this.item, this.meshDefinition );
    }
}
