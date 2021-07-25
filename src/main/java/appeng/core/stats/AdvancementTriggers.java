/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2017, AlgorithmX2, All rights reserved.
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

package appeng.core.stats;

public class AdvancementTriggers {
    /**
     * Has a network with 8 channels
     */
    public static final AppEngAdvancementTrigger NETWORK_APPRENTICE = new AppEngAdvancementTrigger("network_apprentice");
    /**
     * Has a network with 128 channels
     */
    public static final AppEngAdvancementTrigger NETWORK_ENGINEER = new AppEngAdvancementTrigger("network_engineer");
    /**
     * Has a network with 2048 channels
     */
    public static final AppEngAdvancementTrigger NETWORK_ADMIN = new AppEngAdvancementTrigger("network_admin");
    /**
     * Entered spatial dimension
     */
    public static final AppEngAdvancementTrigger SPATIAL_EXPLORER = new AppEngAdvancementTrigger("spatial_explorer");
}
