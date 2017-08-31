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


import java.util.Optional;

import net.minecraft.entity.player.EntityPlayer;


/**
 * The source of any action.
 * 
 * This can either be a {@link EntityPlayer} or an {@link IActionHost}.
 * 
 * In most cases this is used for security checks, but can be used to validate the source itself.
 *
 */
public interface IActionSource
{

	/**
	 * If present, AE will consider the player being the source for the action.
	 * 
	 * This will take precedence over {@link IActionSource#machine()} in any case.
	 * 
	 * @return An optional player issuing the action.
	 */
	Optional<EntityPlayer> player();

	/**
	 * If present, it indicates the {@link IActionHost} of the source.
	 * 
	 * Should {@link IActionSource#player()} be absent, it will consider a machine as source.
	 * 
	 * It is recommended to include the machine even when a player is present.
	 * 
	 * @return An optional machine issuing the action or acting as proxy for a player.
	 */
	Optional<IActionHost> machine();

	/**
	 * An optional priority.
	 * 
	 * This is usually only valid in the context of a similar {@link IActionHost}.
	 * 
	 * An applicable use case is to prevent ME interfaces stealing items from other ME interfaces with the same or a
	 * higher priority.
	 */
	Optional<Integer> priority();

}