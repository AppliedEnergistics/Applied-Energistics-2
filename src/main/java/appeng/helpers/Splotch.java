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

package appeng.helpers;


import io.netty.buffer.ByteBuf;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import appeng.api.util.AEColor;


public class Splotch
{

	public final EnumFacing side;
	public final boolean lumen;
	public final AEColor color;
	private final int pos;

	public Splotch( AEColor col, boolean lit, EnumFacing side, Vec3 position )
	{
		this.color = col;
		this.lumen = lit;

		double x;
		double y;

		if( side == EnumFacing.SOUTH || side == EnumFacing.NORTH )
		{
			x = position.xCoord;
			y = position.yCoord;
		}

		else if( side == EnumFacing.UP || side == EnumFacing.DOWN )
		{
			x = position.xCoord;
			y = position.zCoord;
		}

		else
		{
			x = position.yCoord;
			y = position.zCoord;
		}

		int a = (int) ( x * 0xF );
		int b = (int) ( y * 0xF );
		this.pos = a | ( b << 4 );

		this.side = side;
	}

	public Splotch( ByteBuf data )
	{

		this.pos = data.readByte();
		int val = data.readByte();

		this.side = EnumFacing.VALUES[ val & 0x07 ];
		this.color = AEColor.values()[( val >> 3 ) & 0x0F];
		this.lumen = ( ( val >> 7 ) & 0x01 ) > 0;
	}

	public void writeToStream( ByteBuf stream )
	{
		stream.writeByte( this.pos );
		int val = this.side.ordinal() | ( this.color.ordinal() << 3 ) | ( this.lumen ? 0x80 : 0x00 );
		stream.writeByte( val );
	}

	public float x()
	{
		return ( this.pos & 0x0f ) / 15.0f;
	}

	public float y()
	{
		return ( ( this.pos >> 4 ) & 0x0f ) / 15.0f;
	}

	public int getSeed()
	{
		int val = this.side.ordinal() | ( this.color.ordinal() << 3 ) | ( this.lumen ? 0x80 : 0x00 );
		return Math.abs( this.pos + val );
	}
}
