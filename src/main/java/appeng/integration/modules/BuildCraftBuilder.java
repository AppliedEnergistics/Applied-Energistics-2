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

package appeng.integration.modules;


import appeng.api.AEApi;
import appeng.api.definitions.IBlockDefinition;
import appeng.api.definitions.IBlocks;
import appeng.api.definitions.ITileDefinition;
import appeng.api.util.IOrientableBlock;
import appeng.core.AELog;
import appeng.helpers.Reflected;
import appeng.integration.IIntegrationModule;
import appeng.integration.IntegrationHelper;
import appeng.integration.modules.BCHelpers.AECableSchematicTile;
import appeng.integration.modules.BCHelpers.AEGenericSchematicTile;
import appeng.integration.modules.BCHelpers.AERotatableBlockSchematic;
import buildcraft.api.blueprints.BuilderAPI;
import buildcraft.api.blueprints.ISchematicRegistry;
import com.google.common.base.Optional;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


/**
 * The builder has no interface, because it provides no functionality
 *
 * @author thatsIch
 * @version rv3 - 12.06.2015
 * @since rv3 12.06.2015
 */
@Reflected
public class BuildCraftBuilder implements IIntegrationModule
{
	@Reflected
	public static BuildCraftBuilder instance;

	@Reflected
	public BuildCraftBuilder()
	{
		IntegrationHelper.testClassExistence( this, buildcraft.api.blueprints.BuilderAPI.class );
		IntegrationHelper.testClassExistence( this, buildcraft.api.blueprints.IBuilderContext.class );
		IntegrationHelper.testClassExistence( this, buildcraft.api.blueprints.ISchematicRegistry.class );
		IntegrationHelper.testClassExistence( this, buildcraft.api.blueprints.SchematicTile.class );
		IntegrationHelper.testClassExistence( this, buildcraft.api.blueprints.SchematicBlock.class );
	}

	@Override
	public void init() throws Throwable
	{
		try
		{
			this.initBuilderSupport();
		}
		catch( final Exception builderSupport )
		{
			// not supported?
		}
	}

	@Override
	public void postInit()
	{
	}

	private void initBuilderSupport()
	{
		final ISchematicRegistry schematicRegistry = BuilderAPI.schematicRegistry;

		final IBlocks blocks = AEApi.instance().definitions().blocks();
		final IBlockDefinition maybeMultiPart = blocks.multiPart();

		for( final Method blockDefinition : blocks.getClass().getMethods() )
		{
			try
			{
				final IBlockDefinition def = (IBlockDefinition) blockDefinition.invoke( blocks );
				final Optional<Block> maybeBlock = def.maybeBlock();
				if( !maybeBlock.isPresent() )
				{
					continue;
				}

				final Block block = maybeBlock.get();
				if( block instanceof IOrientableBlock && ( (IOrientableBlock) block ).usesMetadata() && !( def instanceof ITileDefinition ) )
				{
					schematicRegistry.registerSchematicBlock( block, AERotatableBlockSchematic.class );
				}
				else if( maybeMultiPart.isSameAs( new ItemStack( block ) ) )
				{
					schematicRegistry.registerSchematicBlock( block, AECableSchematicTile.class );
				}
				else if( def instanceof ITileDefinition )
				{
					schematicRegistry.registerSchematicBlock( block, AEGenericSchematicTile.class );
				}
			}
			catch( final InvocationTargetException ignore )
			{
				AELog.warn( "Encountered problems while initializing the BuildCraft Builder support. Can not invoke the method %s", blockDefinition );
			}
			catch( final IllegalAccessException ignore )
			{
				AELog.warn( "Encountered problems while initializing the BuildCraft Builder support. Can not access the method %s", blockDefinition );
			}
		}
	}
}
