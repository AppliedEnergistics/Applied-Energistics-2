package appeng.transformer.asm;

import java.io.InputStream;
import java.util.Iterator;

import net.minecraft.item.ItemStack;
import net.minecraft.launchwrapper.IClassTransformer;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import appeng.migration.IItemMigrate;

public class ASMMigration implements IClassTransformer
{

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass)
	{
		try
		{

			if ( transformedName != null && transformedName.equals( "net.minecraft.item.ItemStack" ) )
			{
				ClassNode classNode = new ClassNode();
				ClassReader classReader = new ClassReader( basicClass );
				classReader.accept( classNode, 0 );

				ClassNode srcNode = new ClassNode();
				InputStream is = getClass().getResourceAsStream( "/appeng/transformer/template/ItemStackTemplate.class" );
				ClassReader srcReader = new ClassReader( is );
				srcReader.accept( srcNode, 0 );

				// MD: net/minecraft/item/ItemStack/readFromNBT (Lnet/minecraft/nbt/NBTTagCompound;)V
				// abp/c (Ldg;)V
				for (MethodNode mn : classNode.methods)
				{
					boolean signatureMatch = mn.desc.equals( "(Ldg;)V" ) || mn.desc.equals( "(Lnet/minecraft/nbt/NBTTagCompound;)V" );
					boolean nameMatch = mn.name.equals( "readFromNBT" ) || mn.name.equals( "c" ) || mn.name.equals( "func_77963_c" );

					if ( nameMatch && signatureMatch )
					{
						for (MethodNode smn : srcNode.methods)
						{
							if ( smn.name.equals( "readFromNBT" ) )
								handleChunkAddition( classNode, srcNode.name, mn, smn, false );
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
			t.printStackTrace();
		}

		return basicClass;
	}

	private void handleChunkAddition(ClassNode classNode, String from, MethodNode tmn, MethodNode mn, boolean atbeginning)
	{
		Iterator<AbstractInsnNode> i = mn.instructions.iterator();
		while (i.hasNext())
		{
			processNode( i.next(), from, classNode.name );
		}

		Iterator<AbstractInsnNode> g = mn.instructions.iterator();
		while (g.hasNext())
		{
			AbstractInsnNode ain = g.next();
			if ( ain instanceof LineNumberNode )
				g.remove();
			else if ( ain instanceof LabelNode )
				g.remove();
		}

		AbstractInsnNode finalReturn = mn.instructions.getLast();
		while (!isReturn( finalReturn.getOpcode() ))
		{
			mn.instructions.remove( finalReturn );
			finalReturn = mn.instructions.getLast();
		}
		mn.instructions.remove( finalReturn );

		if ( atbeginning )
			tmn.instructions.insert( mn.instructions );
		else
		{
			AbstractInsnNode node = tmn.instructions.getLast();

			while (!isReturn( node.getOpcode() ))
				node = node.getPrevious();

			tmn.instructions.insertBefore( node.getPrevious(), mn.instructions );
		}
	}

	private boolean isReturn(int opcode)
	{
		switch (opcode)
		{
		case Opcodes.ARETURN:
		case Opcodes.DRETURN:
		case Opcodes.FRETURN:
		case Opcodes.LRETURN:
		case Opcodes.IRETURN:
		case Opcodes.RETURN:
			return true;
		}
		return false;
	}

	private void processNode(AbstractInsnNode next, String from, String nePar)
	{
		if ( next instanceof FieldInsnNode )
		{
			FieldInsnNode min = (FieldInsnNode) next;
			if ( min.owner.equals( from ) )
			{
				min.owner = nePar;
			}
		}
		if ( next instanceof MethodInsnNode )
		{
			MethodInsnNode min = (MethodInsnNode) next;
			if ( min.owner.equals( from ) )
			{
				min.owner = nePar;
			}
		}
	}

	public static void handleMigration(Object itemStackTemplate)
	{
		ItemStack is = (ItemStack) itemStackTemplate;
		if ( is.getItem() != null && is.getItem() instanceof IItemMigrate )
			((IItemMigrate) is.getItem()).modifyItemStack( (ItemStack) itemStackTemplate );
	}
}