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

/**
 * the export package is to export all the required information for recipes into a convenient CSV file
 * often names are difficult to acquire without access to the internal names.
 * <p>
 * To save from rescanning every start-up it can save a list of mods and their version
 * and if only something changed, it requires to update the CSV.
 * <p>
 * There is no explicit check if it was manually tempered
 *
 * @author thatsIch
 * @version rv3 - 14.08.2015
 * @since rv3 14.08.2015
 */

package appeng.services.export;