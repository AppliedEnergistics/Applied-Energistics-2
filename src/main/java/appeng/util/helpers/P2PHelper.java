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

package appeng.util.helpers;


import com.google.common.base.Preconditions;

import appeng.api.util.AEColor;


public class P2PHelper
{

	public AEColor[] toColours( short frequency )
	{
		final AEColor[] colours = new AEColor[4];

		for( int i = 0; i < 4; i++ )
		{
			int nibble = ( frequency >> 4 * ( 3 - i ) ) & 0xF;

			colours[i] = AEColor.values()[nibble];
		}

		return colours;
	}

	public short fromColours( AEColor[] colours )
	{
		Preconditions.checkArgument( colours.length == 4 );

		int t = 0;

		for( int i = 0; i < 4; i++ )
		{
			int code = colours[3 - i].ordinal() << 4 * i;

			t |= code;
		}

		return (short) ( t & 0xFFFF );
	}

}
