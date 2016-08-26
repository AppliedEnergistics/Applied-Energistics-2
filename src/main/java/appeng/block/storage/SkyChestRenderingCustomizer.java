package appeng.block.storage;


import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.bootstrap.BlockRenderingCustomizer;
import appeng.bootstrap.IBlockRendering;
import appeng.bootstrap.IItemRendering;
import appeng.client.render.tesr.SkyChestTESR;


public class SkyChestRenderingCustomizer extends BlockRenderingCustomizer
{

	private final BlockSkyChest.SkyChestType type;

	public SkyChestRenderingCustomizer( BlockSkyChest.SkyChestType type )
	{
		this.type = type;
	}

	@SideOnly( Side.CLIENT )
	@Override
	public void customize( IBlockRendering rendering, IItemRendering itemRendering )
	{
		rendering.tesr( new SkyChestTESR() );

		// Register a custom non-tesr item model
		String modelName = getModelFromType();
		itemRendering.model( new ModelResourceLocation( "appliedenergistics2:" + modelName, "inventory" ) );
	}

	private String getModelFromType()
	{
		final String modelName;
		switch( type )
		{
			default:
			case STONE:
				modelName = "sky_stone_chest";
				break;
			case BLOCK:
				modelName = "smooth_sky_stone_chest";
				break;
		}
		return modelName;
	}
}
