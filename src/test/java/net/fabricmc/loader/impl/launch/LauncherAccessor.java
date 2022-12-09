package net.fabricmc.loader.impl.launch;

import net.bytebuddy.agent.ByteBuddyAgent;

import java.io.File;

/**
 * Due to package-level visibility, we use this hack to access the loader internals.
 */
public class LauncherAccessor {
    private static boolean initialized;

    public synchronized static void init() {
        if (initialized) {
            return;
        }
        initialized = true;
        var instrumentation = ByteBuddyAgent.install();

        new TestLauncher(instrumentation);
    }
}

