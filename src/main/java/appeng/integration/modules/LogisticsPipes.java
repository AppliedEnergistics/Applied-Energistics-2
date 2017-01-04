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
import appeng.helpers.Reflected;
import appeng.integration.IIntegrationModule;
import appeng.integration.IntegrationHelper;
import appeng.integration.abstraction.ILogisticsPipes;
import appeng.integration.modules.LPHelpers.LPPipeHandler;

import buildcraft.api.transport.IInjectable;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import javax.annotation.Nonnull;


/**
 * @author Second_Fry
 * @version rv3 - 12.06.2015
 * @since rv3 12.06.2015
 */
@Reflected
public class LogisticsPipes implements ILogisticsPipes, IIntegrationModule
{
	@Reflected
	public static LogisticsPipes instance;

	@Reflected
	public LogisticsPipes()
	{
		IntegrationHelper.testClassExistence( this, logisticspipes.api.ILPPipe.class );
		IntegrationHelper.testClassExistence( this, logisticspipes.api.ILPPipeTile.class );
		IntegrationHelper.testClassExistence( this, logisticspipes.api.IRequestAPI.class );
		IntegrationHelper.testClassExistence( this, logisticspipes.pipes.basic.CoreRoutedPipe.class );
		IntegrationHelper.testClassExistence( this, logisticspipes.proxy.SimpleServiceLocator.class );
		IntegrationHelper.testClassExistence( this, logisticspipes.routing.ExitRoute.class );
		IntegrationHelper.testClassExistence( this, logisticspipes.utils.AdjacentTile.class );
		IntegrationHelper.testClassExistence( this, logisticspipes.utils.item.ItemIdentifier.class );
	}

	@Override
	public void init() throws Throwable
	{
	}

	@Override
	public void postInit()
	{
		AEApi.instance().registries().externalStorage().addExternalStorageInterface( new LPPipeHandler() );
	}

	@Override
	public boolean canAddItemsToPipe( final TileEntity te, final ItemStack is, final ForgeDirection direction )
	{
		if( is != null && te != null && te instanceof IInjectable )
		{
			final IInjectable pt = (IInjectable) te;
			if( pt.canInjectItems( direction ) )
			{
				final int amt = pt.injectItem( is, false, direction, null );
				if( amt == is.stackSize )
				{
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public boolean addItemsToPipe( final TileEntity te, final ItemStack is, @Nonnull final ForgeDirection direction )
	{
		if( is != null && te != null && te instanceof IInjectable )
		{
			final IInjectable pt = (IInjectable) te;
			if( pt.canInjectItems( direction ) )
			{
				final int amt = pt.injectItem( is, false, direction, null );
				if( amt == is.stackSize )
				{
					pt.injectItem( is, true, direction, null );
					return true;
				}
			}
		}

		return false;
	}
}
