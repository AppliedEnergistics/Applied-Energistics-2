package net.fabricmc.loader.launch.common;

import org.junit.platform.engine.UniqueId;
import org.junit.platform.launcher.LauncherDiscoveryListener;

/**
 * This listener (injected via service loader) ensures that regardless of which tests
 * are being run, the mixins are initialized first.
 */
public class LauncherListener implements LauncherDiscoveryListener {
    @Override
    public void engineDiscoveryStarted(UniqueId engineId) {
        try {
            LauncherAccessor.init();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
