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

package appeng.items.tools.quartz;


import appeng.core.features.AEFeature;
import appeng.core.features.IAEFeature;
import appeng.core.features.IFeatureHandler;
import appeng.core.features.ItemFeatureHandler;
import appeng.util.Platform;
import com.google.common.base.Optional;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemStack;

import java.util.EnumSet;


public class ToolQuartzAxe extends ItemAxe implements IAEFeature
{
	private final AEFeature type;
	private final IFeatureHandler feature;

	public ToolQuartzAxe( final AEFeature type )
	{
		super( ToolMaterial.IRON );
		this.feature = new ItemFeatureHandler( EnumSet.of( this.type = type, AEFeature.QuartzAxe ), this, this, Optional.of( type.name() ) );
	}

	@Override
	public IFeatureHandler handler()
	{
		return this.feature;
	}

	@Override
	public void postInit()
	{
		// override!
	}

	@Override
	public boolean getIsRepairable( final ItemStack a, final ItemStack b )
	{
		return Platform.canRepair( this.type, a, b );
	}
}
