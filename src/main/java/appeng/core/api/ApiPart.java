/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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

package appeng.core.api;


import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.common.base.Joiner;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.RemappingClassAdapter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;

import appeng.api.parts.CableRenderMode;
import appeng.api.parts.IPartHelper;
import appeng.api.parts.IPartItem;
import appeng.api.parts.LayerBase;
import appeng.client.render.BusRenderer;
import appeng.core.AELog;
import appeng.core.CommonHelper;
import appeng.integration.IntegrationRegistry;
import appeng.integration.IntegrationType;
import appeng.integration.abstraction.IFMP;
import appeng.parts.PartPlacement;
import appeng.tile.networking.TileCableBus;
import appeng.util.Platform;


public class ApiPart implements IPartHelper
{

	private final Map<String, Class> tileImplementations = new HashMap<String, Class>();
	private final Map<Class<?>, String> interfaces2Layer = new HashMap<Class<?>, String>();
	private final Map<String, Class> roots = new HashMap<String, Class>();
	private final List<String> desc = new LinkedList<String>();

	public void initFMPSupport()
	{
		for( Class layerInterface : this.interfaces2Layer.keySet() )
		{
			if( IntegrationRegistry.INSTANCE.isEnabled( IntegrationType.FMP ) )
			{
				( (IFMP) IntegrationRegistry.INSTANCE.getInstance( IntegrationType.FMP ) ).registerPassThrough( layerInterface );
			}
		}
	}

	public Class getCombinedInstance( String base )
	{
		if( this.desc.isEmpty() )
		{
			try
			{
				return Class.forName( base );
			}
			catch( ClassNotFoundException e )
			{
				throw new IllegalStateException( e );
			}
		}

		String description = base + ':' + Joiner.on( ";" ).skipNulls().join( this.desc.iterator() );

		if( this.tileImplementations.get( description ) != null )
		{
			return this.tileImplementations.get( description );
		}

		String f = base;// TileCableBus.class.getName();
		String Addendum = "";
		try
		{
			Addendum = Class.forName( base ).getSimpleName();
		}
		catch( ClassNotFoundException e )
		{
			AELog.error( e );
		}
		Class myCLass;

		try
		{
			myCLass = Class.forName( f );
		}
		catch( ClassNotFoundException e )
		{
			throw new IllegalStateException( e );
		}

		String path = f;

		for( String name : this.desc )
		{
			try
			{
				String newPath = path + ';' + name;
				myCLass = this.getClassByDesc( Addendum, newPath, f, this.interfaces2Layer.get( Class.forName( name ) ) );
				path = newPath;
			}
			catch( Throwable t )
			{
				AELog.warning( "Error loading " + name );
				AELog.error( t );
				// throw new RuntimeException( t );
			}
			f = myCLass.getName();
		}

		this.tileImplementations.put( description, myCLass );

		return myCLass;
	}

