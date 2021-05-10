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

package appeng.util;

import java.util.Collections;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;

import net.minecraft.client.resources.ClientLanguageMap;
import net.minecraft.client.resources.Language;
import net.minecraft.util.text.LanguageMap;

import appeng.client.gui.MockResourceManager;

public class LoadTranslationsExtension implements Extension, BeforeAllCallback {
    @Override
    public void beforeAll(ExtensionContext context) throws Exception {

        // Load AE2 translations to test translated texts
        Language language = new Language("en_us", "US", "English", false);
        ClientLanguageMap languageMap = ClientLanguageMap.func_239497_a_(MockResourceManager.create(),
                Collections.singletonList(language));
        LanguageMap.func_240594_a_(languageMap);
    }
}
