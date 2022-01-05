package appeng.core.localization;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;

public interface LocalizationEnum {

    String getEnglishText();

    String getTranslationKey();

    default MutableComponent text() {
        return new TranslatableComponent(getTranslationKey());
    }

    default MutableComponent text(Object... args) {
        return new TranslatableComponent(getTranslationKey(), args);
    }

    default MutableComponent withSuffix(String text) {
        return text().copy().append(text);
    }

    default MutableComponent withSuffix(Component text) {
        return text().copy().append(text);
    }

}
