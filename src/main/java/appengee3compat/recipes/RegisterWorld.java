/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
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

package appengee3compat.recipes;

import appeng.api.AEApi;
import appeng.api.definitions.IDefinitions;
import appeng.api.definitions.IItems;
import appeng.api.definitions.IMaterials;
import appeng.core.AEConfig;
import appeng.core.features.AEFeature;
import appengee3compat.core.AELog;
import com.pahimar.ee3.api.exchange.RecipeRegistryProxy;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import java.util.Arrays;
import java.util.List;

public class RegisterWorld
{
    public static void initRecipes()
    {
        int recipeCount = 0;

        final IDefinitions definitions = AEApi.instance().definitions();
        final IMaterials materials = definitions.materials();
        final IItems items = definitions.items();

        if( AEConfig.instance.isFeatureEnabled( AEFeature.inWorldFluix ) )
        {
            addRecipe( materials.fluixCrystal().maybeStack(1).get(), Arrays.asList(new ItemStack[]{materials.certusQuartzCrystalCharged().maybeStack(1).get(), new ItemStack(Items.redstone, 1), new ItemStack(Items.quartz, 1)}) );
            recipeCount++;
        }

        if( AEConfig.instance.isFeatureEnabled( AEFeature.inWorldPurification ) )
        {
            addRecipe( materials.purifiedCertusQuartzCrystal().maybeStack(1).get(), Arrays.asList(new ItemStack[]{new ItemStack(items.crystalSeed().maybeItem().get(), 1)}) );
            recipeCount++;
            addRecipe( materials.purifiedFluixCrystal().maybeStack(1).get(), Arrays.asList(new ItemStack[]{new ItemStack(items.crystalSeed().maybeItem().get(), 1, 600)}) );
            recipeCount++;
            addRecipe( materials.purifiedNetherQuartzCrystal().maybeStack(1).get(), Arrays.asList(new ItemStack[]{new ItemStack(items.crystalSeed().maybeItem().get(), 1, 1200)}) );
            recipeCount++;
        }

        AELog.info( "Told EE3 about " + recipeCount + " world recipes..." );
    }

    private static void addRecipe( ItemStack output, List<ItemStack> input )
    {
        AELog.debug( ">>> EE3 Recipe Register >>> Output: " + output.toString() + " >>> Input(s): " + input.toString() );
        RecipeRegistryProxy.addRecipe( output, input );
    }
}
