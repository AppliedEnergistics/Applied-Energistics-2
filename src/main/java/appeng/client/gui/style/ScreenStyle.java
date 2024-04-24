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

import java.lang.reflect.Type;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mojang.serialization.JsonOps;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

/**
 * A screen style document defines various visual aspects of AE2 screens.
 */
public class ScreenStyle {

    public static final Gson GSON = new GsonBuilder()
            .disableHtmlEscaping()
            .registerTypeHierarchyAdapter(Component.class,
                    new Component.SerializerAdapter(HolderLookup.Provider.create(Stream.of())))
            .registerTypeAdapter(Style.class, new StyleSerializer())
            .registerTypeAdapter(Blitter.class, BlitterDeserializer.INSTANCE)
            .registerTypeAdapter(Rect2i.class, Rectangle2dDeserializer.INSTANCE)
            .registerTypeAdapter(Color.class, ColorDeserializer.INSTANCE)
            .create();

    /**
     * Overrides the default help topic for this screen. This will be resolved as a link to a page in the guidebook and
     * may contain an optional fragment (#some-heading) to directly link to a heading or anchor in the page.
     */
    @Nullable
    private String helpTopic;

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

    /**
     * If not-null, sets the dialog size. If background is also null, a default background is generated.
     */
    @Nullable
    private GeneratedBackground generatedBackground;

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

    @Nullable
    public Blitter getBackground() {
        return background != null ? background.copy() : null;
    }

    @Nullable
    public GeneratedBackground getGeneratedBackground() {
        return generatedBackground;
    }

    public String getHelpTopic() {
        return helpTopic;
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

    private static class StyleSerializer implements JsonSerializer<Style>, JsonDeserializer<Style> {
        @Override
        public Style deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            return Style.Serializer.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow(JsonParseException::new);
        }

        @Override
        public JsonElement serialize(Style src, Type typeOfSrc, JsonSerializationContext context) {
            return Style.Serializer.CODEC.encodeStart(JsonOps.INSTANCE, src).getOrThrow(JsonParseException::new);
        }
    }
}
