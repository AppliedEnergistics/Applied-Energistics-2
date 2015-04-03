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

package appeng.api.implementations.items;


import java.util.EnumSet;

import net.minecraft.item.ItemStack;

import com.mojang.authlib.GameProfile;

import appeng.api.config.SecurityPermissions;
import appeng.api.features.IPlayerRegistry;
import appeng.api.networking.security.ISecurityRegistry;


public interface IBiometricCard
{

	/**
	 * Set the  {@link GameProfile} to null, to clear it.
	 */
	void setProfile( ItemStack itemStack, GameProfile username );

	/**
	 * @return {@link GameProfile} of the player encoded on this card, or a blank string.
	 */
	GameProfile getProfile( ItemStack is );

	/**
	 * @param itemStack card
	 *
	 * @return the full list of permissions encoded on the card.
	 */
	EnumSet<SecurityPermissions> getPermissions( ItemStack itemStack );

	/**
	 * Check if a permission is encoded on the card.
	 *
	 * @param permission card
	 *
	 * @return true if this permission is set on the card.
	 */
	boolean hasPermission( ItemStack is, SecurityPermissions permission );

	/**
	 * remove a permission from the item stack.
	 *
	 * @param itemStack  card
	 * @param permission to be removed permission
	 */
	void removePermission( ItemStack itemStack, SecurityPermissions permission );

	/**
	 * add a permission to the item stack.
	 *
	 * @param itemStack  card
	 * @param permission to be added permission
	 */
	void addPermission( ItemStack itemStack, SecurityPermissions permission );

	/**
	 * lets you handle submission of security values on the card for custom behavior.
	 *
	 * @param registry security registry
	 * @param pr       player registry
	 * @param is       card
	 */
	void registerPermissions( ISecurityRegistry registry, IPlayerRegistry pr, ItemStack is );
}
