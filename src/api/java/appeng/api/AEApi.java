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

/**
 * 
 * Entry point for api.
 * 
 * Available IMCs:
 * 
 */
public class AEApi
{

	static private IAppEngApi api = null;

	/**
	 * API Entry Point.
	 * 
	 * @return the {@link IAppEngApi} or null if the instance could not be retrieved
	 */
	public static IAppEngApi instance()
	{
		if ( api == null )
		{
			try
			{
				Class c = Class.forName( "appeng.core.Api" );
				api = (IAppEngApi) c.getField( "instance" ).get( c );
			}
			catch (Throwable e)
			{
				return null;
			}
		}

		return api;
	}

}
