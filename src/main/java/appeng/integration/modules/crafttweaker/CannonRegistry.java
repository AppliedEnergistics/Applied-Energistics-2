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
import appeng.api.features.IMatterCannonAmmoRegistry;
import crafttweaker.api.item.IIngredient;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;


@ZenClass("mods.appliedenergistics2.Cannon")
public class CannonRegistry {
    private CannonRegistry() {
    }

    @ZenMethod
    public static void registerAmmo(IIngredient itemStack, double weight) {
        IMatterCannonAmmoRegistry registry = AEApi.instance().registries().matterCannon();
        CTModule.toStacks(itemStack).ifPresent(c -> c.forEach(i -> registry.registerAmmo(i, weight)));
    }
}
