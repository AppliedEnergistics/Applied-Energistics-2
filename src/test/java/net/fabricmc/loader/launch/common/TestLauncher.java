package net.fabricmc.loader.launch.common;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.FabricLoader;
import net.fabricmc.loader.game.MinecraftGameProvider;
import org.junit.platform.commons.util.ReflectionUtils;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.transformer.FabricMixinTransformerProxy;
import org.spongepowered.asm.mixin.transformer.IMixinTransformer;
import org.spongepowered.tools.agent.MixinAgent;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;

class TestLauncher extends FabricLauncherBase {

    public TestLauncher(Instrumentation instrumentation) throws Exception {
        setProperties(new HashMap<>());

        var provider = new MinecraftGameProvider();
        provider.locateGame(EnvType.CLIENT, new String[]{}, Thread.currentThread().getContextClassLoader());

        FabricLoader loader = FabricLoader.INSTANCE;
        loader.setGameProvider(provider);
        loader.load();
        loader.freeze();
        loader.loadAccessWideners();

        MixinBootstrap.init();
        FabricMixinBootstrap.init(getEnvironmentType(), loader);
        FabricLauncherBase.finishMixinBootstrapping();

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
        return false;
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        var stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
        if (stream == null) {
            throw new RuntimeException("Failed to read file '" + name + "'!");
        }
        return stream;
    }

    @Override
    public ClassLoader getTargetClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    private String getClassFileName(String name) {
        return name.replace('.', '/') + ".class";
    }

    @Override
    public byte[] getClassByteArray(String name, boolean runTransformers) throws IOException {
        String classFile = getClassFileName(name);
        InputStream inputStream = getResourceAsStream(classFile);
        if (inputStream == null) return null;

        int a = inputStream.available();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(a < 32 ? 32768 : a);
        byte[] buffer = new byte[8192];
        int len;

        while ((len = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, len);
        }

        inputStream.close();
        return outputStream.toByteArray();
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
