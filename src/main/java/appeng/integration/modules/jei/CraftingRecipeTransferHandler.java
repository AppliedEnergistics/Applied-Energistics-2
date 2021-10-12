/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2020 Team Appliedenergistics, All rights reserved.
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

package appeng.integration.modules.jei;

import me.shedaniel.rei.api.common.display.Display;

import appeng.menu.me.items.CraftingTermMenu;

public class CraftingRecipeTransferHandler extends RecipeTransferHandler<CraftingTermMenu> {

    public CraftingRecipeTransferHandler() {
        super(CraftingTermMenu.class);
    }

    @Override
    protected Result doTransferRecipe(CraftingTermMenu container, Display recipe, Context context) {
        return null;
    }

}
