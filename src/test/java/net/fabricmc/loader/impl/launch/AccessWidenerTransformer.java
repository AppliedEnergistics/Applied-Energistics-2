package net.fabricmc.loader.impl.launch;

import net.fabricmc.accesswidener.AccessWidener;
import net.fabricmc.accesswidener.AccessWidenerClassVisitor;
import net.fabricmc.loader.impl.FabricLoaderImpl;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.spongepowered.asm.mixin.transformer.IMixinTransformer;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

/**
 * Applies AccessWideners using the Java Instrumentation API.
 */
class AccessWidenerTransformer implements ClassFileTransformer {

    private final AccessWidener accessWidener;

    private final IMixinTransformer mixinTransformer;

    public AccessWidenerTransformer(AccessWidener accessWidener, IMixinTransformer mixinTransformer) {
        this.accessWidener = accessWidener;
        this.mixinTransformer = mixinTransformer;
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
            className = className.replace('/', '.');
            return mixinTransformer.transformClassBytes(className, className, classfileBuffer);
        }
        return null;
    }
}
