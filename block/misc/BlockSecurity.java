package appeng.block.misc;

import java.util.EnumSet;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import appeng.block.AEBaseBlock;
import appeng.client.render.BaseBlockRender;
import appeng.client.render.blocks.RendererSecurity;
import appeng.core.features.AEFeature;
import appeng.core.localization.PlayerMessages;
import appeng.core.sync.GuiBridge;
import appeng.tile.misc.TileSecurity;
import appeng.util.Platform;

public class BlockSecurity extends AEBaseBlock
{

	public BlockSecurity() {
		super( BlockSecurity.class, Material.iron );
		setfeature( EnumSet.of( AEFeature.Security ) );
		setTileEntiy( TileSecurity.class );
	}

	@Override
	protected Class<? extends BaseBlockRender> getRenderer()
	{
		return RendererSecurity.class;
	}

	@Override
	public boolean onActivated(World w, int x, int y, int z, EntityPlayer p, int side, float hitX, float hitY, float hitZ)
	{
		if ( p.isSneaking() )
			return false;

		TileSecurity tg = getTileEntity( w, x, y, z );
		if ( tg != null )
		{
			if ( Platform.isServer() )
			{
				if ( tg.isPowered() )
				{
					Platform.openGUI( p, tg, ForgeDirection.getOrientation( side ), GuiBridge.GUI_SECURITY );
				}
				else
					p.sendChatToPlayer( PlayerMessages.MachineNotPowered.get() );
			}
			return true;

		}
		return false;
	}

}
