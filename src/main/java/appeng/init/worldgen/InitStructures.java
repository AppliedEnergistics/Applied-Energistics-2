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

package appeng.init.worldgen;

import java.util.Locale;

import net.minecraft.core.Registry;
import net.minecraft.data.worldgen.StructureFeatures;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;
import net.minecraft.world.level.levelgen.feature.StructureFeature;

import appeng.worldgen.meteorite.MeteoriteStructure;
import appeng.worldgen.meteorite.MeteoriteStructurePiece;

public final class InitStructures {

    private InitStructures() {
    }

    public static void init(Registry<StructureFeature<?>> registry) {
        MeteoriteStructurePiece.register();

        // Registering into the registry alone is INSUFFICIENT!
        // There's a bidirectional map in the Structure class itself primarily for the
        // purposes of NBT serialization
        registerStructure(registry, MeteoriteStructure.ID.toString(), MeteoriteStructure.INSTANCE,
                Decoration.TOP_LAYER_MODIFICATION);

        StructureFeatures.register(MeteoriteStructure.ID.toString(),
                MeteoriteStructure.CONFIGURED_INSTANCE);
    }

    // This mirrors the Vanilla registration method for structures, but uses the
    // Forge registry instead
    private static <F extends StructureFeature<?>> void registerStructure(Registry<StructureFeature<?>> registry,
            String name,
            F structure,
            Decoration stage) {
        StructureFeature.STRUCTURES_REGISTRY.put(name.toLowerCase(Locale.ROOT), structure);
        StructureFeature.STEP.put(structure, stage);
        Registry.register(registry, name.toLowerCase(Locale.ROOT), structure);
    }

}
