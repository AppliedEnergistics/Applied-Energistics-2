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
import appeng.api.config.TunnelType;
import appeng.api.features.IP2PTunnelRegistry;
import crafttweaker.api.item.IIngredient;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;


@ZenClass("mods.appliedenergistics2.Attunement")
public class AttunementRegistry {
    private AttunementRegistry() {
    }

    @ZenMethod
    public static void attuneME(IIngredient itemStack) {
        attune(itemStack, TunnelType.ME);
    }

    @ZenMethod
    public static void attuneME(String modId) {
        attune(modId, TunnelType.ME);
    }

    @ZenMethod
    public static void attuneItem(IIngredient itemStack) {
        attune(itemStack, TunnelType.ITEM);
    }

    @ZenMethod
    public static void attuneItem(String modId) {
        attune(modId, TunnelType.ITEM);
    }

    @ZenMethod
    public static void attuneFluid(IIngredient itemStack) {
        attune(itemStack, TunnelType.FLUID);
    }

    @ZenMethod
    public static void attuneFluid(String modId) {
        attune(modId, TunnelType.FLUID);
    }

    @ZenMethod
    public static void attuneRedstone(IIngredient itemStack) {
        attune(itemStack, TunnelType.REDSTONE);
    }

    @ZenMethod
    public static void attuneRedstone(String modId) {
        attune(modId, TunnelType.REDSTONE);
    }

    @ZenMethod
    public static void attuneRF(IIngredient itemStack) {
        attune(itemStack, TunnelType.FE_POWER);
    }

    @ZenMethod
    public static void attuneRF(String modId) {
        attune(modId, TunnelType.FE_POWER);
    }

    @ZenMethod
    public static void attuneIC2(IIngredient itemStack) {
        attune(itemStack, TunnelType.IC2_POWER);
    }

    @ZenMethod
    public static void attuneIC2(String modId) {
        attune(modId, TunnelType.IC2_POWER);
    }

    @ZenMethod
    public static void attuneLight(IIngredient itemStack) {
        attune(itemStack, TunnelType.LIGHT);
    }

    @ZenMethod
    public static void attuneLight(String modId) {
        attune(modId, TunnelType.LIGHT);
    }

    private static void attune(IIngredient itemStack, TunnelType type) {
        IP2PTunnelRegistry registry = AEApi.instance().registries().p2pTunnel();
        CTModule.toStacks(itemStack).ifPresent(c -> c.forEach(i -> registry.addNewAttunement(i, type)));
    }

    private static void attune(String modid, TunnelType type) {
        AEApi.instance().registries().p2pTunnel().addNewAttunement(modid, type);
    }
}
