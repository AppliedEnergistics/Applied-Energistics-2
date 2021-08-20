package net.fabricmc.loader.launch.common;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.FabricLoader;
import net.fabricmc.loader.game.MinecraftGameProvider;

import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;

class TestLauncher extends FabricLauncherBase {

    public TestLauncher(Instrumentation instrumentation) {
        setProperties(new HashMap<>());

        var provider = new MinecraftGameProvider();
        provider.locateGame(EnvType.CLIENT, new String[]{}, Thread.currentThread().getContextClassLoader());

        FabricLoader loader = FabricLoader.INSTANCE;
        loader.setGameProvider(provider);
        loader.load();
        loader.freeze();
        loader.loadAccessWideners();

        instrumentation.addTransformer(new AccessWidenerTransformer(loader.getAccessWidener()));
    }

    @Override
    public void propose(URL url) {
    }

    @Override
    public EnvType getEnvironmentType() {
        return EnvType.CLIENT;
    }

    @Override
    public boolean isClassLoaded(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ClassLoader getTargetClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    @Override
    public byte[] getClassByteArray(String name, boolean runTransformers) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isDevelopment() {
        return true;
    }

    @Override
    public String getEntrypoint() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getTargetNamespace() {
        return "named";
    }

    @Override
    public Collection<URL> getLoadTimeDependencies() {
        throw new UnsupportedOperationException();
    }
}
