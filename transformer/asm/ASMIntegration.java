package appeng.transformer.asm;

import java.util.Iterator;
import java.util.List;

import net.minecraft.launchwrapper.IClassTransformer;

import org.apache.logging.log4j.Level;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import appeng.integration.IntegrationRegistry;
import appeng.integration.IntegrationSide;
import appeng.transformer.annotations.integration;
import cpw.mods.fml.relauncher.FMLRelaunchLog;

public class ASMIntegration implements IClassTransformer
{

	private IntegrationRegistry integrationModules = new IntegrationRegistry();

	public ASMIntegration() {

		/**
		 * Side, Display Name, ModID ClassPostFix
		 */

		integrationModules.add( IntegrationSide.BOTH, "Rotary Craft", "RotaryCraft", "RotaryCraft" );
		integrationModules.add( IntegrationSide.BOTH, "Industrial Craft 2", "IC2", "IC2" );
		integrationModules.add( IntegrationSide.BOTH, "Railcraft", "Railcraft", "RC" );
		// integrationModules.add( IntegrationSide.BOTH, "Thermal Expansion", "ThermalExpansion", "TE" );
		// integrationModules.add( IntegrationSide.BOTH, "Mystcraft", "Mystcraft", "Mystcraft" );
		integrationModules.add( IntegrationSide.BOTH, "BuildCraft", "BuildCraft|Silicon", "BC" );
		integrationModules.add( IntegrationSide.BOTH, "BuildCraft5 Power", null, "MJ5" );
		integrationModules.add( IntegrationSide.BOTH, "BuildCraft6 Power", null, "MJ6" );
		integrationModules.add( IntegrationSide.BOTH, "RedstoneFlux Power", null, "RF" );
		// integrationModules.add( IntegrationSide.BOTH, "Greg Tech", "gregtech_addon", "GT" );
		// integrationModules.add( IntegrationSide.BOTH, "Universal Electricity", null, "UE" );
		// integrationModules.add( IntegrationSide.BOTH, "Logistics Pipes", "LogisticsPipes|Main", "LP" );
		integrationModules.add( IntegrationSide.CLIENT, "Inventory Tweaks", "", "InvTweaks" );
		integrationModules.add( IntegrationSide.BOTH, "Mine Factory Reloaded", "MineFactoryReloaded", "MFR" );
		integrationModules.add( IntegrationSide.BOTH, "Deep Storage Unit", null, "DSU" );
		// integrationModules.add( IntegrationSide.BOTH, "Better Storage", "betterstorage" );
		integrationModules.add( IntegrationSide.BOTH, "Factorization", "factorization", "FZ" );
		// integrationModules.add( IntegrationSide.BOTH, "Forestry", "Forestry", "Forestry" );
		// integrationModules.add( IntegrationSide.BOTH, "Mekanism", "Mekanism", "Mekanism" );
		integrationModules.add( IntegrationSide.CLIENT, "Waila", "Waila", "Waila" );
		integrationModules.add( IntegrationSide.BOTH, "Colored Lights Core", "coloredlightscore", "CLApi" );
		integrationModules.add( IntegrationSide.BOTH, "Rotatable Blocks", "RotatableBlocks", "RB" );
		integrationModules.add( IntegrationSide.CLIENT, "Inventory Tweaks", "inventorytweaks", "InvTweaks" );
		integrationModules.add( IntegrationSide.CLIENT, "Not Enough Items", "NotEnoughItems", "NEI" );
		integrationModules.add( IntegrationSide.CLIENT, "Craft Guide", "craftguide", "CraftGuide" );
		integrationModules.add( IntegrationSide.BOTH, "Forge MultiPart", "McMultipart", "FMP" );
	}

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass)
	{
		if ( basicClass == null || transformedName.startsWith( "appeng.transformer" ) )
			return basicClass;

		if ( transformedName.startsWith( "appeng." ) )
		{
			// log( "Found " + transformedName );

			ClassNode classNode = new ClassNode();
			ClassReader classReader = new ClassReader( basicClass );
			classReader.accept( classNode, 0 );

			try
			{
				boolean reWrite = removeOptionals( classNode );

				if ( reWrite )
				{
					ClassWriter writer = new ClassWriter( ClassWriter.COMPUTE_MAXS );
					classNode.accept( writer );
					return writer.toByteArray();
				}
			}
			catch (Throwable t)
			{
				t.printStackTrace();
			}
		}
		return basicClass;
	}

	private boolean removeOptionals(ClassNode classNode)
	{
		boolean changed = false;

		if ( classNode.visibleAnnotations != null )
		{
			for (AnnotationNode an : classNode.visibleAnnotations)
			{
				if ( hasAnnotation( an, integration.Interface.class ) )
				{
					if ( stripInterface( classNode, integration.Interface.class, an ) )
						changed = true;
				}
				else if ( hasAnnotation( an, integration.InterfaceList.class ) )
				{
					for (Object o : ((List) an.values.get( 1 )))
					{
						if ( stripInterface( classNode, integration.InterfaceList.class, (AnnotationNode) o ) )
							changed = true;
					}
				}
			}
		}

		Iterator<MethodNode> i = classNode.methods.iterator();
		while (i.hasNext())
		{
			MethodNode mn = i.next();

			if ( mn.visibleAnnotations != null )
			{
				for (AnnotationNode an : mn.visibleAnnotations)
				{
					if ( hasAnnotation( an, integration.Method.class ) )
					{
						if ( stripMethod( classNode, mn, i, integration.Method.class, an ) )
							changed = true;
					}
				}

			}
		}

		if ( changed )
			log( "Updated " + classNode.name );

		return changed;
	}

	private boolean hasAnnotation(AnnotationNode ann, Class anno)
	{
		return ann.desc.equals( Type.getDescriptor( anno ) );
	}

	private boolean stripMethod(ClassNode classNode, MethodNode mn, Iterator<MethodNode> i, Class class1, AnnotationNode an)
	{
		if ( an.values.size() != 2 )
			throw new RuntimeException( "Unable to handle Method annotation on " + classNode.name );

		String iName = null;

		if ( an.values.get( 0 ).equals( "iname" ) )
			iName = (String) an.values.get( 1 );

		if ( iName != null )
		{
			if ( !IntegrationRegistry.instance.isEnabled( iName ) )
			{
				log( "Removing Method " + mn.name + " from " + classNode.name + " because " + iName + " integration is disabled." );
				i.remove();
				return true;
			}
			else
				log( "Allowing Method " + mn.name + " from " + classNode.name + " because " + iName + " integration is enabled." );
		}
		else
			throw new RuntimeException( "Unable to handle Method annotation on " + classNode.name );

		return false;
	}

	private boolean stripInterface(ClassNode classNode, Class class1, AnnotationNode an)
	{
		if ( an.values.size() != 4 )
			throw new RuntimeException( "Unable to handle Interface annotation on " + classNode.name );

		String iFace = null;
		String iName = null;

		if ( an.values.get( 0 ).equals( "iface" ) )
			iFace = (String) an.values.get( 1 );
		else if ( an.values.get( 2 ).equals( "iface" ) )
			iFace = (String) an.values.get( 3 );

		if ( an.values.get( 0 ).equals( "iname" ) )
			iName = (String) an.values.get( 1 );
		else if ( an.values.get( 2 ).equals( "iname" ) )
			iName = (String) an.values.get( 3 );

		if ( iName != null && iFace != null )
		{
			if ( !IntegrationRegistry.instance.isEnabled( iName ) )
			{
				log( "Removing Interface " + iFace + " from " + classNode.name + " because " + iName + " integration is disabled." );
				classNode.interfaces.remove( iFace.replace( '.', '/' ) );
				return true;
			}
			else
				log( "Allowing Interface " + iFace + " from " + classNode.name + " because " + iName + " integration is enabled." );
		}
		else
			throw new RuntimeException( "Unable to handle Method annotation on " + classNode.name );

		return false;
	}

	private void log(String string)
	{
		FMLRelaunchLog.log( "AE2-CORE", Level.INFO, string );
	}

}
