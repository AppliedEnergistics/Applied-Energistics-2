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

package appeng.api.networking.security;


import java.util.EnumSet;
import java.util.HashMap;

import appeng.api.config.SecurityPermissions;


/**
 * Implemented on Security Terminal to interface with security cache.
 */
public interface ISecurityProvider
{

	/**
	 * used to represent the security key for the network, should be based on a unique timestamp.
	 *
	 * @return unique key.
	 */
	long getSecurityKey();

	/**
	 * Push permission data into security cache.
	 *
	 * @param playerPerms player permissions
	 */
	void readPermissions( HashMap<Integer, EnumSet<SecurityPermissions>> playerPerms );

	/**
	 * @return is security on or off?
	 */
	boolean isSecurityEnabled();

	/**
	 * @return player ID for who placed the security provider.
	 */
	int getOwner();
}
