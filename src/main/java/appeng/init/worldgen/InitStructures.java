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
import java.util.Map;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;

import appeng.worldgen.meteorite.MeteoriteStructure;
import appeng.worldgen.meteorite.MeteoriteStructurePiece;

public final class InitStructures {

    private InitStructures() {
    }

    public static void initDatagenStructures(BootstrapContext<Structure> context) {
        var biomes = context.lookup(Registries.BIOME);

        context.register(
                MeteoriteStructure.KEY,
                new MeteoriteStructure(
                        new Structure.StructureSettings(
                                biomes.getOrThrow(MeteoriteStructure.BIOME_TAG_KEY),
                                Map.of(),
                                Decoration.TOP_LAYER_MODIFICATION,
                                TerrainAdjustment.NONE)));

    }

    public static void initDatagenStructureSets(BootstrapContext<StructureSet> context) {
        var structures = context.lookup(Registries.STRUCTURE);
        var meteorite = structures.getOrThrow(MeteoriteStructure.KEY);

        var structureSet = new StructureSet(
                List.of(StructureSet.entry(meteorite)),
                new RandomSpreadStructurePlacement(32, 8, RandomSpreadType.LINEAR, 124895654));

        context.register(MeteoriteStructure.STRUCTURE_SET_KEY, structureSet);
    }

    public static void init() {
        MeteoriteStructurePiece.register();
        MeteoriteStructure.TYPE = StructureType.register("ae2mtrt", MeteoriteStructure.CODEC);
    }
}
