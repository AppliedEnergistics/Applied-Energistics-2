package appeng.debug;

import java.util.EnumSet;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import appeng.block.AEBaseBlock;
import appeng.core.features.AEFeature;

public class BlockPhantomNode extends AEBaseBlock
{

	public BlockPhantomNode() {
		super( BlockPhantomNode.class, Material.iron );
		setfeature( EnumSet.of( AEFeature.UnsupportedDeveloperTools, AEFeature.Creative ) );
		setTileEntiy( TilePhantomNode.class );
	}

	@Override
	public boolean onActivated(World w, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ)
	{
		TilePhantomNode tpn = getTileEntity( w, x, y, z );
		tpn.BOOM();
		return true;
	}

	@Override
	public void registerBlockIcons(IIconRegister iconRegistry)
	{
		registerNoIcons();
	}

}
