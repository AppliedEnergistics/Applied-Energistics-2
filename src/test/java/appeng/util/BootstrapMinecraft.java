package appeng.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.extension.ExtendWith;

import net.neoforged.testframework.junit.EphemeralTestServerProvider;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@ExtendWith({ EphemeralTestServerProvider.class, BootstrapMinecraftExtension.class })
public @interface BootstrapMinecraft {
}
