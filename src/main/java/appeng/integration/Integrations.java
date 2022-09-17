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

package appeng.integration;


import appeng.integration.abstraction.*;


/**
 * Provides convenient access to various integrations with other mods.
 */
public final class Integrations {

    static IIC2 ic2 = new IIC2.Stub();

    static IJEI jei = new IJEI.Stub();

    static IRC rc = new IRC.Stub();

    static IMekanism mekanism = new IMekanism.Stub();

    static IInvTweaks invTweaks = new IInvTweaks.Stub();

    private Integrations() {
    }

    public static IIC2 ic2() {
        return ic2;
    }

    public static IJEI jei() {
        return jei;
    }

    public static IRC rc() {
        return rc;
    }

    public static IMekanism mekanism() {
        return mekanism;
    }

    public static IInvTweaks invTweaks() {
        return invTweaks;
    }

    static IIC2 setIc2(IIC2 ic2) {
        Integrations.ic2 = ic2;
        return ic2;
    }

    static IJEI setJei(IJEI jei) {
        Integrations.jei = jei;
        return jei;
    }

    static IRC setRc(IRC rc) {
        Integrations.rc = rc;
        return rc;
    }

    static IMekanism setMekanism(IMekanism mekanism) {
        Integrations.mekanism = mekanism;
        return mekanism;
    }

    static IInvTweaks setInvTweaks(IInvTweaks invTweaks) {
        Integrations.invTweaks = invTweaks;
        return invTweaks;
    }

}
