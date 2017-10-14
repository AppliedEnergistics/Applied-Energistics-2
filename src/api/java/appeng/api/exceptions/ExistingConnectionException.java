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

package appeng.api.exceptions;


import appeng.api.networking.IGridNode;


/**
 * Exception occurred because of an already existing connection between the two {@link IGridNode}s
 *
 * Intended to signal an internal exception and not intended to be thrown by
 * any 3rd party module.
 *
 * @author yueh
 * @version rv3
 * @since rv3
 */
public class ExistingConnectionException extends FailedConnectionException
{

	private static final long serialVersionUID = 2975450379720353182L;
	private static final String DEFAULT_MESSAGE = "Connection between both nodes already exists.";

	public ExistingConnectionException()
	{
		super( DEFAULT_MESSAGE );
	}

	public ExistingConnectionException( String message )
	{
		super( message );
	}

}
