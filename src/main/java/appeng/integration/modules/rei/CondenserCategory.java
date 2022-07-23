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

package appeng.integration.modules.rei;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.base.Splitter;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Slot;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;

import appeng.api.config.CondenserOutput;
import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;

class CondenserCategory implements DisplayCategory<CondenserOutputDisplay> {

    private static final int PADDING = 7;

    public static final CategoryIdentifier<CondenserOutputDisplay> ID = CategoryIdentifier
            .of(AppEng.makeId("condenser"));

    @Override
    public Renderer getIcon() {
        return EntryStacks.of(AEBlocks.CONDENSER.stack());
    }

    @Override
    public Component getTitle() {
        return Component.translatable("gui.ae2.Condenser");
    }

    @Override
    public CategoryIdentifier<? extends CondenserOutputDisplay> getCategoryIdentifier() {
        return ID;
    }

    @Override
    public List<Widget> setupDisplay(CondenserOutputDisplay recipeDisplay, Rectangle bounds) {

        List<Widget> widgets = new ArrayList<>();
        widgets.add(Widgets.createRecipeBase(bounds));

        Point origin = new Point(bounds.x + PADDING, bounds.y + PADDING);

        ResourceLocation location = AppEng.makeId("textures/guis/condenser.png");
        widgets.add(Widgets.createTexturedWidget(location, origin.x, origin.y, 50, 25, 94, 48));

        ResourceLocation statesLocation = AppEng.makeId("textures/guis/states.png");
        widgets.add(Widgets.createTexturedWidget(statesLocation, origin.x + 2, origin.y + 28, 241, 81, 14, 14));
        widgets.add(Widgets.createTexturedWidget(statesLocation, origin.x + 78, origin.y + 28, 240, 240, 16, 16));

        // FIXME IDrawableStatic progressDrawable = guiHelper.drawableBuilder(location,
        // 178, 25, 6, 18).addPadding(0, 0, 70, 0)
        // FIXME .build();
        // FIXME this.progress = guiHelper.createAnimatedDrawable(progressDrawable, 40,
        // IDrawableAnimated.StartDirection.BOTTOM,
        // FIXME false);

        if (recipeDisplay.getType() == CondenserOutput.MATTER_BALLS) {
            widgets.add(Widgets.createTexturedWidget(statesLocation, origin.x + 78, origin.y + 28, 16, 112, 14, 14));
        } else if (recipeDisplay.getType() == CondenserOutput.SINGULARITY) {
            widgets.add(Widgets.createTexturedWidget(statesLocation, origin.x + 78, origin.y + 28, 32, 112, 14, 14));
        }
        widgets.add(Widgets.createDrawableWidget((helper, matrices, mouseX, mouseY, delta) -> {
            Rectangle rect = new Rectangle(origin.x + 78, origin.y + 28, 16, 16);
            if (rect.contains(mouseX, mouseY)) {
                Tooltip.create(
                        getTooltip(recipeDisplay.getType()).stream().map(Component::literal)
                                .collect(Collectors.toList()))
                        .queue();
            }
        }));

        Slot outputSlot = Widgets.createSlot(new Point(origin.x + 55, origin.y + 27)).disableBackground().markOutput()
                .entries(recipeDisplay.getOutputEntries().get(0));
        widgets.add(outputSlot);

        Slot storageCellSlot = Widgets.createSlot(new Point(origin.x + 51, origin.y + 1)).disableBackground()
                .markInput().entries(recipeDisplay.getViableStorageComponents());
        widgets.add(storageCellSlot);

        return widgets;

    }

    @Override
    public int getDisplayWidth(CondenserOutputDisplay display) {
        return 94 + 2 * PADDING;
    }

    @Override
    public int getDisplayHeight() {
        return 48 + 2 * PADDING;
    }

    private List<String> getTooltip(CondenserOutput type) {
        String key;
        switch (type) {
            case MATTER_BALLS:
                key = "gui.tooltips.ae2.MatterBalls";
                break;
            case SINGULARITY:
                key = "gui.tooltips.ae2.Singularity";
                break;
            default:
                return Collections.emptyList();
        }

        return Splitter.on("\n").splitToList(Component.translatable(key, type.requiredPower).getString());
    }

}
