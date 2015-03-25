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

package appeng.helpers;


import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.oredict.OreDictionary;

import appeng.api.AEApi;
import appeng.core.AEConfig;
import appeng.core.WorldSettings;
import appeng.core.features.AEFeature;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;


public class MeteoritePlacer
{

	final int minBLocks = 200;
	final HashSet<Block> validSpawn = new HashSet<Block>();
	final HashSet<Block> invalidSpawn = new HashSet<Block>();
	final Block skystone = AEApi.instance().blocks().blockSkyStone.block();
	final Block skychest;
	Fallout type = new Fallout();
	double real_sizeOfMeteorite = ( Math.random() * 6.0 ) + 2;
	double realCrater = this.real_sizeOfMeteorite * 2 + 5;
	double sizeOfMeteorite = this.real_sizeOfMeteorite * this.real_sizeOfMeteorite;
	double crater = this.realCrater * this.realCrater;
	NBTTagCompound settings;

	public MeteoritePlacer()
	{

		if( AEApi.instance().blocks().blockSkyChest.block() == null )
			this.skychest = Blocks.chest;
		else
			this.skychest = AEApi.instance().blocks().blockSkyChest.block();

		this.validSpawn.add( Blocks.stone );
		this.validSpawn.add( Blocks.cobblestone );
		this.validSpawn.add( Blocks.grass );
		this.validSpawn.add( Blocks.sand );
		this.validSpawn.add( Blocks.dirt );
		this.validSpawn.add( Blocks.gravel );
		this.validSpawn.add( Blocks.netherrack );
		this.validSpawn.add( Blocks.iron_ore );
		this.validSpawn.add( Blocks.gold_ore );
		this.validSpawn.add( Blocks.diamond_ore );
		this.validSpawn.add( Blocks.redstone_ore );
		this.validSpawn.add( Blocks.hardened_clay );
		this.validSpawn.add( Blocks.ice );
		this.validSpawn.add( Blocks.snow );

		this.invalidSpawn.add( this.skystone );
		this.invalidSpawn.add( Blocks.planks );
		this.invalidSpawn.add( Blocks.iron_door );
		this.invalidSpawn.add( Blocks.iron_bars );
		this.invalidSpawn.add( Blocks.wooden_door );
		this.invalidSpawn.add( Blocks.brick_block );
		this.invalidSpawn.add( Blocks.clay );
		this.invalidSpawn.add( Blocks.water );
		this.invalidSpawn.add( Blocks.log );
		this.invalidSpawn.add( Blocks.log2 );
	}

	public boolean spawnMeteorite( IMeteoriteWorld w, NBTTagCompound meteoriteBlob )
	{
		this.settings = meteoriteBlob;

		int x = this.settings.getInteger( "x" );
		int y = this.settings.getInteger( "y" );
		int z = this.settings.getInteger( "z" );

		this.real_sizeOfMeteorite = this.settings.getDouble( "real_sizeOfMeteorite" );
		this.realCrater = this.settings.getDouble( "realCrater" );
		this.sizeOfMeteorite = this.settings.getDouble( "sizeOfMeteorite" );
		this.crater = this.settings.getDouble( "crater" );

		Block blk = Block.getBlockById( this.settings.getInteger( "blk" ) );

		if( blk == Blocks.sand )
			this.type = new FalloutSand( w, x, y, z );
		else if( blk == Blocks.hardened_clay )
			this.type = new FalloutCopy( w, x, y, z );
		else if( blk == Blocks.ice || blk == Blocks.snow )
			this.type = new FalloutSnow( w, x, y, z );

		int skyMode = this.settings.getInteger( "skyMode" );

		// creator
		if( skyMode > 10 )
			this.placeCrater( w, x, y, z );

		this.placeMeteorite( w, x, y, z );

		// collapse blocks...
		if( skyMode > 3 )
			this.Decay( w, x, y, z );

		w.done();
		return true;
	}

