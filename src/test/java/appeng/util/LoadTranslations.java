package appeng.util;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(LoadTranslationsExtension.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface LoadTranslations {
}
