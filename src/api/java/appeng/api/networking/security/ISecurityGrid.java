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


import net.minecraft.entity.player.EntityPlayer;

import appeng.api.config.SecurityPermissions;
import appeng.api.networking.IGridCache;


public interface ISecurityGrid extends IGridCache
{

	/**
	 * @return true if a security provider is in the network ( and only 1 )
	 */
	boolean isAvailable();

	/**
	 * Check if a player has permissions.
	 *
	 * @param player to be checked player
	 * @param perm   checked permissions
	 *
	 * @return true if the player has permissions.
	 */
	boolean hasPermission( EntityPlayer player, SecurityPermissions perm );

	/**
	 * Check if a player has permissions.
	 *
	 * @param playerID id of player
	 * @param perm     checked permissions
	 *
	 * @return true if the player has permissions.
	 */
	boolean hasPermission( int playerID, SecurityPermissions perm );

	/**
	 * @return PlayerID of the admin, or owner, this is the person who placed the security block.
	 */
	int getOwner();
}