	private void placeCrater( IMeteoriteWorld w, int x, int y, int z )
	{
		boolean lava = this.settings.getBoolean( "lava" );

		int maxY = 255;
		int minX = w.minX( x - 200 );
		int maxX = w.maxX( x + 200 );
		int minZ = w.minZ( z - 200 );
		int maxZ = w.maxZ( z + 200 );

		for( int j = y - 5; j < maxY; j++ )
		{
			boolean changed = false;

			for( int i = minX; i < maxX; i++ )
				for( int k = minZ; k < maxZ; k++ )
				{
					double dx = i - x;
					double dz = k - z;
					double h = y - this.real_sizeOfMeteorite + 1 + this.type.adjustCrater();

					double distanceFrom = dx * dx + dz * dz;

					if( j > h + distanceFrom * 0.02 )
					{
						if( lava && j < y && w.getBlock( x, y - 1, z ).isBlockSolid( w.getWorld(), i, j, k, 0 ) )
						{
							if( j > h + distanceFrom * 0.02 )
								this.put( w, i, j, k, Blocks.lava );
						}
						else
							changed = this.put( w, i, j, k, Platform.AIR ) || changed;
					}
				}
		}

		for( Object o : w.getWorld().getEntitiesWithinAABB( EntityItem.class, AxisAlignedBB.getBoundingBox( w.minX( x - 30 ), y - 5, w.minZ( z - 30 ), w.maxX( x + 30 ), y + 30, w.maxZ( z + 30 ) ) ) )
		{
			Entity e = (Entity) o;
			e.setDead();
		}
	}

	private void placeMeteorite( IMeteoriteWorld w, int x, int y, int z )
	{
		int meteorXLength = w.minX( x - 8 );
		int meteorXHeight = w.maxX( x + 8 );
		int meteorZLength = w.minZ( z - 8 );
		int meteorZHeight = w.maxZ( z + 8 );

		// spawn meteor
		for( int i = meteorXLength; i < meteorXHeight; i++ )
			for( int j = y - 8; j < y + 8; j++ )
				for( int k = meteorZLength; k < meteorZHeight; k++ )
				{
					double dx = i - x;
					double dy = j - y;
					double dz = k - z;

					if( dx * dx * 0.7 + dy * dy * ( j > y ? 1.4 : 0.8 ) + dz * dz * 0.7 < this.sizeOfMeteorite )
						this.put( w, i, j, k, this.skystone );
				}

		if( AEConfig.instance.isFeatureEnabled( AEFeature.SpawnPressesInMeteorites ) )
		{
			this.put( w, x, y, z, this.skychest );
			TileEntity te = w.getTileEntity( x, y, z );
			if( te != null && te instanceof IInventory )
			{
				InventoryAdaptor ap = InventoryAdaptor.getAdaptor( te, ForgeDirection.UP );

				int primary = Math.max( 1, (int) ( Math.random() * 4 ) );

				if( primary > 3 ) // in case math breaks...
					primary = 3;

				for( int zz = 0; zz < primary; zz++ )
				{
					int r = 0;
					boolean duplicate = false;

					do
					{
						duplicate = false;

						if( Math.random() > 0.7 )
							r = WorldSettings.getInstance().getNextOrderedValue( "presses" );
						else
							r = (int) ( Math.random() * 1000 );

						ItemStack toAdd = null;

						switch( r % 4 )
						{
							case 0:
								toAdd = AEApi.instance().materials().materialCalcProcessorPress.stack( 1 );
								break;
							case 1:
								toAdd = AEApi.instance().materials().materialEngProcessorPress.stack( 1 );
								break;
							case 2:
								toAdd = AEApi.instance().materials().materialLogicProcessorPress.stack( 1 );
								break;
							case 3:
								toAdd = AEApi.instance().materials().materialSiliconPress.stack( 1 );
								break;
							default:
						}

						if( toAdd != null )
						{
							if( ap.simulateRemove( 1, toAdd, null ) == null )
								ap.addItems( toAdd );
							else
								duplicate = true;
						}
					}
					while( duplicate );
				}

				int secondary = Math.max( 1, (int) ( Math.random() * 3 ) );
				for( int zz = 0; zz < secondary; zz++ )
				{
					switch( (int) ( Math.random() * 1000 ) % 3 )
					{
						case 0:
							ap.addItems( AEApi.instance().blocks().blockSkyStone.stack( (int) ( Math.random() * 12 ) + 1 ) );
							break;
						case 1:
							List<ItemStack> possibles = new LinkedList<ItemStack>();
							possibles.addAll( OreDictionary.getOres( "nuggetIron" ) );
							possibles.addAll( OreDictionary.getOres( "nuggetCopper" ) );
							possibles.addAll( OreDictionary.getOres( "nuggetTin" ) );
							possibles.addAll( OreDictionary.getOres( "nuggetSilver" ) );
							possibles.addAll( OreDictionary.getOres( "nuggetLead" ) );
							possibles.addAll( OreDictionary.getOres( "nuggetPlatinum" ) );
							possibles.addAll( OreDictionary.getOres( "nuggetNickel" ) );
							possibles.addAll( OreDictionary.getOres( "nuggetAluminium" ) );
							possibles.addAll( OreDictionary.getOres( "nuggetElectrum" ) );
							possibles.add( new ItemStack( net.minecraft.init.Items.gold_nugget ) );

							ItemStack nugget = Platform.pickRandom( possibles );
							if( nugget != null )
							{
								nugget = nugget.copy();
								nugget.stackSize = (int) ( Math.random() * 12 ) + 1;
								ap.addItems( nugget );
							}
							break;
					}
				}
			}
		}
	}

