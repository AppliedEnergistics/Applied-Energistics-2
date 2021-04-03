package appeng;

import net.minecraft.util.registry.Bootstrap;
import net.minecraftforge.fml.ModLoader;
import net.minecraftforge.fml.ModWorkManager;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Simply calls {@link Bootstrap#register()} before any tests are run.
 */
public class MinecraftExtension implements BeforeAllCallback {

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Class<?> bootstrapCl = cl.loadClass("net.minecraft.util.registry.Bootstrap");
        bootstrapCl.getMethod("register").invoke(null);
    }

}
