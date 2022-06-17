package appeng.core.localization;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public interface LocalizationEnum {

    String getEnglishText();

    String getTranslationKey();

    default MutableComponent text() {
        return Component.translatable(getTranslationKey());
    }

    default MutableComponent text(Object... args) {
        return Component.translatable(getTranslationKey(), args);
    }

    default MutableComponent withSuffix(String text) {
        return text().copy().append(text);
    }

    default MutableComponent withSuffix(Component text) {
        return text().copy().append(text);
    }

}
