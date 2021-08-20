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

package appeng.init;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;

import appeng.core.AppEng;
import appeng.core.definitions.AEItems;
import appeng.items.parts.FacadeItem;
import appeng.recipes.entropy.EntropyRecipe;
import appeng.recipes.entropy.EntropyRecipeSerializer;
import appeng.recipes.game.DisassembleRecipe;
import appeng.recipes.game.FacadeRecipe;
import appeng.recipes.handlers.InscriberRecipe;
import appeng.recipes.handlers.InscriberRecipeSerializer;
import appeng.recipes.mattercannon.MatterCannonAmmo;
import appeng.recipes.mattercannon.MatterCannonAmmoSerializer;

public final class InitRecipeSerializers {

    private InitRecipeSerializers() {
    }

    public static void init(Registry<RecipeSerializer<?>> registry) {
        FacadeItem facadeItem = AEItems.FACADE.asItem();
        register(registry, AppEng.makeId("disassemble"), DisassembleRecipe.SERIALIZER);
        register(registry, InscriberRecipe.TYPE_ID, InscriberRecipeSerializer.INSTANCE);
        register(registry, AppEng.makeId("facade"), FacadeRecipe.getSerializer(facadeItem));
        register(registry, EntropyRecipe.TYPE_ID, EntropyRecipeSerializer.INSTANCE);
        register(registry, MatterCannonAmmo.TYPE_ID, MatterCannonAmmoSerializer.INSTANCE);
    }

    private static void register(Registry<RecipeSerializer<?>> registry, ResourceLocation id,
            RecipeSerializer<?> serializer) {
        Registry.register(registry, id, serializer);
    }

}