	private void Decay( IMeteoriteWorld w, int x, int y, int z )
	{
		double randomShit = 0;

		int meteorXLength = w.minX( x - 30 );
		int meteorXHeight = w.maxX( x + 30 );
		int meteorZLength = w.minZ( z - 30 );
		int meteorZHeight = w.maxZ( z + 30 );

		for( int i = meteorXLength; i < meteorXHeight; i++ )
			for( int k = meteorZLength; k < meteorZHeight; k++ )
				for( int j = y - 9; j < y + 30; j++ )
				{
					Block blk = w.getBlock( i, j, k );
					if( blk == Blocks.lava )
						continue;

					if( blk.isReplaceable( w.getWorld(), i, j, k ) )
					{
						blk = Platform.AIR;
						Block blk_b = w.getBlock( i, j + 1, k );

						if( blk_b != blk )
						{
							int meta_b = w.getBlockMetadata( i, j + 1, k );

							w.setBlock( i, j, k, blk_b, meta_b, 3 );
							w.setBlock( i, j + 1, k, blk );
						}
						else if( randomShit < 100 * this.crater )
						{
							double dx = i - x;
							double dy = j - y;
							double dz = k - z;
							double dist = dx * dx + dy * dy + dz * dz;

							Block xf = w.getBlock( i, j - 1, k );
							if( !xf.isReplaceable( w.getWorld(), i, j - 1, k ) )
							{
								double extraRange = Math.random() * 0.6;
								double height = this.crater * ( extraRange + 0.2 ) - Math.abs( dist - this.crater * 1.7 );

								if( xf != blk && height > 0 && Math.random() > 0.6 )
								{
									randomShit++;
									this.type.getRandomFall( w, i, j, k );
								}
							}
						}
					}
					else
					{
						// decay.
						Block blk_b = w.getBlock( i, j + 1, k );
						if( blk_b == Platform.AIR )
						{
							if( Math.random() > 0.4 )
							{
								double dx = i - x;
								double dy = j - y;
								double dz = k - z;

								if( dx * dx + dy * dy + dz * dz < this.crater * 1.6 )
								{
									this.type.getRandomInset( w, i, j, k );
								}
							}
						}
					}
				}
	}

	private boolean put( IMeteoriteWorld w, int i, int j, int k, Block blk )
	{
		Block original = w.getBlock( i, j, k );

		if( original == Blocks.bedrock || original == blk )
			return false;

		w.setBlock( i, j, k, blk );
		return true;
	}

