package net.neoforged.fmljunit;

import cpw.mods.cl.JarModuleFinder;
import cpw.mods.cl.ModuleClassLoader;
import cpw.mods.jarhandling.SecureJar;

import java.io.File;
import java.io.IOException;
import java.lang.module.ModuleFinder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

final class LauncherHolder {
    private static final boolean DEBUG = System.getProperties().containsKey("bsl.debug");

    private static ClassLoader transformingClassLoader;

    private LauncherHolder() {
    }

    public static ClassLoader getTransformingClassLoader() {
        if (transformingClassLoader == null) {
            var originalLoader = Thread.currentThread().getContextClassLoader();

            try {
                // This *should* find the service exposed by ModLauncher's BootstrapLaunchConsumer {This doc is here to help find that class next time we go looking}
                main("-launchTarget", "JUnit", "--assetIndex",
                        "asset-index",
                        "--assetsDir",
                        "C:\\Users\\Sebastian\\.gradle\\caches\\minecraft\\assets\\1.20.2",
                        "--gameDir",
                        ".",
                        "--fml.neoForgeVersion",
                        "20.2.43-beta",
                        "--fml.fmlVersion",
                        "1.0.9",
                        "--fml.mcVersion",
                        "1.20.2",
                        "--fml.neoFormVersion",
                        "20231019.002635");

                transformingClassLoader = Thread.currentThread().getContextClassLoader();
            } finally {
                Thread.currentThread().setContextClassLoader(originalLoader);
            }
        }
        return transformingClassLoader;
    }

    @SuppressWarnings("unchecked")
    private static void main(String... args) {
        var legacyClasspath = loadLegacyClassPath();
        // Ensure backwards compatibility if somebody reads this value later on.
        System.setProperty("legacyClassPath", String.join(File.pathSeparator, legacyClasspath));

        // TODO: find existing modules automatically instead of taking in an ignore list.
        // The ignore list exempts files that start with certain listed keywords from being turned into modules (like existing modules)
        var ignoreList = System.getProperty("ignoreList", "asm,securejarhandler");
        var ignores = ignoreList.split(",");

        // Tracks all previously encountered packages
        // This prevents subsequent modules from including packages from previous modules, which is disallowed by the module system
        var previousPackages = new HashSet<String>();
        // The list of all SecureJars, which represent one module
        var jars = new ArrayList<SecureJar>();
        // path to name lookup
        var pathLookup = new HashMap<Path, String>();
        // Map of filenames to their 'module number', where all filenames sharing the same 'module number' is combined into one
        var filenameMap = getMergeFilenameMap();
        // Map of 'module number' to the list of paths which are combined into that module
        var mergeMap = new LinkedHashMap<String, List<Path>>();

        var order = new ArrayList<String>();

        outer:
        for (var legacy : legacyClasspath) {
            var path = Paths.get(legacy);
            var filename = path.getFileName().toString();

            for (var filter : ignores) {
                if (filename.startsWith(filter)) {
                    if (DEBUG) {
                        System.out.println("bsl: file '" + legacy + "' ignored because filename starts with '" + filter + "'");
                    }
                    continue outer;
                }
            }

            if (DEBUG) {
                System.out.println("bsl: encountered path '" + legacy + "'");
            }

            if (Files.notExists(path)) continue;
            // This computes the name of the artifact for detecting collisions
            var jar = SecureJar.from(path);
            if ("".equals(jar.name())) continue;
            var jarname = pathLookup.computeIfAbsent(path, k -> filenameMap.getOrDefault(filename, jar.name()));
            order.add(jarname);
            mergeMap.computeIfAbsent(jarname, k -> new ArrayList<>()).add(path);
        }


        // Iterate over merged modules map and combine them into one SecureJar each
        mergeMap.entrySet().stream().sorted(Comparator.comparingInt(e-> order.indexOf(e.getKey()))).forEach(e -> {
            // skip empty paths
            var name = e.getKey();
            var paths = e.getValue();
            if (paths.size() == 1 && Files.notExists(paths.get(0))) return;
            var pathsArray = paths.toArray(Path[]::new);
            var jar = SecureJar.from(new PackageTracker(Set.copyOf(previousPackages), pathsArray), pathsArray);
            var packages = jar.getPackages();

            if (DEBUG) {
                System.out.println("bsl: the following paths are merged together in module " + name);
                paths.forEach(path -> System.out.println("bsl:    " + path));
                System.out.println("bsl: list of packages for module " + name);
                packages.forEach(p -> System.out.println("bsl:    " + p));
            }

            previousPackages.addAll(packages);
            jars.add(jar);
        });

        var secureJarsArray = jars.toArray(SecureJar[]::new);

        // Gather all the module names from the SecureJars
        var allTargets = Arrays.stream(secureJarsArray).map(SecureJar::name).toList();
        // Creates a module finder which uses the list of SecureJars to find modules from
        var jarModuleFinder = JarModuleFinder.of(secureJarsArray);
        // Retrieve the boot layer's configuration
        var bootModuleConfiguration = ModuleLayer.boot().configuration();

        // Creates the module layer configuration for the bootstrap layer module
        // The parent configuration is the boot layer configuration (above)
        // The `before` module finder, used to find modules "in" this layer, and is the jar module finder above
        // The `after` module finder, used to find modules that aren't in the jar module finder or the parent configuration,
        //   is the system module finder (which is probably in the boot configuration :hmmm:)
        // And the list of root modules for this configuration (that is, the modules that 'belong' to the configuration) are
        // the above modules from the SecureJars
        var bootstrapConfiguration = bootModuleConfiguration.resolveAndBind(jarModuleFinder, ModuleFinder.ofSystem(), allTargets);
        // Creates the module class loader, which does the loading of classes and resources from the bootstrap module layer/configuration,
        // falling back to the boot layer if not in the bootstrap layer
        var moduleClassLoader = new ModuleClassLoader("MC-BOOTSTRAP", LauncherHolder.class.getClassLoader(), bootstrapConfiguration, List.of(ModuleLayer.boot()));
        // Actually create the module layer, using the bootstrap configuration above, the boot layer as the parent layer (as configured),
        // and mapping all modules to the module class loader
        var layer = ModuleLayer.defineModules(bootstrapConfiguration, List.of(ModuleLayer.boot()), m -> moduleClassLoader);
        // Set the context class loader to the module class loader from this point forward
        Thread.currentThread().setContextClassLoader(moduleClassLoader);

        final var loader = ServiceLoader.load(layer.layer(), Consumer.class);
        // This *should* find the service exposed by ModLauncher's BootstrapLaunchConsumer {This doc is here to help find that class next time we go looking}
        ((Consumer<String[]>) loader.stream().findFirst().orElseThrow().get()).accept(args);
    }

