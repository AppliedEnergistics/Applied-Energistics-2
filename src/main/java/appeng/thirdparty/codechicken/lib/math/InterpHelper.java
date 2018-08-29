/*
 * This file is part of CodeChickenLib.
 * Copyright (c) 2018, covers1624, All rights reserved.
 *
 * CodeChickenLib is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * CodeChickenLib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with CodeChickenLib. If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.thirdparty.codechicken.lib.math;


/**
 * @author covers1624
 */
public class InterpHelper
{

	private float[][] posCache = new float[4][2];
	private float[] valCache = new float[4];

	private float x0;
	private float x1;
	private float y0;
	private float y1;

	private float rX;
	private float rY;

	private int p00;
	private int p10;
	private int p11;
	private int p01;

	/**
	 * Resets the interp helper with the given quad. Does not care what order the vertices are in.
	 */
	public void reset( float dx0, float dy0, float dx1, float dy1, float dx2, float dy2, float dx3, float dy3 )
	{

		float[] vec0 = posCache[0];
		float[] vec1 = posCache[1];
		float[] vec2 = posCache[2];
		float[] vec3 = posCache[3];

		vec0[0] = dx0;
		vec1[0] = dx1;
		vec2[0] = dx2;
		vec3[0] = dx3;

		vec0[1] = dy0;
		vec1[1] = dy1;
		vec2[1] = dy2;
		vec3[1] = dy3;
	}

	/**
	 * Call when you are ready to use the InterpHelper.
	 */
	public void setup()
	{
		p00 = 0;//Bottom Left is always first.
		x0 = posCache[p00][0];
		y0 = posCache[p00][1];
		for( int i = 1; i < 4; i++ )
		{
			float x = posCache[i][0];
			float y = posCache[i][1];
			if( y0 == y )
			{
				p10 = i;// Bottom right.
				x1 = x;
			}
			else if( x0 == x )
			{
				p01 = i;//Top left.
				y1 = y;
			}
			else
			{
				//Top right.
				p11 = i;
			}
		}
	}

	/**
	 * Computes the coefficients for the interpolation.
	 *
	 * @param x X interp location.
	 * @param y Y interp location.
	 */
	public void locate( float x, float y )
	{
		rX = ( x - x0 ) / ( x1 - x0 );
		rY = ( y - y0 ) / ( y1 - y0 );
	}

	/**
	 * Interpolates using the already computed coefficients.
	 *
	 * @param q0 Value at dx0 dy0
	 * @param q1 Value at dx1 dy1
	 * @param q2 Value at dx2 dy2
	 * @param q3 Value at dx3 dy3
	 *
	 * @return The result.
	 */
	public float interpolate( float q0, float q1, float q2, float q3 )
	{
		valCache[0] = q0;
		valCache[1] = q1;
		valCache[2] = q2;
		valCache[3] = q3;
		float f0 = ( valCache[p00] * ( 1 - rX ) ) + ( valCache[p10] * rX );
		float f1 = ( valCache[p01] * ( 1 - rX ) ) + ( valCache[p11] * rX );

		return ( f0 * ( 1 - rY ) ) + ( f1 * rY );
	}
}
