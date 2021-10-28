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

package appeng.mixins.structure;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.ImmutableMap;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.level.levelgen.StructureSettings;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.StrongholdConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.StructureFeatureConfiguration;

import appeng.worldgen.meteorite.MeteoriteStructure;

/**
 * This Mixin will add the structure placement configuration for the meteorite structure to the static final immutable
 * map that contains them. There is currently no Forge hook for this, and registering them during the registry event is
 * already too late.
 * <p>
 * If this is not done, Meteorites spawn every chunk, since that is the default for missing entries.
 */
@Mixin(StructureSettings.class)
public class StructureSettingsMixin {

    @Shadow
    @Mutable
    private static ImmutableMap<StructureFeature<?>, StructureFeatureConfiguration> DEFAULTS;

    @Shadow
    @Mutable
    private Map<StructureFeature<?>, StructureFeatureConfiguration> structureConfig;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void addDefaultMeteoriteSpreadConfig(CallbackInfo ci) {
        DEFAULTS = ImmutableMap.<StructureFeature<?>, StructureFeatureConfiguration>builder().putAll(DEFAULTS)
                .put(MeteoriteStructure.INSTANCE, MeteoriteStructure.PLACEMENT_CONFIG)
                .build();
    }

    /**
     * This constructor is used if the structure config is deserialized from a datapack.
     */
    @Inject(method = "<init>(Ljava/util/Optional;Ljava/util/Map;)V", at = @At("TAIL"))
    private void addMeteoriteSpreadConfig(Optional<StrongholdConfiguration> optional,
            Map<StructureFeature<?>, StructureFeatureConfiguration> map,
            CallbackInfo ci) {
        // There's no guarantee the passed map is mutable
        structureConfig = new HashMap<>(structureConfig);
        if (!structureConfig.containsKey(MeteoriteStructure.INSTANCE)) {
            structureConfig.put(MeteoriteStructure.INSTANCE, MeteoriteStructure.PLACEMENT_CONFIG);
        }
    }

}
