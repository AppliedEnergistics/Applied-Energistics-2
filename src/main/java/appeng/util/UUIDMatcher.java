/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
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


/**
 * Regex wrapper for UUIDs to not rely on try catch
 */
public final class UUIDMatcher
{
	/**
	 * String which is the regular expression for UUIDs
	 */
	private static final String UUID_REGEX = "[0-9a-fA-F]{8}(?:-[0-9a-fA-F]{4}){3}-[0-9a-fA-F]{12}";

	/**
	 * Checks if a potential UUID is an UUID by applying a regular expression on it.
	 *
	 * @param potential to be checked potential UUID
	 *
	 * @return true, if the potential UUID is indeed an UUID
	 */
	public boolean isUUID( String potential )
	{
		return potential.matches( UUID_REGEX );
	}
}
