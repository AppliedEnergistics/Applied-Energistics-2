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
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import appeng.api.parts.CableRenderMode;
import appeng.api.parts.IPartHelper;
import appeng.api.parts.LayerBase;
import appeng.core.AELog;
import appeng.core.CommonHelper;
import appeng.integration.IntegrationRegistry;
import appeng.integration.IntegrationType;
import appeng.integration.abstraction.IFMP;
import appeng.parts.PartPlacement;
import appeng.tile.networking.TileCableBus;


public class ApiPart implements IPartHelper
{

	private final Map<String, Class> tileImplementations = new HashMap<String, Class>();
	private final Map<Class<?>, String> interfaces2Layer = new HashMap<Class<?>, String>();
	private final Map<String, Class> roots = new HashMap<String, Class>();
	private final List<String> desc = new LinkedList<String>();

	public void initFMPSupport()
	{
		for( final Class layerInterface : this.interfaces2Layer.keySet() )
		{
			if( IntegrationRegistry.INSTANCE.isEnabled( IntegrationType.FMP ) )
			{
				( (IFMP) IntegrationRegistry.INSTANCE.getInstance( IntegrationType.FMP ) ).registerPassThrough( layerInterface );
			}
		}
	}

	public Class getCombinedInstance( final String base )
	{
		if( this.desc.isEmpty() )
		{
			try
			{
				return Class.forName( base );
			}
			catch( final ClassNotFoundException e )
			{
				throw new IllegalStateException( e );
			}
		}

		final String description = base + ':' + Joiner.on( ";" ).skipNulls().join( this.desc.iterator() );

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
		catch( final ClassNotFoundException e )
		{
			AELog.debug( e );
		}
		Class myCLass;

		try
		{
			myCLass = Class.forName( f );
		}
		catch( final ClassNotFoundException e )
		{
			throw new IllegalStateException( e );
		}

		String path = f;

		for( final String name : this.desc )
		{
			try
			{
				final String newPath = path + ';' + name;
				myCLass = this.getClassByDesc( Addendum, newPath, f, this.interfaces2Layer.get( Class.forName( name ) ) );
				path = newPath;
			}
			catch( final Throwable t )
			{
				AELog.warn( "Error loading " + name );
				AELog.debug( t );
				// throw new RuntimeException( t );
			}
			f = myCLass.getName();
		}

		this.tileImplementations.put( description, myCLass );

		return myCLass;
	}

	private Class getClassByDesc( final String addendum, final String fullPath, final String root, final String next )
	{
		if( this.roots.get( fullPath ) != null )
		{
			return this.roots.get( fullPath );
		}

		final ClassWriter cw = new ClassWriter( ClassWriter.COMPUTE_MAXS );
		final ClassNode n = this.getReader( next );
		final String originalName = n.name;

		try
		{
			n.name = n.name + '_' + addendum;
			n.superName = Class.forName( root ).getName().replace( ".", "/" );
		}
		catch( final Throwable t )
		{
			AELog.debug( t );
		}

		for( final MethodNode mn : n.methods )
		{
			final Iterator<AbstractInsnNode> i = mn.instructions.iterator();
			while( i.hasNext() )
			{
				this.processNode( i.next(), n.superName );
			}
		}

		final DefaultPackageClassNameRemapper remapper = new DefaultPackageClassNameRemapper();
		remapper.inputOutput.put( "appeng/api/parts/LayerBase", n.superName );
		remapper.inputOutput.put( originalName, n.name );
		n.accept( new RemappingClassAdapter( cw, remapper ) );
		// n.accept( cw );

		// n.accept( new TraceClassVisitor( new PrintWriter( System.out ) ) );
		final byte[] byteArray = cw.toByteArray();
		final int size = byteArray.length;
		final Class clazz = this.loadClass( n.name.replace( "/", "." ), byteArray );

		try
		{
			final Object fish = clazz.newInstance();
			final Class rootC = Class.forName( root );

			boolean hasError = false;

			if( !rootC.isInstance( fish ) )
			{
				hasError = true;
				AELog.error( "Error, Expected layer to implement " + root + " did not." );
			}

			if( fish instanceof LayerBase )
			{
				hasError = true;
				AELog.error( "Error, Expected layer to NOT implement LayerBase but it DID." );
			}

			if( !fullPath.contains( ".fmp." ) )
			{
				if( !( fish instanceof TileCableBus ) )
				{
					hasError = true;
					AELog.error( "Error, Expected layer to implement TileCableBus did not." );
				}

				if( !( fish instanceof TileEntity ) )
				{
					hasError = true;
					AELog.error( "Error, Expected layer to implement TileEntity did not." );
				}
			}

			if( !hasError )
			{
				AELog.info( "Layer: " + n.name + " loaded successfully - " + size + " bytes" );
			}
		}
		catch( final Throwable t )
		{
			AELog.error( "Layer: " + n.name + " Failed." );
			AELog.debug( t );
		}

		this.roots.put( fullPath, clazz );
		return clazz;
	}

