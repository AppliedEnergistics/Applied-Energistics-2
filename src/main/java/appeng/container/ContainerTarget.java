/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
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

package appeng.container;

/**
 * The game object that a container has been opened for.
 * <p/>
 * Containers can be opened for various in-game objects which do not derive from a common base-class:
 * <ul>
 * <li>Items in the player inventory (i.e. wireless terminals)</li>
 * <li>Block entities in the world (i.e. vibration chamber)</li>
 * <li>Parts that are attached to a multi-block block entity</li>
 * </ul>
 * <p/>
 * This class tries to capture all of these cases for container base classes which do not enforce a specific interface
 * requirement.
 */
public final class ContainerTarget {

}
