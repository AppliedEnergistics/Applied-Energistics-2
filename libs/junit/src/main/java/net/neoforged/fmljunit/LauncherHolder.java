package net.neoforged.fmljunit;

import cpw.mods.bootstraplauncher.BootstrapLauncher;

final class LauncherHolder {
    private static final boolean DEBUG = System.getProperties().containsKey("bsl.debug");

    private static ClassLoader transformingClassLoader;

    private LauncherHolder() {
    }

    public static ClassLoader getTransformingClassLoader() {
        if (transformingClassLoader == null) {
            var originalLoader = Thread.currentThread().getContextClassLoader();

            var assetsDir = System.getenv("fmljunit.assetsDir");

            try {
                BootstrapLauncher.main("-launchTarget", "JUnit", "--assetIndex",
                        "asset-index",
                        "--assetsDir",
                        assetsDir,
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
