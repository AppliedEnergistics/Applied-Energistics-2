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

package appeng.server;


public enum AccessType {
    /**
     * allows basic access to manipulate the block via gui, or other.
     */
    BLOCK_ACCESS,

    /**
     * Can player deposit items into the network.
     */
    NETWORK_DEPOSIT,

    /**
     * can player withdraw items from the network.
     */
    NETWORK_WITHDRAW,

    /**
     * can player issue crafting requests?
     */
    NETWORK_CRAFT,

    /**
     * can player add new blocks to the network.
     */
    NETWORK_BUILD,

    /**
     * can player manipulate security settings.
     */
    NETWORK_SECURITY
}
