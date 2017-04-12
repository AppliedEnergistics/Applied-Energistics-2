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
import appeng.api.config.TunnelType;
import appeng.api.features.IP2PTunnelRegistry;
import appeng.helpers.Reflected;
import appeng.integration.IIntegrationModule;
import appeng.integration.IntegrationHelper;
import appeng.integration.abstraction.IBuildCraftCore;
import buildcraft.api.tools.IToolWrench;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;


@Reflected
public final class BuildCraftCore implements IBuildCraftCore, IIntegrationModule
{
	@Reflected
	public static BuildCraftCore instance;

	@Reflected
	public BuildCraftCore()
	{
		IntegrationHelper.testClassExistence( this, buildcraft.BuildCraftCore.class );
		IntegrationHelper.testClassExistence( this, buildcraft.BuildCraftTransport.class );
		IntegrationHelper.testClassExistence( this, buildcraft.api.tools.IToolWrench.class );
	}

	@Override
	public boolean isWrench( final Item eq )
	{
		return eq instanceof IToolWrench;
	}

	@Override
	public boolean canWrench( @Nonnull final Item wrench, final EntityPlayer wrencher, final int x, final int y, final int z )
	{
		return ( (IToolWrench) wrench ).canWrench( wrencher, x, y, z );
	}

	@Override
	public void wrenchUsed( @Nonnull final Item wrench, final EntityPlayer wrencher, final int x, final int y, final int z )
	{
		( (IToolWrench) wrench ).wrenchUsed( wrencher, x, y, z );
	}

	@Override
	public void init()
	{
	}

	@Override
	public void postInit()
	{
		this.registerPowerP2P();
	}

	private void registerPowerP2P()
	{
		final IP2PTunnelRegistry registry = AEApi.instance().registries().p2pTunnel();

		registry.addNewAttunement( new ItemStack( buildcraft.BuildCraftCore.engineBlock, 1, 0 ), TunnelType.RF_POWER );
		registry.addNewAttunement( new ItemStack( buildcraft.BuildCraftCore.engineBlock, 1, 1 ), TunnelType.RF_POWER );
		registry.addNewAttunement( new ItemStack( buildcraft.BuildCraftCore.engineBlock, 1, 2 ), TunnelType.RF_POWER );
	}
}
