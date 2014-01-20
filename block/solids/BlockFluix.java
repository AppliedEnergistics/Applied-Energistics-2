package appeng.block.solids;

import java.util.EnumSet;

import net.minecraft.block.material.Material;
import net.minecraft.world.IBlockAccess;
import appeng.api.util.IOrientable;
import appeng.api.util.IOrientableBlock;
import appeng.block.AEBaseBlock;
import appeng.core.features.AEFeature;
import appeng.helpers.LocationRotation;

public class BlockFluix extends AEBaseBlock implements IOrientableBlock
{

	public BlockFluix() {
		super( BlockFluix.class, Material.rock );
		setfeature( EnumSet.of( AEFeature.DecorativeQuartzBlocks ) );
	}

	@Override
	public IOrientable getOrientable(final IBlockAccess w, final int x, final int y, final int z)
	{
		return new LocationRotation( w, x, y, z );
	}

}
