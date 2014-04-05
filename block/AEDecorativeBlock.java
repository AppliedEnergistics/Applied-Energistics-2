package appeng.block;

import net.minecraft.block.material.Material;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

public class AEDecorativeBlock extends AEBaseBlock
{

	protected AEDecorativeBlock(Class<?> c, Material mat) {
		super( c, mat );
	}

	@Override
	public IIcon getIcon(IBlockAccess w, int x, int y, int z, int s)
	{
		return super.unmappedGetIcon( w, x, y, z, s );
	}

	@Override
	public int getRenderType()
	{
		return 0;
	}

}
