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

package appeng.util;


import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.concurrent.Callable;


/**
 * An interface similar to {@link Callable}, but allowing to pass the {@link World} when calling.
 *
 * @author yueh
 * @version rv3
 * @see Callable
 * @since rv3
 */
public interface IWorldCallable<T> {
    /**
     * Similar to {@link Callable#call()}
     *
     * @param world this param is given to not hold a reference to the world but let the caller handle it. Do not expect
     *              a world here thus can be <tt>null</tt>.
     * @return result of call on the world. Can be <tt>null</tt>.
     * @throws Exception if the call fails
     * @see Callable#call()
     */
    @Nullable
    T call(@Nullable World world) throws Exception;
}
