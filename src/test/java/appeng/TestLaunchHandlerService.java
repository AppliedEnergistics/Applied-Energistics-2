package appeng;

import cpw.mods.modlauncher.api.ILaunchHandlerService;
import cpw.mods.modlauncher.api.ITransformingClassLoader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.userdev.FMLUserdevLaunchProvider;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import static net.minecraftforge.fml.Logging.CORE;

/**
 * This launch service will set up the classpath for loading mods correctly.
 */
public class TestLaunchHandlerService extends FMLUserdevLaunchProvider implements ILaunchHandlerService {

    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * We need to pass launch target "junit" to make FML call this class once loading is complete.
     */
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

            Thread.currentThread().setContextClassLoader(launchClassLoader.getInstance());
            return null;
        };
    }

    @Override
    public Dist getDist() {
        return Dist.CLIENT;
    }

    @Override
    protected void processModClassesEnvironmentVariable(Map<String, List<Pair<Path, List<Path>>>> arguments) {
        // Collect every file system folder that is currently on the classpath, which normally corresponds
        // to the compiler output path for the IDE or Gradle.
        List<Path> folders = new ArrayList<>();
        URLClassLoader classLoader = (URLClassLoader) getClass().getClassLoader();
        for (URL url : classLoader.getURLs()) {
            Path path;
            try {
                path = Paths.get(url.toURI());
            } catch (URISyntaxException e) {
                continue;
            }

            if (Files.isDirectory(path)) {
                folders.add(path);
            }
        }

        sortModsTomlFolderToFront(folders);

        List<Pair<Path, List<Path>>> explodedTargets = arguments.computeIfAbsent("explodedTargets", (a) -> {
            return new ArrayList<>();
        });
        explodedTargets.add(Pair.of(folders.get(0), folders.subList(1, folders.size())));
    }

    private void sortModsTomlFolderToFront(List<Path> folders) {
        // There should be a mods.toml in one of the directories. It needs to be sorted to the front
        // because FML will only search the first directory.
        int modsTomlIndex = -1;
        for (int i = 0; i < folders.size(); i++) {
            if (Files.isRegularFile(folders.get(i).resolve("META-INF/mods.toml"))) {
                modsTomlIndex = i;
                break;
            }
        }
        if (modsTomlIndex == -1) {
            throw new IllegalStateException("Could not find META-INF/mods.toml in the current exploded classpath: "
                    + folders);
        }
        folders.add(0, folders.remove(modsTomlIndex));
    }

}
