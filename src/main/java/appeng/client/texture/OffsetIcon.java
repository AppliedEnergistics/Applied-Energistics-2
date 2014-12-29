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

package appeng.client.texture;

import net.minecraft.util.IIcon;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class OffsetIcon implements IIcon
{

	final float offsetX;
	final float offsetY;

	private IIcon p;

	public OffsetIcon(IIcon o, float x, float y) {
		
		if ( o == null )
			throw new RuntimeException("Cannot create a wrapper icon with a null icon.");
		
		this.p = o;
		this.offsetX = x;
		this.offsetY = y;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public float getMinU()
	{
		return this.u( 0 - this.offsetX );
	}

	@Override
	@SideOnly(Side.CLIENT)
	public float getMaxU()
	{
		return this.u( 16 - this.offsetX );
	}

	@Override
	@SideOnly(Side.CLIENT)
	public float getInterpolatedU(double d0)
	{
		return this.u( d0 - this.offsetX );
	}

	@Override
	@SideOnly(Side.CLIENT)
	public float getMinV()
	{
		return this.v( 0 - this.offsetY );
	}

	@Override
	@SideOnly(Side.CLIENT)
	public float getMaxV()
	{
		return this.v( 16 - this.offsetY );
	}

	@Override
	@SideOnly(Side.CLIENT)
	public float getInterpolatedV(double d0)
	{
		return this.v( d0 - this.offsetY );
	}

	private float v(double d)
	{
		return this.p.getInterpolatedV( Math.min( 16.0, Math.max( 0.0, d ) ) );
	}

	private float u(double d)
	{
		return this.p.getInterpolatedU( Math.min( 16.0, Math.max( 0.0, d ) ) );
	}

	@Override
	@SideOnly(Side.CLIENT)
	public String getIconName()
	{
		return this.p.getIconName();
	}

	@Override
	public int getIconWidth()
	{
		return this.p.getIconWidth();
	}

	@Override
	public int getIconHeight()
	{
		return this.p.getIconHeight();
	}

}
