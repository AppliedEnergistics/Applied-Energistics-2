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

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.ListIterator;

public final class ItemStackPatch {
    private ItemStackPatch() {
    }

    public static void patchCountGetSet(ClassNode node) {
        for (MethodNode mn : node.methods) {
            if ("<init>".equals(mn.name)) {
                ListIterator<AbstractInsnNode> it = mn.instructions.iterator();
                while (it.hasNext()) {
                    AbstractInsnNode in = it.next();
                    if (in instanceof LdcInsnNode && "Count".equals(((LdcInsnNode) in).cst)) {
                        AbstractInsnNode in2 = it.next();
                        if (in2.getOpcode() == Opcodes.INVOKEVIRTUAL) {
                            // :thinking:
                            boolean patched = false;
                            MethodInsnNode min2 = (MethodInsnNode) in2;
                            if (min2.name.equals("getByte")) {
                                min2.name = "getInteger";
                                patched = true;
                            } else if (min2.name.equals("func_74771_c")) {
                                min2.name = "func_74762_e";
                                patched = true;
                            }

                            if (patched) {
                                min2.desc = "(Ljava/lang/String;)I";
                                System.out.println("Patched ItemStack Count getter!");
                            }
                        }
                    }
                }
            } else if ("func_77955_b".equals(mn.name) || "writeToNBT".equals(mn.name)) {
                ListIterator<AbstractInsnNode> it = mn.instructions.iterator();
                while (it.hasNext()) {
                    AbstractInsnNode in = it.next();
                    if (in instanceof LdcInsnNode && "Count".equals(((LdcInsnNode) in).cst)) {
                        it.next();
                        it.next();
                        it.next();
                        AbstractInsnNode in2 = it.next();
                        if (in2.getOpcode() == Opcodes.INVOKEVIRTUAL) {
                            // :thinking:
                            boolean patched = false;
                            MethodInsnNode min2 = (MethodInsnNode) in2;
                            if (min2.name.equals("setByte")) {
                                min2.name = "setInteger";
                                patched = true;
                            } else if (min2.name.equals("func_74774_a")) {
                                min2.name = "func_74768_a";
                                patched = true;
                            }

                            if (patched) {
                                min2.desc = "(Ljava/lang/String;I)V";
                                System.out.println("Patched ItemStack Count setter!");

                                // Remove I2B cast
                                it.previous();
                                it.previous();
                                it.remove();
                            }
                        }
                    }
                }
            }
        }
    }
}
