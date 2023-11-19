package net.neoforged.fmljunit;

import cpw.mods.bootstraplauncher.BootstrapLauncher;

final class LauncherHolder {
    private static ClassLoader transformingClassLoader;

    private LauncherHolder() {
    }

    public static ClassLoader getTransformingClassLoader() {
        if (transformingClassLoader == null) {
            var originalLoader = Thread.currentThread().getContextClassLoader();

            try {
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
            } finally {
                Thread.currentThread().setContextClassLoader(originalLoader);
            }
        }
        return transformingClassLoader;
    }
}
