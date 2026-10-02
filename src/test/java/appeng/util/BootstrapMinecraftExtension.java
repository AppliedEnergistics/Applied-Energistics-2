package appeng.util;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;

import net.neoforged.testframework.junit.EphemeralTestServerProvider;

public class BootstrapMinecraftExtension implements Extension, BeforeAllCallback {
    @Override
    public void beforeAll(ExtensionContext context) {
        // Starts the ephemeral server (once per JUnit session) so that data components and tags are bound,
        // even if the test doesn't inject a MinecraftServer.
        EphemeralTestServerProvider.grabServer();
    }
}
