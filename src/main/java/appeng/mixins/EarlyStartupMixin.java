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

package appeng.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.Bootstrap;

import appeng.core.AppEngBootstrap;

/**
 * Very early, but controlled initialization of AE2's internal registries. This allows other mods to freely use them
 * within their mod constructors.
 */
@Mixin(Bootstrap.class)
public abstract class EarlyStartupMixin {

    // Don't inject at TAIL because Citadel (possibly other mods too) cause bootStrap() to invoke itself,
    // and we don't want this to be invoked from the nested call since MC isn't fully initialized by then.
    @Inject(at = @At(value = "INVOKE", target = "net/minecraftforge/registries/GameData. vanillaSnapshot()V", shift = At.Shift.AFTER, by = 1), method = "bootStrap")
    private static void initRegistries(CallbackInfo ci) {
        AppEngBootstrap.runEarlyStartup();
    }

}
