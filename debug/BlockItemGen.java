package appeng.debug;

import java.util.EnumSet;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import appeng.block.AEBaseBlock;
import appeng.core.features.AEFeature;

public class BlockItemGen extends AEBaseBlock
{

	public BlockItemGen() {
		super( BlockItemGen.class, Material.iron );
		setfeature( EnumSet.of( AEFeature.Debug, AEFeature.Creative ) );
		setTileEntiy( TileItemGen.class );
	}

	@Override
	public void registerBlockIcons(IIconRegister iconRegistry)
	{
		registerNoIcons();
	}

}
