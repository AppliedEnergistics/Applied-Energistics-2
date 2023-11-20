package net.neoforged.fmljunit;

import cpw.mods.modlauncher.Launcher;
import cpw.mods.modlauncher.api.IEnvironment;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.targets.ForgeUserdevLaunchHandler;
import org.mockito.Mockito;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Consumer;

public class JunitLaunchHandlerService extends ForgeUserdevLaunchHandler {
    @Override
    public String name() {
        return "JUnit";
    }

    @Override
    public Dist getDist() {
        return Dist.DEDICATED_SERVER;
    }

    @Override
    protected void runService(String[] arguments, ModuleLayer gameLayer) {
        try {
            var mcLoader = gameLayer.findLoader("minecraft");

            mcLoader.loadClass("net.minecraft.SharedConstants")
                    .getMethod("tryDetectVersion")
                    .invoke(null);
            mcLoader.loadClass("net.minecraft.server.Bootstrap")
                    .getMethod("bootStrap")
                    .invoke(null);

            var fmlCoreLoader = gameLayer.findLoader("fml_core");

            var modLoaderClass = fmlCoreLoader.loadClass("net.neoforged.fml.ModLoader");
            var syncExecutor = fmlCoreLoader.loadClass("net.neoforged.fml.ModWorkManager")
                    .getMethod("syncExecutor").invoke(null);
            var parallelExecutor = fmlCoreLoader.loadClass("net.neoforged.fml.ModWorkManager")
                    .getMethod("parallelExecutor").invoke(null);


            var modLoader = modLoaderClass.getMethod("get").invoke(null);

            Runnable periodicTasks = () -> {
            };
            callMethod(modLoaderClass, "gatherAndInitializeMods", modLoader, syncExecutor, parallelExecutor, periodicTasks);
            callMethod(modLoaderClass, "loadMods", modLoader, syncExecutor, parallelExecutor, periodicTasks);
            callMethod(modLoaderClass, "finishMods", modLoader, syncExecutor, parallelExecutor, periodicTasks);

            Consumer<Dist> extension = Launcher.INSTANCE.environment().findLaunchPlugin("runtimedistcleaner")
                    .get()
                    .getExtension();
            extension.accept(Dist.CLIENT);
//
//            var clientClass = mcLoader.loadClass("net.minecraft.client.Minecraft");
//            var clientMock = Mockito.mock(clientClass);
//            var mocked = Mockito.mockStatic(clientClass, Mockito.CALLS_REAL_METHODS);
//            mocked.when(() -> clientClass.getDeclaredMethod("getInstance").invoke(null))
//                    .thenReturn(clientMock);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void callMethod(Class<?> clazz, String name, Object instance, Object... args) {
        for (Method method : clazz.getMethods()) {
            if (!method.getName().equals(name)) {
                continue;
            }

            var paramTypes = method.getParameterTypes();
            if (paramTypes.length != args.length) {
                continue;
            }

            boolean compatible = true;
            for (int i = 0; i < paramTypes.length; i++) {
                if (args[i] != null && !paramTypes[i].isAssignableFrom(args[i].getClass())) {
                    compatible = false;
                    break;
                }
            }

            if (compatible) {
                try {
                    method.invoke(instance, args);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return;
            }
        }

        throw new RuntimeException("No such method found: " + name);
    }
}
