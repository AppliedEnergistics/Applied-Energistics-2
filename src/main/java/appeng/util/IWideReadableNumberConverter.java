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


import javax.annotation.Nonnegative;


/**
 * Limits a number converter to a char width of at max 4 characters
 *
 * @author thatsIch
 * @version rv2
 * @since rv2
 */
public interface IWideReadableNumberConverter {
    /**
     * Converts a number into a human readable form. It will not round the number, but down it.
     * Will try to cut the number down 1 decimal later if width can be below 4.
     * Can only handle non negative numbers
     * <p>
     * Example:
     * 10000L -> 10K
     * 9999L -> 9999
     *
     * @param number to be converted number
     * @return String in SI format cut down as far as possible
     */
    String toWideReadableForm(@Nonnegative long number);
}
