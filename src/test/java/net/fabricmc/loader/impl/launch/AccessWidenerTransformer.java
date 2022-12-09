package net.fabricmc.loader.impl.launch;

import com.google.common.collect.Sets;
import net.bytebuddy.dynamic.loading.ClassInjector;
import net.fabricmc.accesswidener.AccessWidener;
import net.fabricmc.accesswidener.AccessWidenerClassVisitor;
import net.fabricmc.loader.impl.FabricLoaderImpl;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.transformer.IMixinTransformer;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Applies AccessWideners using the Java Instrumentation API.
 */
class AccessWidenerTransformer implements ClassFileTransformer {

    private final AccessWidener accessWidener;

    private final IMixinTransformer mixinTransformer;
    private final ClassInjector classInjector;
    private final Map<String, ?> syntheticClasses;
    private final Set<String> injectedClasses = new HashSet<>();

    public AccessWidenerTransformer(AccessWidener accessWidener, IMixinTransformer mixinTransformer, ClassInjector classInjector) {
        this.accessWidener = accessWidener;
        this.mixinTransformer = mixinTransformer;
        this.classInjector = classInjector;
        this.syntheticClasses = getSyntheticClassMap(mixinTransformer);
    }

    /**
     * Gets the private map of synthetic classes from the Mixin transformer.
     * Since we only get callbacks in this class to transform classes that *actually* exist,
     * we need to be proactive in injecting those synthetic classes and need access
     * to the list of such classes that Mixin generated.
     */
    @SuppressWarnings("unchecked")
    private Map<String, ?> getSyntheticClassMap(IMixinTransformer mixinTransformer) {
        final Map<String, ?> syntheticClasses;
        try {
            var syntheticClassRegistryField = mixinTransformer.getClass().getDeclaredField("syntheticClassRegistry");
            syntheticClassRegistryField.setAccessible(true);
            var syntheticClassRegistry = syntheticClassRegistryField.get(mixinTransformer);
            var classesField = syntheticClassRegistry.getClass().getDeclaredField("classes");
            classesField.setAccessible(true);
            syntheticClasses = (Map<String, ?>) classesField.get(syntheticClassRegistry);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return syntheticClasses;
    }

    @Override
    public byte[] transform(Module module, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        var niceName = className.replace('/', '.');
        if (accessWidener.getTargets().contains(niceName)) {
            ClassReader classReader = new ClassReader(classfileBuffer);
            ClassWriter classWriter = new ClassWriter(0);
            ClassVisitor visitor = classWriter;
            visitor = AccessWidenerClassVisitor.createClassVisitor(FabricLoaderImpl.ASM_VERSION, visitor, FabricLoaderImpl.INSTANCE.getAccessWidener());
            classReader.accept(visitor, 0);
            classfileBuffer = classWriter.toByteArray();
        }

        if (className.startsWith("net/minecraft") || className.contains("mixin")) {
            var environment = MixinEnvironment.getCurrentEnvironment();

            var transformResult = mixinTransformer.transformClass(environment, niceName, classfileBuffer);

            // Handle any synthetic anonymous classes that Mixins may have generated
            // We need to inject those into the class-loader using ByteBuddy since
            // the instrumentation API does not support this natively.
            Set<String> classesToInject = Sets.difference(syntheticClasses.keySet(), injectedClasses);
            if (!classesToInject.isEmpty()) {
                var newClasses = new HashMap<String, byte[]>();
                for (var syntheticClass : new HashSet<>(classesToInject)) {
                    injectedClasses.add(syntheticClass);
                    newClasses.put(
                            syntheticClass.replace('/', '.'),
                            mixinTransformer.generateClass(environment, syntheticClass)
                    );
                }
                classInjector.injectRaw(newClasses);
            }

            return transformResult;
        }
        return null;
    }
}
