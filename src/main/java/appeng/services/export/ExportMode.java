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
 * Defines the different modes which need to be distinguished upon exporting.
 *
 * using a different mode will result in a different export outcome.
 *
 * @author thatsIch
 * @version rv3 - 23.09.2015
 * @since rv3 - 23.09.2015
 */
enum ExportMode
{
	/**
	 * Will provide general users with information required for recipe making
	 */
	MINIMAL,

	/**
	 * Will provide advanced users with information with debugging functionality
	 */
	VERBOSE
}
