package appeng.util;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;

public class BootstrapMinecraftExtension implements Extension, BeforeAllCallback {
    private static boolean keyTypesInitialized;

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        if (!keyTypesInitialized) {
            keyTypesInitialized = true;
        }

    }
}
