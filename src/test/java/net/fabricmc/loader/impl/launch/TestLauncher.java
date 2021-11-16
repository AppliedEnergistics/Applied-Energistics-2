package net.fabricmc.loader.impl.launch;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.fabricmc.loader.impl.game.minecraft.MinecraftGameProvider;
import net.fabricmc.loader.impl.launch.knot.MixinServiceKnotAccessor;
import org.spongepowered.asm.launch.MixinBootstrap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.jar.Manifest;

class TestLauncher extends FabricLauncherBase {

    public TestLauncher(Instrumentation instrumentation) {
        setProperties(new HashMap<>());

        var provider = new MinecraftGameProvider();
        provider.locateGame(this, new String[]{}, Thread.currentThread().getContextClassLoader());

        FabricLoaderImpl loader = FabricLoaderImpl.INSTANCE;
        loader.setGameProvider(provider);
        loader.load();
        loader.freeze();
        loader.loadAccessWideners();

        MixinBootstrap.init();
        FabricMixinBootstrap.init(getEnvironmentType(), loader);
        FabricLauncherBase.finishMixinBootstrapping();

        var transformer = MixinServiceKnotAccessor.getTransformer();
        instrumentation.addTransformer(new AccessWidenerTransformer(loader.getAccessWidener(), transformer));
    }

    @Override
    public void addToClassPath(Path path, String... allowedPrefixes) {
    }

    @Override
    public void setAllowedPrefixes(Path path, String... prefixes) {
    }

    @Override
    public Class<?> loadIntoTarget(String name) throws ClassNotFoundException {
        return null;
    }

    @Override
    public Manifest getManifest(Path originPath) {
        return null;
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
