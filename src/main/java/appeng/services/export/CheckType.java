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

package appeng.services.export;


/**
 * Defines a concrete result type when using the {@link Checker#isEqual(Object)} from the {@link Checker} class.
 *
 * @author thatsIch
 * @version rv3 - 25.09.2015
 * @see Checker
 * @since rv3 - 25.09.2015
 */
enum CheckType {
    /**
     * If checking resulted in both objects being <b>equal</b>
     */
    EQUAL,

    /**
     * If checking resulted in both objects being <b>unequal</b>
     */
    UNEQUAL
}
