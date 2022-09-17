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

package appeng.integration.modules.crafttweaker;


import appeng.api.AEApi;
import appeng.core.AELog;
import net.minecraft.tileentity.TileEntity;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;


@ZenClass("mods.appliedenergistics2.Spatial")
public class SpatialRegistry {
    private SpatialRegistry() {
    }

    @ZenMethod
    public static void whitelistEntity(String entityClassName) {
        Class<? extends TileEntity> entityClass = loadClass(entityClassName);
        if (entityClass != null) {
            AEApi.instance().registries().movable().whiteListTileEntity(entityClass);
        }
    }

    @SuppressWarnings("unchecked")
    private static Class<? extends TileEntity> loadClass(String className) {
        try {
            return (Class<? extends TileEntity>) Class.forName(className);
        } catch (Exception e) {
            AELog.warn(e, "Failed to load TileEntity class '" + className + "'");
        }
        return null;
    }
}
