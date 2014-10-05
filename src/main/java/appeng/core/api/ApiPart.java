package appeng.core.api;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.*;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.RemappingClassAdapter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import appeng.api.parts.CableRenderMode;
import appeng.api.parts.IPartHelper;
import appeng.api.parts.IPartItem;
import appeng.api.parts.LayerBase;
import appeng.client.render.BusRenderer;
import appeng.core.AELog;
import appeng.core.AppEng;
import appeng.core.CommonHelper;
import appeng.integration.IntegrationType;
import appeng.integration.abstraction.IFMP;
import appeng.parts.PartPlacement;
import appeng.tile.networking.TileCableBus;
import appeng.util.Platform;

import com.google.common.base.Joiner;

public class ApiPart implements IPartHelper
{

	private final Map<String, Class> TileImplementations = new HashMap<String, Class>();
	private final Map<Class, String> interfaces2Layer = new HashMap<Class, String>();
	private final Map<String, Class> roots = new HashMap<String, Class>();
	private final List<String> desc = new LinkedList<String>();

	public void initFMPSupport()
	{
		for (Class layerInterface : interfaces2Layer.keySet())
		{
			if ( AppEng.instance.isIntegrationEnabled( IntegrationType.FMP ) )
				((IFMP) AppEng.instance.getIntegration( IntegrationType.FMP )).registerPassThrough( layerInterface );
		}
	}

	private Class loadClass(String Name, byte[] b)
	{
		// override classDefine (as it is protected) and define the class.
		Class clazz = null;
		try
		{
			ClassLoader loader = getClass().getClassLoader();// ClassLoader.getSystemClassLoader();
			Class root = ClassLoader.class;
			Class cls = loader.getClass();
			Method defineClassMethod = root.getDeclaredMethod( "defineClass", new Class[] { String.class, byte[].class, int.class, int.class } );
			Method runTransformersMethod = cls.getDeclaredMethod( "runTransformers", new Class[] { String.class, String.class, byte[].class } );

			runTransformersMethod.setAccessible( true );
			defineClassMethod.setAccessible( true );
			try
			{
				Object[] argsA = new Object[] { Name, Name, b };
				b = (byte[]) runTransformersMethod.invoke( loader, argsA );

				Object[] args = new Object[] { Name, b, 0, b.length };
				clazz = (Class) defineClassMethod.invoke( loader, args );
			}
			finally
			{
				runTransformersMethod.setAccessible( false );
				defineClassMethod.setAccessible( false );
			}
		}
		catch (Exception e)
		{
			AELog.error( e );
			throw new RuntimeException( "Unable to manage part API.", e );
		}
		return clazz;
	}

	public ClassNode getReader(String name)
	{
		try
		{
			ClassReader cr;
			String path = "/" + name.replace( ".", "/" ) + ".class";
			InputStream is = getClass().getResourceAsStream( path );
			cr = new ClassReader( is );
			ClassNode cn = new ClassNode();
			cr.accept( cn, ClassReader.EXPAND_FRAMES );
			return cn;
		}
		catch (Throwable t)
		{
			throw new RuntimeException( "Error loading " + name, t );
		}
	}

	public Class getCombinedInstance(String base)
	{
		if ( desc.size() == 0 )
		{
			try
			{
				return Class.forName( base );
			}
			catch (Throwable t)
			{
				throw new RuntimeException( t );
			}
		}

		String description = base + ":" + Joiner.on( ";" ).skipNulls().join( desc.iterator() );

		if ( TileImplementations.get( description ) != null )
		{
			try
			{
				return TileImplementations.get( description );
			}
			catch (Throwable t)
			{
				throw new RuntimeException( t );
			}
		}

		String f = base;// TileCableBus.class.getName();
		String Addendum = "";
		try
		{
			Addendum = Class.forName( base ).getSimpleName();
		}
		catch (ClassNotFoundException e)
		{
			AELog.error( e );
		}
		Class myCLass;

		try
		{
			myCLass = Class.forName( f );
		}
		catch (Throwable t)
		{
			throw new RuntimeException( t );
		}

		String path = f;

		for (String name : desc)
		{
			try
			{
				String newPath = path + ";" + name;
				myCLass = getClassByDesc( Addendum, newPath, f, interfaces2Layer.get( Class.forName( name ) ) );
				path = newPath;
			}
			catch (Throwable t)
			{
				AELog.warning( "Error loading " + name );
				AELog.error( t );
				// throw new RuntimeException( t );
			}
			f = myCLass.getName();
		}

		TileImplementations.put( description, myCLass );

		try
		{
			return myCLass;
		}
		catch (Throwable t)
		{
			throw new RuntimeException( t );
		}
	}

