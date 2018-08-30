
package appeng.core.worlddata;


import org.junit.Assert;
import org.junit.Test;

import net.minecraft.util.math.BlockPos;


public class SpatialDimensionManagerTest
{
	private static final BlockPos CONTENT = new BlockPos( 1, 1, 1 );

	private static final int ID_1 = 0;
	private static final int ID_2 = 1;
	private static final int ID_3 = 2;
	private static final int ID_4 = 3;
	private static final int ID_5 = 4;
	private static final int ID_6 = 5;
	private static final int ID_7 = 6;
	private static final int ID_8 = 7;
	private static final int ID_9 = 8;
	private static final int ID_A = 9;
	private static final int ID_B = 10;
	private static final int ID_C = 11;

	private static final BlockPos POS_1 = new BlockPos( -320, 64, -320 );
	private static final BlockPos POS_2 = new BlockPos( 192, 64, -320 );
	private static final BlockPos POS_3 = new BlockPos( -320, 64, 192 );
	private static final BlockPos POS_4 = new BlockPos( 192, 64, 192 );

	private static final BlockPos POS_5 = new BlockPos( -832, 64, -320 );
	private static final BlockPos POS_6 = new BlockPos( 704, 64, -320 );
	private static final BlockPos POS_7 = new BlockPos( -832, 64, 192 );
	private static final BlockPos POS_8 = new BlockPos( 704, 64, 192 );

	private static final BlockPos POS_9 = new BlockPos( -320, 64, -832 );
	private static final BlockPos POS_A = new BlockPos( 192, 64, -832 );
	private static final BlockPos POS_B = new BlockPos( -320, 64, 704 );
	private static final BlockPos POS_C = new BlockPos( 192, 64, 704 );

	@Test
	public void testBlockPosGeneration()
	{
		SpatialDimensionManager manager = new SpatialDimensionManager( null );

		Assert.assertEquals( ID_1, manager.createNewCellDimension( CONTENT, -1 ) );
		Assert.assertEquals( ID_2, manager.createNewCellDimension( CONTENT, -1 ) );
		Assert.assertEquals( ID_3, manager.createNewCellDimension( CONTENT, -1 ) );
		Assert.assertEquals( ID_4, manager.createNewCellDimension( CONTENT, -1 ) );
		Assert.assertEquals( ID_5, manager.createNewCellDimension( CONTENT, -1 ) );
		Assert.assertEquals( ID_6, manager.createNewCellDimension( CONTENT, -1 ) );
		Assert.assertEquals( ID_7, manager.createNewCellDimension( CONTENT, -1 ) );
		Assert.assertEquals( ID_8, manager.createNewCellDimension( CONTENT, -1 ) );
		Assert.assertEquals( ID_9, manager.createNewCellDimension( CONTENT, -1 ) );
		Assert.assertEquals( ID_A, manager.createNewCellDimension( CONTENT, -1 ) );
		Assert.assertEquals( ID_B, manager.createNewCellDimension( CONTENT, -1 ) );
		Assert.assertEquals( ID_C, manager.createNewCellDimension( CONTENT, -1 ) );

		Assert.assertEquals( POS_1, manager.getCellDimensionOrigin( ID_1 ) );
		Assert.assertEquals( POS_2, manager.getCellDimensionOrigin( ID_2 ) );
		Assert.assertEquals( POS_3, manager.getCellDimensionOrigin( ID_3 ) );
		Assert.assertEquals( POS_4, manager.getCellDimensionOrigin( ID_4 ) );
		Assert.assertEquals( POS_5, manager.getCellDimensionOrigin( ID_5 ) );
		Assert.assertEquals( POS_6, manager.getCellDimensionOrigin( ID_6 ) );
		Assert.assertEquals( POS_7, manager.getCellDimensionOrigin( ID_7 ) );
		Assert.assertEquals( POS_8, manager.getCellDimensionOrigin( ID_8 ) );
		Assert.assertEquals( POS_9, manager.getCellDimensionOrigin( ID_9 ) );
		Assert.assertEquals( POS_A, manager.getCellDimensionOrigin( ID_A ) );
		Assert.assertEquals( POS_B, manager.getCellDimensionOrigin( ID_B ) );
		Assert.assertEquals( POS_C, manager.getCellDimensionOrigin( ID_C ) );
	}

}
