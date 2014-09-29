package appeng.helpers;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;

public class AEGlassMaterial extends Material
{

	public AEGlassMaterial(MapColor p_i2116_1_) {
		super( p_i2116_1_ );
	}

	@Override
	public boolean isOpaque()
	{
		return false;
	}

	public static final AEGlassMaterial instance = (new AEGlassMaterial( MapColor.airColor ));

}
