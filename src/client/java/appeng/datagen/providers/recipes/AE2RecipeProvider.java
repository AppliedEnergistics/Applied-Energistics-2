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

package appeng.datagen.providers.recipes;

import java.util.List;

import net.minecraft.advancements.Advancement;
import net.minecraft.core.registries.MultiRegistryBootstrap;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;

import appeng.core.AppEng;

// Recipes became a reloadable datapack registry. RecipeProvider is no longer a
// DataProvider and its nested Runner is gone; it is constructed from the recipe and advancement
// BootstrapContexts and hooked up through RecipeProvider.asBootstrap(). The inherited
// `items` HolderGetter is now provided by RecipeProvider itself, so AE2's own copy was dropped.
public abstract class AE2RecipeProvider extends RecipeProvider {
    public AE2RecipeProvider(BootstrapContext<Recipe<?>> recipeOutput,
            BootstrapContext<Advancement> advancementOutput) {
        super(recipeOutput, advancementOutput);
    }

    protected final String makeId(String path) {
        return AppEng.makeId(path).toString();
    }

    protected static ResourceKey<Recipe<?>> makeKey(String path) {
        return ResourceKey.create(Registries.RECIPE, AppEng.makeId(path));
    }

    @FunctionalInterface
    public interface RecipeProviderFactory {
        AE2RecipeProvider create(BootstrapContext<Recipe<?>> recipeOutput,
                BootstrapContext<Advancement> advancementOutput);
    }

    private static final List<RecipeProviderFactory> PROVIDERS = List.of(
            DecorationRecipes::new,
            DecorationBlockRecipes::new,
            MatterCannonAmmoProvider::new,
            EntropyRecipes::new,
            InscriberRecipes::new,
            SmeltingRecipes::new,
            CraftingRecipes::new,
            SmithingRecipes::new,
            TransformRecipes::new,
            ChargerRecipes::new,
            QuartzCuttingRecipesProvider::new,
            UpgradeRecipes::new);

    // Replaces the old AE2RecipeProvider.Runner DataProvider.
    public static MultiRegistryBootstrap bootstrap() {
        return RecipeProvider.asBootstrap((recipeOutput, advancementOutput) -> new AE2RecipeProvider(recipeOutput,
                advancementOutput) {
            @Override
            protected void buildRecipes() {
                for (var provider : PROVIDERS) {
                    provider.create(recipeOutput, advancementOutput).buildRecipes();
                }
            }
        });
    }
}
