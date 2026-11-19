/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2026, TeamAppliedEnergistics, All rights reserved.
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.ResourceManager;

import appeng.core.AEConfig;

/**
 * Switches the AE2 UI between its light and dark theme at runtime, without reloading resources.
 * <p>
 * In dark mode, textures and GUI sprites are redirected to their dark variant if one exists:
 * <ul>
 * <li>Texture <code>ns:textures/path.png</code> is replaced by <code>ns:textures/path_darkmode.png</code>.</li>
 * <li>GUI sprite <code>ns:path</code> is replaced by <code>ns:path_darkmode</code> (file
 * <code>ns:textures/gui/sprites/path_darkmode.png</code>).</li>
 * </ul>
 * If no dark variant exists, the light variant is used. If a resource pack overrides the light variant of a texture,
 * but not its dark variant, the light variant from the resource pack wins.
 * <p>
 * Colors are switched by {@link ScreenStyle#getColor(PaletteColor)}.
 */
public final class Theme {
    /**
     * Suffix appended to the basename of textures and sprites to get their dark mode variant.
     */
    public static final String DARK_MODE_SUFFIX = "_darkmode";

    /**
     * Cache of texture id to the texture id that should be used in dark mode.
     */
    private static final Map<Identifier, Identifier> darkTextures = new HashMap<>();

    private Theme() {
    }

    public static boolean isDark() {
        var config = AEConfig.instance();
        return config != null && config.isDarkMode();
    }

    /**
     * Called when resources are reloaded, since this changes which dark variants are available.
     */
    static void clearCaches() {
        darkTextures.clear();
    }

    /**
     * Resolves the texture that should actually be used for drawing the given texture with the current theme.
     */
    public static Identifier resolveTexture(Identifier texture) {
        if (!isDark()) {
            return texture;
        }
        return darkTextures.computeIfAbsent(texture, Theme::findDarkTexture);
    }

    /**
     * Resolves the GUI sprite that should actually be used for drawing the given GUI sprite with the current theme.
     */
    public static Identifier resolveGuiSprite(Identifier sprite) {
        if (!isDark()) {
            return sprite;
        }

        // Sprite lookups are cheap hash map lookups, so we do not cache them. This also avoids having to
        // synchronize the cache with the reload of the GUI atlas.
        var darkSprite = sprite.withSuffix(DARK_MODE_SUFFIX);
        var atlas = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.GUI);
        if (atlas.getSprite(darkSprite).contents().name().equals(darkSprite)) {
            return darkSprite;
        }
        return sprite;
    }

    private static Identifier findDarkTexture(Identifier texture) {
        var darkTexture = getDarkTexture(texture);

        var resourceManager = Minecraft.getInstance().getResourceManager();
        var darkResource = resourceManager.getResource(darkTexture).orElse(null);
        if (darkResource == null) {
            return texture;
        }

        // If a resource pack replaced the light texture, but did not supply a dark version of it, we prefer
        // the resource pack's texture over our own dark texture.
        var lightResource = resourceManager.getResource(texture).orElse(null);
        if (lightResource != null && isFromHigherPack(resourceManager, lightResource.sourcePackId(),
                darkResource.sourcePackId())) {
            return texture;
        }

        return darkTexture;
    }

    /**
     * Checks if the pack with the given id has a higher priority than the other pack.
     */
    private static boolean isFromHigherPack(ResourceManager resourceManager, String packId,
            @Nullable String otherPackId) {
        if (packId.equals(otherPackId)) {
            return false;
        }
        // Packs are listed from lowest to highest priority
        List<String> packIds = resourceManager.listPacks().map(PackResources::packId).toList();
        return packIds.indexOf(packId) > packIds.indexOf(otherPackId);
    }

    /**
     * Turns <code>ns:path/name.png</code> into <code>ns:path/name_darkmode.png</code>.
     */
    public static Identifier getDarkTexture(Identifier texture) {
        var path = texture.getPath();
        var extensionStart = path.lastIndexOf('.');
        if (extensionStart <= path.lastIndexOf('/')) {
            return texture.withSuffix(DARK_MODE_SUFFIX);
        }
        return texture.withPath(path.substring(0, extensionStart) + DARK_MODE_SUFFIX + path.substring(extensionStart));
    }
}
