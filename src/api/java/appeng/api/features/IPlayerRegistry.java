/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013 - 2015 AlgorithmX2
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


import javax.annotation.Nullable;

import com.mojang.authlib.GameProfile;

import net.minecraft.entity.player.EntityPlayer;


/**
 * Maintains a save specific list of userids and username combinations this greatly simplifies storage internally and
 * gives a common place to look up and get IDs for the security framework.
 */
public interface IPlayerRegistry
{

	/**
	 * @param gameProfile user game profile
	 *
	 * @return user id of a username.
	 */
	int getID( GameProfile gameProfile );

	/**
	 * @param player player
	 *
	 * @return user id of a player entity.
	 */
	int getID( EntityPlayer player );

	/**
	 * @param playerID to be found player id
	 *
	 * @return PlayerEntity, or null if the player could not be found.
	 */
	@Nullable
	EntityPlayer findPlayer( int playerID );
}
