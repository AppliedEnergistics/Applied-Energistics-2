package net.fabricmc.loader.launch.common;

import net.bytebuddy.agent.ByteBuddyAgent;
import org.spongepowered.tools.agent.MixinAgent;

/**
 * Due to package-level visibility, we use this hack to access the loader internals.
 */
public class LauncherAccessor {
    private static boolean initialized;

    public synchronized static void init() throws Exception {
        if (initialized) {
            return;
        }
        initialized = true;
        var instrumentation = ByteBuddyAgent.install();

        new TestLauncher(instrumentation);
    }
}

