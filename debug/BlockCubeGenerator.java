package appeng.debug;

import java.util.EnumSet;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import appeng.block.AEBaseBlock;
import appeng.core.features.AEFeature;

public class BlockCubeGenerator extends AEBaseBlock
{

	public BlockCubeGenerator() {
		super( BlockCubeGenerator.class, Material.iron );
		setfeature( EnumSet.of( AEFeature.UnsupportedDeveloperTools, AEFeature.Creative ) );
		setTileEntiy( TileCubeGenerator.class );
	}

	@Override
	public boolean onActivated(World w, int x, int y, int z,
			EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
		
		TileCubeGenerator tcg = getTileEntity(w, x, y, z);
		if ( tcg != null )
			tcg.click( player );
		
		return true;
	}
	
	@Override
	public void registerBlockIcons(IIconRegister iconRegistry)
	{
		registerNoIcons();
	}

}
