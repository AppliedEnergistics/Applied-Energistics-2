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

import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraftforge.registries.IForgeRegistry;

import appeng.core.api.definitions.ApiItems;
import appeng.items.parts.FacadeItem;
import appeng.recipes.entropy.EntropyRecipeSerializer;
import appeng.recipes.game.DisassembleRecipe;
import appeng.recipes.game.FacadeRecipe;
import appeng.recipes.handlers.GrinderRecipeSerializer;
import appeng.recipes.handlers.InscriberRecipeSerializer;

public final class InitRecipeSerializers {

    private InitRecipeSerializers() {
    }

    public static void init(IForgeRegistry<IRecipeSerializer<?>> registry) {
        FacadeItem facadeItem = (FacadeItem) ApiItems.FACADE.item();
        registry.registerAll(
                DisassembleRecipe.SERIALIZER,
                GrinderRecipeSerializer.INSTANCE,
                InscriberRecipeSerializer.INSTANCE,
                FacadeRecipe.getSerializer(facadeItem),
                EntropyRecipeSerializer.INSTANCE);
    }

}
