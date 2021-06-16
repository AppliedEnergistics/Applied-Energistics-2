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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoSettings;

import net.minecraft.client.gui.ScreenManager;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.LanguageMap;
import net.minecraft.util.text.TranslationTextComponent;

import appeng.client.gui.MockResourceManager;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.style.StyleManager;
import appeng.client.gui.style.Text;
import appeng.util.LoadTranslations;

@LoadTranslations
@MockitoSettings
class InitScreensTest {

    @BeforeAll
    static void setUp() {
        try (MockedStatic<ScreenManager> registration = Mockito.mockStatic(ScreenManager.class)) {
            InitScreens.init();
        }
    }

    /**
     * Tests that no styles referenced during screen registration are missing.
     */
    @Test
    void testMissingStyles() {
        List<String> missingStyles = InitScreens.CONTAINER_STYLES.values().stream()
                .filter(f -> (getClass().getResourceAsStream("/assets/appliedenergistics2" + f) == null))
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
        for (String path : InitScreens.CONTAINER_STYLES.values()) {
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
        // Load AE2 translation data
        Map<String, String> i18n = new HashMap<>(LanguageMap.getInstance().getLanguageData());
        try (InputStream in = getClass().getResourceAsStream("/assets/appliedenergistics2/lang/en_us.json")) {
            LanguageMap.func_240593_a_(in, i18n::put);
        }

        StyleManager.initialize(MockResourceManager.create());

        Map<String, String> errors = new HashMap<>();
        for (String path : InitScreens.CONTAINER_STYLES.values()) {
            ScreenStyle style = StyleManager.loadStyleDoc(path);

            for (Text text : style.getText().values()) {
                collectMissingTranslations(path, text.getText(), errors, i18n.keySet());
            }
        }

        assertThat(errors).withFailMessage(formatMissingTranslations(errors)).isEmpty();
    }

    private void collectMissingTranslations(String path, ITextComponent text, Map<String, String> errors,
            Set<String> i18nKeys) {
        if (text instanceof TranslationTextComponent) {
            String key = ((TranslationTextComponent) text).getKey();
            if (!i18nKeys.contains(key)) {
                errors.merge(path, key, (a, b) -> a + ", " + b);
            }
        }

        for (ITextComponent sibling : text.getSiblings()) {
            collectMissingTranslations(path, sibling, errors, i18nKeys);
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
