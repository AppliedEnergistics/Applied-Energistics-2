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

package appeng.integration.modules;


import appeng.helpers.Reflected;
import appeng.integration.IIntegrationModule;
import appeng.integration.IntegrationHelper;
import appeng.integration.abstraction.IInvTweaks;
import cpw.mods.fml.common.Loader;
import invtweaks.api.InvTweaksAPI;
import net.minecraft.item.ItemStack;


public class InvTweaks implements IInvTweaks, IIntegrationModule
{
	@Reflected
	public static InvTweaks instance;

	static InvTweaksAPI api;

	@Reflected
	public InvTweaks()
	{
		IntegrationHelper.testClassExistence( this, invtweaks.api.InvTweaksAPI.class );
	}

	@Override
	public void init()
	{
		api = (InvTweaksAPI) Loader.instance().getIndexedModList().get( "inventorytweaks" ).getMod();
	}

	@Override
	public void postInit()
	{
		if( api == null )
		{
			throw new IllegalStateException( "InvTweaks API Instance Failed." );
		}
	}

	@Override
	public int compareItems( final ItemStack i, final ItemStack j )
	{
		return api.compareItems( i, j );
	}
}
