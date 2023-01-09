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

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import java.util.function.Consumer;

public class AE2ELTransformer implements IClassTransformer {

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        transformedName = transformedName.replace('/', '.');

        byte[] data = basicClass;
        Consumer<ClassNode> consumer = (n) -> {
        };
        Consumer<ClassNode> emptyConsumer = consumer;

        if ("net.minecraft.item.ItemStack".equals(transformedName)) {
            consumer = consumer.andThen(ItemStackPatch::patchCountGetSet);
        }

        if (consumer != emptyConsumer) {
            return processNode(basicClass, consumer);
        } else {
            return data;
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

}
