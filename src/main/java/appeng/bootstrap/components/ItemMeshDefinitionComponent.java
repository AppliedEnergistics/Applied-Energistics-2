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


import javax.annotation.Nonnull;

import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;

import appeng.bootstrap.IBootstrapComponent;


/**
 * Registers a custom item mesh definition that can be used to dynamically determine the item model based on
 * item stack properties.
 */
public class ItemMeshDefinitionComponent implements IBootstrapComponent
{

	private final Item item;

	private final ItemMeshDefinition meshDefinition;

	public ItemMeshDefinitionComponent( @Nonnull Item item, @Nonnull ItemMeshDefinition meshDefinition )
	{
		this.item = item;
		this.meshDefinition = meshDefinition;
	}

	@Override
	public void modelReg( Side side )
	{
		ModelLoader.setCustomMeshDefinition( this.item, this.meshDefinition );
	}
}