	public Class getClassByDesc( String addendum, String fullPath, String root, String next )
	{
		if( this.roots.get( fullPath ) != null )
		{
			return this.roots.get( fullPath );
		}

		ClassWriter cw = new ClassWriter( ClassWriter.COMPUTE_MAXS );
		ClassNode n = this.getReader( next );
		String originalName = n.name;

		try
		{
			n.name = n.name + '_' + addendum;
			n.superName = Class.forName( root ).getName().replace( ".", "/" );
		}
		catch( Throwable t )
		{
			AELog.error( t );
		}

		for( MethodNode mn : n.methods )
		{
			Iterator<AbstractInsnNode> i = mn.instructions.iterator();
			while( i.hasNext() )
			{
				this.processNode( i.next(), n.superName );
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
		Class clazz = this.loadClass( n.name.replace( "/", "." ), byteArray );

		try
		{
			Object fish = clazz.newInstance();
			Class rootC = Class.forName( root );

			boolean hasError = false;

			if( !rootC.isInstance( fish ) )
			{
				hasError = true;
				AELog.severe( "Error, Expected layer to implement " + root + " did not." );
			}

			if( fish instanceof LayerBase )
			{
				hasError = true;
				AELog.severe( "Error, Expected layer to NOT implement LayerBase but it DID." );
			}

			if( !fullPath.contains( ".fmp." ) )
			{
				if( !( fish instanceof TileCableBus ) )
				{
					hasError = true;
					AELog.severe( "Error, Expected layer to implement TileCableBus did not." );
				}

				if( !( fish instanceof TileEntity ) )
				{
					hasError = true;
					AELog.severe( "Error, Expected layer to implement TileEntity did not." );
				}
			}

			if( !hasError )
			{
				AELog.info( "Layer: " + n.name + " loaded successfully - " + size + " bytes" );
			}
		}
		catch( Throwable t )
		{
			AELog.severe( "Layer: " + n.name + " Failed." );
			AELog.error( t );
		}

		this.roots.put( fullPath, clazz );
		return clazz;
	}

	public ClassNode getReader( String name )
	{
		ClassReader cr;
		String path = '/' + name.replace( ".", "/" ) + ".class";
		InputStream is = this.getClass().getResourceAsStream( path );
		try
		{
			cr = new ClassReader( is );

			ClassNode cn = new ClassNode();
			cr.accept( cn, ClassReader.EXPAND_FRAMES );

			return cn;
		}
		catch( IOException e )
		{
			throw new IllegalStateException( "Error loading " + name, e );
		}
	}

	private void processNode( AbstractInsnNode next, String nePar )
	{
		if( next instanceof MethodInsnNode )
		{
			MethodInsnNode min = (MethodInsnNode) next;
			if( min.owner.equals( "appeng/api/parts/LayerBase" ) )
			{
				min.owner = nePar;
			}
		}
	}

	private Class loadClass( String name, byte[] b )
	{
		// override classDefine (as it is protected) and define the class.
		Class clazz = null;
		try
		{
			ClassLoader loader = this.getClass().getClassLoader();// ClassLoader.getSystemClassLoader();
			Class<ClassLoader> root = ClassLoader.class;
			Class<? extends ClassLoader> cls = loader.getClass();
			Method defineClassMethod = root.getDeclaredMethod( "defineClass", String.class, byte[].class, int.class, int.class );
			Method runTransformersMethod = cls.getDeclaredMethod( "runTransformers", String.class, String.class, byte[].class );

			runTransformersMethod.setAccessible( true );
			defineClassMethod.setAccessible( true );
			try
			{
				Object[] argsA = new Object[] { name, name, b };
				b = (byte[]) runTransformersMethod.invoke( loader, argsA );

				Object[] args = new Object[] { name, b, 0, b.length };
				clazz = (Class) defineClassMethod.invoke( loader, args );
			}
			finally
			{
				runTransformersMethod.setAccessible( false );
				defineClassMethod.setAccessible( false );
			}
		}
		catch( Exception e )
		{
			AELog.error( e );
			throw new IllegalStateException( "Unable to manage part API.", e );
		}
		return clazz;
	}

	@Override
	public boolean registerNewLayer( String layer, String layerInterface )
	{
		try
		{
			final Class<?> layerInterfaceClass = Class.forName( layerInterface );
			if( this.interfaces2Layer.get( layerInterfaceClass ) == null )
			{
				this.interfaces2Layer.put( layerInterfaceClass, layer );
				this.desc.add( layerInterface );
				return true;
			}
			else
			{
				AELog.info( "Layer " + layer + " not registered, " + layerInterface + " already has a layer." );
			}
		}
		catch( Throwable ignored )
		{
		}

		return false;
	}

	@Override
	public void setItemBusRenderer( IPartItem i )
	{
		if( Platform.isClient() && i instanceof Item )
		{
			MinecraftForgeClient.registerItemRenderer( (Item) i, BusRenderer.INSTANCE );
		}
	}

	@Override
	public boolean placeBus( ItemStack is, int x, int y, int z, int side, EntityPlayer player, World w )
	{
		return PartPlacement.place( is, x, y, z, side, player, w, PartPlacement.PlaceType.PLACE_ITEM, 0 );
	}

	@Override
	public CableRenderMode getCableRenderMode()
	{
		return CommonHelper.proxy.getRenderMode();
	}

	static class DefaultPackageClassNameRemapper extends Remapper
	{

		public final HashMap<String, String> inputOutput = new HashMap<String, String>();

		@Override
		public String map( String typeName )
		{
			String o = this.inputOutput.get( typeName );
			if( o == null )
			{
				return typeName;
			}
			return o;
		}
	}
}
