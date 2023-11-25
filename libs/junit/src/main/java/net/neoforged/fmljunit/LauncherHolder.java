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

            try {
                BootstrapLauncher.main("-launchTarget", "JUnit", "--assetIndex",
                        "asset-index",
                        "--assetsDir",
                        System.getenv("fmljunit.assetsDir"),
                        "--gameDir",
                        ".",
                        "--fml.neoForgeVersion",
                        System.getenv("fmljunit.neoForgeVersion"),
                        "--fml.fmlVersion",
                        System.getenv("fmljunit.fmlVersion"),
                        "--fml.mcVersion",
                        System.getenv("fmljunit.mcVersion"),
                        "--fml.neoFormVersion",
                        System.getenv("fmljunit.neoFormVersion"));

                transformingClassLoader = Thread.currentThread().getContextClassLoader();
            } finally {
                Thread.currentThread().setContextClassLoader(originalLoader);
            }
        }
        return transformingClassLoader;
    }
}
