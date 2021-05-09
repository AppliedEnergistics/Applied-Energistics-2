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
