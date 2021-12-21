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

package appeng.client.gui.style;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Component.Serializer;
import net.minecraft.network.chat.Style;

/**
 * A screen style document defines various visual aspects of AE2 screens.
 */
public class ScreenStyle {

    public static final Gson GSON = new GsonBuilder()
            .disableHtmlEscaping()
            .registerTypeHierarchyAdapter(Component.class, new Serializer())
            .registerTypeHierarchyAdapter(Style.class, new Style.Serializer())
            .registerTypeAdapter(Blitter.class, BlitterDeserializer.INSTANCE)
            .registerTypeAdapter(Rect2i.class, Rectangle2dDeserializer.INSTANCE)
            .registerTypeAdapter(Color.class, ColorDeserializer.INSTANCE)
            .create();

    /**
     * Positioning information for groups of slots.
     */
    private final Map<String, SlotPosition> slots = new HashMap<>();

    /**
     * Various text-labels positioned on the screen.
     */
    private final Map<String, Text> text = new HashMap<>();

    /**
     * Color-Palette for the screen.
     */
    private final Map<PaletteColor, Color> palette = new EnumMap<>(PaletteColor.class);

    /**
     * Additional images that are screen-specific.
     */
    private final Map<String, Blitter> images = new HashMap<>();

    /**
     * The screen background, which is optional. If defined, it is also used to size the dialog.
     */
    @Nullable
    private Blitter background;

    @Nullable
    private TerminalStyle terminalStyle;

    private final Map<String, WidgetStyle> widgets = new HashMap<>();

    private final Map<String, TooltipArea> tooltips = new HashMap<>();

    public Color getColor(PaletteColor color) {
        return palette.get(color);
    }

    public Map<String, SlotPosition> getSlots() {
        return slots;
    }

    public Map<String, Text> getText() {
        return text;
    }

    public Map<String, TooltipArea> getTooltips() {
        return tooltips;
    }

    public Blitter getBackground() {
        return background != null ? background.copy() : null;
    }

    public WidgetStyle getWidget(String id) {
        WidgetStyle widget = widgets.get(id);
        if (widget == null) {
            throw new IllegalStateException("Screen is missing required widget: " + id);
        }
        return widget;
    }

    public Blitter getImage(String id) {
        Blitter blitter = images.get(id);
        if (blitter == null) {
            throw new IllegalStateException("Screen is missing required image: " + id);
        }
        return blitter;
    }

    @Nullable
    public TerminalStyle getTerminalStyle() {
        return terminalStyle;
    }

    public void validate() {
        for (PaletteColor value : PaletteColor.values()) {
            if (!palette.containsKey(value)) {
                throw new RuntimeException("Palette is missing color " + value);
            }
        }

        if (terminalStyle != null) {
            terminalStyle.validate();
        }
    }

}
