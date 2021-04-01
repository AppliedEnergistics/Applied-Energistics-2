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

package appeng.integration.modules.jei;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.RecipeCategory;
import me.shedaniel.rei.api.widgets.Slot;
import me.shedaniel.rei.api.widgets.Widgets;
import me.shedaniel.rei.gui.widget.Widget;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.LanguageMap;
import net.minecraft.util.text.StringTextComponent;
import appeng.core.Api;
import appeng.core.AppEng;

class GrinderRecipeCategory implements RecipeCategory<GrinderRecipeWrapper> {

    public static final ResourceLocation UID = new ResourceLocation(AppEng.MOD_ID, "grinder");

    private final String localizedName;

    private final EntryStack icon;

    public GrinderRecipeCategory() {
        this.localizedName = LanguageMap.getInstance().func_230503_a_("block.appliedenergistics2.grindstone");
        this.icon = EntryStack.create(Api.INSTANCE.definitions().blocks().grindstone().stack(1));
    }

    @Override
    public ResourceLocation getIdentifier() {
        return GrinderRecipeCategory.UID;
    }

    @Override
    public String getCategoryName() {
        return this.localizedName;
    }

    @Override
    public int getDisplayHeight() {
        return 70; // Padded to avoid the "+" button overlapping the UI
    }

    @Override
    public int getDisplayWidth(GrinderRecipeWrapper display) {
        return 154;
    }

    @Override
    public EntryStack getLogo() {
        return icon;
    }

    @Override
    public List<Widget> setupDisplay(GrinderRecipeWrapper recipe, Rectangle bounds) {

        ResourceLocation location = new ResourceLocation(AppEng.MOD_ID, "textures/guis/grinder.png");
        Widget background = Widgets.createTexturedWidget(location, bounds.x, bounds.y, 11, 16, 154, 70);

        List<Widget> widgets = new ArrayList<>();
        widgets.add(background);

        // Add the input
        List<EntryStack> input = recipe.getInputEntries().get(0);
        widgets.add(Widgets.createSlot(new Point(bounds.x + 1, bounds.y + 1)).backgroundEnabled(false).markInput()
                .entries(input));

        // Add the output slots and their chances (if <100%)
        List<EntryStack> output = recipe.getOutputEntries();
        List<Double> outputChances = recipe.getOutputChances();
        DecimalFormat df = new DecimalFormat("###.##");
        int offset = bounds.x + 101;
        for (int i = 0; i < output.size(); i++) {
            Slot slot = Widgets.createSlot(new Point(offset, bounds.y + 47)).backgroundEnabled(false)
                    .entry(output.get(i));
            widgets.add(slot);

            double chance = outputChances.get(i);
            if (chance < 100) {
                Point p = new Point(slot.getBounds().getCenterX(), slot.getBounds().getMaxY() + 2);
                widgets.add(Widgets.createLabel(p, new StringTextComponent(df.format(chance) + "%")).shadow(false)
                        .color(Color.gray.getRGB()));
            }
            offset += 18;
        }

        return widgets;
    }

}
