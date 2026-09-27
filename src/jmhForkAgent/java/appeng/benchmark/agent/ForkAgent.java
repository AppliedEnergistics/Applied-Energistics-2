package appeng.benchmark.agent;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModLoadingException;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.startup.JUnitGameBootstrapper;
import net.neoforged.fml.startup.StartupArgs;

/**
 * Java agent that runs a forked JMH benchmark JVM inside the game.
 * <p>
 * JMH starts its forked JVMs with {@code org.openjdk.jmh.runner.ForkedMain} as the main class, which would load JMH and
 * the benchmarks outside the game class loader. The {@code jmh} task adds this agent to the JMH host JVM, and JMH
 * passes the host's JVM arguments on to the forked JVMs. In a forked JVM, before the main class runs, the agent starts
 * FML and bootstraps the game the same way FML's JUnit integration does for unit tests, runs {@code ForkedMain} inside
 * the game class loader and exits the JVM. In any other JVM, such as the host, it does nothing. JMH and the benchmarks
 * are loaded by FML as part of the ae2 mod (see the {@code jmh} task in build.gradle).
 * <p>
 * The working directory is used as the game directory. JMH runs the forked JVMs one after another, so they don't
 * interfere with each other.
 * <p>
 * FML refuses to start if classes belonging to the game were already loaded outside the game class loader, so this
 * class must only use FML and JDK classes.
 */
public final class ForkAgent {
    private static final String FORKED_MAIN_CLASS = "org.openjdk.jmh.runner.ForkedMain";

    private ForkAgent() {
    }

    public static void premain(String agentArgs, Instrumentation instrumentation) {
        // The launcher puts the main class and its arguments into this property
        var command = System.getProperty("sun.java.command", "").trim().split("\\s+");
        if (!command[0].equals(FORKED_MAIN_CLASS)) {
            return; // Not a forked JMH JVM
        }
        var forkedMainArgs = Arrays.copyOfRange(command, 1, command.length);

        var gameClassLoader = startGame(instrumentation);
        Thread.currentThread().setContextClassLoader(gameClassLoader);
        try {
            var forkedMain = Class.forName(FORKED_MAIN_CLASS, true, gameClassLoader);
            // ForkedMain is package-private, and FML loads JMH as part of the ae2 module, which does not open it
            instrumentation.redefineModule(forkedMain.getModule(), Set.of(), Map.of(),
                    Map.of(forkedMain.getPackageName(), Set.of(ForkAgent.class.getModule())),
                    Set.of(), Map.of());
            var mainMethod = forkedMain.getMethod("main", String[].class);
            mainMethod.setAccessible(true);
            mainMethod.invoke(null, (Object) forkedMainArgs);
        } catch (InvocationTargetException e) {
            e.getCause().printStackTrace();
            System.exit(1);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            System.exit(1);
        }
        // ForkedMain normally exits by itself. Never return, or the JVM would run ForkedMain again, outside the game.
        System.exit(0);
    }

    private static ClassLoader startGame(Instrumentation instrumentation) {
        var startupArgs = new StartupArgs(
                Path.of("").toAbsolutePath(),
                true,
                Dist.DEDICATED_SERVER,
                false,
                new String[0],
                Set.of(),
                List.of(),
                Thread.currentThread().getContextClassLoader());
        var loader = FMLLoader.create(instrumentation, startupArgs);
        if (loader.getLoadingModList().hasErrors()) {
            throw new ModLoadingException(loader.getLoadingModList().getModLoadingIssues());
        }
        for (var bootstrapper : ServiceLoader.load(JUnitGameBootstrapper.class, loader.getCurrentClassLoader())) {
            bootstrapper.bootstrap(loader);
        }
        return loader.getCurrentClassLoader();
    }
}