	public double getSqDistance( int x, int z )
	{
		int Cx = this.settings.getInteger( "x" ) - x;
		int Cz = this.settings.getInteger( "z" ) - z;

		return Cx * Cx + Cz * Cz;
	}

	public boolean spawnMeteorite( IMeteoriteWorld w, int x, int y, int z )
	{
		int validBlocks = 0;

		if( !w.hasNoSky() )
			return false;

		Block blk = w.getBlock( x, y, z );
		if( !this.validSpawn.contains( blk ) )
			return false; // must spawn on a valid block..

		this.settings = new NBTTagCompound();
		this.settings.setInteger( "x", x );
		this.settings.setInteger( "y", y );
		this.settings.setInteger( "z", z );
		this.settings.setInteger( "blk", Block.getIdFromBlock( blk ) );

		this.settings.setDouble( "real_sizeOfMeteorite", this.real_sizeOfMeteorite );
		this.settings.setDouble( "realCrater", this.realCrater );
		this.settings.setDouble( "sizeOfMeteorite", this.sizeOfMeteorite );
		this.settings.setDouble( "crater", this.crater );

		this.settings.setBoolean( "lava", Math.random() > 0.9 );

		if( blk == Blocks.sand )
			this.type = new FalloutSand( w, x, y, z );
		else if( blk == Blocks.hardened_clay )
			this.type = new FalloutCopy( w, x, y, z );
		else if( blk == Blocks.ice || blk == Blocks.snow )
			this.type = new FalloutSnow( w, x, y, z );

		int realValidBlocks = 0;

		for( int i = x - 6; i < x + 6; i++ )
			for( int j = y - 6; j < y + 6; j++ )
				for( int k = z - 6; k < z + 6; k++ )
				{
					blk = w.getBlock( i, j, k );
					if( this.validSpawn.contains( blk ) )
						realValidBlocks++;
				}

		for( int i = x - 15; i < x + 15; i++ )
			for( int j = y - 15; j < y + 15; j++ )
				for( int k = z - 15; k < z + 15; k++ )
				{
					blk = w.getBlock( i, j, k );
					if( this.invalidSpawn.contains( blk ) )
						return false;
					if( this.validSpawn.contains( blk ) )
						validBlocks++;
				}

		if( validBlocks > this.minBLocks && realValidBlocks > 80 )
		{
			// we can spawn here!

			int skyMode = 0;

			for( int i = x - 15; i < x + 15; i++ )
				for( int j = y - 15; j < y + 11; j++ )
					for( int k = z - 15; k < z + 15; k++ )
					{
						if( w.canBlockSeeTheSky( i, j, k ) )
							skyMode++;
					}

			boolean solid = true;
			for( int j = y - 15; j < y - 1; j++ )
			{
				if( w.getBlock( x, j, z ) == Platform.AIR )
					solid = false;
			}

			if( !solid )
				skyMode = 0;

			// creator
			if( skyMode > 10 )
				this.placeCrater( w, x, y, z );

			this.placeMeteorite( w, x, y, z );

			// collapse blocks...
			if( skyMode > 3 )
				this.Decay( w, x, y, z );

			this.settings.setInteger( "skyMode", skyMode );
			w.done();

			WorldSettings.getInstance().addNearByMeteorites( w.getWorld().provider.dimensionId, x >> 4, z >> 4, this.settings );
			return true;
		}
		return false;
	}

	private void put( IMeteoriteWorld w, int i, int j, int k, Block blk, int meta )
	{
		if( w.getBlock( i, j, k ) == Blocks.bedrock )
			return;

		w.setBlock( i, j, k, blk, meta, 3 );
	}

	public NBTTagCompound getSettings()
	{
		return this.settings;
	}

	public interface IMeteoriteWorld
	{

		int minX( int in );

		int minZ( int in );

		int maxX( int in );

		int maxZ( int in );

		boolean hasNoSky();

		int getBlockMetadata( int x, int y, int z );

		Block getBlock( int x, int y, int z );

