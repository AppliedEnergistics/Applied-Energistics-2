package appeng;

import cpw.mods.modlauncher.EnumerationHelper;
import cpw.mods.modlauncher.api.ILaunchHandlerService;
import cpw.mods.modlauncher.api.ITransformingClassLoader;
import net.minecraft.util.registry.Bootstrap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoader;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.ModWorkManager;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.LogMarkers;
import net.minecraftforge.userdev.FMLUserdevLaunchProvider;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.assertj.core.util.Lists;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.stream.Collectors;

import static net.minecraftforge.fml.Logging.CORE;

public class TestLaunchHandlerService extends FMLUserdevLaunchProvider implements ILaunchHandlerService {

    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public String name() {
        return "junit";
    }

    @Override
    public Callable<Void> launchService(String[] arguments, ITransformingClassLoader launchClassLoader) {
        return () -> {
            LOGGER.debug(CORE, "Launching unit test in {} with arguments {}", launchClassLoader, arguments);
            super.beforeStart(launchClassLoader);
            launchClassLoader.addTargetPackageFilter(getPackagePredicate());
            // JUnit will have loaded before Forge, so it needs to come from the parent classloader consistently
            launchClassLoader.addTargetPackageFilter(s -> !s.startsWith("org.junit."));

            Class.forName("appeng.TestLaunchHandlerService$Init", true, launchClassLoader.getInstance()).newInstance();
            return null;
        };
    }

    public static class Init {
        public Init() {
            Bootstrap.register();

            ModLoader.get().gatherAndInitializeMods(ModWorkManager.syncExecutor(), ModWorkManager.parallelExecutor(), () -> {
            });
        }
    }

    @Override
    public Dist getDist() {
        return Dist.CLIENT;
    }

    @Override
    protected void processModClassesEnvironmentVariable(Map<String, List<Pair<Path, List<Path>>>> arguments) {
        String modClasses = (String) Optional.ofNullable(System.getProperty("MOD_CLASSES")).orElse("");
        LOGGER.debug(LogMarkers.CORE, "Got mod coordinates {} from env", modClasses);
        Map<String, List<Path>> modClassPaths = (Map) Arrays.stream(modClasses.split(File.pathSeparator)).map((inp) -> {
            return inp.split("%%", 2);
        }).map(this::buildModPair).collect(Collectors.groupingBy(Pair::getLeft, Collectors.mapping(Pair::getRight, Collectors.toList())));
        LOGGER.debug(LogMarkers.CORE, "Found supplied mod coordinates [{}]", modClassPaths);
        List<Pair<Path, List<Path>>> explodedTargets = (List)arguments.computeIfAbsent("explodedTargets", (a) -> {
            return new ArrayList();
        });
        modClassPaths.forEach((modlabel, paths) -> {
            explodedTargets.add(Pair.of(paths.get(0), paths.subList(1, paths.size())));
        });
    }

    private Pair<String, Path> buildModPair(String[] splitString) {
        String modid = splitString.length == 1 ? "defaultmodid" : splitString[0];
        Path path = Paths.get(splitString[splitString.length - 1]);
        return Pair.of(modid, path);
    }
//
//    @Override
//    protected Function<String, Enumeration<URL>> getClassLoaderResourceEnumerationFunction() {
//        return EnumerationHelper.mergeFunctors(
//                super.getClassLoaderResourceEnumerationFunction(),
//                s -> {
//                    Vector<URL> v = new Vector<>();
//                    URL u = TestLaunchHandlerService.class.getClassLoader().getResource(s);
//                    if (u != null) {
//                        v.add(u);
//                    }
//                    return v.elements();
//                }
//        );
//    }
}
