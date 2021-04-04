package appeng;

import cpw.mods.modlauncher.api.ILaunchHandlerService;
import cpw.mods.modlauncher.api.ITransformingClassLoader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.userdev.FMLUserdevLaunchProvider;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.assertj.core.util.Lists;
import org.junit.platform.console.options.CommandLineOptions;
import org.junit.platform.console.options.Details;
import org.junit.platform.console.tasks.ConsoleTestExecutor;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TestPlan;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;

import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import static net.minecraftforge.fml.Logging.CORE;
import static org.junit.platform.engine.discovery.ClassNameFilter.includeClassNamePatterns;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage;

/**
 * This launch service will set up the classpath for loading mods correctly.
 */
public class TestLauncherLaunchTarget extends FMLUserdevLaunchProvider implements ILaunchHandlerService {

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

            LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                    .selectors(selectPackage("appeng"))
                    .filters(includeClassNamePatterns(".*Test"))
                    .build();

            CommandLineOptions options = new CommandLineOptions();
            options.setSelectedPackages(Lists.newArrayList("appeng"));

            options.setDetails(Details.TREE);
            ConsoleTestExecutor consoleTestExecutor = new ConsoleTestExecutor(options);
            consoleTestExecutor.execute(new PrintWriter(System.out));
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
        String[] cpElements = System.getProperty("java.class.path").split(System.getProperty("path.separator"));
        for (String cpElement : cpElements) {
            Path path = Paths.get(cpElement);

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
