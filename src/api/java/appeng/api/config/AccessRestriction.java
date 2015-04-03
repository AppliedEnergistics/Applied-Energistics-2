/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013 AlgorithmX2
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package appeng.api.config;


public enum AccessRestriction
{
	NO_ACCESS( 0 ), READ( 1 ), WRITE( 2 ), READ_WRITE( 3 );

	private final int permissionBit;

	AccessRestriction( int v )
	{
		this.permissionBit = v;
	}

	public boolean hasPermission( AccessRestriction ar )
	{
		return ( this.permissionBit & ar.permissionBit ) == ar.permissionBit;
	}

	public AccessRestriction restrictPermissions( AccessRestriction ar )
	{
		return this.getPermByBit( this.permissionBit & ar.permissionBit );
	}

	private AccessRestriction getPermByBit( int bit )
	{
		switch( bit )
		{
			default:
			case 0:
				return NO_ACCESS;
			case 1:
				return READ;
			case 2:
				return WRITE;
			case 3:
				return READ_WRITE;
		}
	}

	public AccessRestriction addPermissions( AccessRestriction ar )
	{
		return this.getPermByBit( this.permissionBit | ar.permissionBit );
	}

	public AccessRestriction removePermissions( AccessRestriction ar )
	{
		return this.getPermByBit( this.permissionBit & ( ~ar.permissionBit ) );
	}
}