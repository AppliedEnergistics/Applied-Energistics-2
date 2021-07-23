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

import net.minecraft.client.resources.language.ClientLanguage;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;

import net.minecraft.client.resources.language.LanguageInfo;
import net.minecraft.locale.Language;

import appeng.client.gui.MockResourceManager;

public class LoadTranslationsExtension implements Extension, BeforeAllCallback {
    @Override
    public void beforeAll(ExtensionContext context) throws Exception {

        // Load AE2 translations to test translated texts
        LanguageInfo language = new LanguageInfo("en_us", "US", "English", false);
        ClientLanguage languageMap = ClientLanguage.func_239497_a_(MockResourceManager.create(),
                Collections.singletonList(language));
        Language.func_240594_a_(languageMap);
    }
}
