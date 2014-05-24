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

	Multimap<String, String> privateToPublicMethods = HashMultimap.create();

	public ASMTweaker() {
		privateToPublicMethods.put( "net.minecraft.client.gui.inventory.GuiContainer", "func_146977_a" );
		privateToPublicMethods.put( "net.minecraft.client.gui.inventory.GuiContainer", "a" );
	}

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass)
	{
		if ( basicClass == null )
			return basicClass;
		
		try
		{
			if ( transformedName != null && privateToPublicMethods.containsKey( transformedName ) )
			{
				ClassNode classNode = new ClassNode();
				ClassReader classReader = new ClassReader( basicClass );
				classReader.accept( classNode, 0 );

				for (String Set : privateToPublicMethods.get( transformedName ))
				{
					makePublic( classNode, Set );
				}

				// CALL VIRUAL!
				if ( transformedName.equals( "net.minecraft.client.gui.inventory.GuiContainer" ) )
				{
					for (MethodNode mn : classNode.methods)
					{
						if ( mn.name.equals( "func_146977_a" ) || (mn.name.equals( "a" ) && mn.desc.equals( "(Lzk;)V" )) )
						{
							MethodNode newNode = new MethodNode( Opcodes.ACC_PUBLIC, "func_146977_a_original", mn.desc, mn.signature, new String[0] );
							newNode.instructions.add( new VarInsnNode( Opcodes.ALOAD, 0 ) );
							newNode.instructions.add( new VarInsnNode( Opcodes.ALOAD, 1 ) );
							newNode.instructions.add( new MethodInsnNode( Opcodes.INVOKESPECIAL, classNode.name, mn.name, mn.desc ) );
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
										mn.instructions.insertBefore( n, new MethodInsnNode( Opcodes.INVOKEVIRTUAL, n.owner, n.name, n.desc ) );
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
		catch (Throwable t)
		{
		}

		return basicClass;
	}

	private void log(String string)
	{
		FMLRelaunchLog.log( "AE2-CORE", Level.INFO, string );
	}

	private void makePublic(ClassNode classNode, String set)
	{
		for (MethodNode mn : classNode.methods)
		{
			if ( mn.name.equals( set ) )
			{
				mn.access = Opcodes.ACC_PUBLIC;
				log( mn.name + mn.desc + " - Public" );
			}
		}
	}

}
