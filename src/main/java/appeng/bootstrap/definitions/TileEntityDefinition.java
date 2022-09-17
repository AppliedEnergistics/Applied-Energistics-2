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

package appeng.bootstrap.definitions;


import appeng.tile.AEBaseTile;


/**
 * @author GuntherDW
 */
public class TileEntityDefinition {

    private final Class<? extends AEBaseTile> tileEntityClass;
    private String name;
    private boolean isRegistered = false;

    // This signals the BlockDefinitionBuilder to set the name of the TE to the blockname.
    public TileEntityDefinition(Class<? extends AEBaseTile> tileEntityClass) {
        this.tileEntityClass = tileEntityClass;
        this.name = null;
    }

    public TileEntityDefinition(Class<? extends AEBaseTile> tileEntityClass, String optionalName) {
        this.tileEntityClass = tileEntityClass;
        this.name = optionalName;
    }

    public Class<? extends AEBaseTile> getTileEntityClass() {
        return this.tileEntityClass;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public boolean isRegistered() {
        return this.isRegistered;
    }

    public void setRegistered(boolean registered) {
        this.isRegistered = registered;
    }
}
