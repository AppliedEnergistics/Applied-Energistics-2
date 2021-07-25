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

package appeng.datagen;

import appeng.datagen.providers.advancements.AdvancementGenerator;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import appeng.core.AppEng;
import appeng.datagen.providers.loot.BlockDropProvider;
import appeng.datagen.providers.models.DecorationModelProvider;
import appeng.datagen.providers.recipes.DecorationRecipes;
import appeng.datagen.providers.tags.BlockTagsProvider;
import appeng.datagen.providers.tags.ItemTagsProvider;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

@Mod.EventBusSubscriber(modid = AppEng.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class AE2DataGenerators {

    @SubscribeEvent
    public static void onGatherData(GatherDataEvent dataEvent) {
        DataGenerator generator = dataEvent.getGenerator();
        if (dataEvent.includeServer()) {
            generator.addProvider(new BlockDropProvider(dataEvent));
            generator.addProvider(new DecorationRecipes(generator));
            BlockTagsProvider blockTagsProvider = new BlockTagsProvider(dataEvent);
            generator.addProvider(blockTagsProvider);
            generator.addProvider(new ItemTagsProvider(dataEvent, blockTagsProvider));
            generator.addProvider(new DecorationModelProvider(generator, dataEvent.getExistingFileHelper()));
            generator.addProvider(new AdvancementGenerator(generator));
        }
    }

}
