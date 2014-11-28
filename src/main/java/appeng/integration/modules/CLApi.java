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

import appeng.api.util.AEColor;
import appeng.integration.BaseModule;
import appeng.integration.abstraction.ICLApi;

public class CLApi extends BaseModule implements ICLApi
{

	public static CLApi instance;

	@Override
	public void Init() throws Throwable
	{
		TestClass( coloredlightscore.src.api.CLApi.class );
	}

	@Override
	public void PostInit()
	{
		// :P
	}

	@Override
	public int colorLight(AEColor color, int light)
	{
		int mv = color.mediumVariant;

		float r = (mv >> 16) & 0xff;
		float g = (mv >> 8) & 0xff;
		float b = ( mv ) & 0xff;

		return coloredlightscore.src.api.CLApi.makeRGBLightValue( r / 255.0f, g / 255.0f, b / 255.0f, light / 15.0f );
	}
}
