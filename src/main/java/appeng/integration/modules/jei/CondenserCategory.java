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

import appeng.api.config.CondenserOutput;
import appeng.core.Api;
import appeng.core.AppEng;
import com.google.common.base.Splitter;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.RecipeCategory;
import me.shedaniel.rei.api.widgets.Slot;
import me.shedaniel.rei.api.widgets.Tooltip;
import me.shedaniel.rei.api.widgets.Widgets;
import me.shedaniel.rei.gui.widget.Widget;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.Language;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

class CondenserCategory implements RecipeCategory<CondenserOutputDisplay> {

    private static final int PADDING = 7;

    public static final Identifier UID = new Identifier(AppEng.MOD_ID, "condenser");

    private final String localizedName;

    private final EntryStack icon;

    public CondenserCategory() {
        this.localizedName = Language.getInstance().get("gui.appliedenergistics2.Condenser");
        this.icon = EntryStack.create(Api.INSTANCE.definitions().blocks().condenser().stack(1));
    }

    @Override
    public Identifier getIdentifier() {
        return UID;
    }

    @Override
    public String getCategoryName() {
        return localizedName;
    }

    @Override
    public EntryStack getLogo() {
        return icon;
    }

    @Override
    public List<Widget> setupDisplay(CondenserOutputDisplay recipeDisplay, Rectangle bounds) {

        List<Widget> widgets = new ArrayList<>();
        widgets.add(Widgets.createRecipeBase(bounds));

        Point origin = new Point(bounds.x + PADDING, bounds.y + PADDING);

        Identifier location = new Identifier(AppEng.MOD_ID, "textures/guis/condenser.png");
        widgets.add(Widgets.createTexturedWidget(location, origin.x, origin.y, 50, 25, 94, 48));

        Identifier statesLocation = new Identifier(AppEng.MOD_ID, "textures/guis/states.png");
        widgets.add(Widgets.createTexturedWidget(statesLocation, origin.x + 2, origin.y + 28, 241, 81, 14, 14));
        widgets.add(Widgets.createTexturedWidget(statesLocation, origin.x + 78, origin.y + 28, 240, 240, 16, 16));

        // FIXME IDrawableStatic progressDrawable = guiHelper.drawableBuilder(location, 178, 25, 6, 18).addPadding(0, 0, 70, 0)
        // FIXME         .build();
        // FIXME this.progress = guiHelper.createAnimatedDrawable(progressDrawable, 40, IDrawableAnimated.StartDirection.BOTTOM,
        // FIXME         false);

        if (recipeDisplay.getType() == CondenserOutput.MATTER_BALLS) {
            widgets.add(Widgets.createTexturedWidget(statesLocation, origin.x + 78, origin.y + 28, 16, 112, 14, 14));
        } else if (recipeDisplay.getType() == CondenserOutput.SINGULARITY) {
            widgets.add(Widgets.createTexturedWidget(statesLocation, origin.x + 78, origin.y + 28, 32, 112, 14, 14));
        }
        widgets.add(Widgets.createDrawableWidget((helper, matrices, mouseX, mouseY, delta) -> {
            Rectangle rect = new Rectangle(origin.x + 78, origin.y + 28, 16, 16);
            if (rect.contains(mouseX, mouseY)) {
                Tooltip.create(getTooltip(recipeDisplay.getType()).stream().map(LiteralText::new).collect(Collectors.toList()))
                        .queue();
            }
        }));

        Slot outputSlot = Widgets.createSlot(new Point(origin.x + 55, origin.y + 27))
                .disableBackground()
                .markOutput()
                .entries(recipeDisplay.getOutputEntries());
        widgets.add(outputSlot);

        Slot storageCellSlot = Widgets.createSlot(new Point(origin.x + 51, origin.y + 1))
                .disableBackground()
                .markInput()
                .entries(recipeDisplay.getViableStorageComponents());
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
                key = "gui.tooltips.appliedenergistics2.MatterBalls";
                break;
            case SINGULARITY:
                key = "gui.tooltips.appliedenergistics2.Singularity";
                break;
            default:
                return Collections.emptyList();
        }

        return Splitter.on("\n").splitToList(new TranslatableText(key, type.requiredPower).getString());
    }

}
