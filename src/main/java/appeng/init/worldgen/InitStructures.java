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

import net.minecraft.data.worldgen.StructureSets;
import net.minecraft.data.worldgen.Structures;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;

import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.worldgen.meteorite.MeteoriteStructure;
import appeng.worldgen.meteorite.MeteoriteStructurePiece;

public final class InitStructures {

    private InitStructures() {
    }

    public static void init() {
        MeteoriteStructurePiece.register();

        MeteoriteStructure.TYPE = StructureType.register("ae2mtrt", MeteoriteStructure.CODEC);
        MeteoriteStructure.INSTANCE = Structures.register(
                MeteoriteStructure.KEY,
                new MeteoriteStructure(
                        Structures.structure(
                                MeteoriteStructure.BIOME_TAG_KEY,
                                Map.of(),
                                Decoration.TOP_LAYER_MODIFICATION,
                                TerrainAdjustment.NONE)));

        if (AEConfig.instance().isGenerateMeteorites()) {
            MeteoriteStructure.STRUCTURE_SET = StructureSets.register(
                    MeteoriteStructure.STRUCTURE_SET_KEY,
                    new StructureSet(
                            List.of(StructureSet.entry(MeteoriteStructure.INSTANCE)),
                            new RandomSpreadStructurePlacement(32, 8, RandomSpreadType.LINEAR, 124895654)));
        } else {
            AELog.info("AE2 meteorites are disabled in the config file.");
        }
    }

}