		boolean canBlockSeeTheSky( int i, int j, int k );

		TileEntity getTileEntity( int x, int y, int z );

		World getWorld();

		void setBlock( int i, int j, int k, Block blk );

		void setBlock( int i, int j, int k, Block blk_b, int meta_b, int l );

		void done();
	}


	static public class StandardWorld implements IMeteoriteWorld
	{

		protected final World w;

		public StandardWorld( World w )
		{
			this.w = w;
		}

		@Override
		public int minX( int in )
		{
			return in;
		}

		@Override
		public int minZ( int in )
		{
			return in;
		}

		@Override
		public int maxX( int in )
		{
			return in;
		}

		@Override
		public int maxZ( int in )
		{
			return in;
		}

		@Override
		public boolean hasNoSky()
		{
			return !this.w.provider.hasNoSky;
		}

		@Override
		public int getBlockMetadata( int x, int y, int z )
		{
			if( this.range( x, y, z ) )
				return this.w.getBlockMetadata( x, y, z );
			return 0;
		}

		@Override
		public Block getBlock( int x, int y, int z )
		{
			if( this.range( x, y, z ) )
				return this.w.getBlock( x, y, z );
			return Platform.AIR;
		}

		@Override
		public boolean canBlockSeeTheSky( int x, int y, int z )
		{
			if( this.range( x, y, z ) )
				return this.w.canBlockSeeTheSky( x, y, z );
			return false;
		}

		@Override
		public TileEntity getTileEntity( int x, int y, int z )
		{
			if( this.range( x, y, z ) )
				return this.w.getTileEntity( x, y, z );
			return null;
		}

		@Override
		public World getWorld()
		{
			return this.w;
		}

		@Override
		public void setBlock( int x, int y, int z, Block blk )
		{
			if( this.range( x, y, z ) )
				this.w.setBlock( x, y, z, blk );
		}

		@Override
		public void setBlock( int x, int y, int z, Block blk, int metadata, int flags )
		{
			if( this.range( x, y, z ) )
				this.w.setBlock( x, y, z, blk, metadata, flags );
		}

		@Override
		public void done()
		{

		}

		public boolean range( int x, int y, int z )
		{
			return true;
		}
	}


	static public class ChunkOnly extends StandardWorld
	{

		final Chunk target;
		final int cx, cz;
		int verticalBits = 0;

		public ChunkOnly( World w, int cx, int cz )
		{
			super( w );
			this.target = w.getChunkFromChunkCoords( cx, cz );
			this.cx = cx;
			this.cz = cz;
		}

		@Override
		public int getBlockMetadata( int x, int y, int z )
		{
			if( this.range( x, y, z ) )
				return this.target.getBlockMetadata( x & 0xF, y, z & 0xF );
			return 0;
		}

		@Override
		public Block getBlock( int x, int y, int z )
		{
			if( this.range( x, y, z ) )
				return this.target.getBlock( x & 0xF, y, z & 0xF );
			return Platform.AIR;
		}

		@Override
		public void setBlock( int x, int y, int z, Block blk )
		{
			if( this.range( x, y, z ) )
			{
				this.verticalBits |= 1 << ( y >> 4 );
				this.w.setBlock( x, y, z, blk, 0, 1 );
			}
		}

		@Override
		public void setBlock( int x, int y, int z, Block blk, int metadata, int flags )
		{
			if( this.range( x, y, z ) )
			{
				this.verticalBits |= 1 << ( y >> 4 );
				this.w.setBlock( x, y, z, blk, metadata, flags & ( ~2 ) );
			}
		}

		@Override
		public boolean range( int x, int y, int z )
		{
			return this.cx == ( x >> 4 ) && this.cz == ( z >> 4 );
		}

		@Override
		public int minX( int in )
		{
			return Math.max( in, this.cx << 4 );
		}

		@Override
		public int minZ( int in )
		{
			return Math.max( in, this.cz << 4 );
		}

		@Override
		public int maxX( int in )
		{
			return Math.min( in, ( this.cx + 1 ) << 4 );
		}

