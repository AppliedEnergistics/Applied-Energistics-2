package appeng.block.solids;

import java.util.EnumSet;

import net.minecraft.block.material.Material;
import net.minecraft.world.IBlockAccess;
import appeng.api.util.IOrientable;
import appeng.api.util.IOrientableBlock;
import appeng.block.AEBaseBlock;
import appeng.core.features.AEFeature;
import appeng.helpers.LocationRotation;

public class BlockSkyRock extends AEBaseBlock implements IOrientableBlock
{

	public BlockSkyRock() {
		super( BlockSkyRock.class, Material.rock );
		setfeature( EnumSet.of( AEFeature.Core ) );
		setHardness( 50 );
		blockResistance = 150.0f;
	}

	@Override
	public IOrientable getOrientable(final IBlockAccess w, final int x, final int y, final int z)
	{
		return new LocationRotation( w, x, y, z );
	}

}
