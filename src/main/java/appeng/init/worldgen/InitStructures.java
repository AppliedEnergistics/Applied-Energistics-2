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

import java.util.List;

import net.minecraft.data.worldgen.StructureFeatures;
import net.minecraft.data.worldgen.StructureSets;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;
import net.minecraftforge.registries.IForgeRegistry;

import appeng.worldgen.meteorite.MeteoriteStructure;
import appeng.worldgen.meteorite.MeteoriteStructurePiece;

public final class InitStructures {

    private InitStructures() {
    }

    public static void init(IForgeRegistry<StructureFeature<?>> registry) {
        MeteoriteStructurePiece.register();

        registerStructure(registry, MeteoriteStructure.ID, MeteoriteStructure.INSTANCE,
                Decoration.TOP_LAYER_MODIFICATION);

        MeteoriteStructure.CONFIGURED_INSTANCE = StructureFeatures.register(
                MeteoriteStructure.KEY,
                MeteoriteStructure.INSTANCE.configured(NoneFeatureConfiguration.INSTANCE,
                        MeteoriteStructure.BIOME_TAG_KEY));

        StructureSets.register(
                MeteoriteStructure.STRUCTURE_SET_KEY,
                new StructureSet(
                        List.of(StructureSet.entry(MeteoriteStructure.CONFIGURED_INSTANCE)),
                        new RandomSpreadStructurePlacement(32, 8, RandomSpreadType.LINEAR, 124895654)));
    }

    // This mirrors the Vanilla registration method for structures, but uses the
    // Forge registry instead
    private static <F extends StructureFeature<?>> void registerStructure(
            IForgeRegistry<StructureFeature<?>> registry,
            ResourceLocation id,
            F structure,
            Decoration stage) {
        StructureFeature.STEP.put(structure, stage);
        structure.setRegistryName(id);
        registry.register(structure);
    }

}
