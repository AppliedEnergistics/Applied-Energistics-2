/*
 * Copyright (c) 2018, 2020 Adrian Siekierka
 *
 * This file is part of StackUp.
 *
 * StackUp is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * StackUp is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with StackUp.  If not, see <http://www.gnu.org/licenses/>.
 */

package appeng.core.transformer;

import appeng.core.AE2ELCore;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.ByteStreams;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.common.Loader;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.tree.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.function.Consumer;

public class AE2ELTransformer implements IClassTransformer {

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (Loader.instance().getIndexedModList().get("stackup") != null) {
            return basicClass;
        }
        transformedName = transformedName.replace('/', '.');

        Consumer<ClassNode> consumer = (n) -> {
        };
        Consumer<ClassNode> emptyConsumer = consumer;

        if ("net.minecraft.item.ItemStack".equals(transformedName)) {
            consumer = consumer.andThen(ItemStackPatch::patchCountGetSet);
        } else if ("net.minecraft.network.PacketBuffer".equals(transformedName)) {
            consumer = consumer.andThen((node) -> {
                spliceClasses(node, "appeng.core.transformer.PacketBufferPatch",
                        "readItemStack", "func_150791_c",
                        "writeItemStack", "func_150788_a");
            });
        }

        if (consumer != emptyConsumer) {
            return processNode(basicClass, consumer);
        } else {
            return basicClass;
        }
    }

    public static byte[] processNode(byte[] data, Consumer<ClassNode> classNodeConsumer) {
        ClassReader reader = new ClassReader(data);
        ClassNode nodeOrig = new ClassNode();
        reader.accept(nodeOrig, 0);
        classNodeConsumer.accept(nodeOrig);
        ClassWriter writer = new ClassWriter(0);
        nodeOrig.accept(writer);
        return writer.toByteArray();
    }

    public static void spliceClasses(final ClassNode data, final String className, final String... methods) {
        try (InputStream stream = AE2ELCore.class.getClassLoader().getResourceAsStream(className.replace('.', '/') + ".class")) {
            spliceClasses(data, ByteStreams.toByteArray(stream), className, methods);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public static void spliceClasses(final ClassNode nodeData, final byte[] dataSplice, final String className, final String... methods) {
        // System.out.println("Splicing from " + className + " to " + targetClassName)
        if (dataSplice == null) {
            throw new RuntimeException("Class " + className + " not found! This is a AE2EL bug!");
        }

        final Set<String> methodSet = Sets.newHashSet(methods);
        final List<String> methodList = Lists.newArrayList(methods);

        final ClassReader readerSplice = new ClassReader(dataSplice);
        final String className2 = className.replace('.', '/');
        final String targetClassName2 = nodeData.name;
        final String targetClassName = targetClassName2.replace('/', '.');
        final Remapper remapper = new Remapper() {
            public String map(final String name) {
                return className2.equals(name) ? targetClassName2 : name;
            }
        };

        ClassNode nodeSplice = new ClassNode();
        readerSplice.accept(new ClassRemapper(nodeSplice, remapper), ClassReader.EXPAND_FRAMES);
        for (String s : nodeSplice.interfaces) {
            if (methodSet.contains(s)) {
                nodeData.interfaces.add(s);
                System.out.println("Added INTERFACE: " + s);
            }
        }

        for (int i = 0; i < nodeSplice.methods.size(); i++) {
            if (methodSet.contains(nodeSplice.methods.get(i).name)) {
                MethodNode mn = nodeSplice.methods.get(i);
                boolean added = false;

                for (int j = 0; j < nodeData.methods.size(); j++) {
                    if (nodeData.methods.get(j).name.equals(mn.name)
                            && nodeData.methods.get(j).desc.equals(mn.desc)) {
                        MethodNode oldMn = nodeData.methods.get(j);
                        System.out.println("Spliced in METHOD: " + targetClassName + "." + mn.name);
                        nodeData.methods.set(j, mn);
                        if (nodeData.name.equals(nodeSplice.superName)) {
                            ListIterator<AbstractInsnNode> nodeListIterator = mn.instructions.iterator();
                            while (nodeListIterator.hasNext()) {
                                AbstractInsnNode node = nodeListIterator.next();
                                if (node instanceof MethodInsnNode
                                        && node.getOpcode() == Opcodes.INVOKESPECIAL) {
                                    MethodInsnNode methodNode = (MethodInsnNode) node;
                                    if (targetClassName2.equals(methodNode.owner)) {
                                        methodNode.owner = nodeData.superName;
                                    }
                                }
                            }
                        }

                        oldMn.name = methodList.get((methodList.indexOf(oldMn.name)) & (~1)) + "_ae2el_old";
                        nodeData.methods.add(oldMn);
                        added = true;
                        break;
                    }
                }

                if (!added) {
                    System.out.println("Added METHOD: " + targetClassName + "." + mn.name);
                    nodeData.methods.add(mn);
                    added = true;
                }
            }
        }

        for (int i = 0; i < nodeSplice.fields.size(); i++) {
            if (methodSet.contains(nodeSplice.fields.get(i).name)) {
                FieldNode mn = nodeSplice.fields.get(i);
                boolean added = false;

                for (int j = 0; j < nodeData.fields.size(); j++) {
                    if (nodeData.fields.get(j).name.equals(mn.name)
                            && nodeData.fields.get(j).desc.equals(mn.desc)) {
                        System.out.println("Spliced in FIELD: " + targetClassName + "." + mn.name);
                        nodeData.fields.set(j, mn);
                        added = true;
                        break;
                    }
                }

                if (!added) {
                    System.out.println("Added FIELD: " + targetClassName + "." + mn.name);
                    nodeData.fields.add(mn);
                    added = true;
                }
            }
        }

    }

}
