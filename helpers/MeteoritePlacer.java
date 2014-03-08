package appeng.helpers;

import java.util.HashSet;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.AEApi;
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

		public void getRandomFall(World w, int x, int y, int z)
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

		public void getRandomInset(World w, int x, int y, int z)
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

		public FalloutCopy(World w, int x, int y, int z) {
			blk = w.getBlock( x, y, z );
			meta = w.getBlockMetadata( x, y, z );
		}

		public void getOther(World w, int x, int y, int z, double a)
		{

		}

		public void getRandomFall(World w, int x, int y, int z)
		{
			double a = Math.random();
			if ( a > 0.9 )
				put( w, x, y, z, blk, meta );
			else
				getOther( w, x, y, z, a );
		}

		public void getRandomInset(World w, int x, int y, int z)
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

		public FalloutSand(World w, int x, int y, int z) {
			super( w, x, y, z );
		}

		public int adjustCrator()
		{
			return 2;
		}

		public void getOther(World w, int x, int y, int z, double a)
		{
			if ( a > 0.66 )
				put( w, x, y, z, Blocks.glass );
		}

	};

	private class FalloutSnow extends FalloutCopy
	{

		public FalloutSnow(World w, int x, int y, int z) {
			super( w, x, y, z );
		}

		public int adjustCrator()
		{
			return 2;
		}

		public void getOther(World w, int x, int y, int z, double a)
		{
			if ( a > 0.7 )
				put( w, x, y, z, Blocks.snow );
			else if ( a > 0.5 )
				put( w, x, y, z, Blocks.ice );
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

	public boolean spawnMeteorite(World w, int x, int y, int z)
	{
		int validBlocks = 0;

		if ( w.provider.hasNoSky )
			return false;

		Block blk = w.getBlock( x, y, z );
		if ( !validSpawn.contains( blk ) )
			return false; // must spawn on a valid block..

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

			return true;
		}
		return false;
	}

	private void placeCrator(World w, int x, int y, int z)
	{
		boolean lava = Math.random() > 0.9;

		for (int i = x - 30; i < x + 30; i++)
			for (int j = y - 5; j < y + 40; j++)
				for (int k = z - 30; k < z + 30; k++)
				{
					double dx = i - x;
					double dz = k - z;
					double h = y - real_sizeOfMetorite + 1 + type.adjustCrator();

					double distanceFrom = dx * dx + dz * dz;

					if ( j > h + distanceFrom * 0.02 )
					{
						if ( lava && j < y && w.getBlock( x, y - 1, z ).isBlockSolid( w, i, j, k, 0 ) )
						{
							if ( j > h + distanceFrom * 0.02 )
								put( w, i, j, k, Blocks.lava );
						}
						else
							put( w, i, j, k, Platform.air );
					}
				}

		for (Object o : w.getEntitiesWithinAABB( EntityItem.class, AxisAlignedBB.getBoundingBox( x - 30, y - 5, z - 30, x + 30, y + 30, z + 30 ) ))
		{
			Entity e = (Entity) o;
			e.setDead();
		}
	}

	private void placeMetor(World w, int x, int y, int z)
	{
		// spawn metor
		for (int i = x - 8; i < x + 8; i++)
			for (int j = y - 8; j < y + 8; j++)
				for (int k = z - 8; k < z + 8; k++)
				{
					double dx = i - x;
					double dy = j - y;
					double dz = k - z;

					if ( dx * dx * 0.7 + dy * dy * (j > y ? 1.4 : 0.8) + dz * dz * 0.7 < sizeOfMetorite )
						put( w, i, j, k, skystone );
				}

		put( w, x, y, z, skychest );
		TileEntity te = w.getTileEntity( x, y, z );
		if ( te instanceof IInventory )
		{
			InventoryAdaptor ap = InventoryAdaptor.getAdaptor( te, ForgeDirection.UP );

			int primary = (int) (Math.random() * 4);
			for (int zz = 0; zz < primary; zz++)
			{
				switch ((int) (Math.random() * 1000) % 4)
				{
				case 0:
					ap.addItems( AEApi.instance().materials().materialCalcProcessorPress.stack( 1 ) );
					break;
				case 1:
					ap.addItems( AEApi.instance().materials().materialEngProcessorPress.stack( 1 ) );
					break;
				case 2:
					ap.addItems( AEApi.instance().materials().materialLogicProcessorPress.stack( 1 ) );
					break;
				case 3:
					ap.addItems( AEApi.instance().materials().materialSiliconPress.stack( 1 ) );
					break;
				default:
				}
			}

			int secondary = (int) (Math.random() * 3);
			for (int zz = 0; zz < secondary; zz++)
			{
				switch ((int) (Math.random() * 1000) % 3)
				{
				case 0:
					ap.addItems( AEApi.instance().blocks().blockSkyStone.stack( (int) (Math.random() * 12) + 1 ) );
					break;
				case 1:
					ap.addItems( AEApi.instance().materials().materialIronNugget.stack( (int) (Math.random() * 12) + 1 ) );
					break;
				case 2:
					ap.addItems( new ItemStack( net.minecraft.init.Items.gold_nugget, (int) (Math.random() * 12) + 1 ) );
					break;
				}
			}
		}
	}

	private void Decay(World w, int x, int y, int z)
	{
		double randomShit = 0;

		for (int i = x - 30; i < x + 30; i++)
			for (int k = z - 30; k < z + 30; k++)
				for (int j = y - 9; j < y + 30; j++)
				{
					Block blk = w.getBlock( i, j, k );
					if ( blk == Blocks.lava )
						continue;

					if ( blk.isReplaceable( w, i, j, k ) )
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
							if ( !xf.isReplaceable( w, i, j - 1, k ) )
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

	private void put(World w, int i, int j, int k, Block blk)
	{
		if ( w.getBlock( i, j, k ) == Blocks.bedrock )
			return;

		w.setBlock( i, j, k, blk );
	}

	private void put(World w, int i, int j, int k, Block blk, int meta)
	{
		if ( w.getBlock( i, j, k ) == Blocks.bedrock )
			return;

		w.setBlock( i, j, k, blk, meta, 3 );
	}
}
