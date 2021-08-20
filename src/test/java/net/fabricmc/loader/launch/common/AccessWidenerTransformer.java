package net.fabricmc.loader.launch.common;

import net.fabricmc.accesswidener.AccessWidener;
import net.fabricmc.accesswidener.AccessWidenerVisitor;
import net.fabricmc.loader.FabricLoader;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

/**
 * Applies AccessWideners using the Java Instrumentation API.
 */
record AccessWidenerTransformer(AccessWidener accessWidener) implements ClassFileTransformer {
    @Override
    public byte[] transform(Module module, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        var niceName = className.replace('/', '.');
        if (accessWidener.getTargets().contains(niceName)) {
            ClassReader classReader = new ClassReader(classfileBuffer);
            ClassWriter classWriter = new ClassWriter(0);
            ClassVisitor visitor = classWriter;
            visitor = AccessWidenerVisitor.createClassVisitor(FabricLoader.ASM_VERSION, visitor, FabricLoader.INSTANCE.getAccessWidener());
            classReader.accept(visitor, 0);
            return classWriter.toByteArray();
        }
        return null;
    }
}