	private ClassNode getReader( final String name )
	{
		final String path = '/' + name.replace( ".", "/" ) + ".class";
		final InputStream is = this.getClass().getResourceAsStream( path );
		try
		{
			final ClassReader cr = new ClassReader( is );

			final ClassNode cn = new ClassNode();
			cr.accept( cn, ClassReader.EXPAND_FRAMES );

			return cn;
		}
		catch( final IOException e )
		{
			throw new IllegalStateException( "Error loading " + name, e );
		}
	}

	private void processNode( final AbstractInsnNode next, final String nePar )
	{
		if( next instanceof MethodInsnNode )
		{
			final MethodInsnNode min = (MethodInsnNode) next;
			if( min.owner.equals( "appeng/api/parts/LayerBase" ) )
			{
				min.owner = nePar;
			}
		}
	}

	private Class loadClass( final String name, byte[] b )
	{
		// override classDefine (as it is protected) and define the class.
		Class clazz = null;
		try
		{
			final ClassLoader loader = this.getClass().getClassLoader();// ClassLoader.getSystemClassLoader();
			final Class<ClassLoader> root = ClassLoader.class;
			final Class<? extends ClassLoader> cls = loader.getClass();
			final Method defineClassMethod = root.getDeclaredMethod( "defineClass", String.class, byte[].class, int.class, int.class );
			final Method runTransformersMethod = cls.getDeclaredMethod( "runTransformers", String.class, String.class, byte[].class );

			runTransformersMethod.setAccessible( true );
			defineClassMethod.setAccessible( true );
			try
			{
				final Object[] argsA = { name, name, b };
				b = (byte[]) runTransformersMethod.invoke( loader, argsA );

				final Object[] args = { name, b, 0, b.length };
				clazz = (Class) defineClassMethod.invoke( loader, args );
			}
			finally
			{
				runTransformersMethod.setAccessible( false );
				defineClassMethod.setAccessible( false );
			}
		}
		catch( final Exception e )
		{
			AELog.debug( e );
			throw new IllegalStateException( "Unable to manage part API.", e );
		}
		return clazz;
	}

	@Override
	public boolean registerNewLayer( final String layer, final String layerInterface )
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
		catch( final Throwable ignored )
		{
		}

		return false;
	}

	@Override
	public EnumActionResult placeBus( final ItemStack is, final BlockPos pos, final EnumFacing side, final EntityPlayer player, final EnumHand hand, final World w )
	{
		return PartPlacement.place( is, pos, side, player, hand, w, PartPlacement.PlaceType.PLACE_ITEM, 0 );
	}

	@Override
	public CableRenderMode getCableRenderMode()
	{
		return CommonHelper.proxy.getRenderMode();
	}

	private static class DefaultPackageClassNameRemapper extends Remapper
	{

		private final HashMap<String, String> inputOutput = new HashMap<String, String>();

		@Override
		public String map( final String typeName )
		{
			final String o = this.inputOutput.get( typeName );
			if( o == null )
			{
				return typeName;
			}
			return o;
		}
	}
}
