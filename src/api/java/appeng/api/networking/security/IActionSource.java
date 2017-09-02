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

import javax.annotation.Nonnull;

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
	@Nonnull
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
	@Nonnull
	Optional<IActionHost> machine();

	/**
	 * An {@link IActionSource} can have multiple optional contexts.
	 * 
	 * It is strongly recommended to limit the uses for absolutely necessary cases.
	 * 
	 * Currently there are no public contexts made available by AE.
	 * An example would be the context interfaces use internally to avoid looping items between each other.
	 */
	@Nonnull
	<T> Optional<T> context( @Nonnull Class<T> key );
}