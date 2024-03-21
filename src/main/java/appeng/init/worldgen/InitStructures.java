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
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import appeng.core.AppEng;
import appeng.worldgen.meteorite.MeteoriteStructure;
import appeng.worldgen.meteorite.MeteoriteStructurePiece;

public final class InitStructures {
    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES = DeferredRegister
            .create(Registries.STRUCTURE_TYPE, AppEng.MOD_ID);
    public static final DeferredRegister<StructurePieceType> STRUCTURE_PIECES = DeferredRegister
            .create(Registries.STRUCTURE_PIECE, AppEng.MOD_ID);

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

    public static void register(IEventBus eventBus) {
        STRUCTURE_PIECES.register("ae2mtrt", () -> MeteoriteStructurePiece.TYPE);
        STRUCTURE_TYPES.register("ae2mtrt", () -> MeteoriteStructure.TYPE);

        STRUCTURE_PIECES.register(eventBus);
        STRUCTURE_TYPES.register(eventBus);
    }
}