		@Override
		public int maxZ( int in )
		{
			return Math.min( in, ( this.cz + 1 ) << 4 );
		}

		@Override
		public void done()
		{
			if( this.verticalBits != 0 )
				Platform.sendChunk( this.target, this.verticalBits );
		}
	}


	private class Fallout
	{

		public int adjustCrater()
		{
			return 0;
		}

		public void getRandomFall( IMeteoriteWorld w, int x, int y, int z )
		{
			double a = Math.random();
			if( a > 0.9 )
				MeteoritePlacer.this.put( w, x, y, z, Blocks.stone );
			else if( a > 0.8 )
				MeteoritePlacer.this.put( w, x, y, z, Blocks.cobblestone );
			else if( a > 0.7 )
				MeteoritePlacer.this.put( w, x, y, z, Blocks.dirt );
			else if( a > 0.7 )
				MeteoritePlacer.this.put( w, x, y, z, Blocks.gravel );
		}

		public void getRandomInset( IMeteoriteWorld w, int x, int y, int z )
		{
			double a = Math.random();
			if( a > 0.9 )
				MeteoritePlacer.this.put( w, x, y, z, Blocks.cobblestone );
			else if( a > 0.8 )
				MeteoritePlacer.this.put( w, x, y, z, Blocks.stone );
			else if( a > 0.7 )
				MeteoritePlacer.this.put( w, x, y, z, Blocks.grass );
			else if( a > 0.6 )
				MeteoritePlacer.this.put( w, x, y, z, MeteoritePlacer.this.skystone );
			else if( a > 0.5 )
				MeteoritePlacer.this.put( w, x, y, z, Blocks.gravel );
			else if( a > 0.5 )
				MeteoritePlacer.this.put( w, x, y, z, Platform.AIR );
		}
	}


	private class FalloutCopy extends Fallout
	{

		final Block blk;
		final int meta;

		public FalloutCopy( IMeteoriteWorld w, int x, int y, int z )
		{
			this.blk = w.getBlock( x, y, z );
			this.meta = w.getBlockMetadata( x, y, z );
		}

		public void getOther( IMeteoriteWorld w, int x, int y, int z, double a )
		{

		}

		@Override
		public void getRandomFall( IMeteoriteWorld w, int x, int y, int z )
		{
			double a = Math.random();
			if( a > 0.9 )
				MeteoritePlacer.this.put( w, x, y, z, this.blk, this.meta );
			else
				this.getOther( w, x, y, z, a );
		}

		@Override
		public void getRandomInset( IMeteoriteWorld w, int x, int y, int z )
		{
			double a = Math.random();
			if( a > 0.9 )
				MeteoritePlacer.this.put( w, x, y, z, this.blk, this.meta );
			else if( a > 0.8 )
				MeteoritePlacer.this.put( w, x, y, z, Platform.AIR );
			else
				this.getOther( w, x, y, z, a - 0.1 );
		}
	}


	private class FalloutSand extends FalloutCopy
	{

		public FalloutSand( IMeteoriteWorld w, int x, int y, int z )
		{
			super( w, x, y, z );
		}

		@Override
		public int adjustCrater()
		{
			return 2;
		}

		@Override
		public void getOther( IMeteoriteWorld w, int x, int y, int z, double a )
		{
			if( a > 0.66 )
				MeteoritePlacer.this.put( w, x, y, z, Blocks.glass );
		}
	}


	private class FalloutSnow extends FalloutCopy
	{

		public FalloutSnow( IMeteoriteWorld w, int x, int y, int z )
		{
			super( w, x, y, z );
		}

		@Override
		public int adjustCrater()
		{
			return 2;
		}

		@Override
		public void getOther( IMeteoriteWorld w, int x, int y, int z, double a )
		{
			if( a > 0.7 )
				MeteoritePlacer.this.put( w, x, y, z, Blocks.snow );
			else if( a > 0.5 )
				MeteoritePlacer.this.put( w, x, y, z, Blocks.ice );
		}
	}
}
