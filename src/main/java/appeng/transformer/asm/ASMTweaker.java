/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.transformer.asm;

import java.util.Iterator;

import net.minecraft.launchwrapper.IClassTransformer;

import org.apache.logging.log4j.Level;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import cpw.mods.fml.relauncher.FMLRelaunchLog;

public class ASMTweaker implements IClassTransformer
{

	class publicLine
	{

		public publicLine(String name, String desc) {
			this.name = name;
			this.desc = desc;
		}

		final String name, desc;

	}

	final Multimap<String, publicLine> privateToPublicMethods = HashMultimap.create();

	public ASMTweaker() {
		privateToPublicMethods.put( "net.minecraft.client.gui.inventory.GuiContainer", new publicLine( "func_146977_a", "(Lnet/minecraft/inventory/Slot;)V" ) );
		privateToPublicMethods.put( "net.minecraft.client.gui.inventory.GuiContainer", new publicLine( "a", "(Lzk;)V" ) );

		privateToPublicMethods.put( "appeng.tile.AEBaseTile", new publicLine( "writeToNBT", "(Lnet/minecraft/nbt/NBTTagCompound;)V" ) );
		privateToPublicMethods.put( "appeng.tile.AEBaseTile", new publicLine( "func_145841_b", "(Lnet/minecraft/nbt/NBTTagCompound;)V" ) );
		privateToPublicMethods.put( "appeng.tile.AEBaseTile", new publicLine( "b", "(Ldh;)V" ) );

		privateToPublicMethods.put( "appeng.tile.AEBaseTile", new publicLine( "readFromNBT", "(Lnet/minecraft/nbt/NBTTagCompound;)V" ) );
		privateToPublicMethods.put( "appeng.tile.AEBaseTile", new publicLine( "func_145839_a", "(Lnet/minecraft/nbt/NBTTagCompound;)V" ) );
		privateToPublicMethods.put( "appeng.tile.AEBaseTile", new publicLine( "a", "(Ldh;)V" ) );
	}

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass)
	{
		if ( basicClass == null )
			return null;

		try
		{
			if ( transformedName != null && privateToPublicMethods.containsKey( transformedName ) )
			{
				ClassNode classNode = new ClassNode();
				ClassReader classReader = new ClassReader( basicClass );
				classReader.accept( classNode, 0 );

				for (publicLine Set : privateToPublicMethods.get( transformedName ))
				{
					makePublic( classNode, Set );
				}

				// CALL VIRTUAL!
				if ( transformedName.equals( "net.minecraft.client.gui.inventory.GuiContainer" ) )
				{
					for (MethodNode mn : classNode.methods)
					{
						if ( mn.name.equals( "func_146977_a" ) || (mn.name.equals( "a" ) && mn.desc.equals( "(Lzk;)V" )) )
						{
							MethodNode newNode = new MethodNode( Opcodes.ACC_PUBLIC, "func_146977_a_original", mn.desc, mn.signature, new String[0] );
							newNode.instructions.add( new VarInsnNode( Opcodes.ALOAD, 0 ) );
							newNode.instructions.add( new VarInsnNode( Opcodes.ALOAD, 1 ) );
							newNode.instructions.add( new MethodInsnNode( Opcodes.INVOKESPECIAL, classNode.name, mn.name, mn.desc, false ) );
							newNode.instructions.add( new InsnNode( Opcodes.RETURN ) );
							log( newNode.name + newNode.desc + " - New Method" );
							classNode.methods.add( newNode );
							break;
						}
					}

					for (MethodNode mn : classNode.methods)
					{
						if ( mn.name.equals( "func_73863_a" ) || mn.name.equals( "drawScreen" ) || (mn.name.equals( "a" ) && mn.desc.equals( "(IIF)V" )) )
						{
							Iterator<AbstractInsnNode> i = mn.instructions.iterator();
							while (i.hasNext())
							{
								AbstractInsnNode in = i.next();
								if ( in.getOpcode() == Opcodes.INVOKESPECIAL )
								{
									MethodInsnNode n = (MethodInsnNode) in;
									if ( n.name.equals( "func_146977_a" ) || (n.name.equals( "a" ) && n.desc.equals( "(Lzk;)V" )) )
									{
										log( n.name + n.desc + " - Invoke Virtual" );
										mn.instructions.insertBefore( n, new MethodInsnNode( Opcodes.INVOKEVIRTUAL, n.owner, n.name, n.desc, false ) );
										mn.instructions.remove( in );
										break;
									}
								}
							}
						}
					}
				}

				ClassWriter writer = new ClassWriter( ClassWriter.COMPUTE_MAXS );
				classNode.accept( writer );
				return writer.toByteArray();
			}
		}
		catch (Throwable ignored)
		{
		}

		return basicClass;
	}

	private void log(String string)
	{
		FMLRelaunchLog.log( "AE2-CORE", Level.INFO, string );
	}

	private void makePublic(ClassNode classNode, publicLine set)
	{
		for (MethodNode mn : classNode.methods)
		{
			if ( mn.name.equals( set.name ) && mn.desc.equals( set.desc ) )
			{
				mn.access = (mn.access & (~(Opcodes.ACC_FINAL | Opcodes.ACC_PRIVATE | Opcodes.ACC_PROTECTED))) | Opcodes.ACC_PUBLIC;
				log( mn.name + mn.desc + " - Transformed" );
			}
		}
	}
}
