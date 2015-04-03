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


import java.lang.reflect.Field;

import appeng.api.exceptions.CoreInaccessibleException;


/**
 * Entry point for api.
 *
 * Available IMCs:
 */
public enum AEApi
{
	;

	private static final String CORE_API_FQN = "appeng.core.Api";
	private static final String CORE_API_FIELD = "INSTANCE";
	private static final IAppEngApi HELD_API;

	static
	{
		try
		{
			final Class<?> apiClass = Class.forName( CORE_API_FQN );
			final Field apiField = apiClass.getField( CORE_API_FIELD );

			HELD_API = (IAppEngApi) apiField.get( apiClass );
		}
		catch( ClassNotFoundException e )
		{
			throw new CoreInaccessibleException( "AE2 API tried to access the " + CORE_API_FQN + " class, without it being declared." );
		}
		catch( NoSuchFieldException e )
		{
			throw new CoreInaccessibleException( "AE2 API tried to access the " + CORE_API_FIELD + " field in " + CORE_API_FQN + " without it being declared." );
		}
		catch( IllegalAccessException e )
		{
			throw new CoreInaccessibleException( "AE2 API tried to access the " + CORE_API_FIELD + " field in " + CORE_API_FQN + " without enough access permissions." );
		}
	}

	/**
	 * API Entry Point.
	 *
	 * @return the {@link IAppEngApi}
	 */
	public static IAppEngApi instance()
	{
		return HELD_API;
	}

}
