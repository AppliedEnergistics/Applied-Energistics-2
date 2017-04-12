/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.integration.abstraction;


import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;


/**
 * Contains wrench behaviour
 * <p>
 * and registers the engines as P2P attunements for RF tunnels
 * (since BC 7, they are part of BC Core)
 * The attunement is currently not public anymore,
 * because it was only used internally
 *
 * @author AlgorithmX2
 * @version rv3
 * @since rv0
 */
public interface IBuildCraftCore
{
	/**
	 * @param eq to be checked item, can be {@code null}
	 * @return {@code true} if it is an {@link buildcraft.api.tools.IToolWrench}
	 */
	boolean isWrench( @Nullable Item eq );

	/**
	 * @param wrench   to be checked item, must be an {@link buildcraft.api.tools.IToolWrench}
	 * @param wrencher wrenching player, can be probably {@code null}, but not sure
	 * @param x        x pos
	 * @param y        y pos
	 * @param z        z pos
	 * @return {@code true} if player can wrench with that {@code wrench}
	 * @throws NullPointerException if {@code wrench} is {@code null}
	 * @throws ClassCastException   if {@code wrench} is not an {@link buildcraft.api.tools.IToolWrench}
	 */
	boolean canWrench( @Nonnull Item wrench, EntityPlayer wrencher, int x, int y, int z );

	/**
	 * @param wrench   to be checked item, must be an {@link buildcraft.api.tools.IToolWrench}
	 * @param wrencher wrenching player, can be probably {@code null}, but not sure
	 * @param x        x pos
	 * @param y        y pos
	 * @param z        z pos
	 * @throws NullPointerException if {@code wrench} is {@code null}
	 * @throws ClassCastException   if {@code wrench} is not an {@link buildcraft.api.tools.IToolWrench}
	 */
	void wrenchUsed( @Nonnull Item wrench, EntityPlayer wrencher, int x, int y, int z );
}
