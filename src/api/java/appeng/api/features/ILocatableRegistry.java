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

package appeng.api.features;


/**
 * A Registry for locatable items, works based on serial numbers.
 */
public interface ILocatableRegistry
{
	/**
	 * Attempts to find the object with the serial specified, if it can it
	 * returns the object.
	 *
	 * @param serial serial
	 *
	 * @return requestedObject, or null
	 *
	 * @deprecated use {@link ILocatableRegistry#getLocatableBy(long)}
	 */
	@Deprecated
	Object findLocatableBySerial( long serial );

	/**
	 * Gets the {@link ILocatable} with the registered serial, if available
	 *
	 * @param serial serial
	 *
	 * @return requestedObject, or null, if the object does not exist anymore
	 */
	ILocatable getLocatableBy( long serial );
}