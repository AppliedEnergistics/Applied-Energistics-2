package appeng.block.misc;

import java.util.EnumSet;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.block.AEBaseBlock;
import appeng.client.render.BaseBlockRender;
import appeng.client.render.blocks.RenderBlockInscriber;
import appeng.core.features.AEFeature;
import appeng.core.sync.GuiBridge;
import appeng.tile.misc.TileInscriber;
import appeng.util.Platform;

public class BlockInscriber extends AEBaseBlock
{

	public BlockInscriber() {
		super( BlockInscriber.class, Material.iron );
		setFeature( EnumSet.of( AEFeature.Inscriber ) );
		setTileEntity( TileInscriber.class );
		setLightOpacity( 2 );
		isFullSize = isOpaque = false;
	}

	@Override
	protected Class<? extends BaseBlockRender> getRenderer()
	{
		return RenderBlockInscriber.class;
	}

	@Override
	public boolean onActivated(World w, int x, int y, int z, EntityPlayer p, int side, float hitX, float hitY, float hitZ)
	{
		if ( p.isSneaking() )
			return false;

		TileInscriber tg = getTileEntity( w, x, y, z );
		if ( tg != null )
		{
			if ( Platform.isServer() )
				Platform.openGUI( p, tg, ForgeDirection.getOrientation( side ), GuiBridge.GUI_INSCRIBER );
			return true;
		}
		return false;
	}

}
