package net.neoforged.fmljunit;

import org.junit.platform.launcher.LauncherSession;
import org.junit.platform.launcher.LauncherSessionListener;

public class FmlLauncherSessionListener implements LauncherSessionListener {

    private ClassLoader originalClassLoader;

    public FmlLauncherSessionListener() {
    }

    @Override
    public void launcherSessionOpened(LauncherSession session) {
        Thread currentThread = Thread.currentThread();
        originalClassLoader = currentThread.getContextClassLoader();
        Thread.currentThread().setContextClassLoader(LauncherHolder.getTransformingClassLoader());
    }

    @Override
    public void launcherSessionClosed(LauncherSession session) {
        Thread.currentThread().setContextClassLoader(originalClassLoader);
    }
}

