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

package appeng.parts.reporting;


import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import appeng.api.parts.IPartModel;
import appeng.core.AppEng;
import appeng.helpers.Reflected;
import appeng.items.parts.PartModels;
import appeng.parts.PartModel;


public class PartSemiDarkPanel extends AbstractPartPanel
{
	@PartModels
	public static final ResourceLocation MODEL_OFF = new ResourceLocation( AppEng.MOD_ID, "part/monitor_medium_off" );
	@PartModels
	public static final ResourceLocation MODEL_ON = new ResourceLocation( AppEng.MOD_ID, "part/monitor_medium_on" );

	public static final PartModel MODELS_OFF = new PartModel( MODEL_BASE, MODEL_OFF );
	public static final IPartModel MODELS_ON = new PartModel( MODEL_BASE, MODEL_ON );

	@Reflected
	public PartSemiDarkPanel( final ItemStack is )
	{
		super( is );
	}

	@Override
	protected int getBrightnessColor()
	{
		final int light = this.getColor().whiteVariant;
		final int dark = this.getColor().mediumVariant;
		return ( ( ( ( ( light >> 16 ) & 0xff ) + ( ( dark >> 16 ) & 0xff ) ) / 2 ) << 16 ) | ( ( ( ( ( light >> 8 ) & 0xff ) + ( ( dark >> 8 ) & 0xff ) ) / 2 ) << 8 ) | ( ( ( ( light ) & 0xff ) + ( ( dark ) & 0xff ) ) / 2 );
	}

	@Override
	public IPartModel getStaticModels()
	{
		return isPowered() ? MODELS_ON : MODELS_OFF;
	}

}
