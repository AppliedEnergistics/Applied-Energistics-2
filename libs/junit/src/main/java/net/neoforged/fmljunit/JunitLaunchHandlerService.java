package net.neoforged.fmljunit;

import cpw.mods.modlauncher.Launcher;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.targets.ForgeUserdevLaunchHandler;

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

            var neoforge = gameLayer.findLoader("neoforge");

            var modLoaderClass = neoforge.loadClass("net.neoforged.neoforge.server.loading.ServerModLoader");
            callMethod(modLoaderClass, "load", null);

            Consumer<Dist> extension = Launcher.INSTANCE.environment().findLaunchPlugin("runtimedistcleaner")
                    .get()
                    .getExtension();
            extension.accept(Dist.CLIENT);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void callMethod(Class<?> clazz, String name, Object instance, Object... args) {
        for (Method method : clazz.getDeclaredMethods()) {
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
                    method.setAccessible(true);
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
