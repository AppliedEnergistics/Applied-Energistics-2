package appeng;

import net.minecraft.util.registry.Bootstrap;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Calls {@link Bootstrap#register()} before any tests are run.
 */
@ExtendWith(MinecraftExtension.class)
@Inherited
@Retention(RUNTIME)
public @interface BootstrapMinecraft {
}
