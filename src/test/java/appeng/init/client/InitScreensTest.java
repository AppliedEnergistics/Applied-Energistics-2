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

package appeng.init.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoSettings;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.TranslatableComponent;

import appeng.client.gui.MockResourceManager;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.style.StyleManager;
import appeng.client.gui.style.Text;
import appeng.util.BootstrapMinecraft;
import appeng.util.LoadTranslations;

@BootstrapMinecraft
@LoadTranslations
@MockitoSettings
class InitScreensTest {

    @BeforeAll
    static void setUp() {
        try (MockedStatic<MenuScreens> registration = Mockito.mockStatic(MenuScreens.class)) {
            InitScreens.init();
        }
    }

    /**
     * Tests that no styles referenced during screen registration are missing.
     */
    @Test
    void testMissingStyles() {
        List<String> missingStyles = InitScreens.MENU_STYLES.values().stream()
                .filter(f -> (getClass().getResourceAsStream("/assets/ae2" + f) == null))
                .collect(Collectors.toList());
        assertThat(missingStyles).isEmpty();
    }

    /**
     * Tests that all of the styles referenced can be deserialized.
     */
    @Test
    void testBrokenStyles() throws IOException {
        StyleManager.initialize(MockResourceManager.create());

        List<String> errors = new ArrayList<>();
        for (String path : InitScreens.MENU_STYLES.values()) {
            try {
                StyleManager.loadStyleDoc(path);
            } catch (Exception e) {
                errors.add(path + ": " + getExceptionChain(e));
            }
        }

        assertThat(errors).isEmpty();
    }

    private static String getExceptionChain(Throwable e) {
        if (e.getCause() != e && e.getCause() != null) {
            return e + " <- " + getExceptionChain(e.getCause());
        } else {
            return e.toString();
        }
    }

    /**
     * Check that all the text in styles references existing translation keys.
     */
    @Test
    void testMissingTranslationKeys() throws IOException {
        StyleManager.initialize(MockResourceManager.create());

        Map<String, String> errors = new HashMap<>();
        for (String path : InitScreens.MENU_STYLES.values()) {
            ScreenStyle style = StyleManager.loadStyleDoc(path);

            for (Text text : style.getText().values()) {
                collectMissingTranslations(path, text.getText(), errors);
            }
        }

        assertThat(errors).withFailMessage(formatMissingTranslations(errors)).isEmpty();
    }

    private void collectMissingTranslations(String path, net.minecraft.network.chat.Component text,
            Map<String, String> errors) {
        if (text instanceof TranslatableComponent) {
            String key = ((TranslatableComponent) text).getKey();
            if (!Language.getInstance().has(key)) {
                errors.merge(path, key, (a, b) -> a + ", " + b);
            }
        }

        for (net.minecraft.network.chat.Component sibling : text.getSiblings()) {
            collectMissingTranslations(path, sibling, errors);
        }
    }

    private String formatMissingTranslations(Map<String, String> errors) {
        StringBuilder builder = new StringBuilder("Missing Translations: " + '\n');
        for (Entry<String, String> entry : errors.entrySet()) {
            builder.append(entry.getKey()).append(": ").append(entry.getValue()).append('\n');
        }

        return builder.toString();
    }

}
