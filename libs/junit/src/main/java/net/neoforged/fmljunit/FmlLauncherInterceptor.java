package net.neoforged.fmljunit;

import org.junit.platform.launcher.LauncherInterceptor;

public class FmlLauncherInterceptor implements LauncherInterceptor {
    @Override
    public <T> T intercept(Invocation<T> invocation) {
        var originalLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(LauncherHolder.getTransformingClassLoader());
            return invocation.proceed();
        } finally {
            Thread.currentThread().setContextClassLoader(originalLoader);
        }
    }

    @Override
    public void close() {

    }
}
