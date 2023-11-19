package net.neoforged.fmljunit;

import cpw.mods.bootstraplauncher.BootstrapLauncher;
import org.junit.platform.launcher.LauncherInterceptor;

public class FmlLauncherSessionListener implements LauncherInterceptor {

    private ClassLoader transformingClassLoader;

    public FmlLauncherSessionListener() {
    }

    @Override
    public <T> T intercept(Invocation<T> invocation) {
        Thread currentThread = Thread.currentThread();
        ClassLoader originalClassLoader = currentThread.getContextClassLoader();

        if (transformingClassLoader == null) {
            // This *should* find the service exposed by ModLauncher's BootstrapLaunchConsumer {This doc is here to help find that class next time we go looking}
            BootstrapLauncher.main("-launchTarget", "JUnit", "--assetIndex",
                    "asset-index",
                    "--assetsDir",
                    "C:\\Users\\Sebastian\\.gradle\\caches\\minecraft\\assets\\1.20.2",
                    "--gameDir",
                    ".",
                    "--fml.neoForgeVersion",
                    "20.2.43-beta",
                    "--fml.fmlVersion",
                    "1.0.9",
                    "--fml.mcVersion",
                    "1.20.2",
                    "--fml.neoFormVersion",
                    "20231019.002635");
            transformingClassLoader = Thread.currentThread().getContextClassLoader();
        }

        currentThread.setContextClassLoader(transformingClassLoader);
        try {
            return invocation.proceed();
        } finally {
            currentThread.setContextClassLoader(originalClassLoader);
        }
    }

    @Override
    public void close() {

    }
}

