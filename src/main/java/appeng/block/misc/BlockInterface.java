package appeng.block.misc;

import java.util.EnumSet;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.util.IOrientable;
import appeng.block.AEBaseBlock;
import appeng.client.render.BaseBlockRender;
import appeng.client.render.blocks.RenderBlockInterface;
import appeng.core.features.AEFeature;
import appeng.core.sync.GuiBridge;
import appeng.tile.misc.TileInterface;
import appeng.util.Platform;

public class BlockInterface extends AEBaseBlock
{

	public BlockInterface() {
		super( BlockInterface.class, Material.iron );
		setFeature( EnumSet.of( AEFeature.Core ) );
		setTileEntity( TileInterface.class );
	}

	@Override
	protected Class<? extends BaseBlockRender> getRenderer()
	{
		return RenderBlockInterface.class;
	}

	@Override
	protected boolean hasCustomRotation()
	{
		return true;
	}

	@Override
	protected void customRotateBlock(IOrientable rotatable, ForgeDirection axis)
	{
		if ( rotatable instanceof TileInterface )
		{
			((TileInterface) rotatable).setSide( axis );
		}
	}

	@Override
	public boolean onActivated(World w, int x, int y, int z, EntityPlayer p, int side, float hitX, float hitY, float hitZ)
	{
		if ( p.isSneaking() )
			return false;

		TileInterface tg = getTileEntity( w, x, y, z );
		if ( tg != null )
		{
			if ( Platform.isServer() )
				Platform.openGUI( p, tg, ForgeDirection.getOrientation( side ), GuiBridge.GUI_INTERFACE );
			return true;
		}
		return false;
	}
}
