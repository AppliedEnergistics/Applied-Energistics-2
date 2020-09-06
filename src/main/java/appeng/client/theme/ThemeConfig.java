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

package appeng.client.theme;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Atlases;
import net.minecraft.resources.IResource;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;

import appeng.core.AppEng;

public class ThemeConfig {

    private Map<String, Item> items = new HashMap<>();

    public static final ThemeConfig INSTANCE = new ThemeConfig();

    /**
     * Get the configurable item it will change dynamically when the configuration
     * is reload.
     */
    public Item getItem(String name) {
        Item item = this.items.get(name);
        if (item == null) {
            item = new Item();
            this.items.put(name, item);
        }
        return item;
    }

    /**
     * Get the configurable color item it will change dynamically when the
     * configuration is reload.
     */
    public ColorItem getColorItem(String name, int defaultColor) {
        return new ColorItem(this.getItem(name), defaultColor);
    }

    /**
     * Reload all theme config when the any texture change.
     */
    public void reloadConfig(TextureStitchEvent.Pre evt) {
        if (evt.getMap().getTextureLocation().equals(Atlases.CHEST_ATLAS)) {
            // clean item of each reload.
            this.resetAllItems();
            try {
                // load theme config.
                ResourceLocation location = new ResourceLocation(AppEng.MOD_ID, "theme/config.json");
                IResource resource = Minecraft.getInstance().getResourceManager().getResource(location);
                JsonParser parser = new JsonParser();
                JsonElement element = parser.parse(new InputStreamReader(resource.getInputStream()));
                if (element != null && element.isJsonObject()) {
                    this.changeAllItems(((JsonObject) element).entrySet());
                }
            } catch (IOException exception) {
                // ops
            }
        }
    }

    private void resetAllItems() {
        for (Item value : this.items.values()) {
            value.setElement(null);
        }
    }

    private void changeAllItems(Set<Map.Entry<String, JsonElement>> entrySet) {
        for (Map.Entry<String, JsonElement> entry : entrySet) {
            Item item = this.getItem(entry.getKey());
            item.setElement(entry.getValue());
        }
    }

    public class Item {

        protected int version = 0;
        protected JsonPrimitive primitive;

        protected void setElement(JsonElement element) {
            this.version += 1;
            if (element != null && element.isJsonPrimitive()) {
                this.primitive = element.getAsJsonPrimitive();
            } else {
                this.primitive = null;
            }
        }

        public boolean isString() {
            return this.primitive != null && this.primitive.isString();
        }

        public String getAsString() {
            return this.primitive.getAsString();
        }

        /**
         * each change updates the version
         */
        public int getVersion() {
            return this.version;
        }
    }

    public class ColorItem {

        private final Item item;
        private final int defaultColor;

        private int cachedColor;
        private int cachedVersion;

        public ColorItem(Item colorItem, int defaultColor) {
            this.item = colorItem;
            this.defaultColor = defaultColor;
            this.cachedVersion = Integer.MIN_VALUE;
            this.cachedColor = defaultColor;
        }

        public int getColor() {
            if (this.item == null) {
                return this.defaultColor;
            }
            if (this.cachedVersion != this.item.getVersion()) {
                this.cachedVersion = this.item.getVersion();
                // reset cache to default color before parse.
                // when parse fails, will not try to parse again.
                this.cachedColor = this.defaultColor;
                try {
                    if (this.item.isString()) {
                        String color = this.item.getAsString().replace("#", "");
                        this.cachedColor = Integer.parseUnsignedInt(color, 16);
                        // when specified the value of #aaaaaa, this color alpha channel is zero.
                        if (color.length() <= 4) {
                            this.cachedColor |= 0xFF000000;
                        }
                    }
                } catch (NumberFormatException e) {
                    // ignore
                }
            }
            return this.cachedColor;
        }
    }
}
