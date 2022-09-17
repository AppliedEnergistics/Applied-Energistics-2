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

package appeng.services.version.exceptions;


/**
 * Indicates an invalid version, which does not consists of 3 parts matching /(rv\d+)-(alpha|beta|stable)-(b\d+)/.
 */
public class InvalidVersionException extends VersionCheckerException {

    private static final long serialVersionUID = 4828906902143875942L;

    public InvalidVersionException() {
        super("Invalid Version Format: Need to consist of exactly 3 parts separated by a dash.");
    }
}
