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

package appeng.integration.modules;


import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

import cpw.mods.fml.common.eventhandler.Event;

import codechicken.lib.vec.BlockCoord;
import codechicken.microblock.BlockMicroMaterial;
import codechicken.multipart.MultiPartRegistry;
import codechicken.multipart.MultiPartRegistry.IPartConverter;
import codechicken.multipart.MultiPartRegistry.IPartFactory;
import codechicken.multipart.MultipartGenerator;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TileMultipart;

import appeng.api.AEApi;
import appeng.api.definitions.IBlockDefinition;
import appeng.api.definitions.IBlocks;
import appeng.api.parts.IPartHost;
import appeng.core.AELog;
import appeng.fmp.CableBusPart;
import appeng.fmp.FMPEvent;
import appeng.fmp.FMPPlacementHelper;
import appeng.fmp.PartRegistry;
import appeng.integration.IIntegrationModule;
import appeng.integration.abstraction.IFMP;
import appeng.integration.modules.helpers.FMPPacketEvent;
import appeng.parts.CableBusContainer;


public class FMP implements IIntegrationModule, IPartFactory, IPartConverter, IFMP
{

	public static FMP instance;

	@Override
	public TMultiPart createPart( String name, boolean client )
	{
		for( PartRegistry pr : PartRegistry.values() )
		{
			if( pr.getName().equals( name ) )
			{
				return pr.construct( 0 );
			}
		}

		return null;
	}

	@Override
	public TMultiPart convert( World world, BlockCoord pos )
	{
		Block blk = world.getBlock( pos.x, pos.y, pos.z );
		int meta = world.getBlockMetadata( pos.x, pos.y, pos.z );

		TMultiPart part = PartRegistry.getPartByBlock( blk, meta );
		if( part instanceof CableBusPart )
		{
			CableBusPart cbp = (CableBusPart) part;
			cbp.convertFromTile( world.getTileEntity( pos.x, pos.y, pos.z ) );
		}

		return part;
	}

	@Override
	public Iterable<Block> blockTypes()
	{
		final IBlocks blocks = AEApi.instance().definitions().blocks();
		final List<Block> blockTypes = Lists.newArrayListWithCapacity( 2 );

		this.addBlockTypes( blockTypes, blocks.multiPart() );
		this.addBlockTypes( blockTypes, blocks.quartzTorch() );

		return blockTypes;
	}

	private void addBlockTypes( Collection<Block> blockTypes, IBlockDefinition definition )
	{
		for( Block block : definition.maybeBlock().asSet() )
		{
			blockTypes.add( block );
		}
	}

	@Override
	public void preInit()
	{

	}

	@Override
	public void init() throws Throwable
	{
		final IBlocks blocks = AEApi.instance().definitions().blocks();

		this.createAndRegister( blocks.quartz(), 0 );
		this.createAndRegister( blocks.quartzPillar(), 0 );
		this.createAndRegister( blocks.quartzChiseled(), 0 );
		this.createAndRegister( blocks.skyStone(), 0 );
		this.createAndRegister( blocks.skyStone(), 1 );
		this.createAndRegister( blocks.skyStone(), 2 );
		this.createAndRegister( blocks.skyStone(), 3 );

		PartRegistry[] reg = PartRegistry.values();

		String[] data = new String[reg.length];
		for( int x = 0; x < data.length; x++ )
		{
			data[x] = reg[x].getName();
		}

		MultiPartRegistry.registerConverter( this );
		MultiPartRegistry.registerParts( this, data );

		MultipartGenerator.registerPassThroughInterface( "appeng.helpers.AEMultiTile" );
	}

	private void createAndRegister( IBlockDefinition definition, int i )
	{
		for( Block block : definition.maybeBlock().asSet() )
		{
			BlockMicroMaterial.createAndRegister( block, i );
		}
	}

	@Override
	public void postInit()
	{
		MinecraftForge.EVENT_BUS.register( new FMPEvent() );
	}

	@Override
	public IPartHost getOrCreateHost( TileEntity tile )
	{
		try
		{
			BlockCoord loc = new BlockCoord( tile.xCoord, tile.yCoord, tile.zCoord );

			TileMultipart mp = TileMultipart.getOrConvertTile( tile.getWorldObj(), loc );
			if( mp != null )
			{
				scala.collection.Iterator<TMultiPart> i = mp.partList().iterator();
				while( i.hasNext() )
				{
					TMultiPart p = i.next();
					if( p instanceof CableBusPart )
					{
						return (IPartHost) p;
					}
				}

				return new FMPPlacementHelper( mp );
			}
		}
		catch( Throwable t )
		{
			AELog.error( t );
		}
		return null;
	}

	@Override
	public CableBusContainer getCableContainer( TileEntity te )
	{
		if( te instanceof TileMultipart )
		{
			TileMultipart mp = (TileMultipart) te;
			scala.collection.Iterator<TMultiPart> i = mp.partList().iterator();
			while( i.hasNext() )
			{
				TMultiPart p = i.next();
				if( p instanceof CableBusPart )
				{
					return ( (CableBusPart) p ).cb;
				}
			}
		}
		return null;
	}

	@Override
	public void registerPassThrough( Class<?> layerInterface )
	{
		try
		{
			MultipartGenerator.registerPassThroughInterface( layerInterface.getName() );
		}
		catch( Throwable t )
		{
			AELog.severe( "Failed to register " + layerInterface.getName() + " with FMP, some features may not work with MultiParts." );
			AELog.error( t );
		}
	}

	@Override
	public Event newFMPPacketEvent( EntityPlayerMP sender )
	{
		return new FMPPacketEvent( sender );
	}
}
