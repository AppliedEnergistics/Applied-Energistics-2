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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;

import appeng.client.gui.MockResourceManager;
import appeng.core.AppEng;
import appeng.menu.SlotSemantics;

@MockitoSettings(strictness = Strictness.LENIENT)
class StyleManagerTest {

    @Captor
    ArgumentCaptor<PreparableReloadListener> reloadCaptor;

    @Test
    void testLoadStyleDoc() throws IOException {
        StyleManager.getReloadListener().onResourceManagerReload(MockResourceManager.create());

        ScreenStyle style = StyleManager.loadStyleDoc("/screens/cell_workbench.json");

        assertThat(style.getColor(PaletteColor.DEFAULT_TEXT_COLOR).toARGB()).isEqualTo(0xff413f54);
        assertThat(style.getText()).hasSize(2);
        assertThat(style.getSlots()).containsOnlyKeys(
                SlotSemantics.TOOLBOX.id(),
                SlotSemantics.PLAYER_INVENTORY.id(),
                SlotSemantics.PLAYER_HOTBAR.id(),
                SlotSemantics.CONFIG.id(),
                SlotSemantics.STORAGE_CELL.id());
    }

    @Test
    void testDarkPaletteOverridesLightPalette() {
        StyleManager.getReloadListener().onResourceManagerReload(MockResourceManager.create());

        ScreenStyle style = StyleManager.loadStyleDoc("/screens/cell_workbench.json");

        assertThat(style.getColor(PaletteColor.DEFAULT_TEXT_COLOR, false).toARGB()).isEqualTo(0xff413f54);
        assertThat(style.getColor(PaletteColor.DEFAULT_TEXT_COLOR, true).toARGB()).isEqualTo(0xfff2f2f2);
    }

    /**
     * The colors that were previously hardcoded must keep their previous values in the light theme.
     */
    @Test
    void testLightPaletteMatchesBuiltInDefaults() {
        StyleManager.getReloadListener().onResourceManagerReload(MockResourceManager.create());

        ScreenStyle style = StyleManager.getCommonPalette();

        for (var color : PaletteColor.values()) {
            if (color.getDefaultColor() != null) {
                assertThat(style.getColor(color, false)).as(color.name()).isEqualTo(color.getDefaultColor());
            }
        }
    }

    /**
     * Resource packs that override the palette with an older version, which lacks colors added later, must not break
     * screens.
     */
    @Test
    void testPaletteWithoutNewerColorsIsValid() {
        var style = ScreenStyle.GSON.fromJson("""
                {
                  "palette": {
                    "DEFAULT_TEXT_COLOR": "#413f54",
                    "MUTED_TEXT_COLOR": "#878fa5",
                    "SELECTION_COLOR": "#ace9ff",
                    "TEXTFIELD_PLACEHOLDER": "#dedfe3",
                    "TEXTFIELD_ERROR": "#FF1900",
                    "TEXTFIELD_TEXT": "#f2f2f2",
                    "ERROR": "#CE2401"
                  }
                }
                """, ScreenStyle.class);
        style.validate();

        for (var color : PaletteColor.values()) {
            assertThat(style.getColor(color, false)).as(color.name()).isNotNull();
            assertThat(style.getColor(color, true)).as(color.name()).isNotNull();
        }
        assertThat(style.getColor(PaletteColor.BUTTON_TEXT, true))
                .isEqualTo(PaletteColor.BUTTON_TEXT.getDefaultColor());
    }

    /**
     * Ensure that every UI texture has a dark variant, so new textures are not forgotten.
     */
    @Test
    void testAllGuiTexturesHaveDarkVariant() {
        var resourceManager = MockResourceManager.create();

        var lightTextures = resourceManager.listResources("textures/guis",
                id -> id.getNamespace().equals(AppEng.MOD_ID) && isLightTexture(id)).keySet();
        assertThat(lightTextures).isNotEmpty();
        for (var texture : lightTextures) {
            var darkTexture = Theme.getDarkTexture(texture);
            assertThat(resourceManager.getResource(darkTexture)).as(darkTexture.toString()).isPresent();
        }

        var lightSprites = resourceManager.listResources("textures/gui/sprites",
                id -> id.getNamespace().equals(AppEng.MOD_ID) && isLightTexture(id)
                        && !id.getPath().startsWith("textures/gui/sprites/guide/"))
                .keySet();
        assertThat(lightSprites).isNotEmpty();
        for (var sprite : lightSprites) {
            var darkSprite = Theme.getDarkTexture(sprite);
            assertThat(resourceManager.getResource(darkSprite)).as(darkSprite.toString()).isPresent();
        }
    }

    private static boolean isLightTexture(Identifier id) {
        return id.getPath().endsWith(".png") && !id.getPath().endsWith(Theme.DARK_MODE_SUFFIX + ".png");
    }

    @Test
    void testDarkTextureNames() {
        assertThat(Theme.getDarkTexture(AppEng.makeId("textures/guis/terminal.png")))
                .isEqualTo(AppEng.makeId("textures/guis/terminal_darkmode.png"));
        assertThat(Theme.getDarkTexture(AppEng.makeId("textures/guis/no_extension")))
                .isEqualTo(AppEng.makeId("textures/guis/no_extension_darkmode"));
    }
}