	static class DefaultPackageClassNameRemapper extends Remapper
	{

		public final HashMap<String, String> inputOutput = new HashMap<String, String>();

		@Override
		public String map(String typeName)
		{
			String o = inputOutput.get( typeName );
			if ( o == null )
				return typeName;
			return o;
		}

	}

	public Class getClassByDesc(String Addendum, String fullPath, String root, String next)
	{
		if ( roots.get( fullPath ) != null )
			return roots.get( fullPath );

		ClassWriter cw = new ClassWriter( ClassWriter.COMPUTE_MAXS );
		ClassNode n = getReader( next );
		String originalName = n.name;

		try
		{
			n.name = n.name + "_" + Addendum;
			n.superName = Class.forName( root ).getName().replace( ".", "/" );
		}
		catch (Throwable t)
		{
			AELog.error( t );
		}

		for (MethodNode mn : n.methods)
		{
			Iterator<AbstractInsnNode> i = mn.instructions.iterator();
			while (i.hasNext())
			{
				processNode( i.next(), n.superName );
			}
		}

		DefaultPackageClassNameRemapper remapper = new DefaultPackageClassNameRemapper();
		remapper.inputOutput.put( "appeng/api/parts/LayerBase", n.superName );
		remapper.inputOutput.put( originalName, n.name );
		n.accept( new RemappingClassAdapter( cw, remapper ) );
		// n.accept( cw );

		// n.accept( new TraceClassVisitor( new PrintWriter( System.out ) ) );
		byte[] byteArray = cw.toByteArray();
		int size = byteArray.length;
		Class clazz = loadClass( n.name.replace( "/", "." ), byteArray );

		try
		{
			Object fish = clazz.newInstance();
			Class rootC = Class.forName( root );

			boolean hasError = false;

			if ( !rootC.isInstance( fish ) )
			{
				hasError = true;
				AELog.severe( "Error, Expected layer to implement " + root + " did not." );
			}

			if ( fish instanceof LayerBase )
			{
				hasError = true;
				AELog.severe( "Error, Expected layer to NOT implement LayerBase but it DID." );
			}

			if ( !fullPath.contains( ".fmp." ) )
			{
				if ( !(fish instanceof TileCableBus) )
				{
					hasError = true;
					AELog.severe( "Error, Expected layer to implement TileCableBus did not." );
				}

				if ( !(fish instanceof TileEntity) )
				{
					hasError = true;
					AELog.severe( "Error, Expected layer to implement TileEntity did not." );
				}
			}

			if ( !hasError )
			{
				AELog.info( "Layer: " + n.name + " loaded successfully - " + size + " bytes" );
			}

		}
		catch (Throwable t)
		{
			AELog.severe( "Layer: " + n.name + " Failed." );
			AELog.error( t );
		}

		roots.put( fullPath, clazz );
		return clazz;
	}

	private void processNode(AbstractInsnNode next, String nePar)
	{
		if ( next instanceof MethodInsnNode )
		{
			MethodInsnNode min = (MethodInsnNode) next;
			if ( min.owner.equals( "appeng/api/parts/LayerBase" ) )
			{
				min.owner = nePar;
			}
		}
	}

	@Override
	public void setItemBusRenderer(IPartItem i)
	{
		if ( Platform.isClient() && i instanceof Item )
			MinecraftForgeClient.registerItemRenderer( (Item) i, BusRenderer.instance );
	}

	@Override
	public boolean placeBus(ItemStack is, int x, int y, int z, int side, EntityPlayer player, World w)
	{
		return PartPlacement.place( is, x, y, z, side, player, w, PartPlacement.PlaceType.PLACE_ITEM, 0 );
	}

	@Override
	public boolean registerNewLayer(String layer, String layerInterface)
	{
		try
		{
			final Class<?> layerInterfaceClass = Class.forName( layerInterface );
			if ( interfaces2Layer.get( layerInterfaceClass ) == null )
			{
				interfaces2Layer.put( layerInterfaceClass, layer );
				desc.add( layerInterface );
				return true;
			}
			else
				AELog.info( "Layer " + layer + " not registered, " + layerInterface + " already has a layer." );
		}
		catch (Throwable ignored)
		{
		}

		return false;
	}

	@Override
	public CableRenderMode getCableRenderMode()
	{
		return CommonHelper.proxy.getRenderMode();
	}

}