    private static Map<String, String> getMergeFilenameMap() {
        var mergeModules = System.getProperty("mergeModules");
        if (mergeModules == null)
            return Map.of();
        // `mergeModules` is a semicolon-separated set of comma-separated set of paths, where each (comma) set of paths is
        // combined into a single modules
        // example: filename1.jar,filename2.jar;filename2.jar,filename3.jar

        Map<String, String> filenameMap = new HashMap<>();
        int i = 0;
        for (var merge : mergeModules.split(";")) {
            var targets = merge.split(",");
            for (String target : targets) {
                filenameMap.put(target, String.valueOf(i));
            }
            i++;
        }

        return filenameMap;
    }

    private record PackageTracker(Set<String> packages, Path... paths) implements BiPredicate<String, String> {
        @Override
        public boolean test(final String path, final String basePath) {
            // This method returns true if the given path is allowed within the JAR (filters out 'bad' paths)

            if (packages.isEmpty() || // This is the first jar, nothing is claimed yet, so allow everything
                    path.startsWith("META-INF/")) // Every module can have their own META-INF
                return true;

            int idx = path.lastIndexOf('/');
            return idx < 0 || // Resources at the root are allowed to co-exist
                    idx == path.length() - 1 || // All directories can have a potential to exist without conflict, we only care about real files.
                    !packages.contains(path.substring(0, idx).replace('/', '.')); // If the package hasn't been used by a previous JAR
        }
    }

    private static List<String> loadLegacyClassPath() {
        var legacyCpPath = System.getProperty("legacyClassPath.file");

        if (legacyCpPath != null) {
            var legacyCPFileCandidatePath = Paths.get(legacyCpPath);
            if (Files.exists(legacyCPFileCandidatePath) && Files.isRegularFile(legacyCPFileCandidatePath)) {
                try {
                    return Files.readAllLines(legacyCPFileCandidatePath);
                }
                catch (IOException e) {
                    throw new IllegalStateException("Failed to load the legacy class path from the specified file: " + legacyCpPath, e);
                }
            }
        }

        var legacyClasspath = System.getProperty("legacyClassPath", System.getProperty("java.class.path"));
        Objects.requireNonNull(legacyClasspath, "Missing legacyClassPath, cannot bootstrap");
        if (legacyClasspath.length() == 0) {
            return List.of();
        } else {
            return Arrays.asList(legacyClasspath.split(File.pathSeparator));
        }
    }
}
