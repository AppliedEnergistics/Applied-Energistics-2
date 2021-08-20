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

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Slot;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryStacks;

import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;

class GrinderRecipeCategory implements DisplayCategory<GrinderRecipeWrapper> {

    public static final CategoryIdentifier<GrinderRecipeWrapper> ID = CategoryIdentifier.of(AppEng.makeId("grinder"));

    @Override
    public Renderer getIcon() {
        return EntryStacks.of(AEBlocks.GRINDSTONE.stack(1));
    }

    @Override
    public Component getTitle() {
        return new TranslatableComponent("block.appliedenergistics2.grindstone");
    }

    @Override
    public CategoryIdentifier<? extends GrinderRecipeWrapper> getCategoryIdentifier() {
        return GrinderRecipeCategory.ID;
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
    public List<Widget> setupDisplay(GrinderRecipeWrapper recipe, Rectangle bounds) {

        ResourceLocation location = AppEng.makeId("textures/guis/grinder.png");
        Widget background = Widgets.createTexturedWidget(location, bounds.x, bounds.y, 11, 16, 154, 70);

        List<Widget> widgets = new ArrayList<>();
        widgets.add(background);

        // Add the input
        EntryIngredient input = recipe.getInputEntries().get(0);
        widgets.add(Widgets.createSlot(new Point(bounds.x + 1, bounds.y + 1)).backgroundEnabled(false).markInput()
                .entries(input));

        // Add the output slots and their chances (if <100%)
        List<EntryIngredient> output = recipe.getOutputEntries();
        List<Double> outputChances = recipe.getOutputChances();
        DecimalFormat df = new DecimalFormat("###.##");
        int offset = bounds.x + 101;
        for (int i = 0; i < output.size(); i++) {
            Slot slot = Widgets.createSlot(new Point(offset, bounds.y + 47))
                    .backgroundEnabled(false)
                    .entries(output.get(i));
            widgets.add(slot);

            double chance = outputChances.get(i);
            if (chance < 100) {
                Point p = new Point(slot.getBounds().getCenterX(), slot.getBounds().getMaxY() + 2);
                widgets.add(Widgets.createLabel(p, new TextComponent(df.format(chance) + "%")).shadow(false)
                        .color(Color.gray.getRGB()));
            }
            offset += 18;
        }

        return widgets;
    }

}
