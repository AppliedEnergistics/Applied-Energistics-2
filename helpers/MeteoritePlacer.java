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

	private class Fallout
	{

		public int adjustCrator()
		{
			return 0;
		}

		public void getRandomFall(IMetroiteWorld w, int x, int y, int z)
		{
			double a = Math.random();
			if ( a > 0.9 )
				put( w, x, y, z, Blocks.stone );
			else if ( a > 0.8 )
				put( w, x, y, z, Blocks.cobblestone );
			else if ( a > 0.7 )
				put( w, x, y, z, Blocks.dirt );
			else if ( a > 0.7 )
				put( w, x, y, z, Blocks.gravel );
		}

		public void getRandomInset(IMetroiteWorld w, int x, int y, int z)
		{
			double a = Math.random();
			if ( a > 0.9 )
				put( w, x, y, z, Blocks.cobblestone );
			else if ( a > 0.8 )
				put( w, x, y, z, Blocks.stone );
			else if ( a > 0.7 )
				put( w, x, y, z, Blocks.grass );
			else if ( a > 0.6 )
				put( w, x, y, z, skystone );
			else if ( a > 0.5 )
				put( w, x, y, z, Blocks.gravel );
			else if ( a > 0.5 )
				put( w, x, y, z, Platform.air );
		}

	};

	private class FalloutCopy extends Fallout
	{

		Block blk;
		int meta;

		public FalloutCopy(IMetroiteWorld w, int x, int y, int z) {
			blk = w.getBlock( x, y, z );
			meta = w.getBlockMetadata( x, y, z );
		}

		public void getOther(IMetroiteWorld w, int x, int y, int z, double a)
		{

		}

		public void getRandomFall(IMetroiteWorld w, int x, int y, int z)
		{
			double a = Math.random();
			if ( a > 0.9 )
				put( w, x, y, z, blk, meta );
			else
				getOther( w, x, y, z, a );
		}

		public void getRandomInset(IMetroiteWorld w, int x, int y, int z)
		{
			double a = Math.random();
			if ( a > 0.9 )
				put( w, x, y, z, blk, meta );
			else if ( a > 0.8 )
				put( w, x, y, z, Platform.air );
			else
				getOther( w, x, y, z, a - 0.1 );
		}
	};

	private class FalloutSand extends FalloutCopy
	{

		public FalloutSand(IMetroiteWorld w, int x, int y, int z) {
			super( w, x, y, z );
		}

		public int adjustCrator()
		{
			return 2;
		}

		public void getOther(IMetroiteWorld w, int x, int y, int z, double a)
		{
			if ( a > 0.66 )
				put( w, x, y, z, Blocks.glass );
		}

	};

	private class FalloutSnow extends FalloutCopy
	{

		public FalloutSnow(IMetroiteWorld w, int x, int y, int z) {
			super( w, x, y, z );
		}

		public int adjustCrator()
		{
			return 2;
		}

		public void getOther(IMetroiteWorld w, int x, int y, int z, double a)
		{
			if ( a > 0.7 )
				put( w, x, y, z, Blocks.snow );
			else if ( a > 0.5 )
				put( w, x, y, z, Blocks.ice );
		}

	};

	public interface IMetroiteWorld
	{

		int minX(int in);

		int minZ(int in);

		int maxX(int in);

		int maxZ(int in);

		boolean hasNoSky();

		int getBlockMetadata(int x, int y, int z);

		Block getBlock(int x, int y, int z);

		boolean canBlockSeeTheSky(int i, int j, int k);

		TileEntity getTileEntity(int x, int y, int z);

		World getWorld();

		void setBlock(int i, int j, int k, Block blk);

		void setBlock(int i, int j, int k, Block blk_b, int meta_b, int l);

		void done();

	};

	static public class StandardWorld implements IMetroiteWorld
	{

		protected World w;

		public StandardWorld(World w) {
			this.w = w;
		}

		@Override
		public boolean hasNoSky()
		{
			return !w.provider.hasNoSky;
		}

		@Override
		public int getBlockMetadata(int x, int y, int z)
		{
			if ( range( x, y, z ) )
				return w.getBlockMetadata( x, y, z );
			return 0;
		}

		@Override
		public Block getBlock(int x, int y, int z)
		{
			if ( range( x, y, z ) )
				return w.getBlock( x, y, z );
			return Platform.air;
		}

		@Override
		public boolean canBlockSeeTheSky(int x, int y, int z)
		{
			if ( range( x, y, z ) )
				return w.canBlockSeeTheSky( x, y, z );
			return false;
		}

		@Override
		public TileEntity getTileEntity(int x, int y, int z)
		{
			if ( range( x, y, z ) )
				return w.getTileEntity( x, y, z );
			return null;
		}

		@Override
		public World getWorld()
		{
			return w;
		}

		@Override
		public void setBlock(int x, int y, int z, Block blk)
		{
			if ( range( x, y, z ) )
				w.setBlock( x, y, z, blk );
		}

		@Override
		public void setBlock(int x, int y, int z, Block blk, int metadata, int flags)
		{
			if ( range( x, y, z ) )
				w.setBlock( x, y, z, blk, metadata, flags );
		}

		public boolean range(int x, int y, int z)
		{
			return true;
		}

		@Override
		public int minX(int in)
		{
			return in;
		}

		@Override
		public int minZ(int in)
		{
			return in;
		}

		@Override
		public int maxX(int in)
		{
			return in;
		}

		@Override
		public int maxZ(int in)
		{
			return in;
		}

		@Override
		public void done()
		{

		}

	}

	static public class ChunkOnly extends StandardWorld
	{

		Chunk target;

		int verticalBits = 0;

		final int cx, cz;

		public ChunkOnly(World w, int cx, int cz) {
			super( w );
			target = w.getChunkFromChunkCoords( cx, cz );
			this.cx = cx;
			this.cz = cz;
		}

		@Override
		public void done()
		{
			if ( verticalBits != 0 )
				Platform.sendChunk( target, verticalBits );
		}

		@Override
		public void setBlock(int x, int y, int z, Block blk)
		{
			if ( range( x, y, z ) )
			{
				verticalBits |= 1 << (y >> 4);
				w.setBlock( x, y, z, blk, 0, 1 );
			}
		}

		@Override
		public void setBlock(int x, int y, int z, Block blk, int metadata, int flags)
		{
			if ( range( x, y, z ) )
			{
				verticalBits |= 1 << (y >> 4);
				w.setBlock( x, y, z, blk, metadata, flags & (~2) );
			}
		}

		@Override
		public Block getBlock(int x, int y, int z)
		{
			if ( range( x, y, z ) )
				return target.getBlock( x & 0xF, y, z & 0xF );
			return Platform.air;
		}

		@Override
		public int getBlockMetadata(int x, int y, int z)
		{
			if ( range( x, y, z ) )
				return target.getBlockMetadata( x & 0xF, y, z & 0xF );
			return 0;
		}

		@Override
		public boolean range(int x, int y, int z)
		{
			return cx == (x >> 4) && cz == (z >> 4);
		}

		@Override
		public int minX(int in)
		{
			return Math.max( in, cx << 4 );
		}

		@Override
		public int minZ(int in)
		{
			return Math.max( in, cz << 4 );
		}

		@Override
		public int maxX(int in)
		{
			return Math.min( in, (cx + 1) << 4 );
		}

		@Override
		public int maxZ(int in)
		{
			return Math.min( in, (cz + 1) << 4 );
		}
	};

	int minBLocks = 200;
	HashSet<Block> validSpawn = new HashSet();
	HashSet<Block> invalidSpawn = new HashSet();

	Fallout type = new Fallout();

	Block skystone = AEApi.instance().blocks().blockSkyStone.block();
	Block skychest;

	double real_sizeOfMetorite = (Math.random() * 6.0) + 2;
	double real_crator = real_sizeOfMetorite * 2 + 5;

	double sizeOfMetorite = real_sizeOfMetorite * real_sizeOfMetorite;
	double crator = real_crator * real_crator;

	public MeteoritePlacer() {

		if ( AEApi.instance().blocks().blockSkyChest.block() == null )
			skychest = Blocks.chest;
		else
			skychest = AEApi.instance().blocks().blockSkyChest.block();

		validSpawn.add( Blocks.stone );
		validSpawn.add( Blocks.cobblestone );
		validSpawn.add( Blocks.grass );
		validSpawn.add( Blocks.sand );
		validSpawn.add( Blocks.dirt );
		validSpawn.add( Blocks.gravel );
		validSpawn.add( Blocks.netherrack );
		validSpawn.add( Blocks.iron_ore );
		validSpawn.add( Blocks.gold_ore );
		validSpawn.add( Blocks.diamond_ore );
		validSpawn.add( Blocks.redstone_ore );
		validSpawn.add( Blocks.hardened_clay );
		validSpawn.add( Blocks.ice );
		validSpawn.add( Blocks.snow );

		invalidSpawn.add( skystone );
		invalidSpawn.add( Blocks.planks );
		invalidSpawn.add( Blocks.iron_door );
		invalidSpawn.add( Blocks.iron_bars );
		invalidSpawn.add( Blocks.wooden_door );
		invalidSpawn.add( Blocks.brick_block );
		invalidSpawn.add( Blocks.clay );
		invalidSpawn.add( Blocks.water );
		invalidSpawn.add( Blocks.log );
		invalidSpawn.add( Blocks.log2 );
	}

	NBTTagCompound settings;

	public boolean spawnMeteorite(IMetroiteWorld w, NBTTagCompound metoriteBlob)
	{
		settings = metoriteBlob;

		int x = settings.getInteger( "x" );
		int y = settings.getInteger( "y" );
		int z = settings.getInteger( "z" );

		real_sizeOfMetorite = settings.getDouble( "real_sizeOfMetorite" );
		real_crator = settings.getDouble( "real_crator" );
		sizeOfMetorite = settings.getDouble( "sizeOfMetorite" );
		crator = settings.getDouble( "crator" );

		Block blk = Block.getBlockById( settings.getInteger( "blk" ) );

		if ( blk == Blocks.sand )
			type = new FalloutSand( w, x, y, z );
		else if ( blk == Blocks.hardened_clay )
			type = new FalloutCopy( w, x, y, z );
		else if ( blk == Blocks.ice || blk == Blocks.snow )
			type = new FalloutSnow( w, x, y, z );

		int skyMode = settings.getInteger( "skyMode" );

		// creator
		if ( skyMode > 10 )
			placeCrator( w, x, y, z );

		placeMetor( w, x, y, z );

		// collapse blocks...
		if ( skyMode > 3 )
			Decay( w, x, y, z );

		w.done();
		return true;
	}

	public double getSqDistance(int x, int z)
	{
		int Cx = settings.getInteger( "x" ) - x;
		int Cz = settings.getInteger( "z" ) - z;

		return Cx * Cx + Cz * Cz;
	}

	public boolean spawnMeteorite(IMetroiteWorld w, int x, int y, int z)
	{
		int validBlocks = 0;

		if ( !w.hasNoSky() )
			return false;

		Block blk = w.getBlock( x, y, z );
		if ( !validSpawn.contains( blk ) )
			return false; // must spawn on a valid block..

		settings = new NBTTagCompound();
		settings.setInteger( "x", x );
		settings.setInteger( "y", y );
		settings.setInteger( "z", z );
		settings.setInteger( "blk", Block.getIdFromBlock( blk ) );

		settings.setDouble( "real_sizeOfMetorite", real_sizeOfMetorite );
		settings.setDouble( "real_crator", real_crator );
		settings.setDouble( "sizeOfMetorite", sizeOfMetorite );
		settings.setDouble( "crator", crator );

		settings.setBoolean( "lava", Math.random() > 0.9 );

		if ( blk == Blocks.sand )
			type = new FalloutSand( w, x, y, z );
		else if ( blk == Blocks.hardened_clay )
			type = new FalloutCopy( w, x, y, z );
		else if ( blk == Blocks.ice || blk == Blocks.snow )
			type = new FalloutSnow( w, x, y, z );

		int realValidBlocks = 0;

		for (int i = x - 6; i < x + 6; i++)
			for (int j = y - 6; j < y + 6; j++)
				for (int k = z - 6; k < z + 6; k++)
				{
					blk = w.getBlock( i, j, k );
					if ( validSpawn.contains( blk ) )
						realValidBlocks++;
				}

		for (int i = x - 15; i < x + 15; i++)
			for (int j = y - 15; j < y + 15; j++)
				for (int k = z - 15; k < z + 15; k++)
				{
					blk = w.getBlock( i, j, k );
					if ( invalidSpawn.contains( blk ) )
						return false;
					if ( validSpawn.contains( blk ) )
						validBlocks++;
				}

		if ( validBlocks > minBLocks && realValidBlocks > 80 )
		{
			// we can spawn here!

			int skyMode = 0;

			for (int i = x - 15; i < x + 15; i++)
				for (int j = y - 15; j < y + 11; j++)
					for (int k = z - 15; k < z + 15; k++)
					{
						if ( w.canBlockSeeTheSky( i, j, k ) )
							skyMode++;
					}

			boolean solid = true;
			for (int j = y - 15; j < y - 1; j++)
			{
				if ( w.getBlock( x, j, z ) == Platform.air )
					solid = false;
			}

			if ( !solid )
				skyMode = 0;

			// creator
			if ( skyMode > 10 )
				placeCrator( w, x, y, z );

			placeMetor( w, x, y, z );

			// collapse blocks...
			if ( skyMode > 3 )
				Decay( w, x, y, z );

			settings.setInteger( "skyMode", skyMode );
			w.done();

			WorldSettings.getInstance().addNearByMeteorites( w.getWorld().provider.dimensionId, x >> 4, z >> 4, settings );
			return true;
		}
		return false;
	}

	private void placeCrator(IMetroiteWorld w, int x, int y, int z)
	{
		boolean lava = settings.getBoolean( "lava" );

		int maxY = 255;
		int minX = w.minX( x - 200 );
		int maxX = w.maxX( x + 200 );
		int minZ = w.minZ( z - 200 );
		int maxZ = w.maxZ( z + 200 );

		for (int j = y - 5; j < maxY; j++)
		{
			boolean changed = false;

			for (int i = minX; i < maxX; i++)
				for (int k = minZ; k < maxZ; k++)
				{
					double dx = i - x;
					double dz = k - z;
					double h = y - real_sizeOfMetorite + 1 + type.adjustCrator();

					double distanceFrom = dx * dx + dz * dz;

					if ( j > h + distanceFrom * 0.02 )
					{
						if ( lava && j < y && w.getBlock( x, y - 1, z ).isBlockSolid( w.getWorld(), i, j, k, 0 ) )
						{
							if ( j > h + distanceFrom * 0.02 )
								put( w, i, j, k, Blocks.lava );
						}
						else
							changed = put( w, i, j, k, Platform.air ) || changed;
					}
				}
		}

		for (Object o : w.getWorld().getEntitiesWithinAABB( EntityItem.class,
				AxisAlignedBB.getBoundingBox( w.minX( x - 30 ), y - 5, w.minZ( z - 30 ), w.maxX( x + 30 ), y + 30, w.maxZ( z + 30 ) ) ))
		{
			Entity e = (Entity) o;
			e.setDead();
		}
	}

	private void placeMetor(IMetroiteWorld w, int x, int y, int z)
	{
		int Xmeteor_l = w.minX( x - 8 );
		int Xmeteor_h = w.maxX( x + 8 );
		int Zmeteor_l = w.minZ( z - 8 );
		int Zmeteor_h = w.maxZ( z + 8 );

		// spawn metor
		for (int i = Xmeteor_l; i < Xmeteor_h; i++)
			for (int j = y - 8; j < y + 8; j++)
				for (int k = Zmeteor_l; k < Zmeteor_h; k++)
				{
					double dx = i - x;
					double dy = j - y;
					double dz = k - z;

					if ( dx * dx * 0.7 + dy * dy * (j > y ? 1.4 : 0.8) + dz * dz * 0.7 < sizeOfMetorite )
						put( w, i, j, k, skystone );
				}

		if ( AEConfig.instance.isFeatureEnabled( AEFeature.SpawnPressesInMeteorites ) )
		{
			put( w, x, y, z, skychest );
			TileEntity te = w.getTileEntity( x, y, z );
			if ( te != null && te instanceof IInventory )
			{
				InventoryAdaptor ap = InventoryAdaptor.getAdaptor( te, ForgeDirection.UP );

				int primary = Math.max( 1, (int) (Math.random() * 4) );

				if ( primary > 3 ) // in case math breaks...
					primary = 3;

				for (int zz = 0; zz < primary; zz++)
				{
					int r = 0;
					boolean duplicate = false;

					do
					{
						duplicate = false;

						if ( Math.random() > 0.7 )
							r = WorldSettings.getInstance().getNextOrderedValue( "presses" );
						else
							r = (int) (Math.random() * 1000);

						ItemStack toAdd = null;

						switch (r % 4)
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

						if ( toAdd != null )
						{
							if ( ap.simulateRemove( 1, toAdd, null ) == null )
								ap.addItems( toAdd );
							else
								duplicate = true;
						}
					}
					while (duplicate);
				}

				int secondary = Math.max( 1, (int) (Math.random() * 3) );
				for (int zz = 0; zz < secondary; zz++)
				{
					switch ((int) (Math.random() * 1000) % 3)
					{
					case 0:
						ap.addItems( AEApi.instance().blocks().blockSkyStone.stack( (int) (Math.random() * 12) + 1 ) );
						break;
					case 1:
						List<ItemStack> possibles = new LinkedList();
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
						if ( nugget != null )
						{
							nugget = nugget.copy();
							nugget.stackSize = (int) (Math.random() * 12) + 1;
							ap.addItems( nugget );
						}
						break;
					}
				}
			}
		}
	}

	private void Decay(IMetroiteWorld w, int x, int y, int z)
	{
		double randomShit = 0;

		int Xmeteor_l = w.minX( x - 30 );
		int Xmeteor_h = w.maxX( x + 30 );
		int Zmeteor_l = w.minZ( z - 30 );
		int Zmeteor_h = w.maxZ( z + 30 );

		for (int i = Xmeteor_l; i < Xmeteor_h; i++)
			for (int k = Zmeteor_l; k < Zmeteor_h; k++)
				for (int j = y - 9; j < y + 30; j++)
				{
					Block blk = w.getBlock( i, j, k );
					if ( blk == Blocks.lava )
						continue;

					if ( blk.isReplaceable( w.getWorld(), i, j, k ) )
					{
						blk = Platform.air;
						Block blk_b = w.getBlock( i, j + 1, k );

						if ( blk_b != blk )
						{
							int meta_b = w.getBlockMetadata( i, j + 1, k );

							w.setBlock( i, j, k, blk_b, meta_b, 3 );
							w.setBlock( i, j + 1, k, blk );
						}
						else if ( randomShit < 100 * crator )
						{
							double dx = i - x;
							double dy = j - y;
							double dz = k - z;
							double dist = dx * dx + dy * dy + dz * dz;

							Block xf = w.getBlock( i, j - 1, k );
							if ( !xf.isReplaceable( w.getWorld(), i, j - 1, k ) )
							{
								double extrRange = Math.random() * 0.6;
								double height = crator * (extrRange + 0.2) - Math.abs( dist - crator * 1.7 );

								if ( xf != blk && height > 0 && Math.random() > 0.6 )
								{
									randomShit++;
									type.getRandomFall( w, i, j, k );
								}
							}
						}
					}
					else
					{
						// decay.
						Block blk_b = w.getBlock( i, j + 1, k );
						if ( blk_b == Platform.air )
						{
							if ( Math.random() > 0.4 )
							{
								double dx = i - x;
								double dy = j - y;
								double dz = k - z;

								if ( dx * dx + dy * dy + dz * dz < crator * 1.6 )
								{
									type.getRandomInset( w, i, j, k );
								}

							}
						}
					}

				}
	}

	private boolean put(IMetroiteWorld w, int i, int j, int k, Block blk)
	{
		Block original = w.getBlock( i, j, k );

		if ( original == Blocks.bedrock || original == blk )
			return false;

		w.setBlock( i, j, k, blk );
		return true;
	}

	private void put(IMetroiteWorld w, int i, int j, int k, Block blk, int meta)
	{
		if ( w.getBlock( i, j, k ) == Blocks.bedrock )
			return;

		w.setBlock( i, j, k, blk, meta, 3 );
	}

	public NBTTagCompound getSettings()
	{
		return settings;
	}
}
