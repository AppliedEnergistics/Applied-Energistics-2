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

package appeng.api;


import appeng.api.exceptions.CouldNotAccessCoreAPI;


/**
 * Entry point for api.
 *
 * Available IMCs:
 */
public enum AEApi
{
	;
	private static final String CORE_API_PATH = "appeng.core.Api";
	private static final String API_INSTANCE_NAME = "INSTANCE";

	private static IAppEngApi instance = null;

	/**
	 * API Entry Point.
	 *
	 * @return the {@link IAppEngApi}
	 *
	 * @throws CouldNotAccessCoreAPI if the INSTANCE could not be retrieved
	 */
	public static IAppEngApi instance()

	{
		if ( instance == null )
		{
			try
			{
				Class<?> c = Class.forName( CORE_API_PATH );
				instance = (IAppEngApi) c.getField( API_INSTANCE_NAME ).get( c );
			}
			catch ( Throwable e )
			{
				throw new CouldNotAccessCoreAPI( "Either core API was not in " + CORE_API_PATH + " or " + API_INSTANCE_NAME + " was not accessible" );
			}
		}

		return instance;
	}
}
