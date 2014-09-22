package appeng.block.solids;

import java.util.EnumSet;

import net.minecraft.block.material.Material;
import net.minecraft.world.IBlockAccess;
import appeng.api.util.IOrientable;
import appeng.api.util.IOrientableBlock;
import appeng.block.AEBaseBlock;
import appeng.core.features.AEFeature;
import appeng.helpers.MetaRotation;

public class BlockQuartzPillar extends AEBaseBlock implements IOrientableBlock
{

	public BlockQuartzPillar() {
		super( BlockQuartzPillar.class, Material.rock );
		setFeature( EnumSet.of( AEFeature.DecorativeQuartzBlocks ) );
	}

	@Override
	public IOrientable getOrientable(final IBlockAccess w, final int x, final int y, final int z)
	{
		return new MetaRotation( w, x, y, z );
	}

	@Override
	public boolean usesMetadata()
	{
		return true;
	}

}
